//
//  Initialization.m
//  MSDKSampleAppObjC
//
//  Created by Predrag Jevtic on 28/05/2020.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Initialization.h"
#import "Utilities.h"
#import "ProductionPoolTxnGenesis.h"
#import "MobileSDK.h"
#import "LocalStorage.h"
#import "Config.h"

@implementation Initialization

+(void)initVCX {
    [self initLogger];

    NSMutableDictionary *keychainVcxConfig = [@{} mutableCopy];
    keychainVcxConfig[(__bridge id)kSecClass] = (__bridge id)kSecClassGenericPassword;
    keychainVcxConfig[(__bridge id)kSecAttrAccessible] = (__bridge id)kSecAttrAccessibleWhenUnlocked;
    keychainVcxConfig[(__bridge id)kSecAttrType] = @"vcxConfig";
    keychainVcxConfig[(__bridge id)kSecAttrLabel] = [Config getWalletName];

    if ([self isCloudAgentProvisioned:keychainVcxConfig]) {
        [self initialize:keychainVcxConfig];
    } else {
        [self provisionCloudAgentAndInitializeSdk:keychainVcxConfig];
    }
}

+(void)initialize:(NSMutableDictionary *)keychainVcxConfig {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    NSString* vcxConfig =  [self getSecurePrefVcxConfig:keychainVcxConfig];

    [sdkApi initWithConfig:vcxConfig completion:^(NSError *error) {
        if (error && error.code > 0) {
            return [Utilities printError: error];
        }
        [Utilities printSuccess:@[@"######## VCX Init Successful! :) #########"]];
        [self initPool:^(NSString *successMessage, NSError *error) {
            if (error && error.code > 0) {
                return [Utilities printError: error];
            }

            [MobileSDK shared].sdkInited = true;
            [[NSNotificationCenter defaultCenter] postNotificationName:@"vcxInitialized" object: nil userInfo: nil];
            [Utilities printSuccess:@[@"######## VCX Init Pool Successful! :) #########"]];
        }];
    }];
}

+(void)provisionCloudAgentAndInitializeSdk:(NSMutableDictionary *)keychainVcxConfig {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    [self initLogger];

    NSString* sdkConfig = [Config getSDKConfig];
    NSLog(@"SDK config %@", sdkConfig);

    [self retreiveProvisioningToken:^(NSString *token, NSError *error) {
        if(error != nil && error > 0) {
            NSLog(@"Provisioning token error: %@", error);
            return;
        }
        if (token == nil) {
            NSLog(@"Fail to init vcx because provisioning token is empty");
            return;
        }
        [LocalStorage store:token andString:@"provisioningToken"];

        const char* oneTimeInfo = [sdkApi agentProvisionWithToken: sdkConfig token: token];
        if(oneTimeInfo == nil) {
            NSLog(@"OneTimeInfo is null. Cannot proceed with provisioning");
            return;
        }
        NSString *oneTimeInfoConfig = [NSString stringWithUTF8String: oneTimeInfo];

        [self setSecurePrefVcxConfig:keychainVcxConfig data:oneTimeInfoConfig];
        [self initialize: keychainVcxConfig];
    }];
}

+(void)setSecurePrefVcxConfig:(NSMutableDictionary*) keychainVcxConfig
                         data:(NSString *) data {
    keychainVcxConfig[(__bridge id)kSecValueData] = [data dataUsingEncoding: NSUTF8StringEncoding];
    OSStatus sts = SecItemAdd((__bridge CFDictionaryRef)keychainVcxConfig, NULL);
    [Utilities printErrorMessage: [NSString stringWithFormat: @"Error Code while adding new vcxConfig: %d", (int)sts]];
}

+(NSString *)getSecurePrefVcxConfig:(NSMutableDictionary*) keychainVcxConfig {
    NSString *vcxConfig = nil;

    keychainVcxConfig[(__bridge id)kSecReturnData] = (__bridge id)kCFBooleanTrue;
    keychainVcxConfig[(__bridge id)kSecReturnAttributes] = (__bridge id)kCFBooleanTrue;
    CFDictionaryRef result = nil;
    OSStatus cecItem = SecItemCopyMatching((__bridge CFDictionaryRef)keychainVcxConfig, (CFTypeRef *)&result);

    if (cecItem == noErr) {
        NSDictionary *resultDict = ( NSDictionary *)result;
        NSData *vcxConfigData = resultDict[(__bridge id)kSecValueData];
        vcxConfig = [[NSString alloc] initWithData: vcxConfigData encoding: NSUTF8StringEncoding];
    } else {
        NSLog(@"Error Code while finding vcxConfig: %d", (int)cecItem);
    }
    return vcxConfig;
}

+(void)clearSecureStorage {
    NSArray *secItemClasses = @[(__bridge id)kSecClassGenericPassword,
                                (__bridge id)kSecClassInternetPassword,
                                (__bridge id)kSecClassCertificate,
                                (__bridge id)kSecClassKey,
                                (__bridge id)kSecClassIdentity];
    for (id secItemClass in secItemClasses) {
        NSDictionary *spec = @{(__bridge id)kSecClass: secItemClass};
        SecItemDelete((__bridge CFDictionaryRef)spec);
    }
}

+(BOOL)isCloudAgentProvisioned:(NSMutableDictionary*) keychainVcxConfig {
    NSString *token = [LocalStorage getValueForKey: @"provisioningToken"];
    if (token == nil) {
        [self clearSecureStorage];
        return false;
    }
    return true;
}

+(void)initPool:(ResponseBlock) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    NSString *poolConfig = [Config getPoolConfig];

    [sdkApi initPool:(NSString *)poolConfig
          completion:^(NSError *error) {
        return completionBlock(nil, error);
    }];
}


+(void)retreiveProvisioningToken:(ResponseBlock) completionBlock {
    NSString *sponsorServerURL = [Config getSponsorServerURL];
    if([sponsorServerURL isEqual: @"placeholder"]) {
        NSError *error = [[NSError alloc] initWithDomain: @"connectMe"
                                                    code: 400
                                                userInfo: @{ @"message": @"Error: Sponsor Server URL is not set."}];
        return completionBlock(nil, error);
    }
    NSString* sponseeID = [[[NSUUID alloc] init] UUIDString];
    [LocalStorage store:sponseeID andString:@"sponseeID"];

    [Utilities sendPostRequest: [sponsorServerURL stringByAppendingString:@"/generate"]
                        withBody:@{@"sponseeId": sponseeID }
                   andCompletion:^(NSString *token, NSError *error) {
        if(error != nil) {
            return completionBlock(nil, error);
        }

        [LocalStorage store:token andString:@"provisioningToken"];
        return completionBlock(token, nil);
    }];
}

// MOVE to Log.m
+(void)initLogger {
    [VcxLogger setDefaultLogger: @"TRACE"];
    [VcxLogger setLogger: ^(NSObject *context, NSNumber *level, NSString *target, NSString *message, NSString *modulePath, NSString *file, NSNumber *line) {
        NSLog(@"[Inside VcxLogger.setLogger callback]");
    }];
}

@end
