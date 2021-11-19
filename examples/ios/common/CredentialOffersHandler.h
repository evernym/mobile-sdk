//
//  CredentialOffersHandler.h
//  MSDKSampleAppObjC
//
//  Created by Evernym on 09.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#ifndef CredentialOffersHandler_h
#define CredentialOffersHandler_h


#import <Foundation/Foundation.h>
#import "Utilities.h"

@interface CredentialOffersHandler : NSObject

+(void)createCredentialStateObject:(NSString *) invite
                        attachment:(NSDictionary *) attachment
                existingConnection:(NSString *) existingConnection
             withCompletionHandler:(ResponseBlock) completionBlock;

+(void)handleCredentialOffer:(NSString *) invite
                  attachment:(NSDictionary *) attachment
          existingConnection:(NSString *) existingConnection
                createdOffer:(NSDictionary *) createdOffer
       withCompletionHandler:(ResponseBlock) completionBlock;

+(void)acceptCredentialOffer:(NSString *) pwDid
                  attachment:(NSString *) attachment
                createdOffer:(NSDictionary *) createdOffer
                 fromMessage:(BOOL) fromMessage
       withCompletionHandler:(ResponseBlock) completionBlock;

+(void)acceptCredentialOfferAndCreateConnection:(NSString *) invite
                                     attachment:(NSDictionary *) attachment
                                   createdOffer:(NSDictionary *) createdOffer
                          withCompletionHandler:(ResponseBlock) completionBlock;

+(void)rejectCredentialOffer:(NSString *) pwDid
                  attachment:(NSDictionary *) attachment
                createdOffer:(NSDictionary *) createdOffer
       withCompletionHandler:(ResponseBlock) completionBlock;
@end


#endif /* CredentialOffersHandler_h */
