//
//  LocalStorage.m
//  MSDKSampleAppObjC
//
//  Created by Predrag Jevtic on 23/07/2020.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import "LocalStorage.h"

@implementation LocalStorage

+(void)store:(NSString*)key andObject: (NSDictionary*) object {
    [[NSUserDefaults standardUserDefaults] setObject: object forKey: key];
    [[NSUserDefaults standardUserDefaults] synchronize];

    NSLog(@"%@", [[NSUserDefaults standardUserDefaults] objectForKey: key]);
}

+(NSDictionary*)getValueForKey: (NSString*) key {
    if([NSUserDefaults.standardUserDefaults objectForKey: key] != nil) {
        return [[NSUserDefaults standardUserDefaults] valueForKey: key];
    }
    return nil;
}

+(NSDictionary*)getObjectForKey: (NSString*) key shouldCreate: (BOOL) shouldCreateIfEmpty {
    NSUserDefaults* defaults = [NSUserDefaults standardUserDefaults];
    NSDictionary* object = [defaults objectForKey: key];
    if(shouldCreateIfEmpty && !object) {
        [LocalStorage store: key andObject: @{}];
        return @{};
    }
    return object;
}

+(void)deleteObjectForKey: (NSString*) key {
    [[NSUserDefaults standardUserDefaults] removeObjectForKey: key];
}

+(void) addEventToHistory:(NSString *)name {
    NSMutableDictionary* history = [[LocalStorage getObjectForKey: @"history" shouldCreate: true] mutableCopy];

    NSDictionary* historyObj = @{
        @"name": name,
    };
    NSString *uuid = [[NSUUID UUID] UUIDString];
    [history setValue: historyObj forKey: uuid];
    [LocalStorage store: @"history" andObject: history];
}

@end
