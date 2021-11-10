//
//  ProofRequestsHandler.h
//  MSDKSampleAppObjC
//
//  Created by Evernym on 10.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#ifndef ProofRequestsHandler_h
#define ProofRequestsHandler_h

#import <Foundation/Foundation.h>
#import "Utilities.h"

@interface ProofRequestsHandler : NSObject

+(void) createProofStateObject:(NSString *) invite
                    attachment:(NSDictionary *) attachment
            existingConnection:(NSString *) existingConnection
         withCompletionHandler:(ResponseBlock) completionBlock;

+(void)handleProofRequest:(NSString *) invite
               attachment:(NSDictionary *) attachment
       existingConnection:(NSString *) existingConnection
                  request:(NSDictionary *) request
                     name:(NSString *) name
    withCompletionHandler:(ResponseBlock) completionBlock;

+(void)acceptProofRequest:(NSString *) pwDid
               attachment:(NSDictionary *) attachment
                  request:(NSDictionary *) request
                     name:(NSString *) name
    withCompletionHandler:(ResponseBlock) completionBlock;

+(void)acceptProofRequestAndCreateConnection:(NSString *) invite
                                  attachment:(NSDictionary *) attachment
                                     request:(NSDictionary *) request
                                        name:(NSString *) name
                       withCompletionHandler:(ResponseBlock) completionBlock;

+(void)rejectProofRequest:(NSString *) pwDid
                  request:(NSString *) request
                     name:(NSString *) name
    withCompletionHandler:(ResponseBlock) completionBlock;

@end

#endif /* ProofRequestsHandler_h */
