//
//  CMMessage.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/11/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import "CMMessage.h"
#import "CMConnection.h"
#import "MobileSDK.h"

@implementation CMMessage

+ (void) downloadAllMessages:(ResponseWithArray) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    NSString* messageType = CMMessageStatusTypeValue(Received);

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
            NSArray* messagesArray = [CMUtilities jsonToArray: messages];

            for (NSInteger i = 0; i < messagesArray.count; i++) {
                NSDictionary *message = messagesArray[i];

                NSArray *msgs = [message objectForKey:@"msgs"];
                NSString *pwDid = [message objectForKey:@"pairwiseDID"];
                for (NSInteger j = 0; j < msgs.count; j++) {
                    NSDictionary *msg = msgs[j];
                    NSMutableDictionary *msgDict = [@{} mutableCopy];

                    NSDictionary *payload = [CMUtilities jsonToDictionary:[msg objectForKey:@"decryptedPayload"]];
                    
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

+ (void)waitHandshakeReuse: (ResponseWithBoolean) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    
    @try {
        NSString* messageType = CMMessageStatusTypeValue(Received);
        [sdkApi downloadMessages: messageType
                           uid_s: nil
                          pwdids: nil
                      completion: ^(NSError *error, NSString *messages) {
            NSMutableArray* msgList = [@[] mutableCopy];
            NSLog(@"messages %@", messages);
            if (messages) {
                NSArray* msgArray = [CMUtilities jsonToArray: messages];
                if(msgArray && msgArray.count > 0) {
                    msgList = msgArray[0][@"msgs"];
                    for (int i = 0; i < msgArray.count; i++) {
                        NSDictionary* message = msgList[i];
                        NSDictionary* payload = [CMUtilities jsonToDictionary:[message objectForKey: @"decryptedPayload"]];
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
    NSString *CREDENTIAL = CMMessageType(Credential);
    NSString *CONNECTION_RESPONSE = CMMessageType(ConnectionResponse);
    NSString *ACK = CMMessageType(Ack);
    NSString *HANDSHAKE = CMMessageType(Handshake);
    NSLog(@"state CDMSG message11 %@", messageType);

    [CMMessage downloadAllMessages:^(NSArray *responseArray, NSError *error) {
        NSLog(@"state CDMSG message22 %@", responseArray);

        for (NSInteger i = 0; i < responseArray.count; i++) {
            NSDictionary *message = responseArray[i];
            NSString *payload = [message objectForKey:@"payload"];
            NSDictionary *payloadDict = [CMUtilities jsonToDictionary:payload];
            NSString *type = [payloadDict objectForKey:@"@type"];
            NSLog(@"state CDMSG message33 %@", message);

            if ([messageType isEqual:CREDENTIAL] && [type rangeOfString:@"issue-credential/1.0/issue-credential"].location != NSNotFound) {
                NSDictionary *thread = [payloadDict objectForKey:@"~thread"];
                NSString *thid = [thread objectForKey:@"thid"];

                if ([thid isEqual:soughtId]) {
                    return completionBlock(message, nil);
                    break;
                }
            }
            if ([messageType isEqual:CONNECTION_RESPONSE] && [type rangeOfString:@"connections/1.0/response"].location != NSNotFound) {
                NSString *pwDid = [message objectForKey:@"pwDid"];

                if ([pwDid isEqual:soughtId]) {
                    return completionBlock(message, nil);
                    break;
                }
            }
            if ([messageType isEqual:ACK] && [type rangeOfString:@"ack"].location != NSNotFound) {
                return completionBlock(message, nil);
                break;
            }
            if ([messageType isEqual:HANDSHAKE] && [type rangeOfString:@"handshake-reuse-accepted"].location != NSNotFound) {
                NSDictionary *thread = [payloadDict objectForKey:@"~thread"];
                NSString *thid = [thread objectForKey:@"thid"];
                
                if ([thid isEqual:soughtId]) {
                    return completionBlock(message, nil);
                    break;
                }
            }
        }
        return completionBlock(nil, nil);
    }];
}

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

+(CMMessageType) typeEnum: (NSString *)type {
    NSArray* types = @[@"credOffer"];
    if(![types containsObject: type]) {
        NSLog(@"Invalid type provided");
        return Credential;
    }
    return (int)[types indexOfObject: type];
}

@end
