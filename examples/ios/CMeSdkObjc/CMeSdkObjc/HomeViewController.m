//
//  HomeViewController.m
//  CMeSdkObjc
//
//  Created by Norman Jarvis on 5/3/19.
//  Copyright © 2019 Evernym Inc. All rights reserved.
//

#import "HomeViewController.h"
#import "MobileSDK.h"
#import "CMConnection.h"
#import "CMCredential.h"
#import "CMUtilities.h"
#import "ConnectionDetailsViewController.h"
#import "LocalStorage.h"
#import "QRCodeReaderViewController.h"
#import "MGSwipeTableCell.h"
#import "CMMessage.h"
#import "CMProofRequest.h"

@interface HomeViewController ()

@property (weak, nonatomic) IBOutlet UIButton *addConnectionBtn;
@property (weak, nonatomic) IBOutlet UILabel *infoLbl;
@property (nonatomic, readwrite, strong) IBOutlet UITableView *tableView;

@property NSDictionary* requests;
@property BOOL isInitialized;

@end

@implementation HomeViewController
@synthesize addConnLabel, addConnConfigTextView, addConnectionBtn, requests;

UIGestureRecognizer *tapper;

- (void)viewDidLoad {
    [super viewDidLoad];
    self.tableView.dataSource = self;
    self.tableView.delegate = self;
    addConnConfigTextView.delegate = self;
    addConnConfigTextView.layer.cornerRadius = 5;
    addConnectionBtn.layer.cornerRadius = 5;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear: animated];
    requests = [LocalStorage getObjectForKey: @"requests" shouldCreate: false];
    [self.tableView reloadData];
    [[NSNotificationCenter defaultCenter] addObserver: self selector: @selector(vcxInitialized) name:@"vcxInitialized" object: nil];
    _isInitialized = [[MobileSDK shared] sdkInited];
    addConnectionBtn.enabled = [[MobileSDK shared] sdkInited];
}

- (void) viewWillDisappear:(BOOL)animated {
    [[NSNotificationCenter defaultCenter] removeObserver: self];
}

- (void)handleSingleTap:(UITapGestureRecognizer *) sender {
    [self.view endEditing: YES];
}

- (void)newConnection:(ResponseWithBoolean) completionBlock {
    NSDictionary* connectValues = [[LocalStorage getObjectForKey: @"connectValues" shouldCreate: true] mutableCopy];
    NSLog(@"addNewConn %@", connectValues);

    [CMConnection handleConnection:[CMUtilities dictToJsonString:connectValues]
                    connectionType:QR
                       phoneNumber:@""
             withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(NO, error);
        }
        [LocalStorage deleteObjectForKey:@"connectValues"];
        
        return completionBlock(YES, error);
    }];
}

- (void)acceptCredential:(ResponseWithBoolean) completionBlock {
    NSDictionary *message = [[LocalStorage getObjectForKey: @"message" shouldCreate: true] mutableCopy];
    NSString *pwDidMes = [message objectForKey:@"pwDid"];
    NSString *payload = [message objectForKey:@"payload"];
    
    NSDictionary* connections = [[LocalStorage getObjectForKey: @"connections" shouldCreate: true] mutableCopy];
    NSString *offerConnection = @"";
    for (NSInteger i = 0; i < connections.allKeys.count; i++) {
        NSString *key = connections.allKeys[i];
        NSDictionary *connection = [connections objectForKey:key];
        NSString *serializedConnection = [connection objectForKey:@"serializedConnection"];
        NSString *pwDid = [CMConnection getPwDid:serializedConnection];
        if ([pwDidMes isEqual:pwDid]) {
            offerConnection = serializedConnection;
            break;
        }
    }
    [CMCredential createWithOffer:payload
            withCompletionHandler:^(NSDictionary *serOffer, NSError *error) {
        if (error && error.code > 0) {
            NSLog(@"offer error %@", error);

            return completionBlock(nil, error);
        }
        NSLog(@"offer created %@", serOffer);

        [CMCredential acceptCredentialOffer:offerConnection
                       serializedCredential:[CMUtilities dictToJsonString:serOffer]
                      withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
            if (error && error.code > 0) {
                NSLog(@"offer accept error %@", error);

                return completionBlock(nil, error);
            }
            NSLog(@"Accept credential offer success");
            return completionBlock(YES, error);
        }];
    }];
}

- (void)sendProof:(ResponseWithBoolean) completionBlock {
    NSDictionary *message = [[LocalStorage getObjectForKey: @"message" shouldCreate: true] mutableCopy];
    NSString *pwDidMes = [message objectForKey:@"pwDid"];
    NSString *payload = [message objectForKey:@"payload"];
    
    NSDictionary* connections = [[LocalStorage getObjectForKey: @"connections" shouldCreate: true] mutableCopy];
    NSString *offerConnection = @"";
    for (NSInteger i = 0; i < connections.allKeys.count; i++) {
        NSString *key = connections.allKeys[i];
        NSDictionary *connection = [connections objectForKey:key];
        NSString *serializedConnection = [connection objectForKey:@"serializedConnection"];
        NSString *pwDid = [CMConnection getPwDid:serializedConnection];
        if ([pwDidMes isEqual:pwDid]) {
            offerConnection = serializedConnection;
            break;
        }
    }
    [CMProofRequest createWithRequest:payload
                withCompletionHandler:^(NSDictionary *offer, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        NSLog(@"Proof Request created %@", error);

        [CMProofRequest retrieveAvailableCredentials:[CMUtilities dictToJsonString:offer]
                               withCompletionHandler:^(NSDictionary *creds, NSError *error) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            };

            NSLog(@"Proof Request retrieved %@", creds);
            NSString *attr = [creds objectForKey: @"autofilledAttributes"];
            
            [CMProofRequest send:offerConnection
                 serializedProof:[CMUtilities dictToJsonString:offer]
                   selectedCreds:attr
                selfAttestedAttr:@"{}"
           withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(nil, error);
                }
                [LocalStorage addEventToHistory:@"Proof request send"];
                NSLog(@"Proof Request send %@", error);
                return completionBlock(responseObject, nil);
            }];
        }];
    }];
}

- (IBAction)addNewConn: (id)sender {
    if(addConnConfigTextView.text.length > 3 && ![addConnConfigTextView.text isEqual: @"enter code here"]) {
        NSDictionary *connectValues = [CMConnection parsedInvite: addConnConfigTextView.text];
        [LocalStorage store: @"connectValues" andObject: connectValues];

        NSString *label = [connectValues objectForKey: @"label"];
        NSString *goal = [connectValues objectForKey: @"goal"];
        NSString *profileUrl = [connectValues objectForKey: @"profileUrl"];
        
        NSMutableDictionary* requestsDict = [[LocalStorage getObjectForKey: @"requests" shouldCreate: true] mutableCopy];
        NSString *uuid = [[NSUUID UUID] UUIDString];

        NSDictionary* requestObj = @{
            @"name": label,
            @"profileUrl": profileUrl,
            @"goal": goal,
            @"uuid": uuid,
            @"type": @"null"
        };
        [requestsDict setValue: requestObj forKey: uuid];
        [LocalStorage store: @"requests" andObject: requestsDict];
        
        requests = requestsDict;
        [self.tableView reloadData];
    }
}

- (void)vcxInitialized {
    self.infoLbl.text = @"VCX initialized!";
    _isInitialized = true;
    addConnectionBtn.enabled = true;
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
//    if(!_isInitialized) {
//        NSLog(@"Please wait for VCX to initialize!");
//        return;
//    }
//
//    QRCodeReader *reader = [QRCodeReader readerWithMetadataObjectTypes:@[AVMetadataObjectTypeQRCode]];
//
//    QRCodeReaderViewController *vc = [QRCodeReaderViewController readerWithCancelButtonTitle:@"Cancel" codeReader: reader startScanningAtLoad:YES showSwitchCameraButton:YES showTorchButton:YES];
//    vc.modalPresentationStyle = UIModalPresentationFormSheet;
//
//    [self presentViewController: vc animated: YES completion:^{
//        NSLog(@"QR code scanner presented");
//    }];
//
//    [reader setCompletionWithBlock:^(NSString *scanResult) {
//        NSLog(@"%@", scanResult);
//        self.addConnConfigTextView.text = scanResult;
//        [self addNewConn: sender];
//        [self dismissViewControllerAnimated: vc completion:^{
//            NSLog(@"QR code scanner dissmised");
//        }];
//    }];
//    UIAlertController * alert = [UIAlertController
//                                     alertControllerWithTitle:@"Logout"
//                                     message:@"Are You Sure Want to Logout!"
//                                     preferredStyle:UIAlertControllerStyleAlert];
//
//        //Add Buttons
//
//        UIAlertAction* yesButton = [UIAlertAction
//                                    actionWithTitle:@"Yes"
//                                    style:UIAlertActionStyleDefault
//                                    handler:^(UIAlertAction * action) {
//                                        //Handle your yes please button action here
//
//                                    }];
//
//        UIAlertAction* noButton = [UIAlertAction
//                                   actionWithTitle:@"Cancel"
//                                   style:UIAlertActionStyleDefault
//                                   handler:^(UIAlertAction * action) {
//                                       //Handle no, thanks button
//                                   }];
//
//    UIAlertAction* hzButton = [UIAlertAction
//                               actionWithTitle:@"no"
//                               style:UIAlertActionStyleDefault
//                               handler:^(UIAlertAction * action) {
//                                   //Handle no, thanks button
//                               }];
//
//    UIAlertAction* nButton = [UIAlertAction
//                               actionWithTitle:@"tt"
//                               style:UIAlertActionStyleDefault
//                               handler:^(UIAlertAction * action) {
//                                   //Handle no, thanks button
//                               }];
//
//        //Add your buttons to alert controller
//
//        [alert addAction:yesButton];
//        [alert addAction:noButton];
//        [alert addAction:hzButton];
//        [alert addAction:nButton];
//
//        [self presentViewController:alert animated:YES completion:nil];
}

- (IBAction)checkMessages: (UIButton*) sender {
    NSLog(@"checkMessages");
    [CMMessage downloadAllMessages:^(NSArray *responseArray, NSError *error) {
        NSLog(@"downloadAllMessages %@", responseArray);
        for (NSInteger i = 0; i < responseArray.count; i++) {
            NSDictionary *message = responseArray[i];
            [LocalStorage store: @"message" andObject: message];

            NSString *type = [message objectForKey:@"type"];
            if ([type isEqual:@"credential-offer"]) {
                NSMutableDictionary* requestsDict = [[LocalStorage getObjectForKey: @"requests" shouldCreate: true] mutableCopy];
                NSString *uuid = [[NSUUID UUID] UUIDString];

                NSDictionary* requestObj = @{
                    @"name": @"COVID19 Antibody Test",
                    @"goal": @"Credential offer",
                    @"uuid": [message objectForKey:@"uid"],
                    @"type": type,
                };
                [requestsDict setValue: requestObj forKey: uuid];
                [LocalStorage store: @"requests" andObject: requestsDict];
                
                dispatch_async(dispatch_get_main_queue(), ^{
                    self.requests = requestsDict;
                    [self.tableView reloadData];
                });
            }
        }
    }];
};


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [requests.allKeys count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    MGSwipeTableCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cellact"];

    NSDictionary *requestDict = requests[requests.allKeys[indexPath.row]];
    NSString *type = [requestDict objectForKey: @"type"];
    
    NSString *name = [requestDict objectForKey: @"name"];
    NSString *goal = [requestDict objectForKey: @"goal"];
    NSString *uuid = [requestDict objectForKey: @"uuid"];

    cell.textLabel.text = [NSString stringWithFormat:@"%@ - %@", name, goal];

    if ([type isEqual:@"null"]) {
        NSString *logoUrl = [requestDict objectForKey: @"profileUrl"];
        NSURL *url = [NSURL URLWithString:logoUrl];
        NSData *data = [[NSData alloc] initWithContentsOfURL:url];
        UIImage *image = [UIImage imageWithData:data scale:0.5f];
        
        CGSize size = CGSizeMake(43, 43);
        UIGraphicsBeginImageContextWithOptions(size, NO, 0.0);
        [image drawInRect:CGRectMake(0, 0, size.width, size.height)];
        UIImage *logo = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();

        cell.imageView.image = logo;
        
        cell.leftButtons = @[[MGSwipeButton buttonWithTitle:@" Accept" backgroundColor:[UIColor systemGreenColor] callback:^BOOL(MGSwipeTableCell *sender) {
            NSLog(@"callback");
            [self newConnection:^(BOOL result, NSError *error) {
                if (result) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        NSMutableDictionary* processedDict;
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
            }];
            return YES;
            }]];
        cell.leftSwipeSettings.transition = MGSwipeTransitionDrag;
        
        cell.rightButtons = @[[MGSwipeButton buttonWithTitle:@"Reject" backgroundColor:[UIColor redColor] callback:^BOOL(MGSwipeTableCell *sender) {
            NSLog(@"callback");
            NSMutableDictionary* history = [[LocalStorage getObjectForKey: @"history" shouldCreate: true] mutableCopy];
            NSDictionary* historyObj = @{
                @"name": @"Sun Crest - Rejected",
            };
            NSString *uuidHis = [[NSUUID UUID] UUIDString];
            [history setValue: historyObj forKey: uuidHis];
            [LocalStorage store: @"history" andObject: history];
            
            NSMutableDictionary* processedDict;
            NSDictionary* req = [LocalStorage getObjectForKey: @"requests" shouldCreate: false];
            for (NSInteger i = 0; i < req.allKeys.count; i++) {
                NSString *key = req.allKeys[i];
                if ([key isEqual: uuid]) {
                    [processedDict removeObjectForKey:key];
                }
            }
            [LocalStorage store: @"requests" andObject: processedDict];
            self -> requests = processedDict;
            [self.tableView reloadData];
            return YES;
            }]];
        cell.rightSwipeSettings.transition = MGSwipeTransitionDrag;
    } else {
        cell.leftButtons = @[[MGSwipeButton buttonWithTitle:@" Accept" backgroundColor:[UIColor systemGreenColor] callback:^BOOL(MGSwipeTableCell *sender) {
            NSLog(@"callback");
            
            [self acceptCredential:^(BOOL result, NSError *error) {
                if (result) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        NSMutableDictionary* processedDict;
                        NSDictionary* req = [LocalStorage getObjectForKey: @"requests" shouldCreate: false];
                        for (NSInteger i = 0; i < req.allKeys.count; i++) {
                            NSString *key = req.allKeys[i];
                            if ([key isEqual: uuid]) {
                                [processedDict removeObjectForKey:key];
                            }
                        }
                        [LocalStorage store: @"requests" andObject: processedDict];
                        self -> requests = processedDict;
                        [self.tableView reloadData];
                    });
                };
            }];
            return YES;
            }]];
        cell.leftSwipeSettings.transition = MGSwipeTransitionDrag;
        
        cell.rightButtons = @[[MGSwipeButton buttonWithTitle:@"Reject" backgroundColor:[UIColor redColor] callback:^BOOL(MGSwipeTableCell *sender) {
            NSLog(@"callback");
            NSMutableDictionary* history = [[LocalStorage getObjectForKey: @"history" shouldCreate: true] mutableCopy];
            NSDictionary* historyObj = @{
                @"name": @"Sun Crest - Rejected",
            };
            NSString *uuidHis = [[NSUUID UUID] UUIDString];
            [history setValue: historyObj forKey: uuidHis];
            [LocalStorage store: @"history" andObject: history];
            
            NSMutableDictionary* processedDict;
            NSDictionary* req = [LocalStorage getObjectForKey: @"requests" shouldCreate: false];
            for (NSInteger i = 0; i < req.allKeys.count; i++) {
                NSString *key = req.allKeys[i];
                if ([key isEqual: uuid]) {
                    [processedDict removeObjectForKey:key];
                }
            }
            [LocalStorage store: @"requests" andObject: processedDict];
            self -> requests = processedDict;
            [self.tableView reloadData];
            return YES;
            }]];
            cell.rightSwipeSettings.transition = MGSwipeTransitionDrag;
    }
    return cell;
}

 @end
     
