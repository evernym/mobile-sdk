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

@implementation CMCredential

+ (void)acceptCredOffer: (NSDictionary*) messageObj forConnection: (NSDictionary*) connection withCompletionHandler: (ResponseBlock) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    
    @try {
        NSLog(@"Received Cred Offer to process - %@", messageObj);
        NSString *messageId = messageObj[@"uid"];
        NSString *pw_did = connection[@"data"][@"pw_did"];
        
        [sdkApi connectionDeserialize: connection[@"serializedConnection"] completion:^(NSError *error, NSInteger connectionHandle) {
            if (error != nil && error.code != 0) {
                completionBlock(nil, error);
                return;
            }
            [sdkApi credentialCreateWithMsgid: messageId connectionHandle: (int)connectionHandle msgId: messageId completion: ^(NSError *error, NSInteger credentialHandle, NSString* credentialOffer) {
                if (error != nil && error.code != 0) {
                    completionBlock(nil, error);
                    return;
                }
                
                [sdkApi credentialSerialize: credentialHandle completion: ^(NSError *error, NSString *claimOffer) {
                    if (error != nil && error.code != 0) {
                        completionBlock(nil, error);
                    }
                    [sdkApi credentialGetState:credentialHandle completion:^(NSError *error, NSInteger state) {
                        if (error != nil && error.code != 0) {
                            completionBlock(nil, error);
                            return;
                        }
                        
                        const char *cLetter = (const char *)[claimOffer cStringUsingEncoding: NSUTF8StringEncoding];
                        NSString *serializedCredential_cString = [[NSString alloc] initWithCString: cLetter encoding: NSUTF8StringEncoding];
                        [sdkApi credentialDeserialize: serializedCredential_cString completion: ^(NSError *error, NSInteger credentailHandle) {
                            if (error != nil && error.code != 0) {
                                completionBlock(nil, error);
                                return;
                            }
                            
                            [sdkApi credentialSendRequest: credentialHandle connectionHandle: (int)connectionHandle paymentHandle: 0 completion: ^(NSError *error) {
                                if (error != nil && error.code != 0){
                                    completionBlock(nil, error);
                                    return;
                                }
                                NSLog(@"Successfully sent the credential request for credentialHandle: %ld AND for connectionHandle: %id", (long)credentailHandle, (int)connectionHandle);
                                
                                [sdkApi updateMessages: @"MS-104" pwdidsJson: [NSString stringWithFormat: @"[{\"pairwiseDID\":\"%@\",\"uids\":[\"%@\"]}]", pw_did, messageId] completion: ^(NSError *error) {
                                    if (error != nil && error.code !=0) {
                                        NSLog(@"Error occured while updating message status - %@ :: %ld", error, (long)error.code);
                                    }
                                    NSLog(@"Updated messages for message: %@ and credentialHandle: %ld", messageId, (long)credentialHandle);
                                    NSLog(@"Credential offer accepted successfuly!");
                                    completionBlock(serializedCredential_cString, nil);
                                }];
                            }];
                        }];
                    }];
                }];
            }];
        }];
        
    } @catch (NSException *exception) {
        return completionBlock(nil, error);
    }
}

@end