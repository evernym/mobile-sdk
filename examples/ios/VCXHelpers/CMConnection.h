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

typedef enum {
    Proprietary,
    Connection,
    OutOfBand
} InvitationType;

@interface CMConnection: NSObject

+(NSString*) getPwDid: (NSString*) serializedConnection;

+(void) createConnection: (NSString*)connectJSON connectionType: (int)connectionType phoneNumber: (NSString*)phone withCompletionHandler: (ResponseWithObject)completionBlock;
+(void) connectWithInvite: (NSString*)connectJSON connectionType: (int)connectionType phoneNumber: (NSString*)phone withCompletionHandler: (ResponseWithObject)completionBlock;
+(void) connectWithOutofbandInvite: (NSString*)connectJSON connectionType: (int)connectionType phoneNumber: (NSString*)phone withCompletionHandler: (ResponseWithObject)completionBlock;
+(NSDictionary*)parsedInvite: (NSString*)invite;
+(NSDictionary*)parseInvitationLink: (NSString*)link;
+(NSString*)getConnectionByPwDid: (NSString *) pwDidMes;

+(void)handleConnection:(NSString *)invite
         connectionType: (int)connectionType
            phoneNumber: (NSString*) phone
  withCompletionHandler:(ResponseWithObject) completionBlock;

+(NSString*) connectionID: connectValues;
+(NSString*) connectionName: (NSDictionary*)connection;
+(void) removeConnection: (NSString*) connection withCompletionHandler: (ResponseBlock) completionBlock;

@end
