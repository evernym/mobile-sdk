//
//  CMMessage.h
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/11/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CMUtilities.h"

NS_ASSUME_NONNULL_BEGIN

typedef enum {
    Created = 0, // MS-101
    Sent, // MS-102
    Received, // MS-103
    Accepted, // MS-104
    Rejected, // MS-105
    Reviewed, // MS-106
    Redirected // MS-107
} CMMessageStatusType;

#define CMMessageStatusTypeValue(type) [[[NSArray alloc] initWithObjects: @"MS-101", @"MS-102", @"MS-103", @"MS-104", @"MS-105", @"MS-106", @"MS-107", nil] objectAtIndex: type];

typedef enum {
    Credential,
    ConnectionResponse,
    Ack,
    Handshake
} CMMessageType;

#define CMMessageType(type) [[[NSArray alloc] initWithObjects: @"Credential", @"CoonectionResponse", @"Ack", @"Handshake", nil] objectAtIndex: type];

//[[[NSArray alloc] initWithObjects: @"CredentialOffer", @"Credential", @"CoonectionResponse", @"Ack", @"Handshake", nil] objectAtIndex: type];

@interface CMMessage: NSObject

+ (void)waitHandshakeReuse: (ResponseWithBoolean) completionBlock;

+ (void)downloadMessages: (NSDictionary*) connection andType: (CMMessageStatusType) type andMessageID: (nullable NSString*) messageID withCompletionHandler: (ResponseWithArray) completionBlock;
+ (CMMessageType) typeEnum: (NSString*)type;

+ (void)downloadAllMessages:(ResponseWithArray) completionBlock;

+ (void)updateMessageStatus:(NSString *) pwDid
                  messageId:(NSString *) messageId
        withCompletionBlock:(ResponseWithBoolean) completionBlock;

+ (void)downloadMessage:(NSString *) messageType
              soughtId:(NSString *) soughtId
   withCompletionBlock:(ResponseWithObject) completionBlock;

+ (void)answerQuestion:(NSString *)serializedConnection
               message:(NSString *)message
                answer:(NSString *)answer
   withCompletionBlock:(ResponseWithBoolean) completionBlock;
@end

NS_ASSUME_NONNULL_END
