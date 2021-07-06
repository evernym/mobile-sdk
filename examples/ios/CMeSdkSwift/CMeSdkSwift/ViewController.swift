//
//  ViewController.swift
//  CMeSdkSwift
//
//  Created by Norman Jarvis on 5/7/19.
//  Copyright © 2019 Norman Jarvis. All rights reserved.
//

import UIKit
import QRCodeReaderViewController

class ViewController: UIViewController {
    @IBOutlet var addConnectionBtn: UIButton!
    @IBOutlet var tableView: UITableView!
    @IBOutlet var infoLbl: UILabel!
    @IBOutlet var addConnLabel: UILabel!
    @IBOutlet var addConnConfigTextView: UITextView!

    var existingConnections: [String: Any] = [:]
    var isInitialized = false

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = "MobileSDK"
        let tapper = UITapGestureRecognizer.init(target: self, action: #selector(handleSingleTap))
        tapper.cancelsTouchesInView = false
        self.view.addGestureRecognizer(tapper)

        addConnConfigTextView.delegate = self
        tableView.tableFooterView = UIView.init(frame: .zero)
        addConnConfigTextView.layer.cornerRadius = 5
        addConnectionBtn.layer.cornerRadius = 5
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        if let connections = LocalStorage.getObjectForKey("connections", shouldCreate: false) as? [String: Any] {
            self.existingConnections = connections
            self.tableView.reloadData()
            NotificationCenter.default.addObserver(self, selector: #selector(self.vcxInitialized), name: NSNotification.Name(rawValue: "vcxInitialized"), object: nil)
            isInitialized = MobileSDK.shared().sdkInited
            addConnectionBtn.isEnabled = isInitialized
        }
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        NotificationCenter.default.removeObserver(self)
    }

    @objc func handleSingleTap(_ sender: UITapGestureRecognizer) {
        self.view.endEditing(true)
    }

    @IBAction func addNewConnection(_ sender: Any) {
        let connectionText = addConnConfigTextView.text ?? ""
        if connectionText.count > 3 && connectionText != "enter code here" {
//            CMConnection.connect(connectionText, connectionType: ConnectionType.QR.rawValue, phoneNumber: "") { connectionData, error  in
//                if let error = error {
//                    //                if (error != nil && error > 0) {
//                    print("Error", error.localizedDescription)
//                    return
//                }
//
//                if((connectionData) != nil) {
//                    self.addConnConfigTextView.text = ""
//                    self.performSegue(withIdentifier: "openConnectionDetails", sender: connectionData)
//                }
//            }
        }
    }

    @IBAction func openConnection(_ sender: Any) {
        if existingConnections.count > 0 {
//            let connectionIDs = self.existingConnections.keys
            //            self.performSegue(withIdentifier: "openConnectionDetails", sender: <#T##Any?#>)
            //            [self performSegueWithIdentifier: @"openConnectionDetails" sender: existingConnections[[existingConnections allKeys][0]]];
            addConnConfigTextView.text = ""
        }
    }

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "openConnectionDetails" {
            guard
                let connectionDetailsVC = segue.destination as? ConnectionDetailsViewController,
                let connectionData = sender as? [String: Any]
            else { return }

            connectionDetailsVC.connection = connectionData
        }
    }

    @IBAction func scanQR(_ sender: UIButton) {
        if(!isInitialized) {
            print("Please wait for VCX to initialize!")
            return;
        }
        // Create the reader object
        let reader = QRCodeReader.init(metadataObjectTypes: [AVMetadataObject.ObjectType.qr])
        let vc = QRCodeReaderViewController.init(cancelButtonTitle: "Cancel", codeReader: reader, startScanningAtLoad: true, showSwitchCameraButton: true, showTorchButton: true)
        vc.modalPresentationStyle = modalPresentationStyle

        self.navigationController?.present(vc, animated: true, completion: nil)

        reader.setCompletionWith { (scanResult) in
            print("Scan result", scanResult as Any)
            self.addConnConfigTextView.text = scanResult
            self.addNewConnection(sender)
            self.dismiss(animated: true, completion: nil)
        }
    }

    @objc func vcxInitialized() {
        self.infoLbl.text = "VCX initialized!"
        isInitialized = true;
        addConnectionBtn.isEnabled = true
        let delayInSeconds: Double = 15.0
        DispatchQueue.main.asyncAfter(deadline: .now() + delayInSeconds, execute: {
            self.infoLbl.text = ""
        })
    }

    private func connectionForIndex(_ index: Int) -> Any? {
        let connectionIDs = Array(existingConnections.keys)
        let connectionID = connectionIDs[index]
        return existingConnections[connectionID]
    }
}

extension ViewController: UITableViewDelegate, UITableViewDataSource {

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return existingConnections.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "conectionCell", for: indexPath)
        let connection = self.connectionForIndex(indexPath.row) as? [String: Any] ?? [:]
        cell.textLabel?.text = CMConnection.connectionName(connection)
        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if !self.isInitialized {
            print("Please wait for VCX to initialize!")
            return
        }

        let connection = self.connectionForIndex(indexPath.row)
        self.performSegue(withIdentifier: "openConnectionDetails", sender: connection)
    }

    func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        return isInitialized
    }

    func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCell.EditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == UITableViewCell.EditingStyle.delete {
//            let connectionID = Array(existingConnections.keys)[indexPath.row]
//            existingConnections.removeValue(forKey: connectionID)
//            let connection = self.connectionForIndex(indexPath.row)
//
//            CMConnection.removeConnection(connection as! String) { (success,error) in
//                if let error = error && error > 0 {
//
//                }
//            }
//            removeConnection: (NSString*) connection withCompletionHandler: (ResponseBlock) completionBlock
//            CMConnection.removeConnection(connectionID)
//            [CMConnection removeConnection: connection[@"serializedConnection"] withCompletionHandler:^(NSString *successMessage, NSError *error) {
                //    if (error && error.code > 0) {
                //    [CMUtilities printError: error];
                //    return;
                //    }
                //    [updatedConections removeObjectForKey: [CMConnection connectionID: connection]];
                //    if([[updatedConections allKeys] count] < 1) {
                //    [LocalStorage deleteObjectForKey: @"connections"];
                //    } else {
                //    [LocalStorage store: @"connections" andObject: updatedConections];
                //    }
                //    self.existingConnections = updatedConections;
                //    [tableView reloadData];
            self.tableView.reloadData()
        }
    }
    
    //    - (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    //    if (editingStyle == UITableViewCellEditingStyleDelete) {
    //    NSMutableDictionary* updatedConections = [existingConnections mutableCopy];
    //    NSDictionary* connection = existingConnections[[existingConnections allKeys][indexPath.row]];
    //
    //    [CMConnection removeConnection: connection[@"serializedConnection"] withCompletionHandler:^(NSString *successMessage, NSError *error) {
    //    if (error && error.code > 0) {
    //    [CMUtilities printError: error];
    //    return;
    //    }
    //    [updatedConections removeObjectForKey: [CMConnection connectionID: connection]];
    //    if([[updatedConections allKeys] count] < 1) {
    //    [LocalStorage deleteObjectForKey: @"connections"];
    //    } else {
    //    [LocalStorage store: @"connections" andObject: updatedConections];
    //    }
    //    self.existingConnections = updatedConections;
    //    [tableView reloadData];
    //    }];
    //    }
    //    }

}

extension ViewController: UITextViewDelegate {

}
