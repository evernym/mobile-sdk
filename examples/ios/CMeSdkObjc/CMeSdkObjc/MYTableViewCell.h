//
//  MYTableViewCell.h
//  CMeSdkObjc
//
//  Created by Evernym on 30.06.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MYTableViewCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UIButton *accept;
@property (weak, nonatomic) IBOutlet UIButton *reject;
@property (weak, nonatomic) IBOutlet UIButton *answer;

@property (weak, nonatomic) IBOutlet UILabel *title;
@property (weak, nonatomic) IBOutlet UILabel *subtitle;

@property (weak, nonatomic) IBOutlet UIImageView *logo;
@end
