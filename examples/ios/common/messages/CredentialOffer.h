//
//  CredentialOffer.h
//  MSDKSampleAppObjC
//
//  Created by Evernym on 07.11.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#ifndef CredentialOffer_h
#define CredentialOffer_h

@interface CredentialOffer : NSObject

+(NSString *)getThid:(NSDictionary *) credential;

+(NSString *)getAttributes:(NSDictionary *) credential;

+(NSString *)getOfferName:(NSDictionary *) credential;

@end

#endif /* CredentialOffer_h */
