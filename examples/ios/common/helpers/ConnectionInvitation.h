//
//  ConnectionInvitation.h
//  MSDKSampleAppObjC
//
//  Created by Evernym on 04.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Utilities.h"

NS_ASSUME_NONNULL_BEGIN

typedef enum {
    ConnectionsType = 0,
    OutOfBandType
} InvitationType;

#define InvitationType(type) [[[NSArray alloc] initWithObjects: @"Connection", @"OutOfBand", nil] objectAtIndex: type];

typedef enum {
    CredentialAttach = 0,
    ProofAttach
} AttachmentType;

#define AttachmentType(type) [[[NSArray alloc] initWithObjects: @"CredentialAttach", @"ProofAttach", nil] objectAtIndex: type];

@interface ConnectionInvitation : NSObject

+(void)getPwDid: (NSString*) serializedConnection
withCompletionHandler: (ResponseBlock) completionBlock;
+(void) extractRequestAttach: (NSDictionary*)invite
                withCompletionHandler: (ResponseBlock) completionBlock;

+(NSString*) connectionID: connectValues;
+(NSString*) getConnectionByPwDid: (NSString *) pwDidMes;
+(NSDictionary*) parsedInvite: (NSString *)invite;

+(NSArray*) getAllSerializedConnections;
+(BOOL) compareInvites:(NSString *)newInvite
         storedInvite:(NSString *)storedInvite;
+(NSString *)getConnectionName:(NSString *) invite;
+(NSString*)getInvitationByPwDid: (NSString *) pwDidMes;

+(BOOL) isAriesInvitation:(NSString *) type;
+(BOOL) isOutOfBandInvitation:(NSString *) type;
+(NSString *) getInvitationType:(NSString *) invite;

+(BOOL) isCredentialAttachment:(NSString *) type;
+(BOOL) isProofAttachment:(NSString *) type;
+(NSString *) getAttachmentType:(NSDictionary *) attach;

@end

NS_ASSUME_NONNULL_END
