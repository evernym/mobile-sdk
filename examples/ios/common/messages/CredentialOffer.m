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

+(NSString *)getThid:(NSDictionary *) credential {
    NSString *threadId = [credential valueForKey:@"~thread"][@"thid"];
    if (threadId == nil) {
        threadId = [credential valueForKey:@"@id"];
    }
    return threadId;
}

+(NSString *)getAttributes:(NSDictionary *) credential {
    NSDictionary *credentialPreview = [credential valueForKey: @"credential_preview"];
    NSDictionary *attr = [credentialPreview valueForKey: @"attributes"];
    return [Utilities dictToJsonString:attr];
}

+(NSString *)getOfferName:(NSDictionary *) credential {
    NSString *name = [credential valueForKey: @"comment"];
    return name;
}

@end
