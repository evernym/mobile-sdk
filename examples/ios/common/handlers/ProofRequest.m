//
//  ProofRequest.m
//  MSDKSampleAppObjC
//
//  Created by Predrag Jevtic on 6/18/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import "ProofRequest.h"
#import "MobileSDK.h"
#import "Connection.h"
#import "LocalStorage.h"
#import "ConnectionInvitation.h"

@implementation ProofRequest

+(void) createWithRequest: (NSString *) request
    withCompletionHandler: (ResponseWithObject) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];

    @try {
        NSString *uuid = [[NSUUID UUID] UUIDString];
        [sdkApi proofCreateWithRequest:uuid
                      withProofRequest:request
                        withCompletion:^(NSError *error, vcx_proof_handle_t proofHandle) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            };
            [sdkApi proofSerialize:proofHandle
                    withCompletion:^(NSError *error, NSString *proof_request) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                return completionBlock([Utilities jsonToDictionary:proof_request], error);
            }];
        }];
    } @catch (NSException *exception) {
        return completionBlock(nil, error);
    }
}

+(void) sendProof:(NSDictionary *) proofRequest
serializedConnection:(NSString *) serializedConnection
withCompletionHandler:(ResponseWithObject) completionBlock {
    [self retrieveAvailableCredentials:[Utilities dictToJsonString:proofRequest]
                           withCompletionHandler:^(NSDictionary *creds, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        };
        NSString *attr = [creds objectForKey: @"autofilledAttributes"];

        [self send:serializedConnection
   serializedProof:[Utilities dictToJsonString:proofRequest]
     selectedCreds:attr
  selfAttestedAttr:@"{}"
withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }

            return completionBlock(responseObject, nil);
        }];
    }];
}

+(void) retrieveAvailableCredentials:(NSString *) serializedProof
               withCompletionHandler:(ResponseWithObject) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];

    @try {
        [sdkApi proofDeserialize:serializedProof
                  withCompletion:^(NSError *error, vcx_proof_handle_t proofHandle) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            [sdkApi proofRetrieveCredentials:proofHandle
                              withCompletion:^(NSError *error, NSString *matchingCredentials) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                NSDictionary *proofAttributes = [self vcxMatchingCredentials: matchingCredentials];

                return completionBlock(proofAttributes, nil);
            }];
        }];
    } @catch (NSException *exception) {
        return completionBlock(nil, error);
    }
}

+(void) send:(NSString *) serializedConnection
      serializedProof:(NSString *) serializedProof
        selectedCreds:(NSString *) selectedCreds
     selfAttestedAttr:(NSString *) selfAttestedAttr
withCompletionHandler: (ResponseWithObject) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];

    @try {
        [sdkApi connectionDeserialize:serializedConnection
                           completion:^(NSError *error, NSInteger connectionHandle) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            NSLog(@"Received Proof connection deserialize");

            [sdkApi proofDeserialize:serializedProof
                      withCompletion:^(NSError *error, vcx_proof_handle_t proofHandle) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                };
                NSLog(@"Received Proof proof deserialize");

                [sdkApi proofGenerate:proofHandle
              withSelectedCredentials:selectedCreds
                withSelfAttestedAttrs:selfAttestedAttr
                       withCompletion:^(NSError *error) {
                    if (error && error.code > 0) {
                        return completionBlock(nil, error);
                    };
                    NSLog(@"Received Proof proof generate");

                    [sdkApi proofSend:proofHandle
                 withConnectionHandle:(int)connectionHandle
                       withCompletion:^(NSError *error) {
                        if (error && error.code > 0) {
                            return completionBlock(nil, error);
                        };
                        NSLog(@"Received Proof proof send");

                        [sdkApi proofSerialize:proofHandle
                                withCompletion:^(NSError *error, NSString *proof_request) {
                            if (error && error.code > 0) {
                                return completionBlock(nil, error);
                            }
                            return completionBlock([Utilities jsonToDictionary:proof_request], nil);
                        }];
                    }];
                }];
            }];
        }];
    } @catch (NSException *exception) {
        return completionBlock(nil, error);
    }
}

+ (NSDictionary*)vcxMatchingCredentials: (NSString*) matchingCredentials {
    NSError *error;

    NSMutableDictionary *machingCredsJSON = [[Utilities jsonToDictionary: matchingCredentials] mutableCopy];

    NSMutableDictionary *autofilledAttributes = [@{} mutableCopy];
    NSMutableDictionary *selfAttestedAttributes = [@{} mutableCopy];

    NSMutableDictionary *result = [@{@"autofilledAttributes": @"{}", @"selfAttestedAttributes": @"{}"} mutableCopy];

    if (!machingCredsJSON) {
        NSLog(@"Error parsing matchingCredentials JSON: %@", error);
        return nil;
    }
    NSLog(@"Serialized matchingCredentials to JSON: %@", machingCredsJSON);
    for(NSString *attr in machingCredsJSON[@"attributes"]) {
        NSLog(@"attr: %@ attrs attr: %@", attr, machingCredsJSON[@"attributes"][attr]);

        NSMutableDictionary *attributeField = [@{} mutableCopy];

        if (machingCredsJSON[@"attributes"][attr]) {
            if([machingCredsJSON[@"attributes"][attr][@"credentials"] count] > 0){
                NSDictionary *credentialInfo = [machingCredsJSON[@"attributes"][attr][@"credentials"][0] objectForKey:@"cred_info"];
                attributeField[@"credential"] = @{@"cred_info": credentialInfo, @"interval": @"nil"};
                [autofilledAttributes setValue: attributeField forKey: attr];
                continue;
            }
        }

        // TODO: Add here user input!
        // This detiails we will gather from user entering manually in UI form
        [selfAttestedAttributes setValue: @"myTestValue" forKey: attr];
    }

    [result setValue: [Utilities toJsonString: [autofilledAttributes count] > 0 ? @{@"attrs": autofilledAttributes}: @{}] forKey: @"autofilledAttributes"];
    [result setValue: [Utilities toJsonString: selfAttestedAttributes] forKey:@"selfAttestedAttributes"];

    return result;
}

+ (void) autofillAttributes: (NSDictionary*) proofObject
              andConnection: (NSDictionary*) connection
      withCompletionHandler: (ResponseWithObject) completionBlock {

    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    NSString *messageId = proofObject[@"uid"];
    NSError *error;

    NSMutableDictionary *decryptedPayload = [[Utilities jsonToDictionary: proofObject[@"decryptedPayload"]] mutableCopy];
    if (!decryptedPayload) {
        NSLog(@"Error parsing messages JSON: %@", error);
        completionBlock(nil, error);
        return;
    }

    [sdkApi connectionDeserialize: [Utilities dictToJsonString: connection] completion: ^(NSError *error, NSInteger connectionHandle) {
        if (error != nil && error.code != 0) {
            completionBlock(nil, error);
            return;
        }

        [sdkApi proofCreateWithMsgId: messageId withConnectionHandle: (unsigned int)connectionHandle withMsgId:messageId withCompletion:^(NSError *error, vcx_proof_handle_t proofHandle, NSString *proofRequest) {
            if (error != nil && error.code != 0) {
                completionBlock(nil, error);
                return;
            }

            [sdkApi proofRetrieveCredentials: proofHandle withCompletion: ^(NSError *error, NSString *matchingCredentials) {
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

+(void) rejectProofRequest:(NSString *) serializedConnection
           serializedProof:(NSString *) serializedProof
     withCompletionHandler: (ResponseWithObject) completionBlock {
    NSError* error;
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];

    @try {
        [sdkApi connectionDeserialize:serializedConnection
                           completion:^(NSError *error, NSInteger connectionHandle) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            };
            [sdkApi proofDeserialize:serializedProof
                      withCompletion:^(NSError *error, vcx_proof_handle_t proofHandle) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);

                };
                [sdkApi proofReject:proofHandle
               withConnectionHandle:(int)connectionHandle
                     withCompletion:^(NSError *error) {
                    if (error && error.code > 0) {
                        return completionBlock(nil, error);
                    };
                    [sdkApi proofSerialize:proofHandle
                            withCompletion:^(NSError *error, NSString *proof_request) {
                        if (error && error.code > 0) {
                            return completionBlock(nil, error);
                        }
                        return completionBlock([Utilities jsonToDictionary:proof_request], nil);
                    }];
                }];
            }];
        }];
    } @catch (NSException *exception) {
        return completionBlock(nil, error);
    }
}

@end
