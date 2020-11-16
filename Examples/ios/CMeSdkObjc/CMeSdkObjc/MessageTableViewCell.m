//
//  MessageTableViewCell.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/11/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import "MessageTableViewCell.h"
#import "CMUtilities.h"

@implementation MessageTableViewCell

@synthesize message, messageObjects;

- (void)awakeFromNib {
    [super awakeFromNib];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];
    
    // Configure the view for the selected state
}

- (void) updateCell: (NSDictionary*) message withSender: (NSDictionary*) sender {
    self.message = message;
    NSDictionary* messageData = [CMUtilities jsonToDictionary: message[@"decryptedPayload"]];
    messageObjects = [CMUtilities jsonToArray: messageData[@"@msg"]];
    NSLog(@"message data %@", messageObjects);
    
    [self loadImage: sender];

    NSString* messageType = message[@"type"];

    if([@[@"credOffer", @"cred"] containsObject: messageType]) {
        [self populateCredentialCell];
        return;
    }
    if([messageType isEqual: @"aries"]) {
        NSDictionary* messageObj = [CMUtilities jsonToDictionary: messageData[@"@msg"]];
        NSData* sigData = [CMUtilities decode64String: messageObj[@"connection~sig"][@"sig_data"]];
        NSString* signature = [[NSString alloc] initWithData: sigData encoding: NSASCIIStringEncoding];
        self.typeLbl.text = @"Credential Request";
        NSLog(@"aries %@", signature);
    }
    if([messageType isEqual: @"credReq"]) {
//        self.titleLbl.text = [NSString stringWithFormat:@"Values: %lu", [values count]];
        NSLog(@"credenialReq %@", message);
        self.typeLbl.text = @"Credential Request";
        return;
    }
    
    if([messageType isEqual: @"proofReq"]) {
        NSDictionary* data = [CMUtilities jsonToDictionary: messageData[@"@msg"]];
        self.titleLbl.text = data[@"name"];
        self.typeLbl.text = @"Proof request - Press to populate";
        self.logoImageView.hidden = true;
        self.logoWidthConstraint.constant = 0;
        return;
    }
}

-(void)populateCredentialCell {
    NSDictionary* credentialData = [CMUtilities jsonToDictionary: [CMUtilities jsonToDictionary: message[@"decryptedPayload"]][@"@msg"]];
    NSDictionary* values = [CMUtilities jsonToDictionary: credentialData[@"libindy_cred"]][@"values"];
    self.titleLbl.text = [NSString stringWithFormat:@"Values: %lu", (unsigned long)[values count]];
    self.typeLbl.text = @"Credential";
}

- (void)loadImage: (NSDictionary*) sender {
    NSString* logo = sender[@"logoUrl"];
       self.logoImageView.hidden = logo == nil;
       self.logoWidthConstraint.constant = logo == nil ? 0 : 60;
       if(logo) {
           _logoImageView.layer.cornerRadius = 30.0;
           dispatch_async(dispatch_get_global_queue(0,0), ^{
               NSData * data = [[NSData alloc] initWithContentsOfURL: [NSURL URLWithString: logo]];
               if (data == nil) {
                   return;
               }
               dispatch_async(dispatch_get_main_queue(), ^{
                   // WARNING: is the cell still using the same data by this point??
                   self.logoImageView.image = [UIImage imageWithData: data];
               });
           });
       }
}

@end
