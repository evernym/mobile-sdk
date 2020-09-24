//
//  AppDelegate.h
//  CMeSdkObjc
//
//  Created by Norman Jarvis on 5/3/19.
//  Copyright © 2019 Evernym Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "vcx/vcx.h"
#import <UserNotifications/UserNotifications.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate, UNUserNotificationCenterDelegate>

@property (strong, nonatomic) UIWindow *window;
@property (strong, nonatomic) ConnectMeVcx *sdkApi;
@property (nonatomic) BOOL sdkInited;

@end


