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
#import "ConnectionInvitation.h"

@implementation Connection

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

+(void)createConnection:(NSString *) invitation
                   name:(NSString*) name
  withCompletionHandler:(ResponseWithObject) completionBlock {
    NSString *type = [ConnectionInvitation getInvitationType:[Utilities jsonToDictionary:invitation]];
    
    if ([ConnectionInvitation isOutOfBandInvitation:type]) {
        [self connectWithOutofbandInvite:invitation
                   withCompletionHandler:^(NSDictionary *responseConnection, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }

            [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Connection connect", name]];
            return completionBlock(responseConnection, error);

        }];
    } else {
        [self connectWithInvite:invitation
          withCompletionHandler:^(NSDictionary *responseConnection, NSError *error) {
            [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Connection connect", name]];
            return completionBlock(responseConnection, error);
        }];
    }
}

+(void)connectWithInvite:(NSString *)invitation
withCompletionHandler: (ResponseWithObject) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    
    [sdkApi connectionCreateWithInvite:[ConnectionInvitation connectionID: [Utilities jsonToDictionary:invitation]]
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

        [Utilities printSuccess: @[@"connectionCreateWithInvite",  [NSNumber numberWithLong: connectionHandle]]];
        
        NSString *connectType = @"{\"connection_type\":\"QR\",\"phone\":\"\"}";

        [sdkApi connectionConnect: (int)connectionHandle
                   connectionType: connectType
                       completion: ^(NSError *error, NSString *inviteDetails) {

            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            [Utilities printSuccess: @[@"connectionConnect", inviteDetails]];
            
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
                    [Utilities printSuccess: @[@"v", successMessage]];
                    
                    // Store the serialized connection
                    NSMutableDictionary* connections = [[LocalStorage getObjectForKey: @"connections" shouldCreate: true] mutableCopy];
                    NSDictionary* connectionObj = @{
                        @"serializedConnection": successMessage,
                        @"invitation": invitation
                    };

                    [connections setValue: connectionObj forKey: [ConnectionInvitation connectionID: connectionObj]];
                    [LocalStorage store: @"connections" andObject: connections];
                    
                    return completionBlock(connectionObj, nil);
                }];
                
            }];
        }];
    }];
}

+(void)connectWithOutofbandInvite: (NSString*)invitation
            withCompletionHandler: (ResponseWithObject) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    
    [sdkApi connectionCreateWithOutofbandInvite: [ConnectionInvitation connectionID: [Utilities jsonToDictionary:invitation]]
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
        
        NSString *connectType = @"{\"connection_type\":\"QR\",\"phone\":\"\"}";
        
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
                    NSMutableDictionary* connections = [[LocalStorage getObjectForKey: @"connections" shouldCreate: true] mutableCopy];
                    
                    NSDictionary* connectionObj = @{
                        @"serializedConnection": successMessage,
                        @"invitation": invitation
                    };

                    [connections setValue: connectionObj forKey: [ConnectionInvitation connectionID: connectionObj]];
                    [LocalStorage store: @"connections" andObject: connections];

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
    NSString *pwDid = [ConnectionInvitation getPwDid:serializedConnection];
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
