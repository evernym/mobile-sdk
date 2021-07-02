//
//  HomeViewController.m
//  CMeSdkObjc
//
//  Created by Norman Jarvis on 5/3/19.
//  Copyright © 2019 Evernym Inc. All rights reserved.
//
#import "CustomTableViewCell.h"
#import "HomeViewController.h"
#import "MobileSDK.h"
#import "CMConnection.h"
#import "CMCredential.h"
#import "CMUtilities.h"
#import "LocalStorage.h"
#import "QRCodeReaderViewController.h"
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

- (void)newConnection:(NSString *) data
  withCompletionBlock:(ResponseWithBoolean) completionBlock {
    NSLog(@"newConnection %@", data);
    [CMConnection handleConnection:data
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

- (void)answerQuestion:(NSString *)serializedConnection
               message:(NSString *)message
                answer:(NSString *)answer
   withCompletionBlock:(ResponseWithBoolean) completionBlock {
    ConnectMeVcx* sdkApi = [[MobileSDK shared] sdkApi];

    [sdkApi connectionDeserialize:serializedConnection
                       completion:^(NSError *error, NSInteger connectionHandle) {
            if (error && error.code > 0) {
                return completionBlock(nil, error);
            }
            [sdkApi connectionSendAnswer:(int)connectionHandle
                                question:message
                                  answer:answer
                          withCompletion:^(NSError *error) {
                if (error && error.code > 0) {
                    return completionBlock(NO, error);
                }
                return completionBlock(YES, error);
            }];
        }
    ];
}

- (void)answer:(NSString *) data
withCompletionBlock:(ResponseWithBoolean) completionBlock {
    NSDictionary *message = [CMUtilities jsonToDictionary:data];
    NSString *payload = [message objectForKey:@"payload"];
    NSDictionary *payloadDict = [CMUtilities jsonToDictionary:payload];
    NSArray *responses = [payloadDict objectForKey:@"valid_responses"];

    //TODO: Put to connection class
    NSString *pwDidMes = [message objectForKey:@"pwDid"];
    
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
    //
    
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
                                    [self answerQuestion:offerConnection
                                                 message:payload
                                                  answer:[CMUtilities dictToJsonString:response]
                                     withCompletionBlock:^(BOOL result, NSError *error) {
                                        return completionBlock(result, error);
                                    }];
                                }];
        [alert addAction:button];
    }

    [self presentViewController:alert animated:YES completion:nil];
}

- (void)acceptCredential:(NSString *) data
     withCompletionBlock:(ResponseWithBoolean) completionBlock {
    NSDictionary *message = [CMUtilities jsonToDictionary:data];
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
//TODO: fix me
        [CMCredential acceptCredentialOffer:offerConnection
                       serializedCredential:[CMUtilities dictToJsonString:serOffer]
                                      offer:@""
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

- (void)sendProof:(NSString *) data
withCompletionBlock:(ResponseWithBoolean) completionBlock {
    NSDictionary *message = [CMUtilities jsonToDictionary:data];
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
            @"type": @"null",
            @"data": [CMUtilities dictToJsonString:connectValues]
        };
        [requestsDict setValue: requestObj forKey: uuid];
        [LocalStorage store: @"requests" andObject: requestsDict];
        
        requests = requestsDict;
        [self.tableView reloadData];
    }
}

- (IBAction)checkMessages: (UIButton*) sender {
    [CMMessage downloadAllMessages:^(NSArray *responseArray, NSError *error) {
        NSLog(@"downloadAllMessages %@", responseArray);
        for (NSInteger i = 0; i < responseArray.count; i++) {
            NSDictionary *message = responseArray[i];

            NSString *type = [message objectForKey:@"type"];
            if ([type isEqual:@"credential-offer"]) {
                NSMutableDictionary* requestsDict = [[LocalStorage getObjectForKey: @"requests" shouldCreate: true] mutableCopy];
                NSString *uuid = [[NSUUID UUID] UUIDString];

                NSDictionary* requestObj = @{
                    @"name": @"COVID19 Antibody Test",
                    @"goal": @"Credential offer",
                    @"uuid": [message objectForKey:@"uid"],
                    @"type": type,
                    @"data": [CMUtilities dictToJsonString:message]
                };
                [requestsDict setValue: requestObj forKey: uuid];
                [LocalStorage store: @"requests" andObject: requestsDict];
                
                dispatch_async(dispatch_get_main_queue(), ^{
                    [CMMessage updateMessageStatus:[message objectForKey:@"pwDid"]
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
                NSMutableDictionary* requestsDict = [[LocalStorage getObjectForKey: @"requests" shouldCreate: true] mutableCopy];
                NSString *uuid = [[NSUUID UUID] UUIDString];

                NSDictionary* requestObj = @{
                    @"name": @"SunCrest Portal Access Attempt",
                    @"goal": @"Log in",
                    @"uuid": [message objectForKey:@"uid"],
                    @"type": type,
                    @"data": [CMUtilities dictToJsonString:message]
                };
                [requestsDict setValue: requestObj forKey: uuid];
                [LocalStorage store: @"requests" andObject: requestsDict];
                
                dispatch_async(dispatch_get_main_queue(), ^{
                    [CMMessage updateMessageStatus:[message objectForKey:@"pwDid"]
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
};


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

    if ([type isEqual:@"null"]) {
        NSString *logoUrl = [requestDict objectForKey: @"profileUrl"];
        [cell updateAttribute:name
                     subtitle:goal
                      logoUrl:logoUrl
               acceptCallback:^() {
                        [self newConnection:data
                        withCompletionBlock:^(BOOL result, NSError *error) {
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
                    }
               rejectCallback:^() {}
         ];
        [cell.accept setTitle:@"Accept" forState:UIControlStateNormal];
        [cell.reject setHidden:NO];
    } else if ([type isEqual:@"credential-offer"]) {
        [cell updateAttribute:name
                     subtitle:goal
                      logoUrl:@""
               acceptCallback:^() {
                         [self acceptCredential:data
                            withCompletionBlock:^(BOOL result, NSError *error) {
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
                    }
               rejectCallback:^() {}
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
                    }
               rejectCallback:^() {}
         ];
        [cell.accept setTitle:@"Answer" forState:UIControlStateNormal];
        [cell.reject setHidden:YES];
    }
    return cell;
}

@end
     
