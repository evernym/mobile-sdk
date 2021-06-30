//
//  Connection.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 5/28/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CMConnection.h"
#import "LocalStorage.h"
#import "MobileSDK.h"
#import "CMCredential.h"
#import "CMMessage.h"
#import "CMProofRequest.h"

@implementation CMConnection

/*
 Connection type example:
 "{"connection_type":"SMS","phone":"123"}" OR: "{"connection_type":"QR","phone":""}"
 */
+(NSString*) connectionByType: (int) type {
    return  [@[@"QR", @"SMS"] objectAtIndex: type];
}

+(NSString*)getPwDid: (NSString*) serializedConnection {
    NSError *error;
    NSMutableDictionary *connValues = [NSJSONSerialization JSONObjectWithData: [serializedConnection dataUsingEncoding: NSUTF8StringEncoding] options: NSJSONReadingMutableContainers error: &error];

    return connValues[@"data"][@"pw_did"];
}

+(NSDictionary*)parsedInvite: (NSString *)invite {
    if ([invite rangeOfString:@"oob"].location != NSNotFound) {
        return [self parseInvitationLink: invite];
    } else if ([invite rangeOfString:@"c_i"].location != NSNotFound) {
        return [self parseInvitationLink: invite];
    } else {
        return [self readFromUrl: invite];
    }
}

+(NSDictionary*)readFromUrl: (NSString*)invite {
    if(!invite) {
        return nil;
    }
    NSURL *url = [NSURL URLWithString:invite];
    NSData *data = [[NSData alloc] initWithContentsOfURL:url];
    NSAssert(data, @"No data received!");
    NSDictionary *result = [NSJSONSerialization JSONObjectWithData:data options:0 error:NULL];
    return result;
}

+(NSDictionary*) extractRequestAttach: (NSDictionary*)invite {
    NSArray* requestAttach = [invite objectForKey: @"request~attach"];
    if (requestAttach) {
        NSDictionary* requestAttachItem = requestAttach[0];
        NSDictionary* requestAttachData = [requestAttachItem objectForKey: @"data"];
        NSString* requestAttachBase64 = [requestAttachData objectForKey: @"base64"];

        NSData* invitationData = [CMUtilities decode64String: requestAttachBase64];
        NSString* json = [[NSString alloc] initWithData: invitationData encoding: NSUTF8StringEncoding];
        NSLog(@" JSON %@", json);
        return [CMUtilities jsonToDictionary: json];
    } else {
        return nil;
    }
}

+(NSDictionary*) parseInvitationLink: (NSString*) link {
    NSArray* linkComponents = [link componentsSeparatedByString: @"msg?c_i="];

    if([linkComponents count] < 2) {
        return nil;
    }

    NSData* invitationData = [CMUtilities decode64String: linkComponents[1]];
    NSString*  json = [[NSString alloc] initWithData: invitationData encoding: NSUTF8StringEncoding];

    return [CMUtilities jsonToDictionary: json];
}

+(NSString*)connectionID: connectValues {
    NSString* connectionID = [connectValues objectForKey: @"id"];

    if(!connectionID) {
        connectionID =  [connectValues objectForKey: @"@id"];
    }

    if(!connectionID) {
        NSDictionary* connectionData = [CMUtilities jsonToDictionary: connectValues[@"serializedConnection"]];
        connectionID = connectionData[@"data"][@"pw_did"];
    }

    if(!connectionID) {
        NSLog(@"Connection ID is missing %@", connectValues);
    }

    return connectionID;
}

+(NSString*) connectionName: (NSDictionary*)connection {
    NSString* connectionName = connection[@"invitation"][@"s"][@"n"];
    if(!connectionName) {
        connectionName = connection[@"invitation"][@"label"];
    }

    return connectionName;
}

+(void)connectionRedirectProprietary:(NSString *)invitationDetails
                serializedConnection:(NSString *)serializedConnection
               withCompletionHandler: (ResponseWithBoolean) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    
    @try {
        //TODO: Added parsed invite as input paramert
        [sdkApi connectionCreateWithInvite:[self connectionID: invitationDetails]
                             inviteDetails:invitationDetails
                                completion:^(NSError *error, NSInteger handle) {
            if (error && error.code > 0) {
                return completionBlock(NO, error);
            }
            [sdkApi connectionDeserialize:serializedConnection
                               completion:^(NSError *error, NSInteger connectionHandle) {
                if (error && error.code > 0) {
                    return completionBlock(NO, error);
                }
                [sdkApi connectionRedirect:(int)handle
                      withConnectionHandle:(int)connectionHandle
                            withCompletion:^(NSError *error) {
                    if (error && error.code > 0) {
                        return completionBlock(NO, error);
                    }
                    return completionBlock(YES, nil);
                }];
            }];
        }];
    } @catch (NSException *exception) {
        return completionBlock(false, error);
    }
}

+(void)connectionRedirectAriesOutOfBand: (NSString*)invitation
                   serializedConnection: (NSString*)serializedConnection
                  withCompletionHandler: (ResponseWithBoolean) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    
    @try {
        // TODO: add check for handshake protocols
        [sdkApi connectionDeserialize:serializedConnection
                           completion:^(NSError *error, NSInteger connectionHandle) {
            if (error && error.code > 0) {
                return completionBlock(false, error);
            }
            
            [sdkApi connectionSendReuse:(int) connectionHandle
                                 invite:invitation
                         withCompletion:^(NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(false, error);
                }
                
                [CMMessage waitHandshakeReuse:^(BOOL result, NSError *error) {
                    if (error && error.code > 0) {
                        return completionBlock(false, error);
                    }
                    if (result) {
                        return completionBlock(true, nil);
                    }
                }];
            }];
        }];
    } @catch (NSException *exception) {
        return completionBlock(false, error);
    }
}

+(void) verityConnectionExist:(NSString *)invite
               withCompletion:(ResponseBlock) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    NSDictionary* newInviteDict = [CMUtilities jsonToDictionary:invite];
    NSString* newPublicDid = [newInviteDict objectForKey:@"public_did"];
    
    NSDictionary* connections = [[LocalStorage getObjectForKey: @"connections" shouldCreate: true] mutableCopy];
    if (connections.allKeys.count != 0) {
        for (NSInteger i = 0; i < connections.allKeys.count; i++) {
            NSString* key = connections.allKeys[i];
            NSDictionary* connection = [connections objectForKey:key];
            NSString* serializedConnection = [connection objectForKey:@"serializedConnection"];
            NSLog(@"serializedConnection %@ - %@", serializedConnection, invite);
            [sdkApi connectionDeserialize:serializedConnection
                               completion:^(NSError *error, NSInteger connectionHandle) {
                [sdkApi getConnectionInviteDetails:connectionHandle
                                       abbreviated:0
                                    withCompletion:^(NSError *error, NSString *inviteDetails) {
                    NSDictionary* oldInviteDict = [CMUtilities jsonToDictionary:inviteDetails];
                    NSString* oldPublicDid = [oldInviteDict objectForKey:@"public_did"];
                    NSLog(@"serializedConnectionExit %@", serializedConnection);
                    if ([oldPublicDid isEqual:newPublicDid]) {
                        return completionBlock(serializedConnection, nil);
                    } else if (i == connections.allKeys.count - 1 && oldPublicDid != newPublicDid) {
                        return completionBlock(nil, nil);
                    }
                }];
            }];
        }
    } else {
        return completionBlock(nil, nil);
    }
}

+(void)handleAttach:(NSDictionary *)requestAttach
     connectionData:(NSString *)serializedConnection
withCompletionHandler:(ResponseWithObject) completionBlock {
    NSString *type = [requestAttach objectForKey: @"@type"];
    
    if ([type rangeOfString:@"credential"].location != NSNotFound) {
        [CMCredential createWithOffer:[CMUtilities dictToJsonString:requestAttach]
                withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
            if (error && error.code > 0) {
                NSLog(@"Error createWithOffer %@", error);
                return completionBlock(nil, error);
            }
            NSLog(@"Created offer %@", responseObject);

            [CMCredential acceptCredentialOffer:serializedConnection
                           serializedCredential:[CMUtilities dictToJsonString:responseObject]
                          withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                if (error && error.code > 0) {
                    NSLog(@"Error acceptCredentialOffer %@", error);
                    return completionBlock(nil, error);
                }
                [LocalStorage addEventToHistory:@"Credential offer accept"];
                NSLog(@"Credential Offer Accepted %@", error);
                return completionBlock(responseObject, nil);
            }];
        }];
    } else {
        [CMProofRequest createWithRequest:[CMUtilities dictToJsonString:requestAttach]
                    withCompletionHandler:^(NSDictionary *offer, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            NSLog(@"Proof Request created %@", error);

            [CMProofRequest retrieveAvailableCredentials:[CMUtilities dictToJsonString:offer]
                                   withCompletionHandler:^(NSDictionary *creds, NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                };

                NSLog(@"Proof Request retrieved %@", creds);
                NSString *attr = [creds objectForKey: @"autofilledAttributes"];
                
                [CMProofRequest send:serializedConnection
                     serializedProof:[CMUtilities dictToJsonString:offer]
                       selectedCreds:attr
                    selfAttestedAttr:@"{}"
               withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                    if (error && error.code > 0) {
                        return completionBlock(nil, error);
                    }
                    [LocalStorage addEventToHistory:@"Proof request send"];
                    NSLog(@"Proof Request send %@", error);
                    return completionBlock(responseObject, nil);
                }];
            }];
        }];
    }
}

+(void)handleConnection:(NSString *)invite
         connectionType: (int)connectionType
            phoneNumber: (NSString*) phone
  withCompletionHandler:(ResponseWithObject) completionBlock {
    NSString* type = [[CMUtilities jsonToDictionary:invite] objectForKey: @"@type"];
    NSLog(@"handleConnection");

    [self verityConnectionExist:invite
                 withCompletion:^(NSString *response, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        NSLog(@"verityConnectionExist %@ - %@", response, type);
        if (response != nil) {
            if ([type rangeOfString:@"out-of-band"].location != NSNotFound) {
                NSLog(@"Connection redirect");

                [self connectionRedirectAriesOutOfBand:invite
                                  serializedConnection:response
                                 withCompletionHandler:^(BOOL result, NSError *error) {
                    [LocalStorage addEventToHistory:@"Connection redirect"];
                    NSDictionary *requestAttach = [self extractRequestAttach: [CMUtilities jsonToDictionary:invite]];

                    if (requestAttach) {
                        [self handleAttach:requestAttach
                            connectionData:response
                     withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                            return completionBlock(responseObject, error);
                        }];
                    }
                    return completionBlock(nil, error);
                }];
            } else {
                [self connectionRedirectProprietary:invite
                                  serializedConnection:response
                                 withCompletionHandler:^(BOOL result, NSError *error) {
                    NSLog(@"Connection redirect");
                    [LocalStorage addEventToHistory:@"Connection redirect"];
                    return completionBlock(nil, error);
                }];
            }
        } else {
            [self createConnection:invite
                    connectionType:connectionType
                       phoneNumber:phone
             withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                return completionBlock(responseObject, nil);
            }];
        }
    }];
}

+(void)createConnection:(NSString *)invitation
         connectionType: (int)connectionType
            phoneNumber: (NSString*) phone
  withCompletionHandler: (ResponseWithObject) completionBlock {
    NSDictionary* inviteDict = [CMUtilities jsonToDictionary:invitation];
    NSString* type = [inviteDict objectForKey: @"@type"];
    NSLog(@"createConnection");
    if ([type rangeOfString:@"out-of-band"].location != NSNotFound) {
        [self connectWithOutofbandInvite:invitation
                                 connectionType:(int)connectionType
                                    phoneNumber:phone
                          withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            [LocalStorage addEventToHistory: @"Connection connect"];
            NSString* serializedConnection = [responseObject objectForKey: @"serializedConnection"];

            NSDictionary* requestAttach = [LocalStorage getObjectForKey: @"request~attach" shouldCreate:false];
            
            [self handleAttach:requestAttach
                connectionData:serializedConnection
         withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                return completionBlock(responseObject, error);
            }];
        }];
    } else {
        [self connectWithInvite:invitation
                                 connectionType:(int)connectionType
                                    phoneNumber:phone
                          withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
            return completionBlock(responseObject, error);
        }];
    }
}

+(void)connectWithInvite:(NSString *)invitation
connectionType: (int)connectionType
   phoneNumber: (NSString*) phone
withCompletionHandler: (ResponseWithObject) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];

    [LocalStorage store: @"tempConnection" andObject: [CMUtilities jsonToDictionary:invitation]];
    
    [sdkApi connectionCreateWithInvite:[self connectionID: [CMUtilities jsonToDictionary:invitation]]
                         inviteDetails:invitation
                            completion:^(NSError *error, NSInteger connectionHandle) {
        if (error && error.code > 0) {
            if(error && error.code != 1010) {
                return completionBlock(nil, error);
            }
            [sdkApi connectionSendReuse: (int)connectionHandle
                                 invite: invitation
                         withCompletion: ^(NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
            }];
        }

        [CMUtilities printSuccess: @[@"connectionCreateWithInvite",  [NSNumber numberWithLong: connectionHandle]]];
        
        NSString *connectType = [NSString stringWithFormat:@"{\"connection_type\":\"%@\",\"phone\":%@\"\"}",
                                    [CMConnection connectionByType: connectionType], phone];
        [sdkApi connectionConnect: (int)connectionHandle
                   connectionType: connectType
                       completion: ^(NSError *error, NSString *inviteDetails) {

            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            [CMUtilities printSuccess: @[@"connectionConnect", inviteDetails]];

                dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                    while (true) {
                        dispatch_semaphore_t acceptedWaitSemaphore = dispatch_semaphore_create(0);
                        __block NSInteger connectionState = 0;
                        
                        [sdkApi connectionGetState: (int)connectionHandle completion:^(NSError *error, NSInteger state) {
                            if (error && error.code > 0) {
                                dispatch_semaphore_signal(acceptedWaitSemaphore);
                                return completionBlock(nil, error);
                            }

                            if(state != 4) {
                                [sdkApi connectionUpdateState: (int)connectionHandle withCompletion:^(NSError *error, NSInteger state) {
                                    if (error && error.code > 0) {
                                        dispatch_semaphore_signal(acceptedWaitSemaphore);
                                        return completionBlock(nil, error);
                                    }
                                    connectionState = state;
                                    dispatch_semaphore_signal(acceptedWaitSemaphore);
                                }];
                            } else {
                                connectionState = state;
                                dispatch_semaphore_signal(acceptedWaitSemaphore);
                            }
                        }];
                        
                        dispatch_semaphore_wait(acceptedWaitSemaphore, DISPATCH_TIME_FOREVER);
                        if (connectionState == 4) {
                            
                            [sdkApi connectionSerialize: (int)connectionHandle
                                             completion: ^(NSError *error, NSString *connectionSerialized) {
                                if (error && error.code > 0) {
                                    return completionBlock(nil, error);
                                }
                                [CMUtilities printSuccess: @[@"Connection invitation success", connectionSerialized]];

                                // Store the serialized connection
                                NSDictionary* invitation = [LocalStorage getObjectForKey: @"tempConnection" shouldCreate: true];

                                NSMutableDictionary* connections = [[LocalStorage getObjectForKey: @"connections" shouldCreate: true] mutableCopy];
                                NSDictionary* connectionObj = @{
                                    @"serializedConnection": connectionSerialized,
                                    @"invitation": invitation
                                };

                                [connections setValue: connectionObj forKey: [self connectionID: connectionObj]];
                                [LocalStorage store: @"connections" andObject: connections];
                                [LocalStorage deleteObjectForKey: @"tempConnection"];

                                return completionBlock(connectionObj, nil);
                            }];
                            break;
                        }
                    }
                });
        }];
    }];
}

+(void)connectWithOutofbandInvite: (NSString*)invitation
connectionType: (int)connectionType
   phoneNumber: (NSString*) phone
withCompletionHandler: (ResponseWithObject) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];

    NSDictionary *requestAttach = [self extractRequestAttach: [CMUtilities jsonToDictionary:invitation]];
    [LocalStorage store: @"request~attach"
              andObject: requestAttach];

    [LocalStorage store: @"tempConnection" andObject: [CMUtilities jsonToDictionary:invitation]];
    NSLog(@"IDDD  %@", [self connectionID: [CMUtilities jsonToDictionary:invitation]]);
    
    [sdkApi connectionCreateWithOutofbandInvite: [self connectionID: [CMUtilities jsonToDictionary:invitation]]
                                         invite: invitation
                                     completion: ^(NSError *error, NSInteger connectionHandle) {
        if (error && error.code > 0) {
            if(error && error.code != 1010) {
                return completionBlock(nil, error);
            }
            [sdkApi connectionSendReuse: (int)connectionHandle
                                 invite: invitation
                         withCompletion: ^(NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
            }];
        }

        [CMUtilities printSuccess: @[@"connectionCreateWithOutofbandInvite",  [NSNumber numberWithLong: connectionHandle]]];
        
        NSString *connectType = [NSString stringWithFormat:@"{\"connection_type\":\"%@\",\"phone\":%@\"\"}",
                                    [CMConnection connectionByType: connectionType], phone];
        [sdkApi connectionConnect: (int)connectionHandle
                   connectionType: connectType
                       completion: ^(NSError *error, NSString *inviteDetails) {

            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            [CMUtilities printSuccess: @[@"connectionConnect", inviteDetails]];

                dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                    while (true) {
                        dispatch_semaphore_t acceptedWaitSemaphore = dispatch_semaphore_create(0);
                        __block NSInteger connectionState = 0;
                        
                        [sdkApi connectionGetState: (int)connectionHandle completion:^(NSError *error, NSInteger state) {
                            if (error && error.code > 0) {
                                dispatch_semaphore_signal(acceptedWaitSemaphore);
                                return completionBlock(nil, error);
                            }

                            if(state != 4) {
                                [sdkApi connectionUpdateState: (int)connectionHandle withCompletion:^(NSError *error, NSInteger state) {
                                    if (error && error.code > 0) {
                                        dispatch_semaphore_signal(acceptedWaitSemaphore);
                                        return completionBlock(nil, error);
                                    }
                                    connectionState = state;
                                    dispatch_semaphore_signal(acceptedWaitSemaphore);
                                }];
                            } else {
                                connectionState = state;
                                dispatch_semaphore_signal(acceptedWaitSemaphore);
                            }
                        }];
                        
                        dispatch_semaphore_wait(acceptedWaitSemaphore, DISPATCH_TIME_FOREVER);
                        if (connectionState == 4) {
                            
                            [sdkApi connectionSerialize: (int)connectionHandle
                                             completion: ^(NSError *error, NSString *connectionSerialized) {
                                if (error && error.code > 0) {
                                    return completionBlock(nil, error);
                                }
                                [CMUtilities printSuccess: @[@"Connection invitation success", connectionSerialized]];

                                // Store the serialized connection
                                NSDictionary* invitation = [LocalStorage getObjectForKey: @"tempConnection" shouldCreate: true];

                                NSMutableDictionary* connections = [[LocalStorage getObjectForKey: @"connections" shouldCreate: true] mutableCopy];
                                NSDictionary* connectionObj = @{
                                    @"serializedConnection": connectionSerialized,
                                    @"invitation": invitation
                                };

                                [connections setValue: connectionObj forKey: [self connectionID: connectionObj]];
                                [LocalStorage store: @"connections" andObject: connections];
                                [LocalStorage deleteObjectForKey: @"tempConnection"];

                                return completionBlock(connectionObj, nil);
                            }];
                            break;
                        }
                    }
                });
        }];
    }];
}

+(void)removeConnection: (NSString*) connection withCompletionHandler: (ResponseBlock) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    if(!connection) {
        return;
    }

    [sdkApi connectionDeserialize: connection completion:^(NSError *error, NSInteger connectionHandle) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }

        [sdkApi connectionGetState: (int)connectionHandle withCompletion:^(NSError *error, NSInteger state) {
            if (error && error.code > 0) {
                return completionBlock(nil, nil);
            }
            [sdkApi deleteConnection: (int)connectionHandle withCompletion:^(NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
            }];
        }];
    }];
}

@end
