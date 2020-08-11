//
//  CMProofRequest.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/18/20.
//  Copyright © 2020 Norman Jarvis. All rights reserved.
//

#import "CMProofRequest.h"
#import "AppDelegate.h"

@implementation CMProofRequest

+ (void) sendProofRequest: (NSDictionary*) proofObject proofAttributes: (NSDictionary*) proofAttributes andConnection: (NSDictionary*) connection withCompletionHandler: (ResponseBlock) completionBlock {
    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    
    NSLog(@"Received Proof Req to process - %@", proofObject);
    NSString *messageId = proofObject[@"uid"];
    NSError *error;

    NSMutableDictionary *decryptedPayload = [[CMUtilities jsonToDictionary: proofObject[@"decryptedPayload"]] mutableCopy];
    if (!decryptedPayload) {
        NSLog(@"Error parsing messages JSON: %@", error);
    } else {
        [appDelegate.sdkApi connectionDeserialize: [CMUtilities dictToJsonString: connection] completion:^(NSError *error, NSInteger connectionHandle) {
            if (error != nil && error.code != 0) {
                NSLog(@"Error occurred while deserializing connection - %@ :: %ld", error, (long)error.code);
                return;
            }

            [appDelegate.sdkApi proofCreateWithMsgId: messageId withConnectionHandle: (unsigned int)connectionHandle withMsgId: messageId withCompletion: ^(NSError *error, vcx_proof_handle_t proofHandle, NSString *proofRequest) {

                if (error != nil && error.code != 0) {
                    NSLog(@"Error occurred while proof create with msg proof - %@ :: %ld", error, (long)error.code);
                    return;
                }

                [appDelegate.sdkApi proofRetrieveCredentials: proofHandle withCompletion: ^(NSError *error, NSString *matchingCredentials) {
                    if (error != nil && error.code != 0) {
                        NSLog(@"Error occurred while retrieving proof credentials - %@ :: %ld", error, (long)error.code);
                        return;
                    }

                    [appDelegate.sdkApi proofGenerate: proofHandle withSelectedCredentials: proofAttributes[@"autofilledAttributes"] withSelfAttestedAttrs: proofAttributes[@"selfAttestedAttributes"] withCompletion: ^(NSError *error) {

                        if (error != nil && error.code != 0) {
                            NSLog(@"Error occurred while generating proof - %@ :: %ld", error, (long)error.code);
                            return;
                        }

                        [appDelegate.sdkApi proofSend:proofHandle withConnectionHandle: (unsigned int)connectionHandle withCompletion:^(NSError *error) {
                            if (error != nil && error.code != 0) {
                                NSLog(@"Error occurred while sending proof - %@ :: %ld", error, (long)error.code);
                                return;
                            }
                            NSLog(@"Sent proof for proofReq %@", messageId);

                        }];
                    }];

                }];

            }];

        }];
    };
}

+ (NSDictionary*)vcxMatchingCredentials: (NSString*) matchingCredentials {
    NSError *error;
    
    NSMutableDictionary *machingCredsJSON = [[CMUtilities jsonToDictionary: matchingCredentials] mutableCopy];

    NSMutableDictionary *autofilledAttributes = [@{} mutableCopy];
    NSMutableDictionary *selfAttestedAttributes = [@{} mutableCopy];
    
    NSMutableDictionary *result = [@{@"autofilledAttributes": @"{}", @"selfAttestedAttributes": @"{}"} mutableCopy];
    
    if (!machingCredsJSON) {
        NSLog(@"Error parsing matchingCredentials JSON: %@", error);
        return nil;
    }
    NSLog(@"Serialized matchingCredentials to JSON: %@", machingCredsJSON);
    for(NSString *attr in machingCredsJSON[@"attrs"]) {
        NSLog(@"attr: %@ attrs attr: %@", attr, machingCredsJSON[@"attrs"][attr]);

        NSMutableDictionary *attributeField = [@{} mutableCopy];

        if (machingCredsJSON[@"attrs"][attr]) {
            attributeField[@"tails_file"] = nil;
            if([machingCredsJSON[@"attrs"][attr] count] > 0){
                NSDictionary *credentialInfo = [machingCredsJSON[@"attrs"][attr][0] objectForKey:@"cred_info"];
                attributeField[@"credential"] = @{@"cred_info": credentialInfo, @"interval": @"nil"};
                [autofilledAttributes setValue: attributeField forKey: attr];
                continue;
            }
        }

        // TODO: Add here user input!
        // This detiails we will gather from user entering manually in UI form
        [selfAttestedAttributes setValue: @"myTestValue" forKey: attr];
    }
    
    [result setValue: [CMUtilities toJsonString: [autofilledAttributes count] > 0 ? @{@"attrs": autofilledAttributes}: @{}] forKey: @"autofilledAttributes"];
    [result setValue: [CMUtilities toJsonString: selfAttestedAttributes] forKey:@"selfAttestedAttributes"];
    
    return result;
}

+ (void) autofillAttributes: (NSDictionary*) proofObject andConnection: (NSDictionary*) connection withCompletionHandler: (ResponseWithObject) completionBlock {

    AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
    NSString *messageId = proofObject[@"uid"];
    NSError *error;

    NSMutableDictionary *decryptedPayload = [[CMUtilities jsonToDictionary: proofObject[@"decryptedPayload"]] mutableCopy];
    if (!decryptedPayload) {
        NSLog(@"Error parsing messages JSON: %@", error);
        completionBlock(nil, error);
        return;
    }

    [appDelegate.sdkApi connectionDeserialize: [CMUtilities dictToJsonString: connection] completion: ^(NSError *error, NSInteger connectionHandle) {
        if (error != nil && error.code != 0) {
            completionBlock(nil, error);
            return;
        }

        [appDelegate.sdkApi proofCreateWithMsgId: messageId withConnectionHandle: (unsigned int)connectionHandle withMsgId:messageId withCompletion:^(NSError *error, vcx_proof_handle_t proofHandle, NSString *proofRequest) {
            if (error != nil && error.code != 0) {
                completionBlock(nil, error);
                return;
            }

            [appDelegate.sdkApi proofRetrieveCredentials: proofHandle withCompletion: ^(NSError *error, NSString *matchingCredentials) {
                if (error != nil && error.code != 0) {
                    completionBlock(nil, error);
                    return;
                }
                NSLog(@"matchingCredentials: %@", matchingCredentials);
                NSDictionary *proofAttributes = [self vcxMatchingCredentials: matchingCredentials];

                completionBlock(proofAttributes, nil);
            }];
        }];
    }];
}

@end
