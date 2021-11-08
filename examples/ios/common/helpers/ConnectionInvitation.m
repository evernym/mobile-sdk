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

@implementation ConnectionInvitation

+(BOOL) isAriesInvitation:(NSString *) type {
    return type == InvitationType(ConnectionsType);
}

+(BOOL) isOutOfBandInvitation:(NSString *) type {
    return type == InvitationType(OutOfBandType);
}

+(NSString *) getInvitationType:(NSDictionary *) invite {
    NSString *type = [invite objectForKey:@"@type"];

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

+(NSString*)getPwDid: (NSString*) serializedConnection {
    NSError *error;
    NSMutableDictionary *connValues = [NSJSONSerialization JSONObjectWithData: [serializedConnection dataUsingEncoding: NSUTF8StringEncoding] options: NSJSONReadingMutableContainers error: &error];

    return connValues[@"data"][@"pw_did"];
}

+(NSString*)getConnectionByPwDid: (NSString *) pwDidMes {
    NSDictionary* connections = [[LocalStorage getObjectForKey: @"connections" shouldCreate: true] mutableCopy];
    NSString *resultConnection = @"";
    for (NSInteger i = 0; i < connections.allKeys.count; i++) {
        NSString *key = connections.allKeys[i];
        NSDictionary *connection = [connections objectForKey:key];
        NSString *serializedConnection = [connection objectForKey:@"serializedConnection"];
        NSString *pwDid = [self getPwDid:serializedConnection];
        if ([pwDidMes isEqual:pwDid]) {
            resultConnection = serializedConnection;
            break;
        }
    }
    return resultConnection;
}

+(NSDictionary*)parsedInvite: (NSString *)invite {
    NSLog(@"invite np parsed %@", invite);
    if ([invite rangeOfString:@"oob"].location != NSNotFound) {
        return [self parseInvitationLink: invite];
    } else if ([invite rangeOfString:@"c_i"].location != NSNotFound) {
        return [self parseInvitationLink: invite];
    } else {
        return [self readFromUrl: invite];
    }
}

+(NSDictionary*)readFromUrl: (NSString*)invite {
    if(!invite) {
        return nil;
    }
    NSLog(@"readFromUrl %@ - ", invite);
    NSURL *url = [NSURL URLWithString:invite];
    NSData *data = [[NSData alloc] initWithContentsOfURL:url];
    if (url && data) {
        NSDictionary *result = [NSJSONSerialization JSONObjectWithData:data options:0 error:NULL];
        return result;
    } else {
        return [Utilities jsonToDictionary:invite];
    }
}

+(NSString*)readInviteFromUrl: (NSString*)invite {
    if(!invite) {
        return nil;
    }
    NSLog(@"readFromUrl %@ - ", invite);
    NSURL *url = [NSURL URLWithString:invite];
    NSData *data = [[NSData alloc] initWithContentsOfURL:url];
    if (url && data) {
        NSString *result = [Utilities dictToJsonString:[NSJSONSerialization JSONObjectWithData:data options:0 error:NULL]];
        return result;
    } else {
        return invite;
    }
}

+(NSDictionary*) extractRequestAttach: (NSDictionary*)invite {
    NSArray* requestAttach = [invite objectForKey: @"request~attach"];
    if (requestAttach.count != 0) {
        NSDictionary* requestAttachItem = requestAttach[0];
        NSDictionary* requestAttachData = [requestAttachItem objectForKey: @"data"];
        NSString* requestAttachBase64 = [requestAttachData objectForKey: @"base64"];

        NSData* invitationData = [Utilities decode64String: requestAttachBase64];
        NSString* json = [[NSString alloc] initWithData: invitationData encoding: NSUTF8StringEncoding];
        NSLog(@" JSON %@", json);
        return [Utilities jsonToDictionary: json];
    } else {
        return nil;
    }
}

+(NSDictionary*) parseInvitationLink: (NSString*) link {
    NSArray* linkComponents = [link componentsSeparatedByString: @"msg?c_i="];

    if([linkComponents count] < 2) {
        return nil;
    }

    NSData* invitationData = [Utilities decode64String: linkComponents[1]];
    NSString*  json = [[NSString alloc] initWithData: invitationData encoding: NSUTF8StringEncoding];

    return [Utilities jsonToDictionary: json];
}

+(NSString*)connectionID: connectValues {
    NSString* connectionID = [connectValues objectForKey: @"id"];

    if(!connectionID) {
        connectionID =  [connectValues objectForKey: @"@id"];
    }

    if(!connectionID) {
        NSDictionary* connectionData = [Utilities jsonToDictionary: connectValues[@"serializedConnection"]];
        connectionID = connectionData[@"data"][@"pw_did"];
    }

    if(!connectionID) {
        NSLog(@"Connection ID is missing %@", connectValues);
    }

    return connectionID;
}

+(NSString*) connectionName: (NSDictionary*)connection {
    NSString* connectionName = connection[@"invitation"][@"s"][@"n"];
    if(!connectionName) {
        connectionName = connection[@"invitation"][@"label"];
    }

    return connectionName;
}

@end
