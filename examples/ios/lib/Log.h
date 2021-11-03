//
//  Log.h
//  MSDKSampleAppObjC
//
//  Created by Evernym on 06.07.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//
//

#define NSLog(args...) _Log(@"DEBUG ", __FILE__,__LINE__,__PRETTY_FUNCTION__,args);
@interface Log : NSObject
void _Log(NSString *prefix, const char *file, int lineNumber, const char *funcName, NSString *format,...);
@end
