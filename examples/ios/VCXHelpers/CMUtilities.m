//
//  Utilities.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 5/28/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "CMUtilities.h"

@implementation CMUtilities

+(NSString*)toJsonString: (NSDictionary*)json {
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject: json options: 0 error: &error];

    if(! jsonData) {
        NSLog(@"%s: error", __func__);
        return @"{}";
    }
    return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}

+(NSString*)arrayToJsonString: (NSArray*) json {
    if(!json) {
        return nil;
    }
    NSError* error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject: json options: 0 error: &error];
    if (! jsonData) {
        NSLog(@"%s: error", __func__);
        return @"[]";
    }
    return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}

+(NSString*)dictToJsonString: (NSDictionary*) json {
    if(!json) {
        return nil;
    }
    NSError* error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject: json options: 0 error: &error];
    if (! jsonData) {
        NSLog(@"%s: error", __func__);
        return @"[]";
    }
    return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}

+(NSDictionary*)jsonToDictionary: (NSString*)json {
    if(!json) {
        return nil;
    }
    NSError* error;
    NSDictionary* object = [NSJSONSerialization JSONObjectWithData: [json dataUsingEncoding: NSUTF8StringEncoding] options: NSJSONReadingMutableContainers error: &error];
    
//    if(error != nil) {
//        NSLog(@"Error deserialization");
//        return @{};
//    }

    return object;
}

+(NSArray*)jsonToArray: (NSString*)json {
    if(!json) {
        return nil;
    }
    
    @try {
        NSError* error;
        NSArray* array = [NSJSONSerialization JSONObjectWithData: [json dataUsingEncoding: NSUTF8StringEncoding] options: NSJSONReadingMutableContainers error: &error];

        return array;
    } @catch(NSException* ex) {
        return @[];
    }
}

+(NSString*)encodeStringTo64: (NSString*)fromString {
    NSData *plainData = [fromString dataUsingEncoding: NSUTF8StringEncoding];
    return [plainData base64EncodedStringWithOptions: kNilOptions];
}


+(NSData*)decode64String: (NSString*)fromString {
    if (fromString == nil) {
        return [[NSData alloc] init];
    }
    return [[NSData alloc] initWithBase64EncodedString:fromString options: kNilOptions];
}

// MARK: - Print message helpers

+(void)printError: (NSError*)error{
    NSString *indyErrorCode = [NSString stringWithFormat:@"%ld", (long)error.code];
    NSLog(@"5) Value of indyErrorCode is: %@", indyErrorCode);
}

+(void)printSuccess: (NSArray*) message {
    if(!message) {
        NSLog(@"message error");
        return;
    }
    NSLog(@"Success: %@", [message componentsJoinedByString:@" "]);
}

+(void)printErrorMessage: (NSString*)error{
    NSLog(@"Error message: %@", error);
}

+(void)sendPostRequest: (NSString*)serverURL withBody: (NSDictionary*) data andCompletion: (ResponseBlock) completionBlock  {
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init];
    [request setHTTPMethod:@"POST"];
    [request setURL: [NSURL URLWithString:serverURL]];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];

    [request setHTTPBody: [[CMUtilities dictToJsonString: data] dataUsingEncoding: kCFStringEncodingUTF8]];

    //RESPONSE DATA
    NSURLSession *session = [NSURLSession sharedSession];

    NSURLSessionTask *task = [session dataTaskWithRequest:request completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
        if(error != nil) {
            NSLog(@"Error getting %@", serverURL);
            return completionBlock(nil, error);
        }
            NSString *str = [[NSString alloc] initWithData: data encoding:NSUTF8StringEncoding];
            NSLog(@"responseData: %@", str);
            return completionBlock(str, nil);
    }];
    [task resume];
}

@end
