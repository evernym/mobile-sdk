//
//  ProofAttributeTableViewCell.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/17/20.
//  Copyright © 2020 Norman Jarvis. All rights reserved.
//

#import "ProofAttributeTableViewCell.h"

@implementation ProofAttributeTableViewCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

-(void) updateAttribute: (NSString*) label fieldName: (NSString*) fieldName andValue: (id) value {
    self.attributeKeyLbl.text = label;
    self.attributeValueTextField.text = value;
    self.attributeValueTextField.userInteractionEnabled = false;
}

@end
