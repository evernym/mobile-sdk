//
//  AppDelegate.h
//  CMeSdkObjc
//
//  Created by Norman Jarvis on 5/3/19.
//  Copyright © 2019 Evernym Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "vcx/vcx.h"
@import Firebase;
#import <UserNotifications/UserNotifications.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate, UNUserNotificationCenterDelegate, FIRMessagingDelegate>

@property (strong, nonatomic) UIWindow *window;

@end


