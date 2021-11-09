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

NSString *CONNECTION_PENDING_STATUS = @"pending";
NSString *CONNECTION_COMPLETED_STATUS = @"completed";

NSString *CONNECT_TYPE = @"{\"connection_type\":\"QR\",\"phone\":\"\"}";

+(void)connectionRedirectAriesOutOfBand: (NSString*)invitation
                   serializedConnection: (NSString*)serializedConnection
                  withCompletionHandler: (ResponseWithBoolean) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    NSDictionary *inviteDict = [Utilities jsonToDictionary:invitation];
    NSArray* handshakeProtocols = [inviteDict objectForKey: @"handshake_protocols"];
    NSString* handshakeProtocolsValue = handshakeProtocols[0];

    if ([handshakeProtocolsValue  isEqual: @""] || handshakeProtocolsValue == nil) {
        return completionBlock(false, error);
    }
    
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
}

+(void)verityConnectionExist:(NSString *) invite
       serializedConnections:(NSArray *) serializedConnections
              withCompletion:(ResponseBlock) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    if (serializedConnections.count != 0) {
        for (NSInteger i = 0; i < serializedConnections.count; i++) {
            NSString *connection = serializedConnections[i];
            [sdkApi connectionDeserialize:connection
                               completion:^(NSError *error, NSInteger connectionHandle) {
                [sdkApi getConnectionInviteDetails:connectionHandle
                                       abbreviated:0
                                    withCompletion:^(NSError *error, NSString *inviteDetails) {
                    
                    if ([ConnectionInvitation compareInvites:invite
                                                storedInvite:inviteDetails]) {
                        return completionBlock(connection, nil);
                    }
                    
                    if (i == serializedConnections.count - 1) {
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
  withCompletionHandler:(ResponseBlock) completionBlock {
    NSString *type = [ConnectionInvitation getInvitationType:[Utilities jsonToDictionary:invitation]];
    
    if ([ConnectionInvitation isOutOfBandInvitation:type]) {
        [self connectWithOutofbandInvite:invitation
                   withCompletionHandler:^(NSString *responseConnection, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }

            [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Connection connect", name]];
            return completionBlock(responseConnection, error);

        }];
    } else {
        [self connectWithInvite:invitation
          withCompletionHandler:^(NSString *responseConnection, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            
            [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Connection connect", name]];
            return completionBlock(responseConnection, error);
        }];
    }
}

+(void)connectWithInvite:(NSString *)invitation
withCompletionHandler: (ResponseBlock) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    
    [sdkApi connectionCreateWithInvite:[ConnectionInvitation connectionID: [Utilities jsonToDictionary:invitation]]
                         inviteDetails:invitation
                            completion:^(NSError *error, NSInteger connectionHandle) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }

        [self handleConnectionWithInvite:connectionHandle
                              invitation:invitation
                     withCompletionBlock:^(NSString *successMessage, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            
            return completionBlock(successMessage, error);
        }];
    }];
}

+(void)connectWithOutofbandInvite: (NSString*)invitation
            withCompletionHandler: (ResponseBlock) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    
    [sdkApi connectionCreateWithOutofbandInvite: [ConnectionInvitation connectionID: [Utilities jsonToDictionary:invitation]]
                                         invite: invitation
                                     completion: ^(NSError *error, NSInteger connectionHandle) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        
        [self handleConnectionWithInvite:connectionHandle
                              invitation:invitation
                     withCompletionBlock:^(NSString *successMessage, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            
            return completionBlock(successMessage, error);

        }];
    }];
}

+(void)handleConnectionWithInvite:(NSInteger) connectionHandle
                       invitation:(NSString*) invitation
              withCompletionBlock:(ResponseBlock) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];

    [sdkApi connectionConnect: (int)connectionHandle
               connectionType: CONNECT_TYPE
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
                       withCompletionBlock:^(NSString *successConnection, NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                NSLog(@"Connection invitation success %@", successConnection);

                // Store the serialized connection
                NSMutableDictionary* connections = [[LocalStorage getObjectForKey: @"connections" shouldCreate: true] mutableCopy];
                
                NSString *pwDid = [ConnectionInvitation getPwDid:successConnection];
                NSString *name = [[Utilities jsonToDictionary: invitation] objectForKey: @"label"];
                NSString *profileUrl = [[Utilities jsonToDictionary: invitation] objectForKey: @"profileUrl"];
                
                NSTimeInterval timeStamp = [[NSDate date] timeIntervalSinceNow];
                NSString *timestamp = [NSString stringWithFormat:@"%@", [NSNumber numberWithDouble: timeStamp]];
                NSString *uuid = [[NSUUID UUID] UUIDString];

                NSDictionary* connectionObj = @{
                    @"pwDid": pwDid,
                    @"serialized": successConnection,
                    
                    @"name": name,
                    @"profileUrl": profileUrl,
                    @"timestamp": timestamp,
                    
                    @"status": CONNECTION_COMPLETED_STATUS,
                    
                    @"invitation": invitation
                };

                [connections setValue: connectionObj forKey: uuid];
                [LocalStorage store: @"connections" andObject: connections];

                return completionBlock(successConnection, nil);
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
