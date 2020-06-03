//
//  Utilities.h
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 5/28/20.
//  Copyright © 2020 Norman Jarvis. All rights reserved.
//

#ifndef Utilities_h
#define Utilities_h

@interface CMUtilities : NSObject

+(NSString*)toJsonString:(NSDictionary*)json;
+(NSString*)arrayToJsonString: (NSArray*) json;
+(NSString*)encodeStringTo64: (NSString*)fromString;

+(NSDictionary*)jsonToDictionary: (NSString*)json;

+(void)printError: (NSError*)error;
+(void)printErrorMessage: (NSString*)error;
+(void)printSuccess: (NSArray*)message;

@end

typedef void (^ResponseBlock)(NSString *successMessage, NSError *error);

#endif /* Utilities_h */
