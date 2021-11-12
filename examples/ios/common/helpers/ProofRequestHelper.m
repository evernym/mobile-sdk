//
//  ProofRequestHelper.m
//  MSDKSampleAppObjC
//
//  Created by Evernym on 10.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ProofRequestHelper.h"
#import "Utilities.h"

@implementation ProofRequestHelper

// Is it used???

+(NSString *)getThid:(NSDictionary *) requests {
    NSString *thread_id = [requests valueForKey:@"~thread"];

    return thread_id;
}

@end
