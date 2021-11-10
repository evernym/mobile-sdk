//
//  ConnectionHandler.m
//  MSDKSampleAppObjC
//
//  Created by Evernym on 04.11.2021.
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

@implementation ConnectionHandler

+(void) handleConnectionInvitation: (NSString *)invite
             withCompletionHandler: (ResponseBlock) completionBlock {
    NSString *name = [ConnectionInvitation getConnectionName:invite];
    NSString *type = [ConnectionInvitation getInvitationType:invite];
    
    NSArray *serializedConnections = [ConnectionInvitation getAllSerializedConnections];
    [Connection verityConnectionExist:invite
                serializedConnections:serializedConnections
                 withCompletion:^(NSString *existingConnection, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        
        if ([ConnectionInvitation isAriesInvitation:type]) {
            if (existingConnection != nil) {
                [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Connection redirect", name]];
            } else {
                [Connection createConnection:invite
                                        name:name
                       withCompletionHandler:^(NSString *responseConnection, NSError *error) {
                    if (error && error.code > 0) {
                        [Utilities printError:error];
                    }
                    return completionBlock(responseConnection, error);
                }];
            }
        }
        
        if ([ConnectionInvitation isOutOfBandInvitation:type]) {
            NSDictionary *attachment = [ConnectionInvitation extractRequestAttach: [Utilities jsonToDictionary:invite]];

            if (attachment) {
                [self handleOutOfBandConnectionInvitationWithAttachment: invite
                                                             attachment:attachment
                                                     existingConnection:existingConnection
                                                                   name:name
                                                  withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                    if (error && error.code > 0) {
                        [Utilities printError:error];
                    }
                    return completionBlock([Utilities dictToJsonString: responseObject], error);
                }];
            } else {
                if (existingConnection != nil) {
                    [Connection connectionRedirectAriesOutOfBand:invite
                                            serializedConnection:existingConnection
                                           withCompletionHandler:^(BOOL result, NSError *error) {
                        [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Connection redirect", name]];
                        if (error && error.code > 0) {
                            [Utilities printError:error];
                        }
                        return completionBlock(nil, error);
                    }];
                } else {
                    [Connection createConnection:invite
                           withCompletionHandler:^(NSString *responseConnection, NSError *error) {
                        if (error && error.code > 0) {
                            [Utilities printError:error];
                        }
                        return completionBlock(responseConnection, error);
                    }];
                }
            }
        }
    }];
}

+(void) handleOutOfBandConnectionInvitationWithAttachment:(NSString *) invite
                                               attachment:(NSDictionary *) attachment
                                       existingConnection:(NSString *) existingConnection
                                                     name:(NSString *) name
                                    withCompletionHandler:(ResponseWithObject) completionBlock {
    NSString *attachType = [ConnectionInvitation getAttachmentType:attachment];
    
    if ([ConnectionInvitation isCredentialAttachment:attachType]) {
        [self processInvitationWithCredentialAttachment:invite
                                             attachment:attachment
                                     existingConnection:existingConnection
                                                   name:name
                                  withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
            return completionBlock(responseObject, error);
        }];
    }
    if ([ConnectionInvitation isProofAttachment:attachType]) {
        [self processInvitationWithProffAttachment:invite
                                        attachment:attachment
                                existingConnection:existingConnection
                                              name:name
                             withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
            return completionBlock(responseObject, error);
        }];
    }
    return completionBlock(nil, nil);
}

+(void) processInvitationWithCredentialAttachment:(NSString *) invite
                                       attachment:(NSDictionary *) attachment
                               existingConnection:(NSString *) existingConnection
                                             name:(NSString *) name
                            withCompletionHandler:(ResponseWithObject) completionBlock {
    if (existingConnection != nil) {
        [Connection connectionRedirectAriesOutOfBand:invite
                                serializedConnection:existingConnection
                               withCompletionHandler:^(BOOL result, NSError *error) {
            [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Connection redirect", name]];
            
            [CredentialOffersHandler createCredentialStateObject:invite
                                                      attachment:attachment
                                              existingConnection:existingConnection
                                           withCompletionHandler:^(NSString *successMessage, NSError *error) {
                return completionBlock([Utilities jsonToDictionary:successMessage], error);
            }];
            
//            [Credential handleCredentialOffer:attachment
//                         serializedConnection:existingConnection
//                                         name:name
//                        withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
//                return completionBlock(responseObject, error);
//            }];
        }];
    } else {
        [CredentialOffersHandler createCredentialStateObject:invite
                                                  attachment:attachment
                                          existingConnection:existingConnection
                                       withCompletionHandler:^(NSString *successMessage, NSError *error) {
            return completionBlock([Utilities jsonToDictionary:successMessage], error);
        }];
//        [Connection createConnection:invite
//                                name:name
//               withCompletionHandler:^(NSString *responseConnection, NSError *error) {
//
//            [Credential handleCredentialOffer:attachment
//                         serializedConnection:responseConnection
//                                         name:name
//                        withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
//                return completionBlock(responseObject, error);
//            }];
//        }];
    }
}

+(void) processInvitationWithProffAttachment:(NSString *) invite
                                  attachment:(NSDictionary *) attachment
                          existingConnection:(NSString *) existingConnection
                                        name:(NSString *) name
                       withCompletionHandler:(ResponseWithObject) completionBlock {
    if (existingConnection != nil) {
        [Connection connectionRedirectAriesOutOfBand:invite
                                serializedConnection:existingConnection
                               withCompletionHandler:^(BOOL result, NSError *error) {
            [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Connection redirect", name]];
            
            [ProofRequest handleProofRequest:attachment
                        serializedConnection:existingConnection
                                        name:name
                       withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                return completionBlock(responseObject, error);
            }];
        }];
    } else {
        [Connection createConnection:invite
               withCompletionHandler:^(NSString *responseConnection, NSError *error) {
            [ProofRequest handleProofRequest:attachment
                        serializedConnection:responseConnection
                                        name:name
                       withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                return completionBlock(responseObject, error);
            }];
        }];
    }
}

@end
