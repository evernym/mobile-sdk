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

+(NSString *)getThid:(NSString *) credential
         fromMessage:(BOOL) fromMessage;

+(NSString *)getAttributes:(NSString *) credential
               fromMessage:(BOOL) fromMessage;

+(NSString *)getOfferName:(NSString *) credential
              fromMessage:(BOOL) fromMessage;

@end

#endif /* CredentialOffer_h */
