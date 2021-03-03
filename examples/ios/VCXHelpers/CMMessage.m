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

+ (void)downloadMessages: (NSDictionary*) connection andType: (CMMessageStatusType) type andMessageID: (nullable NSString*) messageID withCompletionHandler: (ResponseWithArray) completionBlock {
    NSString* pwDid = [CMConnection getPwDid: connection[@"serializedConnection"]];
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    NSString* messageType = CMMessageStatusTypeValue(type);
    
    NSLog(@"Connection details %@", connection);

    [sdkApi downloadMessages: messageType uid_s: nil pwdids: pwDid completion: ^(NSError *error, NSString *messages) {
        NSLog(@"Received Messages: %@ for type %@",  messages, messageType);
        NSMutableArray* msgList = [@[] mutableCopy];
        if(messages) {
            NSArray* msgArray = [CMUtilities jsonToArray: messages];
            if(msgArray && [msgArray count] > 0) {
                msgList = msgArray[0][@"msgs"];
            }
        }
        return completionBlock(msgList, error);
    }];
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
