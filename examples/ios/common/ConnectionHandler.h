//
//  ConnectionHandler.h
//  MSDKSampleAppObjC
//
//  Created by Evernym on 04.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#ifndef ConnectionHandler_h
#define ConnectionHandler_h

#import <Foundation/Foundation.h>
#import "Utilities.h"

@interface ConnectionHandler : NSObject

+(void) handleConnectionInvitation: (NSString *)invite
             withCompletionHandler: (ResponseBlock) completionBlock;

+(void) handleOutOfBandConnectionInvitationWithAttachment:(NSString *) invite
                                               attachment:(NSDictionary *) attachment
                                       existingConnection:(NSString *) existingConnection
                                                     name:(NSString *) name
                                    withCompletionHandler:(ResponseWithObject) completionBlock;

+(void) processInvitationWithCredentialAttachment:(NSString *) invite
                                       attachment:(NSDictionary *) attachment
                               existingConnection:(NSString *) existingConnection
                                             name:(NSString *) name
                            withCompletionHandler:(ResponseWithObject) completionBlock;

+(void) processInvitationWithProffAttachment:(NSString *) invite
                                  attachment:(NSDictionary *) attachment
                          existingConnection:(NSString *) existingConnection
                                        name:(NSString *) name
                       withCompletionHandler:(ResponseWithObject) completionBlock;

@end

#endif /* ConnectionHandler_h */
