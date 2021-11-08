//
//  CredentialOffer.m
//  MSDKSampleAppObjC
//
//  Created by Evernym on 07.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CredentialOffer.h"

@implementation CredentialOffer

+(NSString *)getThid:(NSString *) credential {
    NSError *error;
    NSMutableDictionary *credValues = [NSJSONSerialization JSONObjectWithData: [credential dataUsingEncoding: NSUTF8StringEncoding] options: NSJSONReadingMutableContainers error: &error];
    NSLog(@"credValuescredValues %@", credValues);
    
    if (credValues[@"thread_id"]) {
        return credValues[@"thread_id"];
    }
    return credValues[@"~thread"][@"thid"];
}

@end
