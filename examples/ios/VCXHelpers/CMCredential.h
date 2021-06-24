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

+(void) acceptCredOfferWithMsgid: (NSDictionary*) messageObj
                  forConnection: (NSDictionary*) connection
          withCompletionHandler: (ResponseBlock) completionBlock;

+(void) createWithOffer: (NSString*)offer
  withCompletionHandler: (ResponseWithObject) completionBlock;

+(void) acceptCredentialOffer: (NSString*) serializedConnection
         serializedCredential: (NSString*) serializedCredential
        withCompletionHandler: (ResponseWithObject) completionBlock;

+(void) rejectCredentialOffer: (NSString*) serializedConnection
         serializedCredential: (NSString*) serializedCredential
        withCompletionHandler: (ResponseWithObject) completionBlock;

@end

#endif /* CMCredential_h */
