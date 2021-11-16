//
//  ProofRequestsHandler.m
//  MSDKSampleAppObjC
//
//  Created by Evernym on 10.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ProofRequestsHandler.h"
#import "Utilities.h"
#import "LocalStorage.h"
#import "ProofRequest.h"
#import "MobileSDK.h"
#import "Connection.h"
#import "ConnectionInvitation.h"
#import "Proof.h"

@implementation ProofRequestsHandler

NSString *PROOF_PENDING_STATUS = @"pending";
NSString *PROOF_COMPLETED_STATUS = @"completed";

+(void) createProofStateObject:(NSString *) invite
                    attachment:(NSDictionary *) attachment
            existingConnection:(NSString *) existingConnection
         withCompletionHandler:(ResponseBlock) completionBlock {
    NSString *name = [ConnectionInvitation getConnectionName:invite];

    [ProofRequest createWithRequest:[Utilities dictToJsonString: attachment]
              withCompletionHandler:^(NSDictionary *request, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        
        [self handleProofRequest:invite
                      attachment:attachment
              existingConnection:existingConnection
                         request:request
                            name:name
           withCompletionHandler:^(NSString *successMessage, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            
            return completionBlock(successMessage, error);
        }];
    }];
}


+(void)handleProofRequest:(NSString *) invite
               attachment:(NSDictionary *) attachment
       existingConnection:(NSString *) existingConnection
                  request:(NSDictionary *) request
                     name:(NSString *) name
       withCompletionHandler:(ResponseBlock) completionBlock {
    if (existingConnection == nil) {
        [self acceptProofRequestAndCreateConnection:invite
                                         attachment:attachment
                                            request:request
                                               name:name
                              withCompletionHandler:^(NSString *successMessage, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            
            return completionBlock(successMessage, error);
        }];
    } else {
        [ConnectionInvitation getPwDid:existingConnection
                 withCompletionHandler:^(NSString *pwDid, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            
            [self acceptProofRequest:pwDid
                          attachment:attachment
                             request:request
                                name:name
               withCompletionHandler:^(NSString *successMessage, NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                
                return completionBlock(successMessage, error);
            }];
        }];
    }
}

+(void)acceptProofRequest:(NSString *) pwDid
               attachment:(NSDictionary *) attachment
                  request:(NSDictionary *) request
                     name:(NSString *) name
    withCompletionHandler:(ResponseBlock) completionBlock {
    NSString *serializedConnection = [ConnectionInvitation getConnectionByPwDid:pwDid];
    
    [ProofRequest sendProof:request
              serializedConnection:serializedConnection
             withCompletionHandler:^(NSDictionary *proofRequest, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        
        // Store the serialized proof
        NSMutableDictionary* proofs = [[LocalStorage getObjectForKey: @"proofs" shouldCreate: true] mutableCopy];
        
        NSString *threadId = [Proof getThid:attachment];
        NSString *requested_attributes = [Proof getAttributes:attachment];
        NSString *requested_predicates = [Proof getPredicates:attachment];
        
        NSTimeInterval timeStamp = [[NSDate date] timeIntervalSinceNow];
        NSString *timestamp = [NSString stringWithFormat:@"%@", [NSNumber numberWithDouble: timeStamp]];
        NSString *uuid = [[NSUUID UUID] UUIDString];

        NSDictionary* proofObj = @{
            @"pwDid": pwDid,
            @"serialized": [Utilities dictToJsonString:proofRequest],
            @"threadId": threadId,
            
            @"title": name,
            @"requested_attributes": requested_attributes,
            @"requested_predicates": requested_predicates,
            
            @"timestamp": timestamp,
            @"status": PROOF_COMPLETED_STATUS,
        };
        
        [proofs setValue: proofObj forKey: uuid];
        [LocalStorage store: @"proofs" andObject: proofs];
        
        [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Proof request send", @""]];
        
        return completionBlock([Utilities dictToJsonString:proofRequest], error);
    }];
}

+(void)acceptProofRequestAndCreateConnection:(NSString *) invite
                                  attachment:(NSDictionary *) attachment
                                     request:(NSDictionary *) request
                                        name:(NSString *) name
                       withCompletionHandler:(ResponseBlock) completionBlock {
    [Connection createConnection:invite
           withCompletionHandler:^(NSString *responseConnection, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        
        [ConnectionInvitation getPwDid:responseConnection
                 withCompletionHandler:^(NSString *pwDid, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            
            [self acceptProofRequest:pwDid
                          attachment:attachment
                             request:request
                                name:name
                  withCompletionHandler:^(NSString *successMessage, NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                
                return completionBlock(successMessage, error);
            }];
        }];
    }];
}

+(void)rejectProofRequest:(NSString *) pwDid
                  request:(NSString *) request
                     name:(NSString *) name
    withCompletionHandler:(ResponseBlock) completionBlock {
    NSString *serializedConnection = [ConnectionInvitation getConnectionByPwDid:pwDid];
    
    [ProofRequest rejectProofRequest:serializedConnection
                     serializedProof:request
               withCompletionHandler:^(NSDictionary *rejectedProof, NSError *error) {

        [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Proof request rejected", name]];

        return completionBlock([Utilities dictToJsonString:rejectedProof], error);
    }];
}


@end
