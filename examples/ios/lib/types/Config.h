//
//  Config.h
//  MSDKSampleAppObjC
//
//  Created by Evernym on 03.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#ifndef Config_h
#define Config_h

@interface Config : NSObject

+(NSString*) getWalletName;
+(NSString*) getAgencyConfig;
+(NSString*) getWalletKey;
+(NSString*) getGenesisFilePath;
+(NSString*) getPoolConfig;
+(NSString*) getSponsorServerURL;

@end


#endif /* Config_h */
