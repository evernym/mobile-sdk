//
//  Connection.m
//  MSDKSampleAppObjC
//
//  Created by Predrag Jevtic on 5/28/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Connection.h"
#import "LocalStorage.h"
#import "MobileSDK.h"
#import "Credential.h"
#import "Message.h"
#import "ProofRequest.h"

@implementation Connection

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

+(NSString*)getConnectionByPwDid: (NSString *) pwDidMes {
    NSDictionary* connections = [[LocalStorage getObjectForKey: @"connections" shouldCreate: true] mutableCopy];
    NSString *resultConnection = @"";
    for (NSInteger i = 0; i < connections.allKeys.count; i++) {
        NSString *key = connections.allKeys[i];
        NSDictionary *connection = [connections objectForKey:key];
        NSString *serializedConnection = [connection objectForKey:@"serializedConnection"];
        NSString *pwDid = [Connection getPwDid:serializedConnection];
        if ([pwDidMes isEqual:pwDid]) {
            resultConnection = serializedConnection;
            break;
        }
    }
    return resultConnection;
}

+(NSDictionary*)parsedInvite: (NSString *)invite {
    NSLog(@"invite np parsed %@", invite);
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
    NSLog(@"readFromUrl %@ - ", invite);
    NSURL *url = [NSURL URLWithString:invite];
    NSData *data = [[NSData alloc] initWithContentsOfURL:url];
    if (url && data) {
        NSDictionary *result = [NSJSONSerialization JSONObjectWithData:data options:0 error:NULL];
        return result;
    } else {
        return [Utilities jsonToDictionary:invite];
    }
}

+(NSString*)readInviteFromUrl: (NSString*)invite {
    if(!invite) {
        return nil;
    }
    NSLog(@"readFromUrl %@ - ", invite);
    NSURL *url = [NSURL URLWithString:invite];
    NSData *data = [[NSData alloc] initWithContentsOfURL:url];
    if (url && data) {
        NSString *result = [Utilities dictToJsonString:[NSJSONSerialization JSONObjectWithData:data options:0 error:NULL]];
        return result;
    } else {
        return invite;
    }
}

+(NSDictionary*) extractRequestAttach: (NSDictionary*)invite {
    NSArray* requestAttach = [invite objectForKey: @"request~attach"];
    if (requestAttach.count != 0) {
        NSDictionary* requestAttachItem = requestAttach[0];
        NSDictionary* requestAttachData = [requestAttachItem objectForKey: @"data"];
        NSString* requestAttachBase64 = [requestAttachData objectForKey: @"base64"];

        NSData* invitationData = [Utilities decode64String: requestAttachBase64];
        NSString* json = [[NSString alloc] initWithData: invitationData encoding: NSUTF8StringEncoding];
        NSLog(@" JSON %@", json);
        return [Utilities jsonToDictionary: json];
    } else {
        return nil;
    }
}

+(NSDictionary*) parseInvitationLink: (NSString*) link {
    NSArray* linkComponents = [link componentsSeparatedByString: @"msg?c_i="];

    if([linkComponents count] < 2) {
        return nil;
    }

    NSData* invitationData = [Utilities decode64String: linkComponents[1]];
    NSString*  json = [[NSString alloc] initWithData: invitationData encoding: NSUTF8StringEncoding];

    return [Utilities jsonToDictionary: json];
}

+(NSString*)connectionID: connectValues {
    NSString* connectionID = [connectValues objectForKey: @"id"];

    if(!connectionID) {
        connectionID =  [connectValues objectForKey: @"@id"];
    }

    if(!connectionID) {
        NSDictionary* connectionData = [Utilities jsonToDictionary: connectValues[@"serializedConnection"]];
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

+(void)connectionRedirectAriesOutOfBand: (NSString*)invitation
                   serializedConnection: (NSString*)serializedConnection
                  withCompletionHandler: (ResponseWithBoolean) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    
    @try {
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
                
                [Message waitHandshakeReuse:^(BOOL result, NSError *error) {
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

+(void)verityConnectionExist:(NSString *)invite
               withCompletion:(ResponseBlock) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    NSDictionary* newInviteDict = [Utilities jsonToDictionary:invite];
    NSString* newPublicDid = [newInviteDict objectForKey:@"public_did"];
    
    NSDictionary* connections = [[LocalStorage getObjectForKey: @"connections" shouldCreate: true] mutableCopy];
    if (connections.allKeys.count != 0) {
        for (NSInteger i = 0; i < connections.allKeys.count; i++) {
            NSString* key = connections.allKeys[i];
            NSDictionary* connection = [connections objectForKey:key];
            NSString* serializedConnection = [connection objectForKey:@"serializedConnection"];

            [sdkApi connectionDeserialize:serializedConnection
                               completion:^(NSError *error, NSInteger connectionHandle) {
                [sdkApi getConnectionInviteDetails:connectionHandle
                                       abbreviated:0
                                    withCompletion:^(NSError *error, NSString *inviteDetails) {
                    NSDictionary* oldInviteDict = [Utilities jsonToDictionary:inviteDetails];
                    NSString* oldPublicDid = [oldInviteDict objectForKey:@"public_did"];

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

+(void)handleAttach: (NSDictionary *) requestAttach
     connectionData: (NSString *) serializedConnection
               name: (NSString*) name
withCompletionHandler: (ResponseWithObject) completionBlock {
    NSString *type = [requestAttach objectForKey: @"@type"];
    NSLog(@"requestAttachrequestAttach %@", requestAttach);
    NSLog(@"requestAttachrequestAttach %@", serializedConnection);

    if ([type rangeOfString:@"credential"].location != NSNotFound) {
        [Credential createWithOffer:[Utilities dictToJsonString:requestAttach]
                withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
            if (error && error.code > 0) {
                NSLog(@"Error createWithOffer %@", error);
                return completionBlock(nil, error);
            }
            NSLog(@"Created offer %@", responseObject);
            NSLog(@"Created offer serializedConnection %@", serializedConnection);

            [Credential acceptCredentialOffer:serializedConnection
                           serializedCredential:[Utilities dictToJsonString:responseObject]
                                          offer:[Utilities dictToJsonString:requestAttach]
                          withCompletionHandler:^(NSString *responseObject, NSError *error) {
                if (error && error.code > 0) {
                    NSLog(@"Error acceptCredentialOffer %@", error);
                    return completionBlock(nil, error);
                }
                NSLog(@"Created offer acceptCredentialOffer %@", responseObject);
                [Credential awaitCredentialReceived:responseObject
                                        offer:[Utilities dictToJsonString:requestAttach]
                          withCompletionBlock:^(NSString *successMessage, NSError *error) {
                    if (error && error.code > 0) {
                        return completionBlock(nil, error);
                    }
                    [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Credential offer accept", name]];
                    NSLog(@"Credential Offer Accepted %@", error);
                    [LocalStorage deleteObjectForKey:@"request~attach"];

                    return completionBlock([Utilities jsonToDictionary:successMessage], nil);
                }];
                
            }];
        }];
    } else if ([type rangeOfString:@"present-proof"].location != NSNotFound){
        NSLog(@"requestAttachrequestAttach present");

        [ProofRequest createWithRequest:[Utilities dictToJsonString:requestAttach]
                    withCompletionHandler:^(NSDictionary *offer, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            NSLog(@"Proof Request created %@", error);

            [ProofRequest retrieveAvailableCredentials:[Utilities dictToJsonString:offer]
                                   withCompletionHandler:^(NSDictionary *creds, NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                };

                NSLog(@"Proof Request retrieved %@", creds);
                NSString *attr = [creds objectForKey: @"autofilledAttributes"];

                [ProofRequest send:serializedConnection
                     serializedProof:[Utilities dictToJsonString:offer]
                       selectedCreds:attr
                    selfAttestedAttr:@"{}"
               withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                    if (error && error.code > 0) {
                        return completionBlock(nil, error);
                    }
                    [LocalStorage deleteObjectForKey:@"request~attach"];
                    [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Proof request send", name]];
                    NSLog(@"Proof Request send %@", error);
                    return completionBlock(responseObject, nil);
                }];
            }];
        }];
    }
}

+(void)handleConnection: (NSString *)invite
         connectionType: (int)connectionType
            phoneNumber: (NSString*) phone
  withCompletionHandler:(ResponseWithObject) completionBlock {
    NSDictionary* inviteDict = [self parsedInvite:invite];
    NSString* name = [inviteDict objectForKey:@"label"];
    
    [self verityConnectionExist:invite
                 withCompletion:^(NSString *response, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        if (response != nil) {
            [self connectionRedirectAriesOutOfBand:invite
                              serializedConnection:response
                             withCompletionHandler:^(BOOL result, NSError *error) {
                [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Connection redirect", name]];
                NSDictionary *requestAttach = [self extractRequestAttach: [Utilities jsonToDictionary:invite]];

                if (requestAttach) {
                    [self handleAttach:requestAttach
                        connectionData:response
                            name:name
                 withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                        return completionBlock(responseObject, error);
                    }];
                }
                return completionBlock(nil, error);
            }];
        } else {
            [self createConnection:inviteDict
                    connectionType:connectionType
                       phoneNumber:phone
                              name:name
             withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                return completionBlock(responseObject, nil);
            }];
        }
    }];
}

+(void)createConnection: (NSDictionary *) inviteDict
         connectionType: (int)connectionType
            phoneNumber: (NSString*) phone
                   name: (NSString*) name
  withCompletionHandler: (ResponseWithObject) completionBlock {
    [self connectWithOutofbandInvite:[Utilities dictToJsonString:inviteDict]
                             connectionType:(int)connectionType
                                phoneNumber:phone
                      withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }

        [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Connection connect", name]];
        NSString* serializedConnection = [responseObject objectForKey: @"serializedConnection"];

        NSDictionary* requestAttach = [LocalStorage getObjectForKey: @"request~attach" shouldCreate:false];
        if (requestAttach) {
            [self handleAttach:requestAttach
                connectionData:serializedConnection
                          name:name
         withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                return completionBlock(responseObject, error);
            }];
        }
        return completionBlock(nil, error);
    }];
}

+(void)connectWithOutofbandInvite: (NSString*)invitation
connectionType: (int)connectionType
   phoneNumber: (NSString*) phone
withCompletionHandler: (ResponseWithObject) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];

    NSDictionary *requestAttach = [self extractRequestAttach: [Utilities jsonToDictionary:invitation]];
    [LocalStorage store: @"request~attach"
              andObject: requestAttach];

    [LocalStorage store: @"tempConnection" andObject: [Utilities jsonToDictionary:invitation]];
    
    [sdkApi connectionCreateWithOutofbandInvite: [self connectionID: [Utilities jsonToDictionary:invitation]]
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
        
        NSString *connectType = [NSString stringWithFormat:@"{\"connection_type\":\"%@\",\"phone\":%@\"\"}",
                                    [Connection connectionByType: connectionType], phone];
        [sdkApi connectionConnect: (int)connectionHandle
                   connectionType: connectType
                       completion: ^(NSError *error, NSString *inviteDetails) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            [sdkApi connectionSerialize: (int)connectionHandle
                             completion: ^(NSError *error, NSString *connectionSerialized) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                [Utilities printSuccess: @[@"Connection invitation success", connectionSerialized]];
                
                [self awaitConnectionCompleted:connectionSerialized
                           withCompletionBlock:^(NSString *successMessage, NSError *error) {
                    if (error && error.code > 0) {
                        return completionBlock(nil, error);
                    }
                    NSLog(@"Connection invitation success %@", successMessage);

                    // Store the serialized connection
                    NSDictionary* invitation = [LocalStorage getObjectForKey: @"tempConnection" shouldCreate: true];

                    NSMutableDictionary* connections = [[LocalStorage getObjectForKey: @"connections" shouldCreate: true] mutableCopy];
                    
                    NSDictionary* connectionObj = @{
                        @"serializedConnection": successMessage,
                        @"invitation": invitation
                    };

                    [connections setValue: connectionObj forKey: [self connectionID: connectionObj]];
                    [LocalStorage store: @"connections" andObject: connections];
                    [LocalStorage deleteObjectForKey: @"tempConnection"];

                    return completionBlock(connectionObj, nil);
                }];
                
            }];
        }];
    }];
}

+(void)updateConnectionStatus:(NSInteger) connectionHandle
                        pwDid:(NSString *) pwDid
          withCompletionBlock:(ResponseBlock) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    NSString *CONNECTION_RESPONSE = MessageType(ConnectionResponseType);

    [Message downloadMessage:CONNECTION_RESPONSE
                      soughtId:pwDid
           withCompletionBlock:^(NSDictionary *responseObject, NSError *error) {
        if (responseObject != nil) {
            [sdkApi connectionUpdateStateWithMessage:(int)connectionHandle
                                             message:[responseObject objectForKey:@"payload"]
                                      withCompletion:^(NSError *error, NSInteger state) {
                [Message updateMessageStatus:[responseObject objectForKey:@"pwDid"]
                                     messageId:[responseObject objectForKey:@"uid"]
                           withCompletionBlock:^(BOOL result, NSError *error) {
                    if (state == 4) {
                        [sdkApi connectionSerialize:connectionHandle
                                         completion:^(NSError *error, NSString *serializedResult) {
                            return completionBlock(serializedResult, error);
                        }];
                    } else {
                        return completionBlock(nil, error);
                    }
                }];
            }];
        } else {
            return completionBlock(nil, error);
        }
    }];
}

+(void)awaitConnectionCompleted:(NSString *) serializedConnection
            withCompletionBlock:(ResponseBlock) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    NSString *pwDid = [self getPwDid:serializedConnection];
    __block NSString *serialized = @"";
    
    [sdkApi connectionDeserialize: serializedConnection
                       completion:^(NSError *error, NSInteger connectionHandle) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        
        __block BOOL COMPLETE = NO;

        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            while (1) {
                dispatch_semaphore_t acceptedWaitSemaphore = dispatch_semaphore_create(0);
                [self updateConnectionStatus:connectionHandle
                                       pwDid:pwDid
                         withCompletionBlock:^(NSString *successMessage, NSError *error) {
                    if (successMessage) {
                        COMPLETE = YES;
                        serialized = successMessage;
                        completionBlock(successMessage, error);
                    }
                    dispatch_semaphore_signal(acceptedWaitSemaphore);
                }];
                dispatch_semaphore_wait(acceptedWaitSemaphore, DISPATCH_TIME_FOREVER);
                if (COMPLETE) break;
            }
        });
    }];
}


@end
