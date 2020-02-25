//
//  ViewController.h
//  CMeSdkObjc
//
//  Created by Norman Jarvis on 5/3/19.
//  Copyright © 2019 Norman Jarvis. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ViewController : UIViewController
@property (weak, nonatomic) IBOutlet UILabel *addConnLabel;
@property (weak, nonatomic) IBOutlet UITextView *addConnConfig;

- (IBAction)addNewConn:(id)sender;


@end

