//
//  Utilities.h
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 5/28/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#ifndef Utilities_h
#define Utilities_h

@interface CMUtilities : NSObject

+(NSString*)toJsonString:(NSDictionary*)json;
+(NSString*)arrayToJsonString: (NSArray*) json;
+(NSString*)dictToJsonString: (NSDictionary*) json;
+(NSString*)encodeStringTo64: (NSString*)fromString;
+(NSData*)decode64String: (NSString*)fromString;

+(NSDictionary*)jsonToDictionary: (NSString*)json;
+(NSArray*)jsonToArray: (NSString*)json;

+(void)printError: (NSError*)error;
+(void)printErrorMessage: (NSString*)error;
+(void)printSuccess: (NSArray*)message;

@end

typedef void (^ResponseBlock)(NSString *successMessage, NSError *error);
typedef void (^ResponseWithObject)(NSDictionary *responseObject, NSError *error);
typedef void (^ResponseWithArray)(NSArray *responseArray, NSError *error);

#endif /* Utilities_h */

