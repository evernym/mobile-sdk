//
//  Message.m
//  MSDKSampleAppObjC
//
//  Created by Predrag Jevtic on 6/11/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import "Message.h"
#import "Connection.h"
#import "MobileSDK.h"

@implementation Message

+ (void) downloadAllMessages:(ResponseWithArray) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    NSString* messageType = MessageStatusTypeValue(Received);

    @try {
        [sdkApi downloadMessages:messageType
                           uid_s:nil
                          pwdids:nil
                      completion:^(NSError *error, NSString *messages) {
            NSLog(@"downloadMessages123 %@", messages);
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            };

            NSMutableArray* msgList = [@[] mutableCopy];
            NSArray* messagesArray = [Utilities jsonToArray: messages];

            for (NSInteger i = 0; i < messagesArray.count; i++) {
                NSDictionary *message = messagesArray[i];

                NSArray *msgs = [message objectForKey:@"msgs"];
                NSString *pwDid = [message objectForKey:@"pairwiseDID"];
                for (NSInteger j = 0; j < msgs.count; j++) {
                    NSDictionary *msg = msgs[j];
                    NSMutableDictionary *msgDict = [@{} mutableCopy];

                    NSDictionary *payload = [Utilities jsonToDictionary:[msg objectForKey:@"decryptedPayload"]];

                    NSDictionary *typeObj = [payload objectForKey:@"@type"];
                    NSString *type = [typeObj objectForKey:@"name"];

                    NSString *uid = [msg objectForKey:@"uid"];
                    NSString *ms = [payload objectForKey:@"@msg"];
                    NSString *status = [msg objectForKey:@"statusCode"];

                    [msgDict setValue:pwDid forKey:@"pwDid"];
                    [msgDict setValue:type forKey:@"type"];
                    [msgDict setValue:uid forKey:@"uid"];
                    [msgDict setValue:ms forKey:@"payload"];
                    [msgDict setValue:status forKey:@"status"];
                    [msgList addObject:msgDict];
                }
            };
            return completionBlock(msgList, nil);
        }];
    } @catch (NSException *exception) {
        return completionBlock(nil, error);
    }
}

// TODO: move to connections file
+ (void)waitHandshakeReuse: (ResponseWithBoolean) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];

    @try {
        NSString* messageType = MessageStatusTypeValue(Received);
        [sdkApi downloadMessages: messageType
                           uid_s: nil
                          pwdids: nil
                      completion: ^(NSError *error, NSString *messages) {
            NSMutableArray* msgList = [@[] mutableCopy];
            NSLog(@"messages %@", messages);
            if (messages) {
                NSArray* msgArray = [Utilities jsonToArray: messages];
                if(msgArray && msgArray.count > 0) {
                    msgList = msgArray[0][@"msgs"];
                    for (int i = 0; i < msgList.count; i++) {
                        NSDictionary* message = msgList[i];
                        NSDictionary* payload = [Utilities jsonToDictionary:[message objectForKey: @"decryptedPayload"]];
                        NSDictionary *typeObj = [payload objectForKey:@"@type"];
                        NSString *type = [typeObj objectForKey:@"name"];
                        if ([type  isEqual: @"handshake-reuse-accepted"]) {
                            return completionBlock(true, nil);
                        }
                    }
                }
            }
        }];
    } @catch (NSException *exception) {
        return completionBlock(false, error);
    }
}

+ (void)updateMessageStatus:(NSString *) pwDid
                  messageId:(NSString *) messageId
        withCompletionBlock:(ResponseWithBoolean) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];

    @try {
        NSString *pwdidsJson = [NSString stringWithFormat: @"[{\"pairwiseDID\":\"%@\",\"uids\":[\"%@\"]}]", pwDid, messageId];

        [sdkApi updateMessages:@"MS-106"
                    pwdidsJson:pwdidsJson
                    completion:^(NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(NO, nil);
            }
            return completionBlock(YES, nil);
        }];
    } @catch (NSException *exception) {
        NSLog(@"NSException %@", exception);
        return completionBlock(NO, error);
    }
}

+(void)downloadMessage:(NSString *) messageType
              soughtId:(NSString *) soughtId
   withCompletionBlock:(ResponseWithObject) completionBlock {
    NSString *CREDENTIAL = MessageType(CredentialType);
    NSString *CONNECTION_RESPONSE = MessageType(ConnectionResponseType);
    NSString *ACK = MessageType(AckType);
    NSString *HANDSHAKE = MessageType(HandshakeType);
    NSLog(@"state CDMSG message11 %@", messageType);

    [Message downloadAllMessages:^(NSArray *responseArray, NSError *error) {
        NSLog(@"state CDMSG message22 %@", responseArray);
        NSDictionary *result = nil;

        for (NSInteger i = 0; i < responseArray.count; i++) {
            NSDictionary *message = responseArray[i];
            NSString *payload = [message objectForKey:@"payload"];
            NSDictionary *payloadDict = [Utilities jsonToDictionary:payload];
            NSString *type = [payloadDict objectForKey:@"@type"];
            NSLog(@"state CDMSG message33 %@", message);

            if ([messageType isEqual:CREDENTIAL] && [type rangeOfString:@"issue-credential/1.0/issue-credential"].location != NSNotFound) {
                NSDictionary *thread = [payloadDict objectForKey:@"~thread"];
                NSString *thid = [thread objectForKey:@"thid"];

                NSLog(@"state CDMSG message22 %@, %@", soughtId, thid);

                if ([thid isEqual:soughtId]) {
                    result = message;
                    break;
                }
            }
            if ([messageType isEqual:CONNECTION_RESPONSE] && [type rangeOfString:@"connections/1.0/response"].location != NSNotFound) {
                NSString *pwDid = [message objectForKey:@"pwDid"];

                if ([pwDid isEqual:soughtId]) {
                    result = message;
                    break;
                }
            }
            if ([messageType isEqual:ACK] && [type rangeOfString:@"ack"].location != NSNotFound) {
                result = message;
                break;
            }
            if ([messageType isEqual:HANDSHAKE] && [type rangeOfString:@"handshake-reuse-accepted"].location != NSNotFound) {
                NSDictionary *thread = [payloadDict objectForKey:@"~thread"];
                NSString *thid = [thread objectForKey:@"thid"];

                if ([thid isEqual:soughtId]) {
                    result = message;
                    break;
                }
            }
        }
        return completionBlock(result, nil);
    }];
}

// TODO: move to separate file
+ (void)answerQuestion:(NSString *)serializedConnection
               message:(NSString *)message
                answer:(NSString *)answer
   withCompletionBlock:(ResponseWithBoolean) completionBlock {
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];

    [sdkApi connectionDeserialize:serializedConnection
                       completion:^(NSError *error, NSInteger connectionHandle) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            [sdkApi connectionSendAnswer:(int)connectionHandle
                                question:message
                                  answer:answer
                          withCompletion:^(NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(NO, error);
                }
                return completionBlock(YES, error);
            }];
        }
    ];
}

@end
