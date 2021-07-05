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
#import "LocalStorage.h"

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

+(void)acceptCredentilaFromMessage:(NSString *) data
               withCompletionBlock:(ResponseWithBoolean) completionBlock {
    NSDictionary *message = [CMUtilities jsonToDictionary:data];
    NSString *pwDidMes = [message objectForKey:@"pwDid"];
    NSString *payload = [message objectForKey:@"payload"];
    NSArray *payloadArr = [CMUtilities jsonToArray:payload];
    NSString *offerConnection = [CMConnection getConnectionByPwDid:pwDidMes];
    NSDictionary *payloadDict = payloadArr[0];
    
    [CMCredential createWithOffer:payload
            withCompletionHandler:^(NSDictionary *serOffer, NSError *error) {
        if (error && error.code > 0) {
            NSLog(@"offer error %@", error);
            return completionBlock(nil, error);
        }
        NSLog(@"offer created %@", serOffer);
        [CMCredential acceptCredentialOffer:offerConnection
                       serializedCredential:[CMUtilities dictToJsonString:serOffer]
                                      offer:[CMUtilities dictToJsonString:payloadDict]
                      withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
            if (error && error.code > 0) {
                NSLog(@"offer accept error %@", error);

                return completionBlock(nil, error);
            }
            [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Credential offer accept", [payloadDict objectForKey:@"claim_name"]]];
            NSLog(@"Accept credential offer success");
            return completionBlock(YES, error);
        }];
    }];
}

+(void)rejectCredentilaFromMessage:(NSString *) data
               withCompletionBlock:(ResponseWithBoolean) completionBlock {
    NSDictionary *message = [CMUtilities jsonToDictionary:data];
    NSString *pwDidMes = [message objectForKey:@"pwDid"];
    NSString *payload = [message objectForKey:@"payload"];
    NSArray *payloadArr = [CMUtilities jsonToArray:payload];
    NSString *offerConnection = [CMConnection getConnectionByPwDid:pwDidMes];
    NSDictionary *payloadDict = payloadArr[0];
    
    [self createWithOffer:payload
            withCompletionHandler:^(NSDictionary *serOffer, NSError *error) {
        if (error && error.code > 0) {
            NSLog(@"offer error %@", error);
            return completionBlock(nil, error);
        }
        NSLog(@"offer created %@", serOffer);
        [self rejectCredentialOffer:offerConnection
               serializedCredential:[CMUtilities dictToJsonString:serOffer]
              withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
            if (error && error.code > 0) {
                NSLog(@"offer accept error %@", error);

                return completionBlock(nil, error);
            }
            [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Credential offer reject", [payloadDict objectForKey:@"claim_name"]]];
            NSLog(@"Reject credential offer success");
            return completionBlock(YES, error);
        }];
    }];
}

@end
