//
//  ViewController.h
//  CMeSdkObjc
//
//  Created by Norman Jarvis on 5/3/19.
//  Copyright © 2019 Evernym Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController<UITextViewDelegate, UITableViewDelegate, UITableViewDataSource>
@property (weak, nonatomic) IBOutlet UILabel *addConnLabel;
@property (weak, nonatomic) IBOutlet UITextView *addConnConfigTextView;

- (IBAction)addNewConn:(id)sender;

@end


