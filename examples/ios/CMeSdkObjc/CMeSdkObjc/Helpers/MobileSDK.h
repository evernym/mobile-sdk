//
//  MobileSDK.h
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 02/02/2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "vcx/vcx.h"

NS_ASSUME_NONNULL_BEGIN

@interface MobileSDK : NSObject

@property (nonatomic, strong) ConnectMeVcx* sdkApi;
@property (nonatomic) BOOL sdkInited;

+ (instancetype)shared;

@end

NS_ASSUME_NONNULL_END
