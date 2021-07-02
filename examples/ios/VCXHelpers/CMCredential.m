//
//  CMCredential.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/11/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CMCredential.h"
#import "MobileSDK.h"
#import "CMMessage.h"
#import "CMConnection.h"

@implementation CMCredential

+(NSString *)getThid:(NSString *) credential {
    NSError *error;
    NSMutableDictionary *credValues = [NSJSONSerialization JSONObjectWithData: [credential dataUsingEncoding: NSUTF8StringEncoding] options: NSJSONReadingMutableContainers error: &error];
    NSLog(@"credValuescredValues %@", credValues);
    
    if (credValues[@"thread_id"]) {
        return credValues[@"thread_id"];
    }
    return credValues[@"~thread"][@"thid"];
}

+(void)acceptCredOfferWithMsgid: (NSDictionary*) messageObj
                  forConnection: (NSDictionary*) connection
          withCompletionHandler: (ResponseBlock) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    
    @try {
        NSLog(@"Received Cred Offer to process - %@", messageObj);
        NSString *messageId = messageObj[@"uid"];
        NSString* pw_did = [CMConnection getPwDid: connection[@"serializedConnection"]];
        
        [sdkApi connectionDeserialize: connection[@"serializedConnection"]
                           completion: ^(NSError *error, NSInteger connectionHandle) {
            if (error != nil && error.code != 0) {
                completionBlock(nil, error);
                return;
            }
            [sdkApi credentialCreateWithMsgid: messageId
                             connectionHandle: (int)connectionHandle
                                        msgId: messageId
                                   completion: ^(NSError *error, NSInteger credentialHandle, NSString* credentialOffer) {
                if (error != nil && error.code != 0) {
                    completionBlock(nil, error);
                    return;
                }
                
                [sdkApi credentialSendRequest: credentialHandle
                             connectionHandle: (int)connectionHandle
                                paymentHandle: 0
                                   completion: ^(NSError *error) {
                    if (error != nil && error.code != 0) {
                        completionBlock(nil, error);
                        return;
                    }
                    
                    [sdkApi updateMessages: @"MS-106"
                                pwdidsJson: [NSString stringWithFormat: @"[{\"pairwiseDID\":\"%@\",\"uids\":[\"%@\"]}]", pw_did, messageId]
                                completion: ^(NSError *error) {
                        if (error != nil && error.code !=0) {
                            NSLog(@"Error occured while updating message status - %@ :: %ld", error, (long)error.code);
                        }
                        NSLog(@"Updated messages for message: %@ and credentialHandle: %ld", messageId, (long)credentialHandle);
                    }];
                    
                    // start a loop and wait for actual credential to be issued
                    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                        while(true) {
                            dispatch_semaphore_t acceptedWaitSemaphore = dispatch_semaphore_create(0);
                            __block NSInteger credentialState = 0;
                            
                            [sdkApi credentialGetState:credentialHandle
                                            completion:^(NSError *error, NSInteger state) {
                                if (error && error.code > 0) {
                                    dispatch_semaphore_signal(acceptedWaitSemaphore);
                                    return completionBlock(nil, error);
                                }
                                
                                if (state != 4) {
                                    [sdkApi credentialUpdateState:credentialHandle
                                                       completion:^(NSError *error, NSInteger state) {
                                        if (error && error.code > 0) {
                                            dispatch_semaphore_signal(acceptedWaitSemaphore);
                                            return completionBlock(nil, error);
                                        }
                                        
                                        credentialState = state;
                                        dispatch_semaphore_signal(acceptedWaitSemaphore);
                                    }];
                                } else {
                                    credentialState = state;
                                    dispatch_semaphore_signal(acceptedWaitSemaphore);
                                }
                            }];
                            
                            dispatch_semaphore_wait(acceptedWaitSemaphore, DISPATCH_TIME_FOREVER);
                            if (credentialState == 4) {
                                NSLog(@"credential accepted");
                                break;
                            }
                        }
                    });
                }];
            }];
        }];
        
    } @catch (NSException *exception) {
        return completionBlock(nil, error);
    }
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
                return completionBlock([CMUtilities jsonToDictionary:state], nil);
            }];
        }];
    } @catch (NSException *exception) {
        return completionBlock(nil, error);
    }
}

+(void) acceptCredentialOffer: (NSString*) serializedConnection
         serializedCredential: (NSString*) serializedCredential
                        offer: (NSString*) offer
        withCompletionHandler: (ResponseWithObject) completionBlock {
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
                    
                    [self awaitCredentialReceived:handle
                                            offer:offer
                              withCompletionBlock:^(NSString *successMessage, NSError *error) {
                        if (error && error.code > 0) {
                            return completionBlock(nil, error);
                        }
                        return completionBlock([CMUtilities jsonToDictionary:successMessage], error);
                    }];
                }];
            }];
        }];
    }];
    } @catch (NSException *exception) {
        return completionBlock(nil, error);
    }
}

+(void) rejectCredentialOffer: (NSString*) serializedConnection
         serializedCredential: (NSString*) serializedCredential
        withCompletionHandler: (ResponseWithObject) completionBlock {
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
                                     completion:^(NSError *error, NSString *state) {
                        if (error && error.code > 0) {
                            return completionBlock(nil, error);
                        }
                        
                        return completionBlock([CMUtilities jsonToDictionary:state], nil);
                    }];
                }];
            }];
        }];
    } @catch (NSException *exception) {
        return completionBlock(nil, error);
    }
}

+(void)awaitCredentialReceived:(NSString *) serializedCredential
                         offer:(NSString *) offer
            withCompletionBlock:(ResponseBlock) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    NSString *CREDENTIAL = CMMessageType(Credential);
    NSString *thid = [self getThid:offer];
    NSLog(@"thid DMSG %@", thid);

    [sdkApi credentialDeserialize:serializedCredential
                       completion:^(NSError *error, NSInteger credentialHandle) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            while(true) {
                dispatch_semaphore_t acceptedWaitSemaphore = dispatch_semaphore_create(0);
                __block NSInteger credentialState = 0;
                __block NSString *serialized = @"";
                
                [CMMessage downloadMessage:CREDENTIAL
                                  soughtId:thid
                       withCompletionBlock:^(NSDictionary *responseObject, NSError *error) {

                    if (responseObject != nil) {
                        [sdkApi credentialUpdateStateWithMessage:(int)credentialHandle
                                                         message:[responseObject objectForKey:@"payload"]
                                                  withCompletion:^(NSError *error, NSInteger state) {
                            [CMMessage updateMessageStatus:[responseObject objectForKey:@"pwDid"]
                                                 messageId:[responseObject objectForKey:@"uid"]
                                       withCompletionBlock:^(BOOL result, NSError *error) {
                                if (state == 4) {
                                    [sdkApi credentialSerialize:credentialHandle
                                                     completion:^(NSError *error, NSString *serializedResult) {
                                        credentialState = state;
                                        serialized = serializedResult;
                                        dispatch_semaphore_signal(acceptedWaitSemaphore);
                                    }];
                                }
                            }];
                        }];
                    } else {
                        dispatch_semaphore_signal(acceptedWaitSemaphore);
                    }
                }];

                dispatch_semaphore_wait(acceptedWaitSemaphore, DISPATCH_TIME_FOREVER);
                if (credentialState == 4) {
                    return completionBlock(serialized, error);
                    break;
                }
            }
        });
    }];
}

@end
