//
//  HomeViewController.h
//  MSDKSampleAppObjC
//
//  Created by Norman Jarvis on 5/3/19.
//  Copyright © 2019 Evernym Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface HomeViewController : UIViewController<UITextViewDelegate, UITableViewDelegate, UITableViewDataSource>
@property (weak, nonatomic) IBOutlet UILabel *addConnLabel;
@property (weak, nonatomic) IBOutlet UITextView *addConnConfigTextView;

- (IBAction)addNewConnBtnClick:(id)sender;
- (IBAction)scanQR: (UIButton*) sender;
- (IBAction)checkMessages: (UIButton*) sender;

@end
