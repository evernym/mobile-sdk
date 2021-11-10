//
//  Proof.h
//  MSDKSampleAppObjC
//
//  Created by Evernym on 10.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#ifndef Proof_h
#define Proof_h

@interface Proof : NSObject

+(NSString *)getThid:(NSDictionary *) request;
+(NSString *)getAttributes:(NSDictionary *) request;
+(NSString *)getPredicates:(NSDictionary *) request;

@end

#endif /* Proof_h */
