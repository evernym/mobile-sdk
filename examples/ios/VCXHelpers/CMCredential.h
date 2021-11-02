//
//  CMCredential.h
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/11/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#ifndef CMCredential_h
#define CMCredential_h

#import "CMUtilities.h"

@interface CMCredential: NSObject

+(void) createWithOffer: (NSString*)offer
  withCompletionHandler: (ResponseWithObject) completionBlock;

+(void) acceptCredentialOffer: (NSString*) serializedConnection
         serializedCredential: (NSString*) serializedCredential
                        offer: (NSString*) offer
        withCompletionHandler: (ResponseBlock) completionBlock;

+(void) rejectCredentialOffer: (NSString*) serializedConnection
         serializedCredential: (NSString*) serializedCredential
        withCompletionHandler: (ResponseWithObject) completionBlock;

+(void)acceptCredentilaFromMessage:(NSString *) data
               withCompletionBlock:(ResponseWithBoolean) completionBlock;

+(void)rejectCredentilaFromMessage:(NSString *) data
               withCompletionBlock:(ResponseWithBoolean) completionBlock;

+(void)awaitCredentialReceived:(NSString *) serializedCredential
                         offer:(NSString *) offer
           withCompletionBlock:(ResponseBlock) completionBlock;
@end

#endif /* CMCredential_h */
