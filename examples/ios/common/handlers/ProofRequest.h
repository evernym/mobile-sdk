//
//  ProofRequest.h
//  MSDKSampleAppObjC
//
//  Created by Predrag Jevtic on 6/18/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Utilities.h"

NS_ASSUME_NONNULL_BEGIN

@interface ProofRequest: NSObject

/*
    ProofAttributes dictionary should contain 2 keys: autofilledAttributes & selfAttestedAttributes
    autofilledAttributes: attributes retreived from existing credentials (most of the attributes will be this type)
    selfAttestedAttributes: attributes which user will need to fill in UI form
 */
+(void) sendProof:(NSDictionary *) proofRequest
serializedConnection:(NSString *) serializedConnection
withCompletionHandler:(ResponseWithObject) completionBlock;

+(void) autofillAttributes:(NSDictionary*) proofObject
              andConnection:(NSDictionary*) connection
      withCompletionHandler:(ResponseWithObject) completionBlock;

+(void) createWithRequest:(NSString *) request
    withCompletionHandler:(ResponseWithObject) completionBlock;

+(void) retrieveAvailableCredentials:(NSString *) serializedProof
                withCompletionHandler:(ResponseWithObject) completionBlock;

+(void) send:(NSString *) serializedConnection
      serializedProof:(NSString *) serializedProof
        selectedCreds:(NSString *) selectedCreds
     selfAttestedAttr:(NSString *) selfAttestedAttr
withCompletionHandler:(ResponseWithObject) completionBlock;

+(void) rejectProofRequest:(NSString *) serializedConnection
           serializedProof:(NSString *) serializedProof
     withCompletionHandler:(ResponseWithObject) completionBlock;

+(NSDictionary*)vcxMatchingCredentials: (NSString*) matchingCredentials;

@end

NS_ASSUME_NONNULL_END
