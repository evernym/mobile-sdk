//
//  Credential.m
//  MSDKSampleAppObjC
//
//  Created by Predrag Jevtic on 6/11/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Credential.h"
#import "MobileSDK.h"
#import "Message.h"
#import "Connection.h"
#import "LocalStorage.h"
#import "CredentialOffer.h"
#import "ConnectionInvitation.h"

@implementation Credential

+(void) acceptCredential:(NSString *) attachment
    serializedCredential:(NSString *) serializedCredential
    serializedConnection:(NSString *) serializedConnection
             fromMessage:(BOOL) fromMessage
   withCompletionHandler:(ResponseBlock) completionBlock {
    [self acceptCredentialOffer:serializedConnection
                   serializedCredential:serializedCredential
                                  offer:attachment
                  withCompletionHandler:^(NSString *responseObject, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        [self awaitCredentialReceived:responseObject
                                offer:attachment
                          fromMessage:fromMessage
                  withCompletionBlock:^(NSString *successMessage, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }

            return completionBlock(successMessage, nil);
        }];
    }];
}

+(void) createWithOffer: (NSString*)offer
  withCompletionHandler: (ResponseWithObject) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];

    @try {
        NSString *uuid = [[NSUUID UUID] UUIDString];
        [sdkApi credentialCreateWithOffer:uuid
                                    offer:offer
                               completion:^(NSError *error, NSInteger credentailHandle) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }

            [sdkApi credentialSerialize:credentailHandle
                             completion:^(NSError *error, NSString *state) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                return completionBlock([Utilities jsonToDictionary:state], nil);
            }];
        }];
    } @catch (NSException *exception) {
        return completionBlock(nil, error);
    }
}

+(void) acceptCredentialOffer: (NSString*) serializedConnection
         serializedCredential: (NSString*) serializedCredential
                        offer: (NSString*) offer
        withCompletionHandler: (ResponseBlock) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];

    @try {
    [sdkApi connectionDeserialize:serializedConnection
                       completion:^(NSError *error, NSInteger connectionHandle) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }

        [sdkApi credentialDeserialize:serializedCredential
                           completion:^(NSError *error, NSInteger credentialHandle) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }

            [sdkApi credentialSendRequest:credentialHandle
                         connectionHandle:(int)connectionHandle
                            paymentHandle:0
                               completion:^(NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }

                [sdkApi credentialSerialize:credentialHandle
                                 completion:^(NSError *error, NSString *handle) {
                    if (error && error.code > 0) {
                        return completionBlock(nil, error);
                    }
                    return completionBlock(handle, error);
                }];
            }];
        }];
    }];
    } @catch (NSException *exception) {
        NSLog(@"exception %@", exception.name);
        return completionBlock(nil, error);
    }
}

+(void)updateCredentialStatus:(NSInteger) credentialHandle
                         thid:(NSString *) thid
          withCompletionBlock:(ResponseBlock) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    NSString *CREDENTIAL = MessageType(CredentialType);
    NSLog(@"CDMSG updateCredentialStatus");

    [Message downloadMessage:CREDENTIAL
                      soughtId:thid
           withCompletionBlock:^(NSDictionary *responseObject, NSError *error) {
        NSLog(@"downloadMessage CDMSG responseObject %@", responseObject);

        if (responseObject != nil) {
            [sdkApi credentialUpdateStateWithMessage:(int)credentialHandle
                                             message:[responseObject objectForKey:@"payload"]
                                      withCompletion:^(NSError *error, NSInteger state) {

                [Message updateMessageStatus:[responseObject objectForKey:@"pwDid"]
                                     messageId:[responseObject objectForKey:@"uid"]
                           withCompletionBlock:^(BOOL result, NSError *error) {
                    NSLog(@"state CDMSG result");

                    if (state == 4) {
                        [sdkApi credentialSerialize:credentialHandle
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

+(void)awaitCredentialReceived:(NSString *) serializedCredential
                         offer:(NSString *) offer
                   fromMessage:(BOOL) fromMessage
           withCompletionBlock:(ResponseBlock) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    NSDictionary *offerObj = [Utilities jsonToDictionary:offer];

    NSString *thid = [CredentialOffer getThid:offerObj];
    __block NSString *serialized = @"";

    [sdkApi credentialDeserialize:serializedCredential
                       completion:^(NSError *error, NSInteger credentialHandle) {
        NSLog(@"credentialDeserializecredentialDeserialize");
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }

        __block BOOL COMPLETE = NO;

        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            while (1) {
                dispatch_semaphore_t acceptedWaitSemaphore = dispatch_semaphore_create(0);
                [self updateCredentialStatus:credentialHandle
                                        thid:thid
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

+(void) rejectCredentialOffer:(NSString*) serializedConnection
         serializedCredential:(NSString*) serializedCredential
        withCompletionHandler:(ResponseBlock) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];

    @try {
        [sdkApi connectionDeserialize:serializedConnection
                           completion:^(NSError *error, NSInteger connectionHandle) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }

            [sdkApi credentialDeserialize:serializedCredential completion:^(NSError *error, NSInteger credentialHandle) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }

                [sdkApi credentialReject:credentialHandle
                        connectionHandle:(int)connectionHandle
                                 comment:@""
                              completion:^(NSError *error) {
                    if (error && error.code > 0) {
                        return completionBlock(nil, error);
                    }

                    [sdkApi credentialSerialize:credentialHandle
                                     completion:^(NSError *error, NSString *credentialSerialized) {
                        if (error && error.code > 0) {
                            return completionBlock(nil, error);
                        }

                        return completionBlock(credentialSerialized, nil);
                    }];
                }];
            }];
        }];
    } @catch (NSException *exception) {
        return completionBlock(nil, error);
    }
}

@end
