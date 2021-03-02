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
#import "MobileSDK.h"
#import "LocalStorage.h"
#import "CMMessage.h"

#define SYSTEM_VERSION_GRATERTHAN_OR_EQUALTO(v) ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedAscending)

@interface AppDelegate ()
@end

@implementation AppDelegate

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

// Called when a notification is delivered to a foreground app.
-(void)userNotificationCenter: (UNUserNotificationCenter *)center willPresentNotification: (UNNotification *)notification withCompletionHandler: (void (^)(UNNotificationPresentationOptions options)) completionHandler  API_AVAILABLE(ios(10.0)){
    NSLog(@"[3] User Info : %@", notification.request.content.userInfo);
    if (@available(iOS 10.0, *)) {
        completionHandler(UNAuthorizationOptionSound | UNAuthorizationOptionAlert | UNAuthorizationOptionBadge);
    } else {
        // Fallback on earlier versions
    }
    [self handleRemoteNotification: [UIApplication sharedApplication] userInfo: notification.request.content.userInfo];
}

//Called to let your app know which action was selected by the user for a given notification.
-(void)userNotificationCenter: (UNUserNotificationCenter *)center didReceiveNotificationResponse: (UNNotificationResponse *)response withCompletionHandler: (void(^)(void))completionHandler API_AVAILABLE(ios(10.0)) {
    NSLog(@"[4] User Info: %@", response.notification.request.content.userInfo);
    completionHandler();
    [self handleRemoteNotification: [UIApplication sharedApplication] userInfo: response.notification.request.content.userInfo];
}

-(void) handleRemoteNotification:(UIApplication *) application userInfo:(NSDictionary *) remoteNotif {
    NSLog(@"handleRemoteNotification");
    NSLog(@"Handle Remote Notification Dictionary: %@", remoteNotif);

    // TODO: Extract messageID and public_did from push notification
    NSString* messageID = @"";
    NSString* connectionID = @"";

    NSDictionary* connections = [LocalStorage getObjectForKey: @"connections" shouldCreate: NO];
    NSArray* connectionIDs =  [connections allKeys];

    if(![connectionIDs containsObject: connectionID]) {
        return;
    }

    NSDictionary* connection = [connections objectForKey: connectionID];

    [CMMessage downloadMessages: connection andType: 2 andMessageID: messageID withCompletionHandler:^(NSArray *messageList, NSError *error) {
        NSLog(@"messageList %@ %@", messageList, error);
        // TODO: Handle message furhter
    }];
}

- (void)messaging: (FIRMessaging *)messaging didReceiveRegistrationToken: (NSString *)fcmToken {
    // Note: This callback is fired at each app startup and whenever a new token is generated.

    NSLog(@"FCM registration token: %@", fcmToken);
    // TODO: Upload push notification token to sponsor server
    [LocalStorage setValue: fcmToken forKey:@"pushToken"];
}


- (BOOL)application: (UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {

    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:[NSBundle mainBundle]];
    UIViewController *vc = [storyboard instantiateInitialViewController];

    // Set root view controller and make windows visible
    self.window = [[UIWindow alloc] initWithFrame:UIScreen.mainScreen.bounds];
    self.window.rootViewController = vc;
    [self.window makeKeyAndVisible];

    if ([FIRApp defaultApp] == nil) {
        [FIRApp configure];
        [FIRMessaging messaging].delegate = self;
    }

    [[MobileSDK shared] setSdkApi: [[ConnectMeVcx alloc] init]];
    [CMConfig initVCX];

    return YES;
}

@end

