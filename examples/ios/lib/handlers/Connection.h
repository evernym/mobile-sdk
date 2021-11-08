//
//  Connection.h
//  MSDKSampleAppObjC
//
//  Created by Predrag Jevtic on 5/28/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Utilities.h"

typedef NS_ENUM(int, ConnectionType) {
    QR = 0,
    SMS = 1,
};

@interface Connection: NSObject

+(void)createConnection:(NSString *) invitation
                   name:(NSString*) name
  withCompletionHandler:(ResponseBlock) completionBlock;

+(void)verityConnectionExist: (NSString *)invite
              withCompletion: (ResponseBlock) completionBlock;

+(void)connectionRedirectAriesOutOfBand: (NSString*)invitation
                   serializedConnection: (NSString*)serializedConnection
                  withCompletionHandler: (ResponseWithBoolean) completionBlock;

@end
