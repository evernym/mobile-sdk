//
//  MSDKSampleAppObjC.h
//  MSDKSampleAppObjC
//
//  Created by Predrag Jevtic on 5/28/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#ifndef Utilities_h
#define Utilities_h

typedef void (^ResponseBlock)(NSString *successMessage, NSError *error);
typedef void (^ResponseWithObject)(NSDictionary *responseObject, NSError *error);
typedef void (^ResponseWithArray)(NSArray *responseArray, NSError *error);
typedef void (^ResponseWithBoolean)(BOOL result, NSError *error);
typedef void (^ResponseWithError)(NSError *error);

@interface Utilities : NSObject

+(NSString*)toJsonString:(NSDictionary*)json;
+(NSString*)arrayToJsonString: (NSArray*) json;
+(NSString*)dictToJsonString: (NSDictionary*) json;
+(NSString*)encodeStringTo64: (NSString*)fromString;
+(NSData*)decode64String: (NSString*)fromString;

+(NSDictionary*)jsonToDictionary: (NSString*)json;
+(NSArray*)jsonToArray: (NSString*)json;
+(NSDictionary*)jsonToDictionaryWithReturnOld: (NSString*)json;

+(void)printError: (NSError*)error;
+(void)printErrorMessage: (NSString*)error;
+(void)printSuccess: (NSArray*)message;
+(void)sendPostRequest: (NSString*)serverURL withBody: (NSDictionary*) data andCompletion: (ResponseBlock) completionBlock;

@end

#endif /* Utilities_h */

