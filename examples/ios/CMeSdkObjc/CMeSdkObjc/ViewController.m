//
//  ViewController.m
//  CMeSdkObjc
//
//  Created by Norman Jarvis on 5/3/19.
//  Copyright © 2019 Evernym Inc. All rights reserved.
//

#import "ViewController.h"
#import "MobileSDK.h"
#import "CMConnection.h"
#import "CMCredential.h"
#import "CMUtilities.h"
#import "ConnectionDetailsViewController.h"
#import "LocalStorage.h"
#import "QRCodeReaderViewController.h"

@interface ViewController ()

@property (weak, nonatomic) IBOutlet UIButton *addConnectionBtn;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property NSDictionary* existingConnections;
@property (weak, nonatomic) IBOutlet UILabel *infoLbl;
@property BOOL isInitialized;

@end

@implementation ViewController
@synthesize addConnLabel, addConnConfigTextView, existingConnections, tableView, addConnectionBtn;

UIGestureRecognizer *tapper;

- (void)viewDidLoad {
    [super viewDidLoad];

    self.title = @"MobileSDK";
    tapper = [[UITapGestureRecognizer alloc] initWithTarget: self action: @selector(handleSingleTap:)];
    tapper.cancelsTouchesInView = NO;
    [self.view addGestureRecognizer: tapper];
    addConnConfigTextView.delegate = self;
    tableView.tableFooterView = [[UIView alloc] init];
    addConnConfigTextView.layer.cornerRadius = 5;
    addConnectionBtn.layer.cornerRadius = 5;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear: animated];
    existingConnections = [LocalStorage getObjectForKey: @"connections" shouldCreate: false];
    [tableView reloadData];
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

- (IBAction)addNewConn: (id)sender {
    if(addConnConfigTextView.text.length > 3 && ![addConnConfigTextView.text isEqual: @"enter code here"]) {
        [CMConnection connect: addConnConfigTextView.text
               connectionType: QR phoneNumber: @""
        withCompletionHandler: ^(NSDictionary *connectionData, NSError *error) {
            if (error != nil && error > 0) {
                NSLog(@"Error %@", error.localizedDescription);
                return;
            }
            if(connectionData) {
                self.addConnConfigTextView.text = @"";
                [self performSegueWithIdentifier: @"openConnectionDetails" sender: connectionData];
                NSString* serializedConnection = [connectionData objectForKey: @"serializedConnection"];
                NSLog(@"ConnectionData %@", serializedConnection);

                NSDictionary *offer = [LocalStorage getObjectForKey: @"request~attach" shouldCreate: false];
                [CMCredential createWithOffer:[CMUtilities dictToJsonString:offer]
                        withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                    if (error && error.code > 0) {
                        NSLog(@"Error createWithOffer %@", error);

                        return;
                    }
                    NSLog(@"Created offer %@", responseObject);

                    [CMCredential acceptCredentialOffer:serializedConnection
                                   serializedCredential:[CMUtilities dictToJsonString:responseObject]
                                  withCompletionHandler:^(NSDictionary *responseObject, NSError *error) {
                        if (error && error.code > 0) {
                            NSLog(@"Error acceptCredentialOffer %@", error);

                            return;
                        }
                        NSLog(@"Credential Offer Accepted %@", error);
                    }];
                }];
            }
        }];
    }
}

- (IBAction)openConnection: (id)sender {
    if(existingConnections && [existingConnections count] > 0) {
        [self performSegueWithIdentifier: @"openConnectionDetails" sender: existingConnections[[existingConnections allKeys][0]]];
        addConnConfigTextView.text = @"";
    }
}

- (void)prepareForSegue: (UIStoryboardSegue *)segue sender: (id)sender {
    if([segue.identifier isEqual: @"openConnectionDetails"]) {
        ConnectionDetailsViewController* conDetails = [segue destinationViewController];
        conDetails.connection = sender;
    }
}

- (void)vcxInitialized {
    self.infoLbl.text = @"VCX initialized!";
    _isInitialized = true;
    addConnectionBtn.enabled = true;
    double delayInSeconds = 20.0;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        self.infoLbl.text = @"";
    });
}

// MARK - TextArea delegates

- (BOOL)textViewShouldBeginEditing:(UITextView *)textView {
    if([textView.text isEqual: @"enter code here"]) {
        addConnConfigTextView.text = @"";
    }
    return true;
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [existingConnections.allKeys count];
}

-(UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier: @"conectionCell" forIndexPath:indexPath];

    NSDictionary* connection = existingConnections[existingConnections.allKeys[indexPath.row]];
    cell.textLabel.text = [CMConnection connectionName: connection];
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if(!_isInitialized) {
        NSLog(@"Please wait for VCX to initialize!");
        return;
    }
    NSDictionary* connection = existingConnections[[existingConnections allKeys][indexPath.row]];
    [self performSegueWithIdentifier: @"openConnectionDetails" sender: connection];
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    return _isInitialized;
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        NSMutableDictionary* updatedConections = [existingConnections mutableCopy];
        NSDictionary* connection = existingConnections[[existingConnections allKeys][indexPath.row]];

        [CMConnection removeConnection: connection[@"serializedConnection"] withCompletionHandler:^(NSString *successMessage, NSError *error) {
            if (error && error.code > 0) {
                [CMUtilities printError: error];
                return;
            }
            [updatedConections removeObjectForKey: [CMConnection connectionID: connection]];
            if([[updatedConections allKeys] count] < 1) {
                [LocalStorage deleteObjectForKey: @"connections"];
            } else {
                [LocalStorage store: @"connections" andObject: updatedConections];
            }
            self.existingConnections = updatedConections;
            [tableView reloadData];
        }];
    }
}

- (IBAction)scanQR: (UIButton*) sender {
    if(!_isInitialized) {
        NSLog(@"Please wait for VCX to initialize!");
        return;
    }
    // Create the reader object
    QRCodeReader *reader = [QRCodeReader readerWithMetadataObjectTypes:@[AVMetadataObjectTypeQRCode]];
    QRCodeReaderViewController *vc = [QRCodeReaderViewController readerWithCancelButtonTitle:@"Cancel" codeReader: reader startScanningAtLoad:YES showSwitchCameraButton:YES showTorchButton:YES];
    vc.modalPresentationStyle = UIModalPresentationFormSheet;

    [self.navigationController presentViewController: vc animated: YES completion:^{
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

@end
