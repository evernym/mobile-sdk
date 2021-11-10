//
//  CredentialOffer.m
//  MSDKSampleAppObjC
//
//  Created by Evernym on 07.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CredentialOffer.h"
#import "Utilities.h"

@implementation CredentialOffer

+(NSString *)getThid:(NSString *) credential
         fromMessage:(BOOL) fromMessage {
    if (fromMessage) {
        NSArray *credValuesArr = [Utilities jsonToArray:credential];
        NSDictionary *credValues = credValuesArr[0];
        NSString *thread_id = [credValues valueForKey:@"thread_id"];
        
        if (thread_id) {
            return thread_id;
        }
        return [[credValues valueForKey:@"~thread"] valueForKey:@"thid"];
    }
    
    NSDictionary *credValues = [Utilities jsonToDictionary:credential];
    NSString *thread_id = [credValues valueForKey:@"thread_id"];
    
    if (thread_id) {
        return thread_id;
    }
    return [[credValues valueForKey:@"~thread"] valueForKey:@"thid"];
}

+(NSString *)getAttributes:(NSString *) credential
               fromMessage:(BOOL) fromMessage {
    if (fromMessage) {
        NSArray *credValuesArr = [Utilities jsonToArray:credential];
        NSDictionary *credValues = credValuesArr[0];
        NSDictionary *attr = [credValues valueForKey: @"credential_attrs"];

        return [Utilities dictToJsonString:attr];
    }
    NSDictionary *credValuesObj = [Utilities jsonToDictionary:credential];
    NSDictionary *credentialPreview = [credValuesObj valueForKey: @"credential_preview"];
    NSDictionary *attr = [credentialPreview valueForKey: @"attributes"];
    return [Utilities dictToJsonString:attr];
}

+(NSString *)getOfferName:(NSString *) credential
               fromMessage:(BOOL) fromMessage {
    if (fromMessage) {
        NSArray *credValuesArr = [Utilities jsonToArray:credential];
        NSDictionary *credValues = credValuesArr[0];
        NSString *name = [credValues valueForKey: @"claim_name"];

        return name;
    }
    NSDictionary *credValuesObj = [Utilities jsonToDictionary:credential];
    NSString *name = [credValuesObj valueForKey: @"comment"];
    return name;
}

@end
