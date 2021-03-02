//
//  MobileSDK.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 02/02/2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import "MobileSDK.h"
#import "LocalStorage.h"

@implementation MobileSDK

@synthesize sdkApi;
@synthesize sdkInited;

+ (instancetype)shared {
    static id instance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[self alloc] init];
    });
    return instance;
}

-(NSString*)provisioningToken {
    return [LocalStorage getValueForKey: @"provisioningToken"];
}

@end
