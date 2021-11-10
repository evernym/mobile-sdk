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
#import "QRCodeReader.h"
#import "Message.h"
#import "ProofRequest.h"
#import "ConnectionInvitation.h"
#import "ConnectionHandler.h"
#import "CredentialOffersHandler.h"
#import "ProofRequestsHandler.h"

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

NSString *CREDENTIAL_OFFER = @"credential-offer";
NSString *PRESENTATION_REQUEST = @"presentation-request";
NSString *COMMITTED_QUESTION = @"committed-question";
NSString *OOB = @"OOB";

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

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    [super touchesBegan:touches withEvent:event];
    [addConnConfigTextView endEditing:true];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear: animated];
    requests = [LocalStorage getObjectForKey: @"requests" shouldCreate: false];
    [self.tableView reloadData];
    [[NSNotificationCenter defaultCenter] addObserver: self selector: @selector(vcxInitialized) name:@"vcxInitialized" object: nil];
    _isInitialized = [[MobileSDK shared] sdkInited];
}

- (BOOL) textViewShouldBeginEditing:(UITextView *)textView {
    if([textView.text isEqual: @"enter code here"]) {
        addConnConfigTextView.text = @"";
    }
    return true;
}

- (void) viewWillDisappear:(BOOL)animated {
    [[NSNotificationCenter defaultCenter] removeObserver: self];
}

- (void) handleSingleTap:(UITapGestureRecognizer *) sender {
    [self.view endEditing: YES];
}

- (void) vcxInitialized {
    self.infoLbl.text = @"VCX initialized!";
    _isInitialized = true;
    double delayInSeconds = 10.0;
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        self.infoLbl.text = @"";
    });
}

- (void) createActionWithInvitation:(NSString *) data {
    NSDictionary *connectValues = [ConnectionInvitation parsedInvite: data];
    NSString *label = [connectValues objectForKey: @"label"];
    NSString *goal = @"";
    if ([connectValues valueForKey:@"goal"] != nil) {
        goal = [connectValues objectForKey: @"goal"];
    } else {
        goal = @"New connection";
    }
    NSString *profileUrl = [connectValues objectForKey: @"profileUrl"];
    
    [self createAction:label
            profileUrl:profileUrl
                  goal:goal
                  type:OOB
                  data:[Utilities dictToJsonString:connectValues]
        additionalData:@""
                 pwDid:@""];
}

- (void) createAction:(NSString *) label
           profileUrl:(NSString *) profileUrl
                 goal:(NSString *) goal
                 type:(NSString *) type
                 data:(NSString *) data
       additionalData:(NSString *) additionalData
                pwDid:(NSString *) pwDid {
    NSMutableDictionary* allRequests = [[LocalStorage getObjectForKey: @"requests" shouldCreate: true] mutableCopy];
    NSString *uuid = [[NSUUID UUID] UUIDString];

    NSDictionary* request = @{
        @"name": label,
        @"profileUrl": profileUrl,
        @"goal": goal,
        @"uuid": uuid,
        @"type": type,
        @"data": data,
        @"additionalData": additionalData,
        @"pwDid": pwDid
    };
    
    [allRequests setValue: request forKey: uuid];
    [LocalStorage store: @"requests" andObject: allRequests];
    requests = allRequests;
    [self.tableView reloadData];
}

- (IBAction)addNewConnBtnClick: (id)sender {
    if(addConnConfigTextView.text.length > 3 && ![addConnConfigTextView.text isEqual: @"enter code here"]) {
        [self createActionWithInvitation: addConnConfigTextView.text];
    }
}

- (IBAction)scanQR: (UIButton*) sender {
    if(!_isInitialized) {
        NSLog(@"Please wait for VCX to initialize!");
        return;
    }
    QRCodeReader *reader = [QRCodeReader readerWithMetadataObjectTypes:@[AVMetadataObjectTypeQRCode]];
    QRCodeReaderViewController *vc = [QRCodeReaderViewController readerWithCancelButtonTitle:@""
                                                                                  codeReader:reader
                                                                         startScanningAtLoad:YES
                                                                      showSwitchCameraButton:YES
                                                                             showTorchButton:YES];
    vc.modalPresentationStyle = UIModalPresentationFormSheet;
    vc.delegate = self;

    [reader setCompletionWithBlock:^(NSString *resultAsString) {
        NSLog(@"%@", resultAsString);
        [vc dismissViewControllerAnimated:YES completion:^{
            [self createActionWithInvitation: resultAsString];
        }];
    }];
    
    [self presentViewController: vc animated: YES completion:^{
        NSLog(@"QR code scanner presented");
    }];
}

- (IBAction)checkMessages:(id)sender {
    [Message downloadAllMessages:^(NSArray *responseArray, NSError *error) {
        for (NSInteger i = 0; i < responseArray.count; i++) {
            NSDictionary *message = responseArray[i];

            NSString *type = [message objectForKey:@"type"];
            if ([type isEqual:CREDENTIAL_OFFER]) {
                [self handleReceivedCredentialOffer:message withCompletionBlock:^(BOOL result, NSError *error) {
                    if (error && error.code > 0) {
                        [Utilities printError:error];
                    }
                }];
            }
            if ([type isEqual:PRESENTATION_REQUEST]) {
                [self handleReceivedProofRequest:message
                             withCompletionBlock:^(BOOL result, NSError *error) {
                    if (error && error.code > 0) {
                        [Utilities printError:error];
                    }
                }];
            }
            if ([type isEqual:COMMITTED_QUESTION]) {
                [self handleReceivedQuestion:message];
            }
        }
    }];
}

- (void) handleReceivedCredentialOffer:(NSDictionary *) message
                   withCompletionBlock:(ResponseWithBoolean) completionBlock {
    [self messageStatusUpdate: message];
    NSString *pwDid = [message objectForKey:@"pwDid"];
    NSString *payloadJson = [message objectForKey:@"payload"];
    NSArray *payloadArray = [Utilities jsonToArray:payloadJson];
    NSDictionary *payloadDict = payloadArray[0];
    
    [Credential createWithOffer:payloadJson
          withCompletionHandler:^(NSDictionary *createdOffer, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        
        [self createAction:[payloadDict objectForKey:@"claim_name"]
                profileUrl:@""
                      goal:@"Credential offer"
                      type:CREDENTIAL_OFFER
                      data:payloadJson
            additionalData:[Utilities dictToJsonString:createdOffer]
                     pwDid:pwDid];
    }];
}

- (void) handleReceivedProofRequest:(NSDictionary *) message
                withCompletionBlock:(ResponseWithBoolean) completionBlock{
    [self messageStatusUpdate: message];
    NSString *pwDid = [message objectForKey:@"pwDid"];
    NSString *payload = [message objectForKey:@"payload"];
    NSDictionary *payloadDict = [Utilities jsonToDictionary:payload];

    [ProofRequest createWithRequest:payload
              withCompletionHandler:^(NSDictionary *request, NSError *error) {
        if (error && error.code > 0) {
            return completionBlock(nil, error);
        }
        
        [self createAction:[payloadDict objectForKey:@"comment"]
                profileUrl:@""
                      goal:@"Proof Request"
                      type:PRESENTATION_REQUEST
                      data:payload
            additionalData:[Utilities dictToJsonString:request]
                     pwDid:pwDid];
    }];
}

- (void) handleReceivedQuestion:(NSDictionary *) message {
    [self messageStatusUpdate: message];
    NSString *pwDid = [message objectForKey:@"pwDid"];
    NSString *payload = [message objectForKey:@"payload"];
    NSDictionary *payloadDict = [Utilities jsonToDictionary:payload];
    
    [self createAction:[payloadDict objectForKey:@"question_text"]
            profileUrl:@""
                  goal:@"Question"
                  type:COMMITTED_QUESTION
                  data:[Utilities dictToJsonString:message]
        additionalData:payload
                 pwDid:pwDid];
}

- (void) handleAcceptAction:(NSString *) data
                    forType:(NSString *) forType
                      pwDid:(NSString *) pwDid
             additionalData:(NSString *) additionalData
                       name:(NSString *) name
        withCompletionBlock:(ResponseWithBoolean) completionBlock {
    if ([forType isEqual:OOB]) {
        [ConnectionHandler handleConnectionInvitation:data
                                withCompletionHandler:^(NSString *result, NSError *error) {
            return completionBlock(result, error);
        }];
    }
    if ([forType isEqual:CREDENTIAL_OFFER]) {
        [CredentialOffersHandler acceptCredentialOffer:pwDid
                                            attachment:data
                                          createdOffer:[Utilities jsonToDictionary:additionalData]
                                           fromMessage:true
                                 withCompletionHandler:^(NSString *result, NSError *error) {
            return completionBlock(result, error);
        }];
    }
    if ([forType isEqual:PRESENTATION_REQUEST]) {
        [ProofRequestsHandler acceptProofRequest:pwDid
                                      attachment:[Utilities jsonToDictionary:data]
                                         request:[Utilities jsonToDictionary:additionalData]
                                            name:name
                           withCompletionHandler:^(NSString *result, NSError *error) {
            return completionBlock(result, error);
        }];
    }
    if ([forType isEqual:COMMITTED_QUESTION]) {
        [self answer:data
 withCompletionBlock:^(BOOL result, NSError *error) {
            return completionBlock(result, error);
        }];
    }
}

- (void) handleRejectAction:(NSString *) data
                    forType:(NSString *) forType
                      pwDid:(NSString *) pwDid
             additionalData:(NSString *) additionalData
                       name:(NSString *) name
        withCompletionBlock:(ResponseWithBoolean) completionBlock  {
    if ([forType isEqual:OOB]) {
        return completionBlock(true, nil);
    }
    if ([forType isEqual:CREDENTIAL_OFFER]) {
        [CredentialOffersHandler rejectCredentialOffer:pwDid
                                            attachment:data
                                          createdOffer:[Utilities jsonToDictionary:additionalData] withCompletionHandler:^(NSString *result, NSError *error) {
            return completionBlock(result, error);
        }];
    }
    if ([forType isEqual:PRESENTATION_REQUEST]) {
        [ProofRequestsHandler rejectProofRequest:pwDid
                                         request:additionalData
                                            name:name
                           withCompletionHandler:^(NSString *result, NSError *error) {
            return completionBlock(result, error);
        }];
    }
    if ([forType isEqual:COMMITTED_QUESTION]) {
        return completionBlock(true, nil);
    }
}

- (void)answer:(NSString *) data
withCompletionBlock:(ResponseWithBoolean) completionBlock {
    NSDictionary *message = [Utilities jsonToDictionary:data];
    NSString *payload = [message objectForKey:@"payload"];
    NSDictionary *payloadDict = [Utilities jsonToDictionary:payload];
    NSArray *responses = [payloadDict objectForKey:@"valid_responses"];

    NSString *pwDidMes = [message objectForKey:@"pwDid"];
    NSString *questionsConnection = [ConnectionInvitation getConnectionByPwDid:pwDidMes];

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

- (void) messageStatusUpdate:(NSDictionary *) message {
    dispatch_async(dispatch_get_main_queue(), ^{
        [Message updateMessageStatus:[message objectForKey:@"pwDid"]
                             messageId:[message objectForKey:@"uid"]
                   withCompletionBlock:^(BOOL result, NSError *error) {
            if (error) {
                [Utilities printError:error];
            }
        }];
    });
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
    NSString *logoUrl = [requestDict objectForKey: @"profileUrl"];
    NSString *pwDid = [requestDict objectForKey: @"pwDid"];
    NSString *additionalData = [requestDict objectForKey: @"additionalData"];

    [cell updateAttribute:name
                 subtitle:goal
                  logoUrl:logoUrl
           acceptCallback:^() {
        [self handleAcceptAction:data
                         forType:type
                           pwDid:pwDid
                  additionalData:additionalData
                            name:name
             withCompletionBlock:^(BOOL result, NSError *error) {
            [self switchActionToHistoryView: uuid];
        }];
                }
            rejectCallback:^() {
        [self handleRejectAction:data
                         forType:type
                           pwDid:pwDid
                  additionalData:additionalData
                            name:name
             withCompletionBlock:^(BOOL result, NSError *error) {
            [self switchActionToHistoryView: uuid];
            if (type == OOB || type == COMMITTED_QUESTION) {
                [self addRejectAction: name];
            }
        }];
            }
     ];
    [cell.accept setTitle:@"Handle" forState:UIControlStateNormal];
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
     
