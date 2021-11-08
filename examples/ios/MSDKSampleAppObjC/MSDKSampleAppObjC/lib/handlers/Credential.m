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

+(void) handleCredentialOffer:(NSDictionary *) attachment
         serializedConnection:(NSString *) serializedConnection
                         name:(NSString *) name
        withCompletionHandler:(ResponseWithObject) completionBlock {
    [self createWithOffer:[Utilities dictToJsonString:attachment]
            withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }

        [self acceptCredentialOffer:serializedConnection
                       serializedCredential:[Utilities dictToJsonString:responseObject]
                                      offer:[Utilities dictToJsonString:attachment]
                      withCompletionHandler:^(NSString *responseObject, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            [self awaitCredentialReceived:responseObject
                                    offer:[Utilities dictToJsonString:attachment]
                      withCompletionBlock:^(NSString *successMessage, NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Credential offer accept", name]];

                return completionBlock([Utilities jsonToDictionary:successMessage], nil);
            }];
            
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
        NSLog(@"Created offer connectionDeserialize %d");
        [sdkApi credentialDeserialize:serializedCredential
                           completion:^(NSError *error, NSInteger credentialHandle) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            NSLog(@"Created offer credentialDeserialize %d");
            [sdkApi credentialSendRequest:credentialHandle
                         connectionHandle:(int)connectionHandle
                            paymentHandle:0
                               completion:^(NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                NSLog(@"Created offer credentialSendRequest %@");
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
                        
                        return completionBlock([Utilities jsonToDictionary:state], nil);
                    }];
                }];
            }];
        }];
    } @catch (NSException *exception) {
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
            withCompletionBlock:(ResponseBlock) completionBlock {
    ConnectMeVcx *sdkApi = [[MobileSDK shared] sdkApi];
    NSString *thid = [CredentialOffer getThid:offer];
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

+(void)acceptCredentilaFromMessage:(NSString *) data
               withCompletionBlock:(ResponseWithBoolean) completionBlock {
    NSDictionary *message = [Utilities jsonToDictionary:data];
    NSString *pwDidMes = [message objectForKey:@"pwDid"];
    NSString *payload = [message objectForKey:@"payload"];
    NSArray *payloadArr = [Utilities jsonToArray:payload];
    NSString *offerConnection = [ConnectionInvitation getConnectionByPwDid:pwDidMes];
    NSDictionary *payloadDict = payloadArr[0];
    
    [Credential createWithOffer:payload
            withCompletionHandler:^(NSDictionary *serOffer, NSError *error) {
        if (error && error.code > 0) {
            NSLog(@"offer error %@", error);
            return completionBlock(nil, error);
        }
        NSLog(@"offer created %@", serOffer);
        [Credential acceptCredentialOffer:offerConnection
                       serializedCredential:[Utilities dictToJsonString:serOffer]
                                      offer:[Utilities dictToJsonString:payloadDict]
                      withCompletionHandler:^(NSString *responseObject, NSError *error) {
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
    NSDictionary *message = [Utilities jsonToDictionary:data];
    NSString *pwDidMes = [message objectForKey:@"pwDid"];
    NSString *payload = [message objectForKey:@"payload"];
    NSArray *payloadArr = [Utilities jsonToArray:payload];
    NSString *offerConnection = [ConnectionInvitation getConnectionByPwDid:pwDidMes];
    NSDictionary *payloadDict = payloadArr[0];
    
    [self createWithOffer:payload
            withCompletionHandler:^(NSDictionary *serOffer, NSError *error) {
        if (error && error.code > 0) {
            NSLog(@"offer error %@", error);
            return completionBlock(nil, error);
        }
        NSLog(@"offer created %@", serOffer);
        [self rejectCredentialOffer:offerConnection
               serializedCredential:[Utilities dictToJsonString:serOffer]
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
