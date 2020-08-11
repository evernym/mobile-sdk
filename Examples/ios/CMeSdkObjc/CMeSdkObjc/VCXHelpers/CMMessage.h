//
//  CMMessage.h
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/11/20.
//  Copyright © 2020 Norman Jarvis. All rights reserved.
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
    CredentialOffer,
    Credential,
    ProofRequest,
    Question
} CMMessageType;

//[[[NSArray alloc] initWithObjects: @"credOffer", @"cred", @"proofReq", @"Question", nil] objectAtIndex: type];

@interface CMMessage: NSObject

+ (void)downloadMessages: (NSDictionary*) connection andType: (CMMessageStatusType) type andMessageID: (nullable NSString*) messageID withCompletionHandler: (ResponseWithArray) completionBlock;
+ (CMMessageType) typeEnum: (NSString*)type;

@end

NS_ASSUME_NONNULL_END
