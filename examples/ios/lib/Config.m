//
//  Config.m
//  MSDKSampleAppObjC
//
//  Created by Predrag Jevtic on 28/05/2020.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Config.h"
#import "Utilities.h"
#import "ProductionPoolTxnGenesis.h"
//#import "StagingPoolTxnGenesis.h"
//#import "DevTeam1PoolTxnGenesis.h"
#import "MobileSDK.h"
#import "LocalStorage.h"

@implementation Config

// Define your wallet name constant here
NSString* walletName = @"Lor6Ohwaichool"; //@"PleaseSetYourConnectMeWalletName";
NSString* sponsorServerURL = @"https://3341-83-139-159-140.ngrok.io";

// Below settings will depend on your choosen environment
// Selected here is Production Enviroment
//Environment environment = Staging;
Environment environment = Production;

+(NSString*)agencyConfig {
    NSString* walletKey = [Config walletKey];

    NSDictionary* configs = @{
        @"1": @{
                @"agencyUrl": @"https://agency.pps.evernym.com",
                @"agencyDid": @"3mbwr7i85JNSL3LoNQecaW",
                @"agencyVerKey": @"2WXxo6y1FJvXWgZnoYUP5BJej2mceFrqBDNPE3p6HDPf",
        },
        @"2": @{
                @"agencyUrl": @"https://agency.pstg.evernym.com",
                @"agencyDid": @"LqnB96M6wBALqRZsrTTwda",
                @"agencyVerKey": @"BpDPZHLbJFu67sWujecoreojiWZbi2dgf4xnYemUzFvB",
        },
//        @"3": @{
//                @"agencyUrl": @"https://agency-team1.pdev.evernym.com",
//                @"agencyDid": @"TGLBMTcW9fHdkSqown9jD8",
//                @"agencyVerKey": @"FKGV9jKvorzKPtPJPNLZkYPkLhiS1VbxdvBgd1RjcQHR",
//        }
        @"3": @{
                @"agencyUrl": @"https://agency.pdev.evernym.com",
                @"agencyDid": @"LiLBGgFarh954ZtTByLM1C",
                @"agencyVerKey": @"Bk9wFrud3rz8v3nAFKGib6sQs8zHWzZxfst7Wh3Mbc9W",
        }
    };
    NSLog(@"ENV %u@", environment);
    NSString* agencyUrl = [[configs objectForKey:[NSString stringWithFormat:@"%i", environment]] valueForKey: @"agencyUrl"];
    NSString* agencyDid = [[configs objectForKey:[NSString stringWithFormat:@"%i", environment]] valueForKey: @"agencyDid"];
    NSString* agencyVerKey = [[configs objectForKey:[NSString stringWithFormat:@"%i", environment]] valueForKey: @"agencyVerKey"];

    return [NSString stringWithFormat: @"{\"agency_url\":\"%@\",\"agency_did\":\"%@\",\"agency_verkey\":\"%@\",\"wallet_name\":\"%@\",\"wallet_key\":\"%@\",\"agent_seed\":null,\"enterprise_seed\":null,\"protocol_type\":\"3.0\"}", agencyUrl, agencyDid, agencyVerKey, walletName, walletKey];
}

+(NSString*)walletKey {
    NSString* walletKey = @"";

    @try {
        NSMutableData *data = [[walletName dataUsingEncoding: NSUTF8StringEncoding] mutableCopy];
        int result = 0; //SecRandomCopyBytes(NULL, 128, data.mutableBytes);
        if (result == 0) {
            walletKey = [data base64EncodedStringWithOptions: 0];
        } else {
            NSString *indyErrorCode = @"W-001: Error occurred while generating wallet key";
            NSLog(@"Value of indyErrorCode is: %@", indyErrorCode);
        }
        return walletKey;
    } @catch (NSException *exception) {
        [Utilities printErrorMessage: exception.reason];
    }

    return walletKey;
}

// MARK: - Genesis file for server node
+(NSString*)genesisFileName: (Environment)environment {
    switch (environment) {
        case Sandbox:
            return @"pool_transactions_genesis_DEMO";

        case Staging:
            return @"pool_transactions_genesis_STAG";

        case DevTeam1:
            return @"pool_transactions_genesis_DEVTEAM1";

        default:
            return @"pool_transactions_genesis_PROD";
    }
}

+(NSString*)genesisFile: (Environment)environment {
    switch (environment) {
            // Default is Production genesis file:
//        case Staging:
//            return stagingPoolTxnGenesisDef;
//        case DevTeam1:
//            return devTeam1PoolTxnGenesisDef;
        default:
            return productionPoolTxnGenesisDef;
            break;
    }
}

+(NSString*)genesisFilePath {
    NSError* error;
    NSString *filePath = [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject] stringByAppendingPathComponent: [Config genesisFileName: environment]];
    NSFileManager *fileManager = [NSFileManager defaultManager];

    if (![fileManager fileExistsAtPath: filePath]) {
        BOOL success = [[Config genesisFile: environment] writeToFile: filePath atomically: YES encoding: NSUTF8StringEncoding error: &error];

        if(!success) {
            NSLog(@"error while creating pool transaction genesis file");
            [Utilities printError: error];
            return @"";
        }
    }

    [Utilities printSuccess: @[@"Creating pool transaction genesis file was successful:", filePath]];
    return filePath;
}

// MARK: - JSON config helper methods
+(NSString*)removeJSONConfig:(NSString*)jsonConfig toRemove: (NSString*)values {
    NSError* error;
    NSMutableDictionary *parsedValues = [@{} mutableCopy];
    if (values) {
        parsedValues = [[Utilities jsonToDictionary: values] mutableCopy];
    }

    NSMutableDictionary *currentConfig = [[Utilities jsonToDictionary: jsonConfig] mutableCopy];

    for (NSString *obj in parsedValues) {
        [currentConfig removeObjectForKey: obj];
    }

    NSData *jsonData = [NSJSONSerialization dataWithJSONObject: currentConfig options: (NSJSONWritingOptions) (0) error: &error];
    return [[NSString alloc] initWithData: jsonData encoding: NSUTF8StringEncoding];
}

+(NSString*)updateJSONConfig: (NSString*)jsonConfig
                  withValues: (NSString*)values {
    NSError* error;
    NSMutableDictionary *parsedValues = [@{} mutableCopy];
    if (values) {
        parsedValues =  [[Utilities jsonToDictionary: values] mutableCopy];
    }
    NSMutableDictionary *currentConfig = [[Utilities jsonToDictionary: jsonConfig] mutableCopy];

    for (NSString *obj in parsedValues) {
        currentConfig[obj] = parsedValues[obj];
    }

    NSData *jsonData = [NSJSONSerialization dataWithJSONObject: currentConfig options: (NSJSONWritingOptions) (0) error: &error];

    return [[NSString alloc] initWithData: jsonData encoding: NSUTF8StringEncoding];
}

+(NSString*)updateJSONConfig: (NSString*)jsonConfig  withKey: (NSString*)key withValue: (NSString*)value {
    NSError* error;
    NSMutableDictionary *currentConfig = [[Utilities jsonToDictionary: jsonConfig] mutableCopy];
    currentConfig[key] = value;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject: currentConfig options: (NSJSONWritingOptions) (0) error: &error];
    return [[NSString alloc] initWithData: jsonData encoding: NSUTF8StringEncoding];
}

// MARK: - VCX Init

+(void)initVCX {
    [VcxLogger setDefaultLogger: @"TRACE"];

    [VcxLogger setLogger: ^(NSObject *context, NSNumber *level, NSString *target, NSString *message, NSString *modulePath, NSString *file, NSNumber *line) {
        NSLog(@"[Inside VcxLogger.setLogger callback] %@    %@:%@ | %@", [levelMappings valueForKey: [NSString stringWithFormat: @"%@", level]], file, line, message);
    }];

    __block NSString* token = [[MobileSDK shared] provisioningToken];

    NSString* agencyConfig = [Config agencyConfig];
    NSLog(@"Agency config %@", agencyConfig);

    if(token != nil) {
        [self initializeMobileSDK: agencyConfig withProvisioningToken: token];
        return;
    }

    // retrieve provisioning token from sponsor service
    [self retreiveProvisioningToken:^(NSString *response, NSError *error) {
        if(error != nil && error > 0) {
            NSLog(@"Provisioning token error: %@", error);
            return;
        }

        [self initializeMobileSDK: agencyConfig withProvisioningToken: response];
    }];
}

+(void) initializeMobileSDK: (NSString*) agencyConfig withProvisioningToken: (NSString*) provisioningToken {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    NSMutableDictionary* config = [[Utilities jsonToDictionary: agencyConfig] mutableCopy];

    if (provisioningToken == nil) {
        NSLog(@"Fail to init vcx because provisioning token is empty");
        return;
    }

    const char* oneTimeInfo = [sdkApi agentProvisionWithToken: agencyConfig token: provisioningToken];

    if(oneTimeInfo == nil) {
        NSLog(@"OneTimeInfo is null. Cannot proceed with provisioning");
        return;
    }

    NSDictionary* oneTimeInfoDict = [Utilities jsonToDictionary: [NSString stringWithUTF8String: oneTimeInfo]];
    [config addEntriesFromDictionary: oneTimeInfoDict];

    NSMutableDictionary *keychainVcxConfig = [@{} mutableCopy];
    keychainVcxConfig[(__bridge id)kSecClass] = (__bridge id)kSecClassGenericPassword;
    keychainVcxConfig[(__bridge id)kSecAttrAccessible] = (__bridge id)kSecAttrAccessibleWhenUnlocked;
    keychainVcxConfig[(__bridge id)kSecAttrType] = @"vcxConfig";
    keychainVcxConfig[(__bridge id)kSecAttrLabel] = walletName;

    NSString *vcxConfig = [Config vsxConfig: [NSString stringWithUTF8String: oneTimeInfo]
                           withKeychainConfig: keychainVcxConfig];

    [sdkApi initWithConfig: vcxConfig completion:^(NSError *error) {
        if (error && error.code > 0) {
            return [Utilities printError: error];
        }

        [MobileSDK shared].sdkInited = true;
        [[NSNotificationCenter defaultCenter] postNotificationName:@"vcxInitialized" object: nil userInfo: nil];
        [Utilities printSuccess:@[@"######## VCX Init Successful! :) #########"]];
    }];
}

+(NSString*)vsxConfig: (NSString*) oneTimeInfo withKeychainConfig: (NSMutableDictionary*) keychainVcxConfig {
    NSString *vcxConfig;

    if(oneTimeInfo == nil) {
        // Get vcxConfig from secure keychain storage: https://www.andyibanez.com/using-ios-keychain/
        keychainVcxConfig[(__bridge id)kSecReturnData] = (__bridge id)kCFBooleanTrue;
        keychainVcxConfig[(__bridge id)kSecReturnAttributes] = (__bridge id)kCFBooleanTrue;
        CFDictionaryRef result = nil;
        OSStatus sts = SecItemCopyMatching((__bridge CFDictionaryRef)keychainVcxConfig, (CFTypeRef *)&result);
        if(sts == noErr)
        {
            NSDictionary *resultDict = (__bridge_transfer NSDictionary *)result;
            NSData *vcxConfigData = resultDict[(__bridge id)kSecValueData];
            vcxConfig = [[NSString alloc] initWithData: vcxConfigData encoding: NSUTF8StringEncoding];
        } else {
            NSLog(@"Error Code while finding vcxConfig: %d", (int)sts);
            [Utilities printErrorMessage: [NSString stringWithFormat: @"Error Code while finding vcxConfig: %d", (int)sts]];
        }
        return vcxConfig;
    }

    // to be updated:
    // - institution_logo_url,
    // - institution_name,
    // - pool_name,
    // - config and
    // - genesis_path
    vcxConfig = [Config updateJSONConfig: oneTimeInfo withValues: [NSString stringWithFormat: @"{\"genesis_path\": \"%@\", \"institution_logo_url\": \"%@\", \"institution_name\": \"%@\", \"pool_name\":\"7e96cbb3b0a1711f3b843af3cb28e31dcmpool\", \"protocol_version\":\"2\"}", [Config genesisFilePath], @"https://robothash.com/logo.png", @"real institution name"]];

    // Check if the keychainVcxConfig already exists.
    // Store the vcxConfig into the secure keychain storage!
    if(SecItemCopyMatching((__bridge CFDictionaryRef)keychainVcxConfig, NULL) == noErr) {
        //We can update the keychain item.
        NSMutableDictionary *attributesToUpdate = [NSMutableDictionary dictionary];
        attributesToUpdate[(__bridge id)kSecValueData] = [vcxConfig dataUsingEncoding:NSUTF8StringEncoding];
        OSStatus sts = SecItemUpdate((__bridge CFDictionaryRef)keychainVcxConfig, (__bridge CFDictionaryRef)attributesToUpdate);

        [Utilities printErrorMessage: [NSString stringWithFormat: @"Error Code while updating vcxConfig: %d", (int)sts]];
    } else {
        keychainVcxConfig[(__bridge id)kSecValueData] = [vcxConfig dataUsingEncoding: NSUTF8StringEncoding];
        OSStatus sts = SecItemAdd((__bridge CFDictionaryRef)keychainVcxConfig, NULL);
        [Utilities printErrorMessage: [NSString stringWithFormat: @"Error Code while adding new vcxConfig: %d", (int)sts]];
    }

    return vcxConfig;
}

+(void)retreiveProvisioningToken: (ResponseBlock) completionBlock {
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
@end
