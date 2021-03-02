//
//  AppDelegate.swift
//  CMeSdkSwift
//
//  Created by Norman Jarvis on 5/7/19.
//  Copyright © 2019 Norman Jarvis. All rights reserved.
//

import UIKit
import vcx
import Firebase
import FirebaseMessaging

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {

        FirebaseApp.configure()
        MobileSDK.shared().sdkApi = ConnectMeVcx.init()
        CMConfig.initVCX()

        return true
    }

    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any]) {
        // If you are receiving a notification message while your app is in the background,
        // this callback will not be fired till the user taps on the notification launching the application.
        print("[1] User Info: ", userInfo)
        self.handleRemoteNotification(application, remoteNotif: userInfo)
    }

    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        // If you are receiving a notification message while your app is in the background,
        // this callback will not be fired till the user taps on the notification launching the application.
        print("[2] User Info: ", userInfo)
        self.handleRemoteNotification(application, remoteNotif: userInfo)
    }

    func application(_ application: UIApplication, handleActionWithIdentifier identifier: String?, forRemoteNotification userInfo: [AnyHashable : Any], completionHandler: @escaping () -> Void) {
        print("handleRemoteNotification")
        print("Handle Remote Notification Dictionary: ", userInfo)
        self.handleRemoteNotification(application, remoteNotif: userInfo)
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        print("push token", deviceToken)
        LocalStorage.setValue(String(data: deviceToken, encoding: .utf8), forKey: "pushToken")
    }

    func handleRemoteNotification(_ application: UIApplication, remoteNotif: [AnyHashable: Any]) {
        print("handleRemoteNotification")
        print("Handle Remote Notification Dictionary: ", remoteNotif)

        // TODO: Extract messageID and public_did from push notification
        let messageID = ""
        let connectionID = ""

        guard
            let connections = LocalStorage.getObjectForKey("connections", shouldCreate: false) as? [String: Any]
        else { return }

        let connectionIDs = Array(connections.keys)

        guard
            connectionIDs.contains(connectionID),
            let connection = connections[connectionID] as? [String: Any]
        else { return }
        CMMessage.downloadMessages(connection, andType: CMMessageStatusType(rawValue: 2), andMessageID: messageID) { messageList, error in
            print("messageList \(String(describing: messageList)), \(String(describing: error))")
        }
    }

}
