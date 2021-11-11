//
//  CredentialOffersHandler.m
//  MSDKSampleAppObjC
//
//  Created by Evernym on 09.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ConnectionHandler.h"
#import "Utilities.h"
#import "Connection.h"
#import "ConnectionInvitation.h"
#import "LocalStorage.h"
#import "Credential.h"
#import "ProofRequest.h"
#import "CredentialOffersHandler.h"
#import "CredentialOffer.h"
#import "MobileSDK.h"

@implementation CredentialOffersHandler

NSString *CREDENTIAL_PENDING_STATUS = @"pending";
NSString *CREDENTIAL_COMPLETED_STATUS = @"completed";

+(void)createCredentialStateObject:(NSString *) invite
                        attachment:(NSDictionary *) attachment
                existingConnection:(NSString *) existingConnection
             withCompletionHandler:(ResponseBlock) completionBlock {
    [Credential createWithOffer:[Utilities dictToJsonString:attachment]
          withCompletionHandler:^(NSDictionary *createdOffer, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        
        [self handleCredentialOffer:invite
                         attachment:attachment
                 existingConnection:existingConnection
                       createdOffer:createdOffer
              withCompletionHandler:^(NSString *successCredential, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            
            return completionBlock(successCredential, error);
        }];
    }];
}

+(void)handleCredentialOffer:(NSString *) invite
                  attachment:(NSDictionary *) attachment
          existingConnection:(NSString *) existingConnection
                createdOffer:(NSDictionary *) createdOffer
       withCompletionHandler:(ResponseBlock) completionBlock {
    if (existingConnection == nil) {
        [self acceptCredentialOfferAndCreateConnection:invite
                                            attachment:attachment
                                          createdOffer:createdOffer
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
            
            [self acceptCredentialOffer:pwDid
                             attachment:attachment
                           createdOffer:createdOffer
                            fromMessage:false
                  withCompletionHandler:^(NSString *successCredential, NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                
                return completionBlock(successCredential, error);
            }];
        }];
    }
}

+(void)acceptCredentialOffer:(NSString *) pwDid
                  attachment:(NSDictionary *) attachment
                createdOffer:(NSDictionary *) createdOffer
                 fromMessage:(BOOL) fromMessage
       withCompletionHandler:(ResponseBlock) completionBlock {
    NSString *serializedConnection = [ConnectionInvitation getConnectionByPwDid:pwDid];
    
    [Credential acceptCredential:[Utilities dictToJsonString: attachment]
            serializedCredential:[Utilities dictToJsonString: createdOffer]
            serializedConnection:serializedConnection
                     fromMessage:fromMessage
           withCompletionHandler:^(NSString *successCredential, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        
        // Store the serialized credential
        NSMutableDictionary* credentials = [[LocalStorage getObjectForKey: @"credentials" shouldCreate: true] mutableCopy];
        
        NSString *threadId = [CredentialOffer getThid:[Utilities dictToJsonString:attachment] fromMessage:fromMessage];
        NSString *name = [CredentialOffer getOfferName:[Utilities dictToJsonString:attachment] fromMessage:fromMessage];
        NSString *attr = [CredentialOffer getAttributes:[Utilities dictToJsonString:attachment] fromMessage:fromMessage];
        
        NSTimeInterval timeStamp = [[NSDate date] timeIntervalSinceNow];
        NSString *timestamp = [NSString stringWithFormat:@"%@", [NSNumber numberWithDouble: timeStamp]];
        NSString *uuid = [[NSUUID UUID] UUIDString];

        NSDictionary* credentialObj = @{
            @"pwDid": pwDid,
            @"serialized": successCredential,
            @"threadId": threadId,
            
            @"name": name,
            @"attributes": attr,
            @"timestamp": timestamp,
            
            @"status": CREDENTIAL_COMPLETED_STATUS,
        };
        
        [credentials setValue: credentialObj forKey: uuid];
        [LocalStorage store: @"credentials" andObject: credentials];
        
        [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Credential offer accept", name]];

        return completionBlock(successCredential, error);
    }];
}

+(void)acceptCredentialOfferAndCreateConnection:(NSString *) invite
                                     attachment:(NSDictionary *) attachment
                                   createdOffer:(NSDictionary *) createdOffer
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
            
            [self acceptCredentialOffer:pwDid
                             attachment:attachment
                           createdOffer:createdOffer
                            fromMessage:false
                  withCompletionHandler:^(NSString *successMessage, NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                
                return completionBlock(successMessage, error);
            }];
        }];
    }];
}

+(void)rejectCredentialOffer:(NSString *) pwDid
                  attachment:(NSDictionary *) attachment
                createdOffer:(NSDictionary *) createdOffer
       withCompletionHandler:(ResponseBlock) completionBlock {
    NSString *serializedConnection = [ConnectionInvitation getConnectionByPwDid:pwDid];
    
    [Credential rejectCredentialOffer:serializedConnection
                 serializedCredential:[Utilities dictToJsonString: createdOffer]
                withCompletionHandler:^(NSString *rejectedCredential, NSError *error) {
        NSString *name = [CredentialOffer getOfferName:[Utilities dictToJsonString:attachment] fromMessage:true];
        
        [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Credential offer rejected", name]];

        return completionBlock(rejectedCredential, error);
    }];
}

@end
