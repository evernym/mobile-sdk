//
//  AppDelegate.swift
//  CMeSdkSwift
//
//  Created by Norman Jarvis on 5/7/19.
//  Copyright © 2019 Norman Jarvis. All rights reserved.
//

import UIKit
import vcx

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {

        MobileSDK.shared().sdkApi = ConnectMeVcx.init()
        CMConfig.initVCX()

        return true
    }
}
