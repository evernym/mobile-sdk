//
//  ProofDetailsViewController.h
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/17/20.
//  Copyright © 2020 Norman Jarvis. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ProofDetailsViewController : UIViewController

@property NSDictionary* proof;
@property NSDictionary* connection;
@property NSDictionary* proofAttributes;
@property NSDictionary* decriptedProof;
@property NSArray* proofFields;

@end

NS_ASSUME_NONNULL_END
