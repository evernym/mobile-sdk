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

@implementation Proof

+(NSString *)getThid:(NSDictionary *) request {
    NSString *threadId = [request valueForKey:@"~thread"][@"thid"];
    if (threadId == nil) {
        threadId = [request valueForKey:@"thread_id"];
    }
    return threadId;
}

+(NSString *)getAttributes:(NSDictionary *) request {
    NSDictionary *proofRequestData = [request valueForKey:@"proof_request_data"];
    NSString *requested_attributes = nil;
    if (proofRequestData) {
        requested_attributes = [Utilities dictToJsonString:[proofRequestData valueForKey:@"requested_attributes"]];
    } else {
        NSArray* requestAttach = [request objectForKey: @"request_presentations~attach"];
        NSDictionary* requestAttachItem = requestAttach[0];
        NSDictionary* requestAttachData = [requestAttachItem objectForKey: @"data"];
        NSString* requestAttachBase64 = [requestAttachData objectForKey: @"base64"];

        NSData* invitationData = [Utilities decode64String: requestAttachBase64];
        NSString* json = [[NSString alloc] initWithData: invitationData encoding: NSUTF8StringEncoding];
        NSDictionary* dict = [Utilities jsonToDictionary:json];
        requested_attributes = [Utilities dictToJsonString:[dict valueForKey:@"requested_attributes"]];
    }
    return requested_attributes;
}

+(NSString *)getPredicates:(NSDictionary *) request {
    NSDictionary *proofRequestData = [request valueForKey:@"proof_request_data"];
    NSString *requested_predicates = nil;
    if (proofRequestData) {
        requested_predicates = [Utilities dictToJsonString:[proofRequestData valueForKey:@"requested_predicates"]];
    } else {
        NSArray* requestAttach = [request objectForKey: @"request_presentations~attach"];
        NSDictionary* requestAttachItem = requestAttach[0];
        NSDictionary* requestAttachData = [requestAttachItem objectForKey: @"data"];
        NSString* requestAttachBase64 = [requestAttachData objectForKey: @"base64"];

        NSData* invitationData = [Utilities decode64String: requestAttachBase64];
        NSString* json = [[NSString alloc] initWithData: invitationData encoding: NSUTF8StringEncoding];
        NSDictionary* dict = [Utilities jsonToDictionary:json];
        requested_predicates = [Utilities dictToJsonString:[dict valueForKey:@"requested_predicates"]];
    }
    return requested_predicates;
}

@end
