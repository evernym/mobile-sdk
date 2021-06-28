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
    [CMConnection createConnection: addConnConfigTextView.text
           connectionType: QR phoneNumber: @""
    withCompletionHandler: ^(NSDictionary *connectionData, NSError *error) {
        if (error != nil && error > 0) {
            NSLog(@"Error %@", error.localizedDescription);
            return completionBlock(NO, error);
        }
        if(connectionData) {
            NSMutableDictionary* history = [[LocalStorage getObjectForKey: @"history" shouldCreate: true] mutableCopy];

            NSDictionary* historyObj = @{
                @"name": @"Sun Crest - Connection Accept",
            };
            NSString *uuid = [[NSUUID UUID] UUIDString];
            [history setValue: historyObj forKey: uuid];
            [LocalStorage store: @"history" andObject: history];
            
            NSString* serializedConnection = [connectionData objectForKey: @"serializedConnection"];

            NSLog(@"serializedConnection %@", serializedConnection);
            NSDictionary *offer = [LocalStorage getObjectForKey: @"request~attach" shouldCreate: false];
            [CMCredential createWithOffer:[CMUtilities dictToJsonString:offer]
                    withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                if (error && error.code > 0) {
                    NSLog(@"Error createWithOffer %@", error);
                    return completionBlock(NO, error);
                }
                NSLog(@"Created offer %@", responseObject);

                [CMCredential acceptCredentialOffer:serializedConnection
                               serializedCredential:[CMUtilities dictToJsonString:responseObject]
                              withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                    if (error && error.code > 0) {
                        NSLog(@"Error acceptCredentialOffer %@", error);
                        return completionBlock(NO, error);
                    }
                    NSMutableDictionary* history = [[LocalStorage getObjectForKey: @"history" shouldCreate: true] mutableCopy];
                    
                    NSDictionary* historyObj = @{
                        @"name": @"Sun Crest - Credential Accept",
                    };
                    NSString *uuid = [[NSUUID UUID] UUIDString];
                    [history setValue: historyObj forKey: uuid];
                    [LocalStorage store: @"history" andObject: history];
                    
                    NSLog(@"Credential Offer Accepted %@", error);
                    return completionBlock(YES, error);
                }];
            }];
        }
    }];
}

- (IBAction)addNewConn: (id)sender {
    if(addConnConfigTextView.text.length > 3 && ![addConnConfigTextView.text isEqual: @"enter code here"]) {
        NSDictionary *connectValues = [CMConnection parsedInvite: addConnConfigTextView.text];
        NSLog(@"addNewConn %@", connectValues);
        NSString *label = [connectValues objectForKey: @"label"];
        NSString *goal = [connectValues objectForKey: @"goal"];
        NSString *profileUrl = [connectValues objectForKey: @"profileUrl"];
        
        NSMutableDictionary* requestsDict = [[LocalStorage getObjectForKey: @"requests" shouldCreate: true] mutableCopy];
        NSString *uuid = [[NSUUID UUID] UUIDString];

        NSDictionary* requestObj = @{
            @"name": label,
            @"profileUrl": profileUrl,
            @"goal": goal,
            @"accepted": @"0",
            @"uuid": uuid
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
    if(!_isInitialized) {
        NSLog(@"Please wait for VCX to initialize!");
        return;
    }

    QRCodeReader *reader = [QRCodeReader readerWithMetadataObjectTypes:@[AVMetadataObjectTypeQRCode]];

    QRCodeReaderViewController *vc = [QRCodeReaderViewController readerWithCancelButtonTitle:@"Cancel" codeReader: reader startScanningAtLoad:YES showSwitchCameraButton:YES showTorchButton:YES];
    vc.modalPresentationStyle = UIModalPresentationFormSheet;

    [self presentViewController: vc animated: YES completion:^{
        NSLog(@"QR code scanner presented");
    }];

    [reader setCompletionWithBlock:^(NSString *scanResult) {
        NSLog(@"%@", scanResult);
        self.addConnConfigTextView.text = scanResult;
        [self addNewConn: sender];
        [self dismissViewControllerAnimated: vc completion:^{
            NSLog(@"QR code scanner dissmised");
        }];
    }];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [requests.allKeys count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    MGSwipeTableCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cellact"];

    NSDictionary *requestDict = requests[requests.allKeys[indexPath.row]];
    NSString *name = [requestDict objectForKey: @"name"];
    NSString *goal = [requestDict objectForKey: @"goal"];
    NSString *logoUrl = [requestDict objectForKey: @"profileUrl"];
    NSString *uuid = [requestDict objectForKey: @"uuid"];
    
    NSURL *url = [NSURL URLWithString:logoUrl];
    NSData *data = [[NSData alloc] initWithContentsOfURL:url];
    UIImage *image = [UIImage imageWithData:data scale:0.5f];
    
    CGSize size = CGSizeMake(43, 43);
    UIGraphicsBeginImageContextWithOptions(size, NO, 0.0);
    [image drawInRect:CGRectMake(0, 0, size.width, size.height)];
    UIImage *logo = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    cell.textLabel.text = [NSString stringWithFormat:@"%@ - %@", name, goal];
    cell.imageView.image = logo;
    
    cell.leftButtons = @[[MGSwipeButton buttonWithTitle:@" Accept" backgroundColor:[UIColor systemGreenColor] callback:^BOOL(MGSwipeTableCell *sender) {
        NSLog(@"callback");
        [self newConnection:^(BOOL result, NSError *error) {
            if (result) {
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
            }
        }];
        [self.tableView reloadData];
        return YES;
        }]];
    cell.leftSwipeSettings.transition = MGSwipeTransitionClipCenter;
    
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
    cell.rightSwipeSettings.transition = MGSwipeTransitionClipCenter;

    return cell;
}

 @end
     
