//
//  Connection.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 5/28/20.
//  Copyright © 2020 Norman Jarvis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AppDelegate.h"
#import "CMConnection.h"

@implementation CMConnection

/*
 Connection type example:
 "{"connection_type":"SMS","phone":"123"}" OR: "{"connection_type":"QR","phone":""}"
 */
+(NSString*) connectionByType: (ConnectionType*) type {
    return  [@[@"QR", @"SMS"] objectAtIndex: (int)type];
}

+(NSString*)getPwDid {
    NSString *serializedConnection = [CMConnection getSerializedConnection];
    NSError *error;
    
    NSMutableDictionary *connValues = [NSJSONSerialization JSONObjectWithData: [serializedConnection dataUsingEncoding: NSUTF8StringEncoding] options:NSJSONReadingMutableContainers error: &error];
    
    return connValues[@"data"][@"pw_did"];
}

+(NSString*)getSerializedConnection {
    NSString *serializedConnection = [[NSUserDefaults standardUserDefaults] stringForKey: @"serializedConnection"];
    NSLog(@"Using serializedConnection: %@", serializedConnection);
    return serializedConnection;
}

+(void)connect: (NSString*)connectJSON connectionType: (ConnectionType*) type phoneNumber: (NSString*) phone withCompletionHandler: (ResponseBlock)completionBlock {
    ConnectMeVcx *sdkApi = [(AppDelegate*)[[UIApplication sharedApplication] delegate] sdkApi];

    NSDictionary *connectValues = [CMUtilities jsonToDictionary: connectJSON];
    if([connectValues count] < 1) {
        NSError* error = [NSError errorWithDomain: @"connections" code: 400 userInfo: @{
            NSLocalizedDescriptionKey: @"Invalid Connection JSON"
        }];

        return completionBlock(nil, error);
    }

    [sdkApi connectionCreateWithInvite: [connectValues valueForKey: @"id"] inviteDetails: connectJSON completion: ^(NSError *error, NSInteger connectionHandle) {
        if (error) {
            return completionBlock(nil, error);
        }

        [CMUtilities printSuccess: @[@"createConnectionWithInvite",  [NSNumber numberWithLong: connectionHandle]]];

        [sdkApi connectionConnect: (int)connectionHandle connectionType: [NSString stringWithFormat:@"{\"connection_type\":\"%@\",\"phone\":%@\"\"}", [CMConnection connectionByType: type], phone] completion: ^(NSError *error, NSString *inviteDetails) {

            if (error) {
                return completionBlock(nil, error);
            }
            [CMUtilities printSuccess: @[@"connectionConnect", inviteDetails]];

            [sdkApi connectionSerialize: (int)connectionHandle completion: ^(NSError *error, NSString *state) {
                if (error) {
                    return completionBlock(nil, error);
                }

                [CMUtilities printSuccess: @[@"Connection success", state]];

                // Store the serialized connection
                NSUserDefaults *standardUserDefaults = [NSUserDefaults standardUserDefaults];
                if (standardUserDefaults) {
                    [standardUserDefaults setObject: state forKey:@"serializedConnection"];
                    [standardUserDefaults synchronize];
                }
            }];
        }];
    }];
}

@end
