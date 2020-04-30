//
//  AppDelegate.m
//  CMeSdkObjc
//
//  Created by Norman Jarvis on 5/3/19.
//  Copyright © 2019 Norman Jarvis. All rights reserved.
//

#import "AppDelegate.h"
#import "PoolTxnGenesis.h"
#import <Security/Security.h>
@import Firebase;

#define SYSTEM_VERSION_GRATERTHAN_OR_EQUALTO(v) ([[[UIDevice currentDevice] systemVersion] compare:v options:NSNumericSearch] != NSOrderedAscending)

@interface AppDelegate ()

@end

@implementation AppDelegate
@synthesize sdkInited;

#define levelMappings @{@"1": @"Error", @"2": @"Warning", @"3": @"Info", @"4": @"Debug", @"5": @"Trace"}

+(NSString*)removeJSONConfig:(NSString*)jsonConfig
                    toRemove:(NSString*)values
{
  NSError* error;
  NSMutableDictionary *parsedValues;
  if (values == nil) {
    parsedValues = [NSMutableDictionary dictionary];
  } else {
    parsedValues = [NSJSONSerialization JSONObjectWithData:[values dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingMutableContainers error:&error];
  }
  NSMutableDictionary *currentConfig = [NSJSONSerialization JSONObjectWithData:[jsonConfig dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingMutableContainers error:&error];
  for (NSString *obj in parsedValues)
  {
    [currentConfig removeObjectForKey:obj];
  }

  NSData *jsonData = [NSJSONSerialization dataWithJSONObject:currentConfig
                                                     options:(NSJSONWritingOptions) (0)
                                                       error:&error];
  return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}


+(NSString*)updateJSONConfig:(NSString*)jsonConfig
                  withValues:(NSString*)values
{
  NSError* error;
  NSMutableDictionary *parsedValues;
  if (values == nil) {
    parsedValues = [NSMutableDictionary dictionary];
  } else {
    parsedValues = [NSJSONSerialization JSONObjectWithData:[values dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingMutableContainers error:&error];
  }
  NSMutableDictionary *currentConfig = [NSJSONSerialization JSONObjectWithData:[jsonConfig dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingMutableContainers error:&error];
  for (NSString *obj in parsedValues)
  {
    currentConfig[obj] = parsedValues[obj];
  }

  NSData *jsonData = [NSJSONSerialization dataWithJSONObject:currentConfig
                                                     options:(NSJSONWritingOptions) (0)
                                                       error:&error];
  return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}


+(NSString*)updateJSONConfig:(NSString*)jsonConfig
                     withKey:(NSString*)key
                   withValue:(NSString*)value
{
  NSError* error;
  NSMutableDictionary *currentConfig = [NSJSONSerialization JSONObjectWithData:[jsonConfig dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingMutableContainers error:&error];
  currentConfig[key] = value;
  NSData *jsonData = [NSJSONSerialization dataWithJSONObject:currentConfig
                                                     options:(NSJSONWritingOptions) (0)
                                                       error:&error];
  return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}


- (id)init
{
  self = [super init];
  if (self) {
    // Initialization code here.
    self.sdkApi = [[ConnectMeVcx alloc] init];
    self.sdkInited = false;
  }

  return self;
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
  // If you are receiving a notification message while your app is in the background,
  // this callback will not be fired till the user taps on the notification launching the application.
  //[self showAlert:userInfo];
  NSLog(@"[1] User Info : %@", userInfo);
  [self handleRemoteNotification:[UIApplication sharedApplication] userInfo:userInfo];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
  // If you are receiving a notification message while your app is in the background,
  // this callback will not be fired till the user taps on the notification launching the application.
  //[self showAlert:userInfo];
  NSLog(@"[2] User Info : %@", userInfo);
  completionHandler(UIBackgroundFetchResultNewData);
  [self handleRemoteNotification:[UIApplication sharedApplication] userInfo:userInfo];
}


//Called when a notification is delivered to a foreground app.
-(void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler {
  NSLog(@"[3] User Info : %@", notification.request.content.userInfo);
  completionHandler(UNAuthorizationOptionSound | UNAuthorizationOptionAlert | UNAuthorizationOptionBadge);
  [self handleRemoteNotification:[UIApplication sharedApplication] userInfo:notification.request.content.userInfo];
}


//Called to let your app know which action was selected by the user for a given notification.
-(void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void(^)())completionHandler{
  NSLog(@"[4] User Info : %@", response.notification.request.content.userInfo);
  completionHandler();
  [self handleRemoteNotification:[UIApplication sharedApplication] userInfo:response.notification.request.content.userInfo];
}


-(void) handleRemoteNotification:(UIApplication *) application   userInfo:(NSDictionary *) remoteNotif {
  NSLog(@"handleRemoteNotification");
  NSLog(@"Handle Remote Notification Dictionary: %@", remoteNotif);

  // Handle Click of the Push Notification From Here…
  // You can write a code to redirect user to specific screen of the app here….

  /*
   Handle Remote Notification Dictionary: {
   aps =     {
   "content-available" = 1;
   };
   forDID = XuG9QwaLATDZdYYM4DJY7S;
   "gcm.message_id" = "0:1557343662684237%243f4476243f4476";
   pushNotifMsgText = "Remote connection responded with successful response (detail: uid -> ndi3zgr, msg type -> a Conn Req Answer)";
   pushNotifMsgTitle = "Hi there";
   senderLogoUrl = "<set a default logo-url>";
   type = success;
   uid = ndi3zgr;
   }

   Handle Remote Notification Dictionary: {
   aps =     {
   alert = "Thrift CU is offering you a credential: Member Information";
   badge = 1;
   };
   forDID = XuG9QwaLATDZdYYM4DJY7S;
   "gcm.message_id" = "0:1557343832193625%243f4476243f4476";
   "google.c.a.e" = 1;
   pushNotifMsgText = "Thrift CU is offering you a credential: Member Information";
   pushNotifMsgTitle = "Thrift CU is offering you a credential: Member Information";
   senderLogoUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSPgI3HfspcXjsAA9xDHRA_T7xShb5GvbXF3OvPUXoPIhIBIjXecA";
   type = credOffer;
   uid = n2ixzty;
   }

   */

  NSString *notifType = remoteNotif[@"type"];
  NSString *forDID = remoteNotif[@"forDID"];
  NSString *msgNotifUid = remoteNotif[@"uid"];
  NSLog(@"notifType is: %@", notifType);
  NSUserDefaults *standardUserDefaults = [NSUserDefaults standardUserDefaults];

  if ([notifType  isEqual: @"cred"]) {
    NSString *serializedConnection = [standardUserDefaults stringForKey:@"serializedConnection"];
    NSLog(@"Using serializedConnection: %@", serializedConnection);

    NSError *error;
    NSMutableDictionary *connValues = [NSJSONSerialization JSONObjectWithData:[serializedConnection dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingMutableContainers error:&error];

    NSInteger credentialHandle = [[standardUserDefaults stringForKey:@"credentialHandle"] integerValue];

    // credentialHandle  NSInteger  2710148733
    // it would return error code, json string of credential inside callback
    [self.sdkApi credentialSerialize:credentialHandle completion:^(NSError *error, NSString *claimOffer) {
      if (error != nil && error.code != 0)
      {
        NSLog(@"Error occurred while serializing claim offer - %@ :: %ld", error, (long)error.code);
      }else{

        // credentialHandle  int  -1584818563
        [self.sdkApi credentialGetState:credentialHandle completion:^(NSError *error, NSInteger state) {
          if (error != nil && error.code != 0) {
            //NSString *indyErrorCode = [NSString stringWithFormat:@"%ld", (long)error.code];
            NSLog(@"Error occurred while getting claim offer state - %@ :: %ld", error, (long)error.code);
          }
          else {

            // credentialHandle  int  -1584818563
            [self.sdkApi credentialUpdateState:credentialHandle
                                    completion:^(NSError *error, NSInteger state)
             {
               if (error != nil && error.code != 0) {
                 //NSString *indyErrorCode = [NSString stringWithFormat:@"%ld", (long)error.code];
                 NSLog(@"Error occurred while updating claim offer state - %@ :: %ld", error, (long)error.code);
               }
               else {

                 // credentialHandle  int  -1584818563
                 [self.sdkApi getCredential:credentialHandle completion:^(NSError *error, NSString *credential) {
                   if (error != nil && error.code != 0) {
                     NSLog(@"Error occurred while getting claim - %@ :: %ld", error, (long)error.code);
                   }
                   else {

                     if (standardUserDefaults) {
                       [standardUserDefaults setObject:credential forKey:@"credential"];
                       [standardUserDefaults synchronize];
                     }

                     //  messageStatus  NSTaggedPointerString *  @"MS-106"  0xa003630312d534d6
                     //  pwdidsJson  __NSCFString *  @"[{\"pairwiseDID\":\"V4vjyfE6LXAEoaGANDSscJ\",\"uids\":[\"ytg5mjg\",\"yta5zdr\"]}]"  0x00000001c02ba2e0
                     [self.sdkApi updateMessages:@"MS-106" pwdidsJson:[NSString stringWithFormat:@"[{\"pairwiseDID\":\"%@\",\"uids\":[\"%@\"]}]", connValues[@"data"][@"pw_did"], msgNotifUid] completion:^(NSError *error) {
                       if (error != nil && error.code !=0) {
                         NSLog(@"Error occured while updating message status - %@ :: %ld", error, (long)error.code);
                       } else {
                         NSLog(@"Updated messages for message: %@ and credentialHandle: %ld", msgNotifUid, credentialHandle);
                       }
                     }];
                   }
                 }];
               }
             }];
          }
        }];
      }
    }];

  } else if([notifType  isEqual: @"credOffer"]) {
    NSString *serializedConnection = [standardUserDefaults stringForKey:@"serializedConnection"];
    NSLog(@"Using serializedConnection: %@", serializedConnection);

    NSError *error;
    NSMutableDictionary *connValues = [NSJSONSerialization JSONObjectWithData:[serializedConnection dataUsingEncoding:NSUTF8StringEncoding] options:NSJSONReadingMutableContainers error:&error];

//  // Steps to reply for a type = credOffer push notification
//  serializedConnection  = {"version":"1.0","data":{"source_id":"oweynjl","pw_did":"Vevsjg44LRrhWuEUiSgCLF","pw_verkey":"GcorBppuNkskjufCTGYgdyBVsv9td14EufPToWxBsLcc","state":4,"uuid":"","endpoint":"","invite_detail":{"statusCode":"MS-101","connReqId":"oweynjl","senderDetail":{"name":"Thrift CU","agentKeyDlgProof":{"agentDID":"DS3UV9eDYTvJNnDxgLtXo1","agentDelegatedKey":"7mzadBYfut82L22UbfR3vtZjy3LXQznFdxTnQcAxW3oA","signature":"LwQio2HSCQWaLh3lywZJgePqJrG44xM4QWdUXzCVb7XQusr4uadcOg4ZmUs0fdIJICtSetAMIXRDJuVMwS0/Aw=="},"DID":"KXePhRBpmQVVjgnHZKLGNt","logoUrl":"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSPgI3HfspcXjsAA9xDHRA_T7xShb5GvbXF3OvPUXoPIhIBIjXecA","verKey":"B6idwEhUWGXrfbEvXB99Co1kHBAcoVTxB2xx4az4Ybrn"},"senderAgencyDetail":{"DID":"UNM2cmvMVoWpk6r3pG5FAq","verKey":"FvA7e4DuD2f9kYHq6B3n7hE7NQvmpgeFRrox3ELKv9vX","endpoint":"52.26.236.159:80/agency/msg"},"targetName":"there","statusMsg":"message created","threadId":null},"invite_url":null,"agent_did":"2XyqA1ZEage1Tu7M24QNmQ","agent_vk":"qVq25jwsCwHDke3coDsrKKhqUJzRLDgHZSZkz1X1gQk","their_pw_did":"KXePhRBpmQVVjgnHZKLGNt","their_pw_verkey":"B6idwEhUWGXrfbEvXB99Co1kHBAcoVTxB2xx4az4Ybrn","public_did":null,"their_public_did":null}}
    [self.sdkApi connectionDeserialize:serializedConnection completion:^(NSError *error, NSInteger connectionHandle) {
      if (error != nil && error.code != 0)
      {
        NSLog(@"Error occurred while deserializing connection - %@ :: %ld", error, (long)error.code);
      } else {
//        sourceId  NSTaggedPointerString *  @"ytg5mjg"  0xa676a6d356774797
//        connectionHandle  NSInteger  2136885044
//        messageId  NSTaggedPointerString *  @"ytg5mjg"  0xa676a6d356774797
        NSLog(@"Using connectionHandle: %ld AND using msgNotifUid: %@", connectionHandle, msgNotifUid);
        [self.sdkApi credentialCreateWithMsgid:msgNotifUid
            connectionHandle:connectionHandle
                       msgId:msgNotifUid
                  completion:^(NSError *error, NSInteger credentialHandle, NSString* credentialOffer) {
                    if (error != nil && error.code != 0)
                    {
                      NSLog(@"Error occurred while creating credential handle - %@ :: %ld", error, (long)error.code);
                    } else {
//                      NSDictionary* vcxCredentialCreateResult = @{
//                          @"credential_handle": @(credentialHandle),
//                          @"credential_offer": credentialOffer
//                          };
                      NSLog(@"Received credentialOffer: %@ FOR credentialHandle: %ld", credentialOffer, credentialHandle);

                      //credentialHandle  NSInteger  1947028328
                      [self.sdkApi credentialSerialize:credentialHandle completion:^(NSError *error, NSString *claimOffer) {
                        if (error != nil && error.code != 0)
                        {
                          NSLog(@"Error occurred while serializing claim offer - %@ :: %ld", error, (long)error.code);
                        }else{

                          NSLog(@"Received claimOffer: %@ FOR credentialHandle: %ld", claimOffer, credentialHandle);

                          // credentialHandle  int  1947028328
                          [self.sdkApi credentialGetState:credentialHandle completion:^(NSError *error, NSInteger state) {
                            if (error != nil && error.code != 0) {
                              NSLog(@"Error occurred while getting claim offer state - %@ :: %ld", error, (long)error.code);
                            }
                            else {

                              NSLog(@"Received state: %ld FOR credentialHandle: %ld", state, credentialHandle);

                              if (standardUserDefaults) {
                                [standardUserDefaults setObject:claimOffer forKey:@"serializedCredential"];
                                [standardUserDefaults setObject:[NSString stringWithFormat:@"%ld", credentialHandle] forKey:@"credentialHandle"];
                                [standardUserDefaults synchronize];
                              }

                              // Now you see the screen in ConnectMe asking you to accept the credOffer from the offering institution
                              // We are assuming that the user wantes to accept the credential
                              // Use the claimOffer NSString as the parameter to the [self.sdkApi credentialDeserialize:claimOffer
                              //  if you click Accept then the following APIs are called...
                              //  serializedCredential  __NSCFString *  @"{\"version\":\"1.0\",\"data\":{\"source_id\":\"ytg5mjg\",\"state\":3,\"credential_name\":null,\"credential_request\":null,\"credential_offer\":{\"msg_type\":\"CRED_OFFER\",\"version\":\"0.1\",\"to_did\":\"AFMYF6MzsSPkpvVoRW14gR\",\"from_did\":\"AFMYF6MzsSPkpvVoRW14gR\",\"libindy_offer\":\"{\\\"schema_id\\\":\\\"BYyWHMqbjKBfUUJF3ZaiBW:2:Thrift Credit Union1541457058512:1.0\\\",\\\"cred_def_id\\\":\\\"BYyWHMqbjKBfUUJF3ZaiBW:3:CL:10834:tag1\\\",\\\"key_correctness_proof\\\":{\\\"c\\\":\\\"14007556475921899032787861664467072135879585214429915234844686005272417568693\\\",\\\"xz_cap\\\":\\\"2680623921933743826869199161170119899940341158466912771174025029452600687620600243786251511024030243435443588280175737569190676239049624236533677044504417265464681602621615877721922176644143315888860654065229246387507301439740177831563689118076828602868660203566877228687773996489232840723184873020306158158991278002461448554957961314634925771368690388177855123975584136502750957534477256537883425812562692356539243650637820535811080614112175873264547226833914835877790773872313102427025226204133901376"  0x0000000106b0c800
                              //NSString serializedCredential must be converted to cString because special characters in NSString
                              const char *cLetter = (const char *)[claimOffer cStringUsingEncoding:NSUTF8StringEncoding];
                              NSString *serializedCredential_cString = [[NSString alloc] initWithCString:cLetter encoding:NSUTF8StringEncoding];
                              // it would return an error code and an integer credential handle in callback
                              [self.sdkApi credentialDeserialize:serializedCredential_cString
                                completion:^(NSError *error, NSInteger credentailHandle) {
                                  if (error != nil && error.code != 0) {
                                    NSLog(@"Error occurred while deserializing claim offer - %@ :: %ld", error, (long)error.code);
                                  }
                                  else {
                                    // credentialHandle  NSInteger  2710148733
                                    // connectionHandle  NSInteger  2136885044
                                    // paymentHandle  NSInteger  0
                                    [self.sdkApi credentialSendRequest:credentialHandle
                                                                      connectionHandle:connectionHandle
                                                                         paymentHandle:0
                                      completion:^(NSError *error) {
                                        if (error != nil && error.code != 0)
                                        {
                                          NSLog(@"Error occurred while sending claim request - %@ :: %ld", error, (long)error.code);
                                        }
                                        else {
                                          NSLog(@"Successfully sent the credential request for credentialHandle: %ld AND for connectionHandle: %ld", credentailHandle, connectionHandle);

                                           //  messageStatus  NSTaggedPointerString *  @"MS-106"  0xa003630312d534d6
                                           //  pwdidsJson  __NSCFString *  @"[{\"pairwiseDID\":\"V4vjyfE6LXAEoaGANDSscJ\",\"uids\":[\"ytg5mjg\",\"yta5zdr\"]}]"  0x00000001c02ba2e0
                                           [self.sdkApi updateMessages:@"MS-106" pwdidsJson:[NSString stringWithFormat:@"[{\"pairwiseDID\":\"%@\",\"uids\":[\"%@\"]}]", connValues[@"data"][@"pw_did"], msgNotifUid] completion:^(NSError *error) {
                                             if (error != nil && error.code !=0) {
                                               NSLog(@"Error occured while updating message status - %@ :: %ld", error, (long)error.code);
                                             } else {
                                               NSLog(@"Updated messages for message: %@ and credentialHandle: %ld", msgNotifUid, credentialHandle);
                                             }
                                           }];

                                        }
                                      }];
                                  }
                                }];
                            }
                          }];
                        }
                      }];
                    }
                  }];
          }
      }];

  }


}


- (void)registerForRemoteNotifications {

  if(SYSTEM_VERSION_GRATERTHAN_OR_EQUALTO(@"10.0")){
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    center.delegate = self;
    [center requestAuthorizationWithOptions:(UNAuthorizationOptionSound | UNAuthorizationOptionAlert | UNAuthorizationOptionBadge) completionHandler:^(BOOL granted, NSError * _Nullable error){
      if(!error) {
        dispatch_async(dispatch_get_main_queue(), ^{
          [[UIApplication sharedApplication] registerForRemoteNotifications];
        });
      }
    }];
  } else {
    // Code for old versions
    UIUserNotificationType userNotificationTypes = (UIUserNotificationTypeAlert |
                                                    UIUserNotificationTypeBadge |
                                                    UIUserNotificationTypeSound);

    UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:userNotificationTypes
                                                                             categories:nil];

    [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
    [[UIApplication sharedApplication] registerForRemoteNotifications];
  }
}


- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)deviceToken {

//  NSString *firebaseClientId = [FIRApp defaultApp].options.clientID;
//  NSLog(@"firebaseClientId is %@", firebaseClientId);
//
//  NSString *tokenString = [deviceToken description];
//  tokenString = [[deviceToken description] stringByTrimmingCharactersInSet:[NSCharacterSet characterSetWithCharactersInString:@"<>"]];
//  tokenString = [tokenString stringByReplacingOccurrencesOfString:@" " withString:@""];
//  NSLog(@"Push Notification tokenstring is %@", tokenString);

//  [[FIRInstanceID instanceID] instanceIDWithHandler:^(FIRInstanceIDResult * _Nullable result,
//                                                      NSError * _Nullable error) {
//    if (error != nil) {
//      NSLog(@"Error fetching remote instance ID: %@", error);
//    } else {
//      NSLog(@"Remote instance ID token: %@", result.token);
//      [[NSUserDefaults standardUserDefaults]setObject:[NSString stringWithFormat:@"FCM:%@", result.token] forKey:@"DeviceTokenFinal"];
//      [[NSUserDefaults standardUserDefaults]synchronize];
//    }
//  }];
}


- (void)messaging:(FIRMessaging *)messaging didReceiveRegistrationToken:(NSString *)fcmToken {
  // Note: This callback is fired at each app startup and whenever a new token is generated.
    
  NSLog(@"FCM registration token: %@", fcmToken);
  // Notify about received token.
  NSDictionary *dataDict = [NSDictionary dictionaryWithObject:fcmToken forKey:@"token"];
  [[NSNotificationCenter defaultCenter] postNotificationName:
   @"FCMToken" object:nil userInfo:dataDict];

  if (self.sdkInited) {
    NSString *pushNotifConfig = [NSString stringWithFormat:@"{\"id\": \"%@\", \"value\":\"%@\"}", [[NSUUID UUID] UUIDString], fcmToken];
    NSLog(@"3) Sending pushNotifConfig: %@", pushNotifConfig);
    [self.sdkApi agentUpdateInfo:pushNotifConfig completion:^(NSError *error) {
      if (error != nil && error.code != 0)
      {
        NSString *indyErrorCode = [NSString stringWithFormat:@"%ld", (long)error.code];
        NSLog(@"3) Value of indyErrorCode is: %@", indyErrorCode);
      } else {
        NSLog(@"Updated the push notification token to: %@", fcmToken);
      }
    }];
  }

  NSLog(@"Remote instance ID token: %@", fcmToken);
  [[NSUserDefaults standardUserDefaults]setObject:[NSString stringWithFormat:@"FCM:%@", fcmToken] forKey:@"DeviceTokenFinal"];
  [[NSUserDefaults standardUserDefaults]synchronize];
}


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {

//  [FIRApp configure];
//  [FIRMessaging messaging].delegate = self;
  [self registerForRemoteNotifications];

  UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:[NSBundle mainBundle]];
  UIViewController *vc = [storyboard instantiateInitialViewController];

  // Set root view controller and make windows visible
  self.window = [[UIWindow alloc] initWithFrame:UIScreen.mainScreen.bounds];
  self.window.rootViewController = vc;
  [self.window makeKeyAndVisible];


  // Step a.
  [VcxLogger setDefaultLogger:@"DEBUG"];
  //    [VcxLogger setLogger:^(NSObject *context, NSNumber *level, NSString *target, NSString *message, NSString *modulePath, NSString *file, NSNumber *line) {
  //        NSLog(@"[Inside VcxLogger.setLogger callback] %@    %@:%@ | %@", [levelMappings valueForKey:[NSString stringWithFormat:@"%@", level]], file, line, message);
  //    }];


  // Step b.
  NSError *error;
  NSString *fileName = @"pool_transactions_genesis_DEMO";
  NSString *filePath = [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject] stringByAppendingPathComponent:fileName];
  NSFileManager *fileManager = [NSFileManager defaultManager];
  if (![fileManager fileExistsAtPath: filePath])
  {
    BOOL success=[poolTxnGenesis writeToFile:filePath atomically:YES encoding:NSUTF8StringEncoding error:&error];
    if(!success)
    {
      NSLog(@"error while creating pool transaction genesis file");
    }
  }
  NSLog(@"Creating pool transaction genesis file was successful: %@", filePath);

  // Step c.
  [self.sdkApi initSovToken];
    
  // Step d.
  NSString *walletName = @"wallet_name";
  NSString *walletKey;
  // Generate secure random string for wallet_key
  NSMutableData *data = [NSMutableData dataWithLength:128];
  int result = SecRandomCopyBytes(NULL, 128, data.mutableBytes);
  if (result == 0) {
    NSLog(@"create walletKey was successful!");
    walletKey = [data base64EncodedStringWithOptions:0];
  } else {
    NSString *indyErrorCode = @"W-001: Error occurred while generating wallet key";
    NSLog(@"Value of indyErrorCode is: %@", indyErrorCode);
  }

  NSString *agencyConfig = [NSString stringWithFormat:@"{\"agency_url\":\"http://agency.pps.evernym.com\",\"agency_did\":\"3mbwr7i85JNSL3LoNQecaW\",\"agency_verkey\":\"2WXxo6y1FJvXWgZnoYUP5BJej2mceFrqBDNPE3p6HDPf\",\"wallet_name\":\"%@\",\"wallet_key\":\"%@\",\"agent_seed\":null,\"enterprise_seed\":null}", walletName, walletKey];

  [self.sdkApi agentProvisionAsync:agencyConfig completion:^(NSError *error, NSString *oneTimeInfo) {

    //{"agency_did":"3mbwr7i85JNSL3LoNQecaW","agency_endpoint":"http://agency.pps.evernym.com","agency_verkey":"2WXxo6y1FJvXWgZnoYUP5BJej2mceFrqBDNPE3p6HDPf","genesis_path":"<CHANGE_ME>","institution_did":"7c6iuHAJQCqfGzLwsW5ASV","institution_logo_url":"<CHANGE_ME>","institution_name":"<CHANGE_ME>","institution_verkey":"4boWELPYk4tUbA52kFcrYvHG7mnBc4isY2mVEp3aWruw","protocol_type":"1.0","remote_to_sdk_did":"GH1Ww8cJdgLkDspBiY3NzJ","remote_to_sdk_verkey":"9KuVJR8mxnJ1GovpktZPRQY7uJNS6f3tRbioDMg5L1Tb","sdk_to_remote_did":"7c6iuHAJQCqfGzLwsW5ASV","sdk_to_remote_verkey":"4boWELPYk4tUbA52kFcrYvHG7mnBc4isY2mVEp3aWruw","wallet_key":"uYRcdn0EMTRD2py7DkWSaFc6sHimgiuRMHN1yUd1P/1AFvGtKAvt1vMZTwv6OFnaxzfSgfr9yVntJ1RzyLTKbQ==","wallet_name":"eb064c19-436e-af87-b8d3-028e55d3e27f-cm-wallet"}

    NSLog(@"applicationDidBecomeActive callback:%@",oneTimeInfo);
    if (error != nil && error.code != 0)
    {
      NSString *indyErrorCode = [NSString stringWithFormat:@"Error occurred while creating one time info: %@ :: %ld", error.domain, (long)error.code];
      NSLog(@"Value of indyErrorCode is: %@ -- %@", indyErrorCode, oneTimeInfo);
    } else {
      NSLog(@"createOneTimeInfo was successful: %@", oneTimeInfo);
    }


    // Step e.
    NSMutableDictionary *keychainVcxConfig = [NSMutableDictionary dictionary];
    // We specify what kind of keychain item this is.
    keychainVcxConfig[(__bridge id)kSecClass] = (__bridge id)kSecClassGenericPassword;
    // This item can only be accessed when the user unlocks the device.
    keychainVcxConfig[(__bridge id)kSecAttrAccessible] = (__bridge id)kSecAttrAccessibleWhenUnlocked;
    keychainVcxConfig[(__bridge id)kSecAttrType] = @"vcxConfig";
    keychainVcxConfig[(__bridge id)kSecAttrLabel] = walletName;

    NSString *vcxConfig;
    if (oneTimeInfo == nil) {
      // get vcxConfig from secure keychain storage: https://www.andyibanez.com/using-ios-keychain/
      keychainVcxConfig[(__bridge id)kSecReturnData] = (__bridge id)kCFBooleanTrue;
      keychainVcxConfig[(__bridge id)kSecReturnAttributes] = (__bridge id)kCFBooleanTrue;
      CFDictionaryRef result = nil;
      OSStatus sts = SecItemCopyMatching((__bridge CFDictionaryRef)keychainVcxConfig, (CFTypeRef *)&result);
      //NSLog(@"Error Code while finding vcxConfig: %d", (int)sts);

      if(sts == noErr)
      {
        NSDictionary *resultDict = (__bridge_transfer NSDictionary *)result;
        NSData *vcxConfigData = resultDict[(__bridge id)kSecValueData];
        vcxConfig = [[NSString alloc] initWithData:vcxConfigData encoding:NSUTF8StringEncoding];
      } else {
        NSLog(@"Error Code while finding vcxConfig: %d", (int)sts);
      }

    } else {
      // update: institution_logo_url, institution_name, pool_name, config, and genesis_path
      vcxConfig = [AppDelegate
                   updateJSONConfig:oneTimeInfo
                   withValues:[NSString stringWithFormat:@"{\"genesis_path\": \"%@\", \"institution_logo_url\": \"%@\", \"institution_name\": \"%@\", \"pool_name\":\"7e96cbb3b0a1711f3b843af3cb28e31dcmpool\", \"protocol_version\":\"2\"}",
                               filePath, @"https://robothash.com/logo.png", @"real institution name"]
                   ];
      //            vcxConfig = [AppDelegate updateJSONConfig:[AppDelegate
      //                updateJSONConfig:oneTimeInfo
      //                    withValues:[NSString stringWithFormat:@"{\"genesis_path\": \"%@\", \"institution_logo_url\": \"%@\", \"institution_name\": \"%@\", \"pool_name\":\"7e96cbb3b0a1711f3b843af3cb28e31dcmpool\", \"protocol_version\":\"2\", \"protocol_type\": \"1.0\"}",
      //                             filePath, @"https://robothash.com/logo.png", @"real institution name"]
      //                ] withKey:@"config" withValue:poolTxnGenesis];

      //Check if the keychainVcxConfig already exists.
      // store the vcxConfig into the secure keychain storage!
      if(SecItemCopyMatching((__bridge CFDictionaryRef)keychainVcxConfig, NULL) == noErr) {
        //The item was found.
        //We can update the keychain item.
        NSMutableDictionary *attributesToUpdate = [NSMutableDictionary dictionary];
        attributesToUpdate[(__bridge id)kSecValueData] = [vcxConfig dataUsingEncoding:NSUTF8StringEncoding];
        OSStatus sts = SecItemUpdate((__bridge CFDictionaryRef)keychainVcxConfig, (__bridge CFDictionaryRef)attributesToUpdate);
        NSLog(@"Error Code while updating vcxConfig: %d", (int)sts);
      } else {
        keychainVcxConfig[(__bridge id)kSecValueData] = [vcxConfig dataUsingEncoding:NSUTF8StringEncoding];
        OSStatus sts = SecItemAdd((__bridge CFDictionaryRef)keychainVcxConfig, NULL);
        NSLog(@"Error Code while adding new vcxConfig: %d", (int)sts);
      }
    }
    //NSLog(@"vcxConfig is set to: %@", [AppDelegate removeJSONConfig:vcxConfig toRemove:@"{\"config\": \"\"}"]);
    NSLog(@"vcxConfig is set to: %@", vcxConfig);


    // Step 5
    //{"agency_did":"3mbwr7i85JNSL3LoNQecaW","pool_name":"511c910f5352c34302a5ae694c3c5d74cmpool","remote_to_sdk_did":"RYMaQok8vn4erEUzzYHkZt","sdk_to_remote_verkey":"FBgYdX87ZZaGtDCoiWcPFCymhhQFknXrRUkrq8UoXLdg","institution_name":"some-random-name","institution_verkey":"FBgYdX87ZZaGtDCoiWcPFCymhhQFknXrRUkrq8UoXLdg","remote_to_sdk_verkey":"ENn3tx4PE6zRmcqu5XoSRNUxNs176dTs62i3gFAjzPWA","agency_endpoint":"http:\/\/agency.pps.evernym.com","genesis_path":"\/var\/mobile\/Containers\/Data\/Application\/2B850908-CEB7-4783-B9D3-C326DE6D3A74\/Documents\/pool_transactions_genesis_DEMO","agency_verkey":"2WXxo6y1FJvXWgZnoYUP5BJej2mceFrqBDNPE3p6HDPf","wallet_key":"MYo3hlmmEgqtD42N8Jk16hj6\/ScQBx0EYvmsQzEd0FK9XnhKb7SmEbl6iDtemGiP+1ir0k3LV4veuOCNOu3mpA==","institution_logo_url":"https:\/\/robothash.com\/logo.png","institution_did":"T2Qzy9pNAj2msF7meWd69M","wallet_name":"511c910f-5352-c343-02a5-ae694c3c5d74-cm-wallet","sdk_to_remote_did":"T2Qzy9pNAj2msF7meWd69M"}

    [self.sdkApi initWithConfig:vcxConfig completion:^(NSError *error) {
      if (error != nil && error.code != 0)
      {
        NSString *indyErrorCode = [NSString stringWithFormat:@"Error occurred while initializing vcx: %@ :: %ld", error.domain, (long)error.code];
        NSLog(@"Value of indyErrorCode is: %@", indyErrorCode);
      } else {

        NSUserDefaults *standardUserDefaults = [NSUserDefaults standardUserDefaults];
        if (standardUserDefaults) {
          NSString *tokenString = [standardUserDefaults stringForKey:@"DeviceTokenFinal"];
          NSLog(@"3) Using tokenString: %@", tokenString);
          NSString *pushNotifConfig = [NSString stringWithFormat:@"{\"id\": \"%@\", \"value\":\"%@\"}", [[NSUUID UUID] UUIDString], tokenString];
          NSLog(@"3) Sending pushNotifConfig: %@", pushNotifConfig);
          [self.sdkApi agentUpdateInfo:pushNotifConfig completion:^(NSError *error) {
            if (error != nil && error.code != 0)
            {
              NSString *indyErrorCode = [NSString stringWithFormat:@"%ld", (long)error.code];
              NSLog(@"3) Value of indyErrorCode is: %@", indyErrorCode);
            } else {
              NSLog(@"Updated the push notification token to: %@", tokenString);
            }
          }];
        }

        self.sdkInited = true;
        NSLog(@"init was successful!");
      }
    }];

  }];

  return YES;
}


- (void)applicationWillResignActive:(UIApplication *)application {
  // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
  // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
  // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
  // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
  // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
}


- (void)applicationDidBecomeActive:(UIApplication *)application {
  // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}


- (void)applicationWillTerminate:(UIApplication *)application {
  // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}


@end
