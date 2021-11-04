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
+ (void) sendProofRequest: (NSDictionary*) proofObject
          proofAttributes: (NSDictionary*) proofAttributes
            andConnection: (NSDictionary*) connection
    withCompletionHandler: (ResponseBlock) completionBlock;

+ (void) autofillAttributes: (NSDictionary*) proofObject
              andConnection: (NSDictionary*) connection
      withCompletionHandler: (ResponseWithObject) completionBlock;

+ (void) createWithRequest: (NSString *) request
    withCompletionHandler: (ResponseWithObject) completionBlock;

+ (void) retrieveAvailableCredentials:(NSString *) serializedProof
                withCompletionHandler:(ResponseWithObject) completionBlock;

+ (void) send:(NSString *) serializedConnection
      serializedProof:(NSString *) serializedProof
        selectedCreds:(NSString *) selectedCreds
     selfAttestedAttr:(NSString *) selfAttestedAttr
withCompletionHandler: (ResponseWithObject) completionBlock;

+ (void) reject:(NSString *) serializedConnection
serializedProof:(NSString *) serializedProof
withCompletionHandler: (ResponseWithObject) completionBlock;

+ (NSDictionary*)vcxMatchingCredentials: (NSString*) matchingCredentials;

+(void)sendProofRequestFromMessage:(NSString *) data
             withCompletionHandler:(ResponseWithBoolean) completionBlock;

+(void)rejectProofRequestFromMessage:(NSString *) data
               withCompletionHandler:(ResponseWithBoolean) completionBlock;
@end

NS_ASSUME_NONNULL_END
