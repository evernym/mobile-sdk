//
//  CustomTableViewCell.h
//  MSDKSampleAppObjC
//
//  Created by Evernym on 01.07.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
typedef void (^ActionBlock)(void);

@interface CustomTableViewCell : UITableViewCell

-(void) updateAttribute: (NSString*) title
               subtitle: (NSString*) subtitle
                logoUrl: (NSString*) logoUrl
         acceptCallback: (ActionBlock) acceptCallback
         rejectCallback: (ActionBlock) rejectCallback;

@property (weak, nonatomic) IBOutlet UIImageView *logo;
@property (weak, nonatomic) IBOutlet UILabel *title;
@property (weak, nonatomic) IBOutlet UILabel *subtitle;

@property (weak, nonatomic) IBOutlet UIButton *accept;
@property (weak, nonatomic) IBOutlet UIButton *reject;

@end

NS_ASSUME_NONNULL_END
