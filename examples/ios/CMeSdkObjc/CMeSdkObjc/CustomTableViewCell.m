//
//  CustomTableViewCell.m
//  CMeSdkObjc
//
//  Created by Evernym on 01.07.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import "CustomTableViewCell.h"
typedef void (^ActionBlock)(void);

@interface CustomTableViewCell ()

@property ActionBlock _acceptBlock;
@property ActionBlock _rejectBlock;

@end

@implementation CustomTableViewCell
@synthesize _acceptBlock, _rejectBlock;

- (void)awakeFromNib {
    [super awakeFromNib];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];
}

-(void) callAcceptBlock:(id)sender {
    _acceptBlock();
}

-(void) callRejectBlock:(id)sender {
    _rejectBlock();
}

- (void)updateAttribute: (NSString*) title
               subtitle: (NSString*) subtitle
                logoUrl: (NSString*) logoUrl
                 acceptCallback: (ActionBlock) acceptCallback
         rejectCallback: (ActionBlock) rejectCallback {
    self.title.text = title;
    self.subtitle.text = subtitle;
    
    NSURL *url = [NSURL URLWithString:logoUrl];
    NSData *data = [[NSData alloc] initWithContentsOfURL:url];
    UIImage *image = [UIImage imageWithData:data];
    self.logo.image = image;
    
    _acceptBlock = acceptCallback;
    [self.accept addTarget:self action:@selector(callAcceptBlock:) forControlEvents:UIControlEventTouchUpInside];
    [self.accept showsTouchWhenHighlighted];

    _rejectBlock = rejectCallback;
    [self.reject addTarget:self action:@selector(callRejectBlock:) forControlEvents:UIControlEventTouchUpInside];
    [self.reject showsTouchWhenHighlighted];
}

@end
