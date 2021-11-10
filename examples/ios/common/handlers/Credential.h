//
//  Credential.h
//  MSDKSampleAppObjC
//
//  Created by Predrag Jevtic on 6/11/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#ifndef Credential_h
#define Credential_h

#import "Utilities.h"

@interface Credential: NSObject

+(void) createWithOffer:(NSString*)offer
  withCompletionHandler:(ResponseWithObject) completionBlock;

+(void) acceptCredential:(NSString *) attachment
    serializedCredential:(NSString *) serializedCredential
    serializedConnection:(NSString *) serializedConnection
             fromMessage:(BOOL) fromMessage
   withCompletionHandler:(ResponseBlock) completionBlock;

+(void) rejectCredentialOffer:(NSString*) serializedConnection
         serializedCredential:(NSString*) serializedCredential
        withCompletionHandler:(ResponseBlock) completionBlock;
@end

#endif /* Credential_h */
