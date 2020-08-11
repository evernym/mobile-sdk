//
//  ProofAttributeTableViewCell.h
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/17/20.
//  Copyright © 2020 Norman Jarvis. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ProofAttributeTableViewCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UITextField *attributeValueTextField;
@property (weak, nonatomic) IBOutlet UILabel *attributeKeyLbl;

-(void) updateAttribute: (NSString*) label fieldName: (NSString*) fieldName andValue: (id) value;
@end

NS_ASSUME_NONNULL_END
