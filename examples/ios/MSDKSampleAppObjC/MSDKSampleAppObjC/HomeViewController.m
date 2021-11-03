//
//  HomeViewController.m
//  MSDKSampleAppObjC
//
//  Created by Norman Jarvis on 5/3/19.
//  Copyright © 2019 Evernym Inc. All rights reserved.
//
#import "CustomTableViewCell.h"
#import "HomeViewController.h"
#import "MobileSDK.h"
#import "Connection.h"
#import "Credential.h"
#import "Utilities.h"
#import "LocalStorage.h"
#import "QRCodeReaderViewController.h"
#import "Message.h"
#import "ProofRequest.h"

@interface HomeViewController ()

@property (weak, nonatomic) IBOutlet UILabel *infoLbl;
@property (nonatomic, readwrite, strong) IBOutlet UITableView *tableView;
@property (retain, nonatomic) IBOutlet UIButton *addConnectionButton;
@property (retain, nonatomic) IBOutlet UILabel *newConnLabel;

@property NSDictionary* requests;
@property BOOL isInitialized;

@end

@implementation HomeViewController
@synthesize addConnLabel, addConnConfigTextView, requests, newConnLabel, addConnectionButton;

UIGestureRecognizer *tapper;

- (void)viewDidLoad {
    // On iOS diveces you cann't use qr code scnner
    // But you can handle invite with text input and button on home page
    // For show input set this flag to false
    BOOL const isHideInput = true;
    
    [super viewDidLoad];
    self.tableView.dataSource = self;
    self.tableView.delegate = self;
    addConnConfigTextView.delegate = self;
    addConnConfigTextView.layer.cornerRadius = 5;
    [addConnConfigTextView setHidden: isHideInput];
    [newConnLabel setHidden: isHideInput];
    [addConnectionButton setHidden:isHideInput];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear: animated];
    requests = [LocalStorage getObjectForKey: @"requests" shouldCreate: false];
    [self.tableView reloadData];
    [[NSNotificationCenter defaultCenter] addObserver: self selector: @selector(vcxInitialized) name:@"vcxInitialized" object: nil];
    _isInitialized = [[MobileSDK shared] sdkInited];
}

- (void) viewWillDisappear:(BOOL)animated {
    [[NSNotificationCenter defaultCenter] removeObserver: self];
}

- (void)handleSingleTap:(UITapGestureRecognizer *) sender {
    [self.view endEditing: YES];
}

- (void)vcxInitialized {
    self.infoLbl.text = @"VCX initialized!";
    _isInitialized = true;
    double delayInSeconds = 10.0;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        self.infoLbl.text = @"";
    });
}

- (BOOL)textViewShouldBeginEditing:(UITextView *)textView {
    if([textView.text isEqual: @"enter code here"]) {
        addConnConfigTextView.text = @"";
    }
    return true;
}

- (IBAction)scanQR: (UIButton*) sender {
    if(!_isInitialized) {
        NSLog(@"Please wait for VCX to initialize!");
        return;
    }

    QRCodeReader *reader = [QRCodeReader readerWithMetadataObjectTypes:@[AVMetadataObjectTypeQRCode]];

    QRCodeReaderViewController *vc = [QRCodeReaderViewController readerWithCancelButtonTitle:@""
                                                                                  codeReader: reader startScanningAtLoad:YES showSwitchCameraButton:YES showTorchButton:YES];
    vc.modalPresentationStyle = UIModalPresentationFormSheet;
    vc.delegate = self;
    
    [self presentViewController: vc animated: YES completion:^{
        NSLog(@"QR code scanner presented");
    }];
    
    [reader setCompletionWithBlock:^(NSString *resultAsString) {
        NSLog(@"%@", resultAsString);
        [vc dismissViewControllerAnimated:YES completion:^{
            [self addNewConn: resultAsString];
        }];
    }];
}

- (void)newConnection:(NSString *) data
  withCompletionBlock:(ResponseWithBoolean) completionBlock {
    NSLog(@"newConnection %@", data);
    [Connection handleConnection:data
                    connectionType:QR
                       phoneNumber:@""
             withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(NO, error);
        }
        return completionBlock(YES, error);
    }];
}

- (void)answer:(NSString *) data
withCompletionBlock:(ResponseWithBoolean) completionBlock {
    NSDictionary *message = [Utilities jsonToDictionary:data];
    NSString *payload = [message objectForKey:@"payload"];
    NSDictionary *payloadDict = [Utilities jsonToDictionary:payload];
    NSArray *responses = [payloadDict objectForKey:@"valid_responses"];

    NSString *pwDidMes = [message objectForKey:@"pwDid"];
    NSString *questionsConnection = [Connection getConnectionByPwDid:pwDidMes];

    UIAlertController * alert = [UIAlertController
                                     alertControllerWithTitle:[payloadDict objectForKey:@"question_text"]
                                     message:[payloadDict objectForKey:@"question_detail"]
                                     preferredStyle:UIAlertControllerStyleAlert];
    
    for(NSInteger i = 0; i < responses.count; i++) {
        NSDictionary *response = responses[i];
        UIAlertAction* button = [UIAlertAction
                                actionWithTitle:[response objectForKey:@"text"]
                                style:UIAlertActionStyleDefault
                                handler:^(UIAlertAction * action) {
                                    [Message answerQuestion:questionsConnection
                                                 message:payload
                                                  answer:[Utilities dictToJsonString:response]
                                     withCompletionBlock:^(BOOL result, NSError *error) {
                                        [LocalStorage addEventToHistory:[NSString stringWithFormat:@"%@ - Answer question", [payloadDict objectForKey:@"question_text"]]];
                                        return completionBlock(result, error);
                                    }];
                                }];
        [alert addAction:button];
    }

    [self presentViewController:alert animated:YES completion:nil];
}

- (void)acceptCredential:(NSString *) data
     withCompletionBlock:(ResponseWithBoolean) completionBlock {
    [Credential acceptCredentilaFromMessage:data withCompletionBlock:^(BOOL result, NSError *error) {
        return completionBlock(result, error);
    }];
}

- (void)rejectCredential:(NSString *) data
     withCompletionBlock:(ResponseWithBoolean) completionBlock {
    [Credential rejectCredentilaFromMessage:data
                          withCompletionBlock:^(BOOL result, NSError *error) {
        return completionBlock(result, error);
    }];
}

- (void)sendProof:(NSString *) data
withCompletionBlock:(ResponseWithBoolean) completionBlock {
    [ProofRequest sendProofRequestFromMessage:data
                          withCompletionHandler:^(BOOL result, NSError *error) {
        return completionBlock(result, error);
    }];
}

- (void)rejectProof:(NSString *) data
withCompletionBlock:(ResponseWithBoolean) completionBlock {
    [ProofRequest rejectProofRequestFromMessage:data
                          withCompletionHandler:^(BOOL result, NSError *error) {
        return completionBlock(result, error);
    }];
}

- (void)addNewConn:(NSString *) data {
    NSDictionary *connectValues = [Connection parsedInvite: data];
    NSString *label = [connectValues objectForKey: @"label"];
    NSString *goal = @"";
    if ([connectValues valueForKey:@"goal"] != nil) {
        goal = [connectValues objectForKey: @"goal"];
    } else {
        goal = @"New connection";
    }
    NSString *profileUrl = [connectValues objectForKey: @"profileUrl"];
    
    NSMutableDictionary* requestsDict = [[LocalStorage getObjectForKey: @"requests" shouldCreate: true] mutableCopy];
    NSString *uuid = [[NSUUID UUID] UUIDString];

    NSDictionary* requestObj = @{
        @"name": label,
        @"profileUrl": profileUrl,
        @"goal": goal,
        @"uuid": uuid,
        @"type": @"null",
        @"data": [Utilities dictToJsonString:connectValues]
    };
    [requestsDict setValue: requestObj forKey: uuid];
    [LocalStorage store: @"requests" andObject: requestsDict];
    
    requests = requestsDict;
    [self.tableView reloadData];
}

- (IBAction)addNewConnBtnClick: (id)sender {
    if(addConnConfigTextView.text.length > 3 && ![addConnConfigTextView.text isEqual: @"enter code here"]) {
        NSDictionary *connectValues = [Connection parsedInvite: addConnConfigTextView.text];
        NSString *label = [connectValues objectForKey: @"label"];
        NSString *goal = @"";
        if ([connectValues valueForKey:@"goal"] != nil) {
            goal = [connectValues objectForKey: @"goal"];
        } else {
            goal = @"New connection";
        }
        NSString *profileUrl = [connectValues objectForKey: @"profileUrl"];
        
        NSMutableDictionary* requestsDict = [[LocalStorage getObjectForKey: @"requests" shouldCreate: true] mutableCopy];
        NSString *uuid = [[NSUUID UUID] UUIDString];

        NSDictionary* requestObj = @{
            @"name": label,
            @"profileUrl": profileUrl,
            @"goal": goal,
            @"uuid": uuid,
            @"type": @"null",
            @"data": [Utilities dictToJsonString:connectValues]
        };
        [requestsDict setValue: requestObj forKey: uuid];
        [LocalStorage store: @"requests" andObject: requestsDict];
        
        requests = requestsDict;
        [self.tableView reloadData];
    }
}

- (IBAction)checkMessages:(id)sender {
    [Message downloadAllMessages:^(NSArray *responseArray, NSError *error) {
        NSLog(@"downloadAllMessages %@", responseArray);
        for (NSInteger i = 0; i < responseArray.count; i++) {
            NSDictionary *message = responseArray[i];

            NSString *type = [message objectForKey:@"type"];
            NSLog(@"typetypetype %@", type);
            if ([type isEqual:@"credential-offer"]) {
                NSString *payload = [message objectForKey:@"payload"];
                NSArray *payloadArr = [Utilities jsonToArray:payload];
                NSDictionary *payloadDict = payloadArr[0];
                
                NSMutableDictionary* requestsDict = [[LocalStorage getObjectForKey: @"requests" shouldCreate: true] mutableCopy];
                NSString *uuid = [[NSUUID UUID] UUIDString];

                NSDictionary* requestObj = @{
                    @"name": [payloadDict objectForKey:@"claim_name"],
                    @"goal": @"Credential offer",
                    @"uuid": [message objectForKey:@"uid"],
                    @"type": type,
                    @"data": [Utilities dictToJsonString:message]
                };
                [requestsDict setValue: requestObj forKey: uuid];
                [LocalStorage store: @"requests" andObject: requestsDict];

                dispatch_async(dispatch_get_main_queue(), ^{
                    [Message updateMessageStatus:[message objectForKey:@"pwDid"]
                                         messageId:[message objectForKey:@"uid"]
                               withCompletionBlock:^(BOOL result, NSError *error) {
                        if (error) {
                            NSLog(@"%@", error);
                        }
                    }];
                });

                dispatch_async(dispatch_get_main_queue(), ^{
                    self.requests = requestsDict;
                    [self.tableView reloadData];
                });
            }
            if ([type isEqual:@"committed-question"]) {
                NSString *payload = [message objectForKey:@"payload"];
                NSDictionary *payloadDict = [Utilities jsonToDictionary:payload];
                
                NSLog(@"payloadDict oofer %@", payloadDict);
                
                NSMutableDictionary* requestsDict = [[LocalStorage getObjectForKey: @"requests" shouldCreate: true] mutableCopy];
                NSString *uuid = [[NSUUID UUID] UUIDString];

                NSDictionary* requestObj = @{
                    @"name": [payloadDict objectForKey:@"question_text"],
                    @"goal": @"Question",
                    @"uuid": [message objectForKey:@"uid"],
                    @"type": type,
                    @"data": [Utilities dictToJsonString:message]
                };
                [requestsDict setValue: requestObj forKey: uuid];
                [LocalStorage store: @"requests" andObject: requestsDict];

                dispatch_async(dispatch_get_main_queue(), ^{
                    [Message updateMessageStatus:[message objectForKey:@"pwDid"]
                                         messageId:[message objectForKey:@"uid"]
                               withCompletionBlock:^(BOOL result, NSError *error) {
                        if (error) {
                            NSLog(@"%@", error);
                        }
                    }];
                });

                dispatch_async(dispatch_get_main_queue(), ^{
                    self.requests = requestsDict;
                    [self.tableView reloadData];
                });
            }
            if ([type isEqual:@"presentation-request"]) {
                NSDictionary *payload = [Utilities jsonToDictionary:[message objectForKey:@"payload"]];

                NSLog(@"payloadDict oofer %@", payload);
                
                NSMutableDictionary* requestsDict = [[LocalStorage getObjectForKey: @"requests" shouldCreate: true] mutableCopy];
                NSString *uuid = [[NSUUID UUID] UUIDString];

                NSDictionary* requestObj = @{
                    @"name": [payload objectForKey:@"comment"],
                    @"goal": @"Proof Request",
                    @"uuid": [message objectForKey:@"uid"],
                    @"type": type,
                    @"data": [Utilities dictToJsonString:message]
                };
                [requestsDict setValue: requestObj forKey: uuid];
                [LocalStorage store: @"requests" andObject: requestsDict];

                dispatch_async(dispatch_get_main_queue(), ^{
                    [Message updateMessageStatus:[message objectForKey:@"pwDid"]
                                         messageId:[message objectForKey:@"uid"]
                               withCompletionBlock:^(BOOL result, NSError *error) {
                        if (error) {
                            NSLog(@"%@", error);
                        }
                    }];
                });

                dispatch_async(dispatch_get_main_queue(), ^{
                    self.requests = requestsDict;
                    [self.tableView reloadData];
                });
            }
        }
    }];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [requests.allKeys count];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
   return 110;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    CustomTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cell" forIndexPath:indexPath];
    if (cell == nil) {
        cell = [[CustomTableViewCell alloc] init];
    }
    
    NSDictionary *requestDict = requests[requests.allKeys[indexPath.row]];
    NSString *type = [requestDict objectForKey: @"type"];
    NSString *name = [requestDict objectForKey: @"name"];
    NSString *goal = [requestDict objectForKey: @"goal"];
    NSString *uuid = [requestDict objectForKey: @"uuid"];
    NSString *data = [requestDict objectForKey: @"data"];
    NSLog(@"typetype %@", requestDict);
    
    if ([type isEqual:@"null"]) {
        NSString *logoUrl = [requestDict objectForKey: @"profileUrl"];
        [cell updateAttribute:name
                     subtitle:goal
                      logoUrl:logoUrl
               acceptCallback:^() {
                        [self newConnection:data
                        withCompletionBlock:^(BOOL result, NSError *error) {
                            if (result) {
                                [self switchActionToHistoryView: uuid];
                            }
                        }];
                    }
                rejectCallback:^() {
                        [self switchActionToHistoryView: uuid];
                        [self addRejectAction: name];
                }
         ];
        [cell.accept setTitle:@"Accept" forState:UIControlStateNormal];
    } else if ([type isEqual:@"credential-offer"]) {
        [cell updateAttribute:name
                     subtitle:goal
                      logoUrl:@""
               acceptCallback:^() {
                         [self acceptCredential:data
                            withCompletionBlock:^(BOOL result, NSError *error) {
                            if (result) {
                                [self switchActionToHistoryView: uuid];
                            };
                        }];
                    }
               rejectCallback:^() {
                        [self rejectCredential:data
                            withCompletionBlock:^(BOOL result, NSError *error) {
                            if (result) {
                                [self switchActionToHistoryView: uuid];
                            };
                        }];
                    }
         ];
        [cell.accept setTitle:@"Accept" forState:UIControlStateNormal];
        [cell.reject setHidden:NO];
    } else if ([type isEqual:@"committed-question"]) {
        [cell updateAttribute:name
                     subtitle:goal
                      logoUrl:@""
               acceptCallback:^() {
                        [self answer:data
                 withCompletionBlock:^(BOOL result, NSError *error) {
                            if (result) {
                                [self switchActionToHistoryView: uuid];
                            };
                        }];
                    }
               rejectCallback:^() {
                        [self switchActionToHistoryView: uuid];
               }
         ];
        [cell.accept setTitle:@"Answer" forState:UIControlStateNormal];
    } else if ([type isEqual:@"presentation-request"]) {
        [cell updateAttribute:name
                     subtitle:goal
                      logoUrl:@""
               acceptCallback:^() {
                        [self sendProof:data
                 withCompletionBlock:^(BOOL result, NSError *error) {
                            if (result) {
                                [self switchActionToHistoryView: uuid];
                            };
                        }];
                    }
               rejectCallback:^() {
                    [self rejectProof:data
             withCompletionBlock:^(BOOL result, NSError *error) {
                        if (result) {
                            [self switchActionToHistoryView: uuid];
                        };
                    }];
                }
         ];
        [cell.accept setTitle:@"Accept" forState:UIControlStateNormal];
    }
    return cell;
}

- (void) switchActionToHistoryView:(NSString *) uuid {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSMutableDictionary* processedDict = nil;
        NSDictionary* req = [LocalStorage getObjectForKey: @"requests" shouldCreate: false];
        for (NSInteger i = 0; i < req.allKeys.count; i++) {
            NSString *key = req.allKeys[i];
            if ([key isEqual: uuid]) {
                [processedDict removeObjectForKey:key];
            }
        }
        [LocalStorage store: @"requests" andObject: processedDict];
        self.requests = processedDict;
        [self.tableView reloadData];
    });
}

- (void) addRejectAction:(NSString *) name {
    [LocalStorage addEventToHistory: [NSString stringWithFormat:@"%@ - Rejected", name]];
    [self.tableView reloadData];
}

@end
     
