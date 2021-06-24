//
//  ConnectionDetailsViewController.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/11/20.
//  Copyright © 2020 Evernym Inc. All rights reserved.
//

#import "ConnectionDetailsViewController.h"
#import "CMMessage.h"
#import "MessageTableViewCell.h"
#import "CMCredential.h"
#import "CMConnection.h"
#import "ProofDetailsViewController.h"

@interface ConnectionDetailsViewController ()

@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property NSDictionary* sender;
@property NSArray* messages;
@end

@implementation ConnectionDetailsViewController

@synthesize connection;
@synthesize sender;

- (void)viewDidLoad {
    [super viewDidLoad];

    self.title = [CMConnection connectionName: connection];
}

- (IBAction)checkForMessages: (id)sender {
    // NOTE: same method will be used when receiving push notification
    // Only difference is:
    // in push notification, we will also receive messageID, which will be used for downloading details for that specific messageID
    // here (without push notification) we will download all messages for given connection

    for (int i = 0; i <= 6; i += 1) {
        [CMMessage downloadMessages: connection andType: i andMessageID: nil withCompletionHandler: ^(NSArray *messages, NSError *error) {
            NSLog(@"Received Messages: %@ for type %i",  messages, i);
            if([messages count] < 1){
                return;
            }
            NSPredicate* predicate =  [NSPredicate predicateWithFormat: @"NOT (type IN %@)", @[@"connReqAnswer", @"connReq", @"ACCEPT_CONN_REQ"]];
            dispatch_async(dispatch_get_main_queue(), ^{
                self.messages = [messages filteredArrayUsingPredicate: predicate];
                [self.tableView reloadData];
            });
        }];
    }
}

- (NSInteger)tableView: (UITableView *)tableView numberOfRowsInSection: (NSInteger)section {
    return [self.messages count];
}

- (UITableViewCell *)tableView: (UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    MessageTableViewCell* cell = [tableView dequeueReusableCellWithIdentifier: @"messageCell" forIndexPath: indexPath];

    NSLog(@"cell %@", _messages[indexPath.row]);
    [cell updateCell: _messages[indexPath.row] withSender: sender];
    return cell;
}

- (void)tableView: (UITableView *)tableView didSelectRowAtIndexPath: (NSIndexPath *)indexPath {
    NSDictionary* message = _messages[indexPath.row];
    if(!message) {
        return;
    }
    NSLog(@"message is %@", message);

    [self openMessageControllerForType: message[@"type"] withMessage: message];
}

- (void)openMessageControllerForType: (NSString*)messageType withMessage: (NSDictionary*)message {
    if([messageType isEqual: @"proofReq"]) {
        NSMutableDictionary* proofObj = [message mutableCopy];
        proofObj[@"payload"] = [CMUtilities jsonToDictionary: message[@"decryptedPayload"]];
        [self performSegueWithIdentifier: @"openProofDetails" sender: proofObj];
        return;
    }

    if([messageType isEqual: @"credOffer"]) {
        NSLog(@"credOffer");
        [CMCredential acceptCredOfferWithMsgid: message
                        forConnection: connection
                withCompletionHandler: ^(NSString *credentialDetails, NSError *error) {
            NSLog(@"credential offer results %@ %@", credentialDetails, error.localizedDescription);
            if (error && error.code > 0) {
                [CMUtilities printError: error];
                return;
            }
            dispatch_async(dispatch_get_main_queue(), ^{
//                [self goBack];
            });
        }];
        return;
    }

    if([messageType isEqual: @"cred"]) {
        NSLog(@"Credential");
        return;
    }

    if([messageType isEqual: @"Question"]) {
        NSLog(@"Question");
        return;
    }
}

-(void)goBack {
    [self.navigationController popViewControllerAnimated: true];
}

- (void)prepareForSegue: (UIStoryboardSegue *)segue sender: (id)sender {
    if([segue.identifier isEqual: @"openProofDetails"]) {
        ProofDetailsViewController* proofVC = [segue destinationViewController];
        proofVC.proof = sender;
        proofVC.connection = self.connection;
    }
}

@end

