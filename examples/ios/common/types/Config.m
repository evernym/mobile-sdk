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

@implementation Config

// Define your wallet name constant here
NSString* walletName = @"Lor6Ohwaichool";
NSString* sponsorServerURL = @"https://8591-83-139-167-37.ngrok.io";
NSString * const productionPoolTxnGenesisDef = @"\n{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"ev1\",\"client_ip\":\"54.207.36.81\",\"client_port\":\"9702\",\"node_ip\":\"18.231.96.215\",\"node_port\":\"9701\",\"services\":[\"VALIDATOR\"]},\"dest\":\"GWgp6huggos5HrzHVDy5xeBkYHxPvrRZzjPNAyJAqpjA\"},\"metadata\":{\"from\":\"J4N1K1SEB8uY2muwmecY5q\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":1,\"txnId\":\"b0c82a3ade3497964cb8034be915da179459287823d92b5717e6d642784c50e6\"},\"ver\":\"1\"}\n{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"zaValidator\",\"client_ip\":\"154.0.164.39\",\"client_port\":\"9702\",\"node_ip\":\"154.0.164.39\",\"node_port\":\"9701\",\"services\":[\"VALIDATOR\"]},\"dest\":\"BnubzSjE3dDVakR77yuJAuDdNajBdsh71ZtWePKhZTWe\"},\"metadata\":{\"from\":\"UoFyxT8BAqotbkhiehxHCn\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":2,\"txnId\":\"d5f775f65e44af60ff69cfbcf4f081cd31a218bf16a941d949339dadd55024d0\"},\"ver\":\"1\"}\n{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"danube\",\"client_ip\":\"128.130.204.35\",\"client_port\":\"9722\",\"node_ip\":\"128.130.204.35\",\"node_port\":\"9721\",\"services\":[\"VALIDATOR\"]},\"dest\":\"476kwEjDj5rxH5ZcmTtgnWqDbAnYJAGGMgX7Sq183VED\"},\"metadata\":{\"from\":\"BrYDA5NubejDVHkCYBbpY5\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":3,\"txnId\":\"ebf340b317c044d970fcd0ca018d8903726fa70c8d8854752cd65e29d443686c\"},\"ver\":\"1\"}\n{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"royal_sovrin\",\"client_ip\":\"35.167.133.255\",\"client_port\":\"9702\",\"node_ip\":\"35.167.133.255\",\"node_port\":\"9701\",\"services\":[\"VALIDATOR\"]},\"dest\":\"Et6M1U7zXQksf7QM6Y61TtmXF1JU23nsHCwcp1M9S8Ly\"},\"metadata\":{\"from\":\"4ohadAwtb2kfqvXynfmfbq\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":4,\"txnId\":\"24d391604c62e0e142ea51c6527481ae114722102e27f7878144d405d40df88d\"},\"ver\":\"1\"}\n{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"digitalbazaar\",\"client_ip\":\"34.226.105.29\",\"client_port\":\"9701\",\"node_ip\":\"34.226.105.29\",\"node_port\":\"9700\",\"services\":[\"VALIDATOR\"]},\"dest\":\"D9oXgXC3b6ms3bXxrUu6KqR65TGhmC1eu7SUUanPoF71\"},\"metadata\":{\"from\":\"rckdVhnC5R5WvdtC83NQp\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":5,\"txnId\":\"56e1af48ef806615659304b1e5cf3ebf87050ad48e6310c5e8a8d9332ac5c0d8\"},\"ver\":\"1\"}\n{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"OASFCU\",\"client_ip\":\"38.70.17.248\",\"client_port\":\"9702\",\"node_ip\":\"38.70.17.248\",\"node_port\":\"9701\",\"services\":[\"VALIDATOR\"]},\"dest\":\"8gM8NHpq2cE13rJYF33iDroEGiyU6wWLiU1jd2J4jSBz\"},\"metadata\":{\"from\":\"BFAeui85mkcuNeQQhZfqQY\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":6,\"txnId\":\"825aeaa33bc238449ec9bd58374b2b747a0b4859c5418da0ad201e928c3049ad\"},\"ver\":\"1\"}\n{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"BIGAWSUSEAST1-001\",\"client_ip\":\"34.224.255.108\",\"client_port\":\"9796\",\"node_ip\":\"34.224.255.108\",\"node_port\":\"9769\",\"services\":[\"VALIDATOR\"]},\"dest\":\"HMJedzRbFkkuijvijASW2HZvQ93ooEVprxvNhqhCJUti\"},\"metadata\":{\"from\":\"L851TgZcjr6xqh4w6vYa34\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":7,\"txnId\":\"40fceb5fea4dbcadbd270be6d5752980e89692151baf77a6bb64c8ade42ac148\"},\"ver\":\"1\"}\n{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"DustStorm\",\"client_ip\":\"207.224.246.57\",\"client_port\":\"9712\",\"node_ip\":\"207.224.246.57\",\"node_port\":\"9711\",\"services\":[\"VALIDATOR\"]},\"dest\":\"8gGDjbrn6wdq6CEjwoVStjQCEj3r7FCxKrA5d3qqXxjm\"},\"metadata\":{\"from\":\"FjuHvTjq76Pr9kdZiDadqq\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":8,\"txnId\":\"6d1ee3eb2057b8435333b23f271ab5c255a598193090452e9767f1edf1b4c72b\"},\"ver\":\"1\"}\n{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"prosovitor\",\"client_ip\":\"138.68.240.143\",\"client_port\":\"9711\",\"node_ip\":\"138.68.240.143\",\"node_port\":\"9710\",\"services\":[\"VALIDATOR\"]},\"dest\":\"C8W35r9D2eubcrnAjyb4F3PC3vWQS1BHDg7UvDkvdV6Q\"},\"metadata\":{\"from\":\"Y1ENo59jsXYvTeP378hKWG\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":9,\"txnId\":\"15f22de8c95ef194f6448cfc03e93aeef199b9b1b7075c5ea13cfef71985bd83\"},\"ver\":\"1\"}\n{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"iRespond\",\"client_ip\":\"52.187.10.28\",\"client_port\":\"9702\",\"node_ip\":\"52.187.10.28\",\"node_port\":\"9701\",\"services\":[\"VALIDATOR\"]},\"dest\":\"3SD8yyJsK7iKYdesQjwuYbBGCPSs1Y9kYJizdwp2Q1zp\"},\"metadata\":{\"from\":\"JdJi97RRDH7Bx7khr1znAq\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":10,\"txnId\":\"b65ce086b631ed75722a4e1f28fc9cf6119b8bc695bbb77b7bdff53cfe0fc2e2\"},\"ver\":\"1\"}";


+(NSString*) getSponsorServerURL {
    return sponsorServerURL;
}

+(NSString*) getWalletName {
    return walletName;
}

+(NSString*) getSDKConfig {
    NSString* walletKey = [self getWalletKey];
    NSString* agencyUrl = @"https://agency.evernym.com";
    NSString* agencyDid = @"DwXzE7GdE5DNfsrRXJChSD";
    NSString* agencyVerKey = @"844sJfb2snyeEugKvpY7Y4jZJk9LT6BnS6bnuKoiqbip";
    NSString* protocolType = @"3.0";
    NSString* logo = @"https://robothash.com/logo.png";
    NSString* name = @"real institution name";

    return [NSString stringWithFormat: @"{\"agency_url\":\"%@\",\"agency_did\":\"%@\",\"agency_verkey\":\"%@\",\"wallet_name\":\"%@\",\"wallet_key\":\"%@\",\"protocol_type\":\"%@\",\"logo\":\"%@\",\"name\":\"%@\"}", agencyUrl, agencyDid, agencyVerKey, walletName, walletKey, protocolType, logo, name];
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
