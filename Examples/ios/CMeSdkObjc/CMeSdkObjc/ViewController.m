//
//  ViewController.m
//  CMeSdkObjc
//
//  Created by Norman Jarvis on 5/3/19.
//  Copyright © 2019 Norman Jarvis. All rights reserved.
//

#import "ViewController.h"
#import "AppDelegate.h"

@interface ViewController ()

@end

@implementation ViewController
@synthesize addConnLabel, addConnConfig;

UIGestureRecognizer *tapper;

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
  tapper = [[UITapGestureRecognizer alloc]
            initWithTarget:self action:@selector(handleSingleTap:)];
  tapper.cancelsTouchesInView = NO;
  [self.view addGestureRecognizer:tapper];
}


- (void)handleSingleTap:(UITapGestureRecognizer *) sender
{
  [self.view endEditing:YES];
}


- (IBAction)addNewConn:(id)sender {
    AppDelegate *appDelegate = (AppDelegate*)[[UIApplication sharedApplication] delegate];

//    [appDelegate.sdkApi downloadMessages:@"MS-103" uid_s:nil pwdids:@"" completion:^(NSError *error, NSString *messages) {
//        if (error != nil && error.code !=0) {
//            NSString *indyErrorCode = [NSString stringWithFormat:@"%ld", (long)error.code];
//            NSLog(@"1) Value of indyErrorCode is: %@", indyErrorCode);
//        } else {
//            NSLog(@"1) Value of messages is: %@", messages);
//            [appDelegate.sdkApi downloadMessages:@"MS-103" uid_s:nil pwdids:@"" completion:^(NSError *error, NSString *messages) {
//                if (error != nil && error.code !=0) {
//                    NSString *indyErrorCode = [NSString stringWithFormat:@"%ld", (long)error.code];
//                    NSLog(@"2) Value of indyErrorCode is: %@", indyErrorCode);
//                } else {
//                    NSLog(@"2) Value of messages is: %@", messages);
//                }
//            }];
//        }
//    }];



    // config -- {"id":"96c5e512-7ef6-b1fc-8a1f-5f349dc317c1","value":"FCM:f-_r62MMQgE:APA91bFB_8NRXeqtKRerz8TsH9z8HtVofMemLvWwqzI5irikHnjsPiXneQ7Ktci7olT4YczqjKu0y-qBWvos5SSkSCopgRK-tT5AkPATuJ43E8yoifZ5FYoQo4sEg4zOlnOMbWHZ8S1q"}

    //config    __NSCFString *    @"{\"id\":\"43c6aed0-0d2e-0898-2d99-d66d43264a8f\",\"value\":\"FCM:dFIrpmT-Geg:APA91bF46dA5jDG22X5P8Z6U8SSNa4d2UZOILx32KDgeXO5UBpx7TnQCdj2ivDxnIQadm6gr-ojdOOWltsgfKGh7qNXqAbUap01UHCvPvWxi0CdIUKYYfd4uHtJ1iPLmUbM98IZ2JSan\"}"    0x00000001c41da040

    NSUserDefaults *standardUserDefaults = [NSUserDefaults standardUserDefaults];
//    if (standardUserDefaults) {
//        NSString *tokenString = [standardUserDefaults stringForKey:@"DeviceTokenFinal"];
//        NSLog(@"3) Using tokenString: %@", tokenString);
//        NSString *pushNotifConfig = [NSString stringWithFormat:@"{\"id\": \"%@\", \"value\":\"%@\"}", [[NSUUID UUID] UUIDString], tokenString];
//        NSLog(@"3) Sending pushNotifConfig: %@", pushNotifConfig);
//        [appDelegate.sdkApi agentUpdateInfo:pushNotifConfig completion:^(NSError *error) {
//            if (error != nil && error.code != 0)
//            {
//                NSString *indyErrorCode = [NSString stringWithFormat:@"%ld", (long)error.code];
//                NSLog(@"3) Value of indyErrorCode is: %@", indyErrorCode);
//            } else {
//                NSLog(@"Updated the push notification token to: %@", tokenString);
//            }
//        }];
//    }

    // Create the connection using the invite JSON
    //connectionCreateWithInvite with invitationId: yti0ode; inviteDetails: {"connReqId":"yti0ode","statusCode":"MS-102","senderDetail":{"name":"Thrift CU","agentKeyDlgProof":{"agentDID":"WtPqHgACgKnszepTceJypm","agentDelegatedKey":"HHkzUUN9Hgt5uG6Nn6YErWYeoULRKBxzJCQMa11SX2Jn","signature":"6x7yY/thawgTH9XsCvZqaCYd6bw1J0mLmsYZWoyi0GLO4FnprcDUwxdVyb/D5ON66MqvsGd8waHnIxa0LFJaAA=="},"DID":"QoaKYKpUG4vTikrrU1BM6a","logoUrl":"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSPgI3HfspcXjsAA9xDHRA_T7xShb5GvbXF3OvPUXoPIhIBIjXecA","verKey":"DyTgVQioZta7YsYBFftwwWyc3PEgKSZ4ixXGaK8FDNxp"},"senderAgencyDetail":{"DID":"UNM2cmvMVoWpk6r3pG5FAq","verKey":"FvA7e4DuD2f9kYHq6B3n7hE7NQvmpgeFRrox3ELKv9vX","endpoint":"52.26.236.159:80/agency/msg"},"targetName":"Thrift CU","statusMsg":"message sent"}
    NSString *connConfig = self.addConnConfig.text;
    //NSString *connConfig = @"{\"id\":\"m2vizta\",\"s\":{\"d\":\"7t56ttyuBSBRSk6bGvqKnb\",\"dp\":{\"d\":\"8RTMRZLUxknnj67qfEZmjs\",\"k\":\"53cWcKyJNA4vvSfLq9DS7j1XttM6ExnsNNUCJUKfd7FM\",\"s\":\"WyoM8fbzqPswV3OyGeOueAwd/1k339bmqhtbnRfaB8CR0ysTTxCAMMm2lJt/jx33xYQNlHe01ZqQrLUejKLSDw==\"},\"l\":\"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSPgI3HfspcXjsAA9xDHRA_T7xShb5GvbXF3OvPUXoPIhIBIjXecA\",\"n\":\"Thrift CU\",\"v\":\"4kWQuMDFFNCokXPwdWafecbqX2mu2eSyfEnK4BjDSNkT\"},\"sa\":{\"d\":\"UNM2cmvMVoWpk6r3pG5FAq\",\"e\":\"52.26.236.159:80/agency/msg\",\"v\":\"FvA7e4DuD2f9kYHq6B3n7hE7NQvmpgeFRrox3ELKv9vX\"},\"sc\":\"MS-101\",\"sm\":\"message created\",\"t\":\"there\"}";
    NSLog(@"Connection Config: %@", connConfig);
    NSError* error;
    NSMutableDictionary *configValues = [NSJSONSerialization JSONObjectWithData:[connConfig dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingMutableContainers error:&error];

    [appDelegate.sdkApi connectionCreateWithInvite:[configValues valueForKey:@"id"] inviteDetails:connConfig
        completion:^(NSError *error, NSInteger connectionHandle) {
            if (error != nil && error.code != 0)
            {
                NSString *indyErrorCode = [NSString stringWithFormat:@"Error occurred while creating connection: %@ :: %ld", error.domain, (long)error.code];
                NSLog(@"4) Value of indyErrorCode is: %@", indyErrorCode);
            } else {
              NSLog(@"[4] createConnectionWithInvite was successful: %ld", connectionHandle);

                // connectionConnect with connectionHandle
                [appDelegate.sdkApi connectionConnect:connectionHandle
                    connectionType:@"{\"connection_type\":\"QR\",\"phone\":\"\"}"
                   completion:^(NSError *error, NSString *inviteDetails) {

                       if (error != nil && error.code != 0)
                       {
                           NSString *indyErrorCode = [NSString stringWithFormat:@"%ld", (long)error.code];
                           NSLog(@"5) Value of indyErrorCode is: %@", indyErrorCode);
                       } else {
                         NSLog(@"[5] connectionConnect was successful with inviteDetails: %@", inviteDetails);

                           [appDelegate.sdkApi connectionSerialize:connectionHandle
                                completion:^(NSError *error, NSString *state) {
                                    if (error != nil && error.code != 0)
                                    {
                                        NSString *indyErrorCode = [NSString stringWithFormat:@"%ld", (long)error.code];
                                        NSLog(@"6) Value of indyErrorCode is: %@", indyErrorCode);
                                    } else {
                                      NSLog(@"[6] connectionSerialize was successful with state: %@", state);
                                      // Store the serialized connection
                                      if (standardUserDefaults) {
                                        [standardUserDefaults setObject:state forKey:@"serializedConnection"];
                                        [standardUserDefaults synchronize];
                                      }
                                      //state = {"version":"1.0","data":{"source_id":"oweynjl","pw_did":"Vevsjg44LRrhWuEUiSgCLF","pw_verkey":"GcorBppuNkskjufCTGYgdyBVsv9td14EufPToWxBsLcc","state":4,"uuid":"","endpoint":"","invite_detail":{"statusCode":"MS-101","connReqId":"oweynjl","senderDetail":{"name":"Thrift CU","agentKeyDlgProof":{"agentDID":"DS3UV9eDYTvJNnDxgLtXo1","agentDelegatedKey":"7mzadBYfut82L22UbfR3vtZjy3LXQznFdxTnQcAxW3oA","signature":"LwQio2HSCQWaLh3lywZJgePqJrG44xM4QWdUXzCVb7XQusr4uadcOg4ZmUs0fdIJICtSetAMIXRDJuVMwS0/Aw=="},"DID":"KXePhRBpmQVVjgnHZKLGNt","logoUrl":"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSPgI3HfspcXjsAA9xDHRA_T7xShb5GvbXF3OvPUXoPIhIBIjXecA","verKey":"B6idwEhUWGXrfbEvXB99Co1kHBAcoVTxB2xx4az4Ybrn"},"senderAgencyDetail":{"DID":"UNM2cmvMVoWpk6r3pG5FAq","verKey":"FvA7e4DuD2f9kYHq6B3n7hE7NQvmpgeFRrox3ELKv9vX","endpoint":"52.26.236.159:80/agency/msg"},"targetName":"there","statusMsg":"message created","threadId":null},"invite_url":null,"agent_did":"2XyqA1ZEage1Tu7M24QNmQ","agent_vk":"qVq25jwsCwHDke3coDsrKKhqUJzRLDgHZSZkz1X1gQk","their_pw_did":"KXePhRBpmQVVjgnHZKLGNt","their_pw_verkey":"B6idwEhUWGXrfbEvXB99Co1kHBAcoVTxB2xx4az4Ybrn","public_did":null,"their_public_did":null}}

//                                        [appDelegate.sdkApi connectionSerialize:connectionHandle
//                                             completion:^(NSError *error, NSString *state2) {
//                                                 if (error != nil && error.code != 0)
//                                                 {
//                                                     NSString *indyErrorCode = [NSString stringWithFormat:@"%ld", (long)error.code];
//                                                     NSLog(@"7) Value of indyErrorCode is: %@", indyErrorCode);
//                                                 }else{
//                                                     NSLog(@"[7] connectionSerialize was successful with state: %@", state2);
//                                                 }
//                                             }];
                                    }
                                }];
                       }
                   }];

            }
        }];

}
@end
