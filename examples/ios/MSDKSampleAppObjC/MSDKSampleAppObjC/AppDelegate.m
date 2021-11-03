//
//  AppDelegate.m
//  MSDKSampleAppObjC
//
//  Created by Norman Jarvis on 5/3/19.
//  Copyright © 2019 Evernym Inc. All rights reserved.
//

#import "AppDelegate.h"
#import <Security/Security.h>
#import "Config.h"
#import "MobileSDK.h"
#import "LocalStorage.h"
#import "Message.h"

#define SYSTEM_VERSION_GRATERTHAN_OR_EQUALTO(v) ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedAscending)

@interface AppDelegate ()
@end

@implementation AppDelegate

-(BOOL)application: (UIApplication *)application
didFinishLaunchingWithOptions:(NSDictionary *) launchOptions {
    self.window = [[UIWindow alloc]initWithFrame: UIScreen.mainScreen.bounds];
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    
    UIViewController *homeViewController = [storyboard instantiateViewControllerWithIdentifier:@"HomeViewController"];
    
    UIViewController *historyViewController = [storyboard instantiateViewControllerWithIdentifier:@"HistoryViewController"];
    
    UITabBarController *tabBarController = [[UITabBarController alloc] init];
    tabBarController.viewControllers = @[homeViewController, historyViewController];
    
    self.window.rootViewController = tabBarController;
    [self.window makeKeyWindow];
    
    [[MobileSDK shared] setSdkApi: [[ConnectMeVcx alloc] init]];
    [Config initVCX];
    
    return YES;
}

@end

