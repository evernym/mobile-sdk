//
//  LocalStorage.h
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 23/07/2020.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface LocalStorage: NSObject

+(void)store: (NSString*)key andObject: (NSDictionary*) object;
+(void)store: (NSString*)key andString: (NSString*) value;
+(NSDictionary*)getObjectForKey: (NSString*) key shouldCreate: (BOOL) shouldCreateIfEmpty;
+(NSString*)getValueForKey: (NSString*) key;
+(void)deleteObjectForKey: (NSString*) key;
+(void)addEventToHistory:(NSString *)name;
@end

NS_ASSUME_NONNULL_END
