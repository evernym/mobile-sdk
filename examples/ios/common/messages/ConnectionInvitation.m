//
//  ConnectionInvitation.m
//  MSDKSampleAppObjC
//
//  Created by Evernym on 04.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ConnectionInvitation.h"
#import "LocalStorage.h"
#import "MobileSDK.h"

@implementation ConnectionInvitation

+(BOOL) isAriesInvitation:(NSString *) type {
    return type == InvitationType(ConnectionsType);
}

+(BOOL) isOutOfBandInvitation:(NSString *) type {
    return type == InvitationType(OutOfBandType);
}

+(NSString *) getInvitationType:(NSString *) invite {
    NSDictionary *parsedInvite = [Utilities jsonToDictionary:invite];
    NSString *type = [parsedInvite objectForKey:@"@type"];

    if ([type rangeOfString:@"out-of-band"].location != NSNotFound) {
        return InvitationType(OutOfBandType);
    }
    if ([type rangeOfString:@"connections"].location != NSNotFound) {
        return InvitationType(ConnectionsType);
    }
    return nil;
}

+(BOOL) isCredentialAttachment:(NSString *) type {
    return type == AttachmentType(CredentialAttach);
}

+(BOOL) isProofAttachment:(NSString *) type {
    return type == AttachmentType(ProofAttach);
}

+(NSString *) getAttachmentType:(NSDictionary *) attach {
    NSString *type = [attach objectForKey:@"@type"];

    if ([type rangeOfString:@"credential"].location != NSNotFound) {
        return AttachmentType(CredentialAttach);
    }
    if ([type rangeOfString:@"present-proof"].location != NSNotFound) {
        return AttachmentType(ProofAttach);
    }
    return nil;
}

+(void)parsedInvite: (NSString *)invite
       withCompletionHandler: (ResponseWithObject) completionBlock {
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    NSURL *url = [NSURL URLWithString:invite];

    if (url) {
        [sdkApi resolveMessageByUrl:invite
                         completion:^(NSError *error, NSString *parsedInvite) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }

            return completionBlock([Utilities jsonToDictionary:parsedInvite], nil);
        }];
    } else {
        return completionBlock([Utilities jsonToDictionary:invite], nil);
    }
}

+(void) extractRequestAttach: (NSDictionary*) invite
       withCompletionHandler: (ResponseBlock) completionBlock {
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    NSDictionary *requestAttach = [invite objectForKey: @"request~attach"];

    if (requestAttach) {
        [sdkApi extractAttachedMessage:[Utilities dictToJsonString: invite]
                            completion:^(NSError *error, NSString *attachedMessage) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }

            return completionBlock(attachedMessage, error);
        }];
    } else {
        [sdkApi extractAttachedMessage:[Utilities dictToJsonString: invite]
                            completion:^(NSError *error, NSString *attachedMessage) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }

            return completionBlock(attachedMessage, error);
        }];
    }
}

+(NSString*)connectionID: connectValues {
    NSString* connectionID = [connectValues objectForKey: @"@id"];
    return connectionID;
}

+(NSString *)getConnectionName:(NSString *) invite {
    NSDictionary *parsedInvite = [Utilities jsonToDictionary:invite];
    NSString *name = [parsedInvite objectForKey:@"label"];
    return name;
}

+(BOOL)compareInvites:(NSString *)newInvite
         storedInvite:(NSString *)storedInvite {
    NSDictionary *newObject = [Utilities jsonToDictionary:newInvite];
    NSDictionary *storedObject = [Utilities jsonToDictionary:storedInvite];

    NSString *newPublicDid = [newObject valueForKey: @"public_did"];
    NSString *storedPublicDid = [storedObject valueForKey: @"public_did"];

    if ([storedPublicDid isEqual:@""] != true) {
        return [newPublicDid isEqual:storedPublicDid];
    } else {
        NSString *newDid = [Utilities jsonToArray: [newObject valueForKey: @"recipientKeys"]][0];
        NSString *storedDid = [Utilities jsonToArray: [storedObject valueForKey: @"recipientKeys"]][0];
        return [newDid isEqual:storedDid];
    }
}

@end
