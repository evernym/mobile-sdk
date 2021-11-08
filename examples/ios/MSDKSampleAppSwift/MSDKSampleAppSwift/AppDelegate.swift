//
//  AppDelegate.swift
//  MSDKSampleAppSwift
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
        
        window = UIWindow(frame: UIScreen.main.bounds);
                
        let storyboard = UIStoryboard(name: "Main", bundle: nil);
        
        let homeViewController = storyboard.instantiateViewController(withIdentifier: "HomeViewController");
        let historyWatchViewController = storyboard.instantiateViewController(withIdentifier: "HistoryViewController");
        
        let tabBarController = UITabBarController();
        tabBarController.viewControllers = [homeViewController, historyWatchViewController];
        
        window?.rootViewController = tabBarController;
        window?.makeKeyAndVisible();
        
        MobileSDK.shared().sdkApi = ConnectMeVcx.init();
        CMConfig.initVCX();

        return true;
    }
}
