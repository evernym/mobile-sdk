//
//  Config.m
//  MSDKSampleAppObjC
//
//  Created by Evernym on 03.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Config.h"
#import "Utilities.h"
#import "ProductionPoolTxnGenesis.h"

@implementation Config

// Define your wallet name constant here
NSString* walletName = @"Lor6Ohwaichool";
NSString* sponsorServerURL = @"https://3341-83-139-159-140.ngrok.io";

+(NSString*) getSponsorServerURL {
    return sponsorServerURL;
}

+(NSString*) getWalletName {
    return walletName;
}

+(NSString*) getAgencyConfig {
    NSString* walletKey = [self getWalletKey];
    NSString* agencyUrl = @"https://agency.pstg.evernym.com";
    NSString* agencyDid = @"LqnB96M6wBALqRZsrTTwda";
    NSString* agencyVerKey = @"BpDPZHLbJFu67sWujecoreojiWZbi2dgf4xnYemUzFvB";
    NSString* protocolType = @"3.0";
    NSString* institutionLogoUrl = @"https://robothash.com/logo.png";
    NSString* institutionName = @"real institution name";
    
    return [NSString stringWithFormat: @"{\"agency_url\":\"%@\",\"agency_did\":\"%@\",\"agency_verkey\":\"%@\",\"wallet_name\":\"%@\",\"wallet_key\":\"%@\",\"agent_seed\":null,\"enterprise_seed\":null,\"protocol_type\":\"%@\",\"institution_logo_url\":\"%@\",\"institution_name\":\"%@\"}", agencyUrl, agencyDid, agencyVerKey, walletName, walletKey, protocolType, institutionLogoUrl, institutionName];
}


+(NSString*) getWalletKey {
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


+(NSString*) getGenesisFilePath {
    NSError* error;
    NSString *filePath = [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject] stringByAppendingPathComponent: @"pool_transactions_genesis_PROD"];
    NSFileManager *fileManager = [NSFileManager defaultManager];

    if (![fileManager fileExistsAtPath: filePath]) {
        BOOL success = [productionPoolTxnGenesisDef writeToFile: filePath atomically: YES encoding: NSUTF8StringEncoding error: &error];

        if(!success) {
            NSLog(@"error while creating pool transaction genesis file");
            [Utilities printError: error];
            return @"";
        }
    }

    [Utilities printSuccess: @[@"Creating pool transaction genesis file was successful:", filePath]];
    return filePath;
}

+(NSString*) getPoolConfig {
    NSString *poolConfig = [NSString stringWithFormat: @"{\"genesis_path\": \"%@\",\"pool_name\":\"iso-sample-app\"}", [self getGenesisFilePath]];
    return poolConfig;
}

@end
