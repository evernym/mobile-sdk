//
//  CMConfig.h
//  MSDKSampleAppObjC
//
//  Created by Predrag Jevtic on 28/05/2020.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import "vcx/vcx.h"

#ifndef CMConfig_h

#define CMConfig_h
typedef enum {
    Sandbox,
    Production,
    Staging,
    DevTeam1,
} CMEnvironment;

#define levelMappings @{@"1": @"Error", @"2": @"Warning", @"3": @"Info", @"4": @"Debug", @"5": @"Trace"}

@interface CMConfig: NSObject

// MARK: - JSON Config
+(NSString*)updateJSONConfig:(NSString*)jsonConfig
                     withKey:(NSString*)key
                   withValue:(NSString*)value;
+(NSString*)updateJSONConfig:(NSString*)jsonConfig withValues:(NSString*)values;
+(NSString*)removeJSONConfig:(NSString*)jsonConfig
                    toRemove:(NSString*)values;

// MARK: - VCX Init
+(void)initVCX;

@end

#endif /* CMConfig_h */
