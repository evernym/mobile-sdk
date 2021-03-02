//
//  CMCredential.h
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/11/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#ifndef CMCredential_h
#define CMCredential_h

#import "CMUtilities.h"

@interface CMCredential: NSObject

+ (void)acceptCredOffer: (NSDictionary*) messageObj forConnection: (NSDictionary*) connection withCompletionHandler: (ResponseBlock) completionBlock;

@end

#endif /* CMCredential_h */
