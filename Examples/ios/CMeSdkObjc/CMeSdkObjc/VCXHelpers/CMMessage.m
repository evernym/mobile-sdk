//
//  CMMessage.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/11/20.
//  Copyright © 2020 Norman Jarvis. All rights reserved.
//

#import "CMMessage.h"
#import "AppDelegate.h"

@implementation CMMessage

+ (void)downloadMessages: (NSDictionary*) connection andType: (CMMessageStatusType) type andMessageID: (nullable NSString*) messageID withCompletionHandler: (ResponseWithArray) completionBlock {
    NSString* pwDid = connection[@"data"][@"pw_did"];
    AppDelegate *appDelegate = (AppDelegate*)[[UIApplication sharedApplication] delegate];
    NSString* messageType = CMMessageStatusTypeValue(type);
    
    NSLog(@"Connection details %@", connection);
    
    [appDelegate.sdkApi downloadMessages: messageType uid_s: nil pwdids: pwDid completion: ^(NSError *error, NSString *messages) {
        NSLog(@"Received messages %@", messages);
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
