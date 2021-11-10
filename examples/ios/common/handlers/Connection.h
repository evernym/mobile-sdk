//
//  Connection.h
//  MSDKSampleAppObjC
//
//  Created by Predrag Jevtic on 5/28/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Utilities.h"

@interface Connection: NSObject

+(void)createConnection:(NSString *) invitation
  withCompletionHandler:(ResponseBlock) completionBlock;

+(void)verityConnectionExist:(NSString *) invite
       serializedConnections:(NSArray *) serializedConnections
              withCompletion:(ResponseBlock) completionBlock ;

+(void)connectionRedirectAriesOutOfBand:(NSString*) invitation
                   serializedConnection:(NSString*) serializedConnection
                  withCompletionHandler:(ResponseWithBoolean) completionBlock;

@end
