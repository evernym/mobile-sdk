//
//  CMConfig.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 28/05/2020.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CMConfig.h"
#import "CMUtilities.h"
#import "ProductionPoolTxnGenesis.h"
#import "StagingPoolTxnGenesis.h"
#import "DevTeam1PoolTxnGenesis.h"
#import "MobileSDK.h"
#import "LocalStorage.h"

@implementation CMConfig

// Define your wallet name constant here
NSString* walletName = @"Lor6Ohwaichool"; //@"PleaseSetYourConnectMeWalletName";
NSString* sponsorServerURL = @"placeholder";

// Below settings will depend on your choosen environment
// Selected here is Production Enviroment
CMEnvironment environment = Production;

+(NSString*)agencyConfig {
    NSString* walletKey = [CMConfig walletKey];
    
    NSDictionary* configs = @{
        @"1": @{
                @"agencyUrl": @"https://agency.evernym.com",
                @"agencyDid": @"DwXzE7GdE5DNfsrRXJChSD",
                @"agencyVerKey": @"844sJfb2snyeEugKvpY7Y4jZJk9LT6BnS6bnuKoiqbip",
        },
        @"2": @{
                @"agencyUrl": @"https://agency.pstg.evernym.com",
                @"agencyDid": @"LqnB96M6wBALqRZsrTTwda",
                @"agencyVerKey": @"BpDPZHLbJFu67sWujecoreojiWZbi2dgf4xnYemUzFvB",
        },
        @"3": @{
                @"agencyUrl": @"https://agency-team1.pdev.evernym.com",
                @"agencyDid": @"TGLBMTcW9fHdkSqown9jD8",
                @"agencyVerKey": @"FKGV9jKvorzKPtPJPNLZkYPkLhiS1VbxdvBgd1RjcQHR",
        }
    };
    
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
        [CMUtilities printErrorMessage: exception.reason];
    }
    
    return walletKey;
}

// MARK: - Genesis file for server node
+(NSString*)genesisFileName: (CMEnvironment)environment {
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

+(NSString*)genesisFile: (CMEnvironment)environment {
    switch (environment) {
            // Default is Production genesis file:
        case Staging:
            return stagingPoolTxnGenesisDef;
        case DevTeam1:
            return devTeam1PoolTxnGenesisDef;
        default:
            return productionPoolTxnGenesisDef;
            break;
    }
}

+(NSString*)genesisFilePath {
    NSError* error;
    NSString *filePath = [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject] stringByAppendingPathComponent: [CMConfig genesisFileName: environment]];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    if (![fileManager fileExistsAtPath: filePath]) {
        BOOL success = [[CMConfig genesisFile: environment] writeToFile: filePath atomically: YES encoding: NSUTF8StringEncoding error: &error];
        
        if(!success) {
            NSLog(@"error while creating pool transaction genesis file");
            [CMUtilities printError: error];
            return @"";
        }
    }
    
    [CMUtilities printSuccess: @[@"Creating pool transaction genesis file was successful:", filePath]];
    return filePath;
}

// MARK: - JSON config helper methods
+(NSString*)removeJSONConfig:(NSString*)jsonConfig toRemove: (NSString*)values {
    NSError* error;
    NSMutableDictionary *parsedValues = [@{} mutableCopy];
    if (values) {
        parsedValues = [[CMUtilities jsonToDictionary: values] mutableCopy];
    }
    
    NSMutableDictionary *currentConfig = [[CMUtilities jsonToDictionary: jsonConfig] mutableCopy];
    
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
        parsedValues =  [[CMUtilities jsonToDictionary: values] mutableCopy];
    }
    NSMutableDictionary *currentConfig = [[CMUtilities jsonToDictionary: jsonConfig] mutableCopy];
    
    for (NSString *obj in parsedValues) {
        currentConfig[obj] = parsedValues[obj];
    }
    
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject: currentConfig options: (NSJSONWritingOptions) (0) error: &error];
    
    return [[NSString alloc] initWithData: jsonData encoding: NSUTF8StringEncoding];
}

+(NSString*)updateJSONConfig: (NSString*)jsonConfig  withKey: (NSString*)key withValue: (NSString*)value {
    NSError* error;
    NSMutableDictionary *currentConfig = [[CMUtilities jsonToDictionary: jsonConfig] mutableCopy];
    currentConfig[key] = value;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject: currentConfig options: (NSJSONWritingOptions) (0) error: &error];
    return [[NSString alloc] initWithData: jsonData encoding: NSUTF8StringEncoding];
}

// MARK: - VCX Init

+(void)initVCX {
    [VcxLogger setDefaultLogger: @"TRACE"];
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    
    [VcxLogger setLogger: ^(NSObject *context, NSNumber *level, NSString *target, NSString *message, NSString *modulePath, NSString *file, NSNumber *line) {
        NSLog(@"[Inside VcxLogger.setLogger callback] %@    %@:%@ | %@", [levelMappings valueForKey: [NSString stringWithFormat: @"%@", level]], file, line, message);
    }];
    
    __block NSString* token = [[MobileSDK shared] provisioningToken];
    
    [sdkApi initSovToken];
    NSString* agencyConfig = [CMConfig agencyConfig];
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
    NSMutableDictionary* config = [[CMUtilities jsonToDictionary: agencyConfig] mutableCopy];
    
    if (provisioningToken == nil) {
        NSLog(@"Fail to init vcx because provisioning token is empty");
        return;
    }
    
    const char* oneTimeInfo = [sdkApi agentProvisionWithToken: agencyConfig token: provisioningToken];
    
    if(oneTimeInfo == nil) {
        NSLog(@"OneTimeInfo is null. Cannot proceed with provisioning");
        return;
    }
    
    NSDictionary* oneTimeInfoDict = [CMUtilities jsonToDictionary: [NSString stringWithUTF8String: oneTimeInfo]];
    [config addEntriesFromDictionary: oneTimeInfoDict];
    
    NSMutableDictionary *keychainVcxConfig = [@{} mutableCopy];
    keychainVcxConfig[(__bridge id)kSecClass] = (__bridge id)kSecClassGenericPassword;
    keychainVcxConfig[(__bridge id)kSecAttrAccessible] = (__bridge id)kSecAttrAccessibleWhenUnlocked;
    keychainVcxConfig[(__bridge id)kSecAttrType] = @"vcxConfig";
    keychainVcxConfig[(__bridge id)kSecAttrLabel] = walletName;
    
    NSString *vcxConfig = [CMConfig vsxConfig: [NSString stringWithUTF8String: oneTimeInfo]
                           withKeychainConfig: keychainVcxConfig];
    
    [sdkApi initWithConfig: vcxConfig completion:^(NSError *error) {
        if (error && error.code > 0) {
            return [CMUtilities printError: error];
        }
        
        [MobileSDK shared].sdkInited = true;
        [[NSNotificationCenter defaultCenter] postNotificationName:@"vcxInitialized" object: nil userInfo: nil];
        [CMUtilities printSuccess:@[@"######## VCX Init Successful! :) #########"]];
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
            [CMUtilities printErrorMessage: [NSString stringWithFormat: @"Error Code while finding vcxConfig: %d", (int)sts]];
        }
        return vcxConfig;
    }
    
    // to be updated:
    // - institution_logo_url,
    // - institution_name,
    // - pool_name,
    // - config and
    // - genesis_path
    vcxConfig = [CMConfig updateJSONConfig: oneTimeInfo withValues: [NSString stringWithFormat: @"{\"genesis_path\": \"%@\", \"institution_logo_url\": \"%@\", \"institution_name\": \"%@\", \"pool_name\":\"7e96cbb3b0a1711f3b843af3cb28e31dcmpool\", \"protocol_version\":\"2\"}", [CMConfig genesisFilePath], @"https://robothash.com/logo.png", @"real institution name"]];
    
    // Check if the keychainVcxConfig already exists.
    // Store the vcxConfig into the secure keychain storage!
    if(SecItemCopyMatching((__bridge CFDictionaryRef)keychainVcxConfig, NULL) == noErr) {
        //We can update the keychain item.
        NSMutableDictionary *attributesToUpdate = [NSMutableDictionary dictionary];
        attributesToUpdate[(__bridge id)kSecValueData] = [vcxConfig dataUsingEncoding:NSUTF8StringEncoding];
        OSStatus sts = SecItemUpdate((__bridge CFDictionaryRef)keychainVcxConfig, (__bridge CFDictionaryRef)attributesToUpdate);
        
        [CMUtilities printErrorMessage: [NSString stringWithFormat: @"Error Code while updating vcxConfig: %d", (int)sts]];
    } else {
        keychainVcxConfig[(__bridge id)kSecValueData] = [vcxConfig dataUsingEncoding: NSUTF8StringEncoding];
        OSStatus sts = SecItemAdd((__bridge CFDictionaryRef)keychainVcxConfig, NULL);
        [CMUtilities printErrorMessage: [NSString stringWithFormat: @"Error Code while adding new vcxConfig: %d", (int)sts]];
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
    
    [CMUtilities sendPostRequest: [sponsorServerURL stringByAppendingString:@"/generate"]
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
