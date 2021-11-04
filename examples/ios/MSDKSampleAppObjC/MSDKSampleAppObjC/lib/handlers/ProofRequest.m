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

+ (void) sendProofRequest: (NSDictionary*)proofObject
          proofAttributes: (NSDictionary*) proofAttributes
            andConnection: (NSDictionary*) connection
    withCompletionHandler: (ResponseBlock) completionBlock {
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];
    
    NSLog(@"Received Proof Req to process - %@", proofObject);
    NSString *messageId = proofObject[@"uid"];
    NSError *error = nil;

    NSMutableDictionary *decryptedPayload = [[Utilities jsonToDictionary: proofObject[@"decryptedPayload"]] mutableCopy];
    if (!decryptedPayload) {
        NSLog(@"Error parsing messages JSON: %@", error);
    } else {
        [sdkApi connectionDeserialize: [Utilities dictToJsonString: connection] completion:^(NSError *error, NSInteger connectionHandle) {
            if (error != nil && error.code != 0) {
                NSLog(@"Error occurred while deserializing connection - %@ :: %ld", error, (long)error.code);
                return;
            }

            [sdkApi proofCreateWithMsgId: messageId withConnectionHandle: (unsigned int)connectionHandle withMsgId: messageId withCompletion: ^(NSError *error, vcx_proof_handle_t proofHandle, NSString *proofRequest) {

                if (error != nil && error.code != 0) {
                    NSLog(@"Error occurred while proof create with msg proof - %@ :: %ld", error, (long)error.code);
                    return;
                }

                [sdkApi proofRetrieveCredentials: proofHandle withCompletion: ^(NSError *error, NSString *matchingCredentials) {
                    if (error != nil && error.code != 0) {
                        NSLog(@"Error occurred while retrieving proof credentials - %@ :: %ld", error, (long)error.code);
                        return;
                    }

                    [sdkApi proofGenerate: proofHandle withSelectedCredentials: proofAttributes[@"autofilledAttributes"] withSelfAttestedAttrs: proofAttributes[@"selfAttestedAttributes"] withCompletion: ^(NSError *error) {

                        if (error != nil && error.code != 0) {
                            NSLog(@"Error occurred while generating proof - %@ :: %ld", error, (long)error.code);
                            return;
                        }

                        [sdkApi proofSend:proofHandle withConnectionHandle: (unsigned int)connectionHandle withCompletion:^(NSError *error) {
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
    
    NSMutableDictionary *machingCredsJSON = [[Utilities jsonToDictionary: matchingCredentials] mutableCopy];

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

+(void) reject:(NSString *) serializedConnection
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

+(void)sendProofRequestFromMessage:(NSString *) data
             withCompletionHandler:(ResponseWithBoolean) completionBlock {
    NSDictionary *message = [Utilities jsonToDictionary:data];
    NSString *pwDidMes = [message objectForKey:@"pwDid"];
    NSString *payload = [message objectForKey:@"payload"];
    NSString *offerConnection = [Connection getConnectionByPwDid:pwDidMes];
    NSDictionary *payloadDict = [Utilities jsonToDictionary:payload];
    
    [ProofRequest createWithRequest:payload
                withCompletionHandler:^(NSDictionary *offer, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        NSLog(@"Proof Request created %@", error);

        [ProofRequest retrieveAvailableCredentials:[Utilities dictToJsonString:offer]
                               withCompletionHandler:^(NSDictionary *creds, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            };

            NSLog(@"Proof Request retrieved %@", creds);
            NSString *attr = [creds objectForKey: @"autofilledAttributes"];
            
            [ProofRequest send:offerConnection
                 serializedProof:[Utilities dictToJsonString:offer]
                   selectedCreds:attr
                selfAttestedAttr:@"{}"
           withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Proof request send", [payloadDict objectForKey:@"comment"]]];
                NSLog(@"Proof Request send %@", error);
                return completionBlock(responseObject, nil);
            }];
        }];
    }];
}

+(void)rejectProofRequestFromMessage:(NSString *) data
             withCompletionHandler:(ResponseWithBoolean) completionBlock {
    NSDictionary *message = [Utilities jsonToDictionary:data];
    NSString *pwDidMes = [message objectForKey:@"pwDid"];
    NSString *payload = [message objectForKey:@"payload"];
    NSString *offerConnection = [Connection getConnectionByPwDid:pwDidMes];
    NSDictionary *payloadDict = [Utilities jsonToDictionary:payload];

    [self createWithRequest:payload
                withCompletionHandler:^(NSDictionary *request, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        NSLog(@"Proof Request created %@", error);
        
        [self reject:offerConnection serializedProof:[Utilities dictToJsonString:request]
                               withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Proof request reject", [payloadDict objectForKey:@"comment"]]];
            NSLog(@"Proof Request reject %@", error);
            return completionBlock(responseObject, nil);
        }];
    }];
}

@end
