//
//  Proof.m
//  MSDKSampleAppObjC
//
//  Created by Evernym on 10.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Utilities.h"
#import "Proof.h"
#import "MobileSDK.h"

@implementation Proof

+(NSString *)getThid:(NSDictionary *) request {
    NSString *threadId = [request valueForKey:@"~thread"][@"thid"];
    if (threadId == nil) {
        threadId = [request valueForKey:@"@id"];
    }
    return threadId;
}

@end
