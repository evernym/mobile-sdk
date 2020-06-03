//
//  Connection.h
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 5/28/20.
//  Copyright © 2020 Norman Jarvis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CMUtilities.h"

typedef enum { QR = 0, SMS, } ConnectionType;

@interface CMConnection: NSObject

+(NSString*) getPwDid;
+(NSString*) getSerializedConnection;

+(void) connect: (NSString*)connectJSON connectionType: (ConnectionType*)type phoneNumber: (NSString*)phone withCompletionHandler: (ResponseBlock)completionBlock;

@end
