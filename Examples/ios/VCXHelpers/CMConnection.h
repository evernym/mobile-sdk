//
//  Connection.h
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 5/28/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CMUtilities.h"

typedef NS_ENUM(int, ConnectionType) {
    QR = 0,
    SMS = 1,
};

@interface CMConnection: NSObject

+(NSString*)getPwDid: (NSString*) serializedConnection;

+(void) connect: (NSString*)connectJSON connectionType: (int)connectionType phoneNumber: (NSString*)phone withCompletionHandler: (ResponseWithObject)completionBlock;
+(NSDictionary*)parseInvitationLink: (NSString*)link;

+(NSString*)connectionID: connectValues;
+(NSString*) connectionName: (NSDictionary*)connection;
+(void)removeConnection: (NSString*) connection withCompletionHandler: (ResponseBlock) completionBlock;

@end