//
//  AppDelegate.m
//  CMeSdkObjc
//
//  Created by Norman Jarvis on 5/3/19.
//  Copyright © 2019 Evernym Inc. All rights reserved.
//

#import "AppDelegate.h"
#import <Security/Security.h>
#import "CMConfig.h"
#import "MobileSDK.h"
#import "LocalStorage.h"
#import "CMMessage.h"

#define SYSTEM_VERSION_GRATERTHAN_OR_EQUALTO(v) ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedAscending)

@interface AppDelegate ()
@end

@implementation AppDelegate

- (BOOL)application: (UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {

    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:[NSBundle mainBundle]];
    UIViewController *vc = [storyboard instantiateInitialViewController];

    // Set root view controller and make windows visible
    self.window = [[UIWindow alloc] initWithFrame:UIScreen.mainScreen.bounds];
    self.window.rootViewController = vc;
    [self.window makeKeyAndVisible];

    [[MobileSDK shared] setSdkApi: [[ConnectMeVcx alloc] init]];
    [CMConfig initVCX];

    return YES;
}

@end

