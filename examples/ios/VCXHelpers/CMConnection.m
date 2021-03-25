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

+(void)connect: (NSString*)invitation
connectionType: (int)connectionType
   phoneNumber: (NSString*) phone
withCompletionHandler: (ResponseWithObject) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];

    NSDictionary *connectValues = [CMUtilities jsonToDictionary: invitation];

    if([[connectValues allKeys] count] < 1) {
        connectValues = [CMConnection parseInvitationLink: invitation];
        invitation = [CMUtilities toJsonString: connectValues];
    }

    if([connectValues count] < 1) {
        NSError* error = [NSError errorWithDomain: @"connections" code: 400 userInfo: @{
            NSLocalizedDescriptionKey: @"Invalid Connection JSON"
        }];

        return completionBlock(nil, error);
    }

    [LocalStorage store: @"tempConnection" andObject: connectValues];

    [sdkApi connectionCreateWithInvite: [self connectionID: connectValues]
                         inviteDetails: invitation
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

        [CMUtilities printSuccess: @[@"createConnectionWithInvite",  [NSNumber numberWithLong: connectionHandle]]];
        
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
                            
                            [sdkApi connectionSerialize: (int)connectionHandle completion: ^(NSError *error, NSString *connectionSerialized) {
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
