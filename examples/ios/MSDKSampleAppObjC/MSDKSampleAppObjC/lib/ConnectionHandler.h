//
//  ConnectionHandler.h
//  MSDKSampleAppObjC
//
//  Created by Evernym on 04.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#ifndef ConnectionHandler_h
#define ConnectionHandler_h

@interface ConnectionHandler : NSObject

+(void) handleConnectionInvitation;
+(void) handleOutOfBandConnectionInvitationWithAttachment;
+(void) processInvitationWithCredentialAttachment;
+(void) processInvitationWithProffAttachment;
+(void) connectionCreate;

@end

#endif /* ConnectionHandler_h */
