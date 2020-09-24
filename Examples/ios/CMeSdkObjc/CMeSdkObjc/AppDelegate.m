//
//  AppDelegate.m
//  CMeSdkObjc
//
//  Created by Norman Jarvis on 5/3/19.
//  Copyright © 2019 Evernym Inc. All rights reserved.
//

#import "AppDelegate.h"
#import <Security/Security.h>
@import Firebase;
#import "CMConfig.h"

#define SYSTEM_VERSION_GRATERTHAN_OR_EQUALTO(v) ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedAscending)

@interface AppDelegate ()

@end

@implementation AppDelegate
@synthesize sdkInited;

- (id)init
{
    self = [super init];
    if (self) {
        // Initialization code here.
        self.sdkApi = [[ConnectMeVcx alloc] init];
        self.sdkInited = false;
    }

    return self;
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
    // If you are receiving a notification message while your app is in the background,
    // this callback will not be fired till the user taps on the notification launching the application.
    //[self showAlert:userInfo];
    NSLog(@"[1] User Info : %@", userInfo);
    [self handleRemoteNotification:[UIApplication sharedApplication] userInfo:userInfo];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    // If you are receiving a notification message while your app is in the background,
    // this callback will not be fired till the user taps on the notification launching the application.
    //[self showAlert:userInfo];
    NSLog(@"[2] User Info : %@", userInfo);
    completionHandler(UIBackgroundFetchResultNewData);
    [self handleRemoteNotification:[UIApplication sharedApplication] userInfo:userInfo];
}


//Called when a notification is delivered to a foreground app.
-(void)userNotificationCenter: (UNUserNotificationCenter *)center willPresentNotification: (UNNotification *)notification withCompletionHandler: (void (^)(UNNotificationPresentationOptions options)) completionHandler {
    NSLog(@"[3] User Info : %@", notification.request.content.userInfo);
    completionHandler(UNAuthorizationOptionSound | UNAuthorizationOptionAlert | UNAuthorizationOptionBadge);
    [self handleRemoteNotification: [UIApplication sharedApplication] userInfo: notification.request.content.userInfo];
}


//Called to let your app know which action was selected by the user for a given notification.
-(void)userNotificationCenter: (UNUserNotificationCenter *)center didReceiveNotificationResponse: (UNNotificationResponse *)response withCompletionHandler:(void(^)())completionHandler{
    NSLog(@"[4] User Info : %@", response.notification.request.content.userInfo);
    completionHandler();
    [self handleRemoteNotification: [UIApplication sharedApplication] userInfo: response.notification.request.content.userInfo];
}


-(void) handleRemoteNotification:(UIApplication *) application   userInfo:(NSDictionary *) remoteNotif {
    NSLog(@"handleRemoteNotification");
    NSLog(@"Handle Remote Notification Dictionary: %@", remoteNotif);

    // Handle Click of the Push Notification From Here…
    // You can write a code to redirect user to specific screen of the app here….
}


- (void)registerForRemoteNotifications {

}


- (void)application: (UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken: (NSData*)deviceToken {
}


- (void)messaging: (FIRMessaging *)messaging didReceiveRegistrationToken: (NSString *)fcmToken {
    // Note: This callback is fired at each app startup and whenever a new token is generated.

//    NSLog(@"FCM registration token: %@", fcmToken);
//    // Notify about received token.
//    NSDictionary *dataDict = [NSDictionary dictionaryWithObject:fcmToken forKey:@"token"];
//    [[NSNotificationCenter defaultCenter] postNotificationName:
//     @"FCMToken" object:nil userInfo:dataDict];
//
//    if (self.sdkInited) {
//        NSString *pushNotifConfig = [NSString stringWithFormat:@"{\"id\": \"%@\", \"value\":\"%@\"}", [[NSUUID UUID] UUIDString], fcmToken];
//        NSLog(@"3) Sending pushNotifConfig: %@", pushNotifConfig);
//        [self.sdkApi agentUpdateInfo:pushNotifConfig completion:^(NSError *error) {
//            if (error != nil && error.code != 0)
//            {
//                NSString *indyErrorCode = [NSString stringWithFormat:@"%ld", (long)error.code];
//                NSLog(@"3) Value of indyErrorCode is: %@", indyErrorCode);
//            } else {
//                NSLog(@"Updated the push notification token to: %@", fcmToken);
//            }
//        }];
//    }
//
//    NSLog(@"Remote instance ID token: %@", fcmToken);
//    [[NSUserDefaults standardUserDefaults]setObject:[NSString stringWithFormat:@"FCM:%@", fcmToken] forKey:@"DeviceTokenFinal"];
//    [[NSUserDefaults standardUserDefaults]synchronize];
}


- (BOOL)application: (UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {

    //  [FIRApp configure];
    //  [FIRMessaging messaging].delegate = self;
    [self registerForRemoteNotifications];

    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:[NSBundle mainBundle]];
    UIViewController *vc = [storyboard instantiateInitialViewController];

    // Set root view controller and make windows visible
    self.window = [[UIWindow alloc] initWithFrame:UIScreen.mainScreen.bounds];
    self.window.rootViewController = vc;
    [self.window makeKeyAndVisible];

    [CMConfig init];

    return YES;
}


- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
}


- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}


- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}


@end

