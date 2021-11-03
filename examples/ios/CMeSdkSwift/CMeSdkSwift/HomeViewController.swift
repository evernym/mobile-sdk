//
//  HomeViewController.swift
//  CMeSdkSwift
//
//  Created by Evernym on 06.07.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

import AVFoundation
import UIKit
import QRCodeReader

class HomeViewController: UIViewController, QRCodeReaderViewControllerDelegate {
    lazy var readerVC: QRCodeReaderViewController = {
        let builder = QRCodeReaderViewControllerBuilder {
            $0.reader = QRCodeReader(metadataObjectTypes: [.qr], captureDevicePosition: .back)
            
            $0.showTorchButton        = true
            $0.showSwitchCameraButton = true
            $0.showCancelButton       = true
            $0.showOverlayView        = true
            $0.rectOfInterest         = CGRect(x: 0.2, y: 0.2, width: 0.6, height: 0.6)
        }
        
        return QRCodeReaderViewController(builder: builder)
    }()
    
    func reader(_ reader: QRCodeReaderViewController, didScanResult result: QRCodeReaderResult) {
      reader.stopScanning()

      dismiss(animated: true, completion: nil)
    }

    func reader(_ reader: QRCodeReaderViewController, didSwitchCamera newCaptureDevice: AVCaptureDeviceInput) {
        print("Switching capture to: \(newCaptureDevice.device)")
    }

    func readerDidCancel(_ reader: QRCodeReaderViewController) {
      reader.stopScanning()

      dismiss(animated: true, completion: nil)
    }
    
    @IBOutlet var tableView: UITableView!
    @IBOutlet var infoLbl: UILabel!
    @IBOutlet var addConnConfigTextView: UITextView!
    @IBOutlet var newConnLabel: UILabel!
    @IBOutlet var addConnectionButton: UIButton!
    
    var requests: [String: Any] = [:]
    var isInitialized = false;
    
    typealias CompletionHandler = (_ result:Bool, _ error:Error?) -> Void

    override func viewDidLoad() {
        // On iOS diveces you cann't use qr code scnner
        // But you can handle invite with text input and button on home page
        // For show input set this flag to false
        let isHideInput = true;
        
        super.viewDidLoad();
        self.tableView.delegate = self;
        self.tableView.dataSource = self;
        self.addConnConfigTextView.delegate = self;
        self.addConnConfigTextView.layer.cornerRadius = 5;
        
        self.addConnConfigTextView.isHidden = isHideInput;
        self.addConnectionButton.isHidden = isHideInput;
        self.newConnLabel.isHidden = isHideInput;
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated);
        if let storageRequests = LocalStorage.getObjectForKey("requests", shouldCreate: false) as? [String: Any] {
            self.requests = storageRequests;
            self.tableView.reloadData();
            NotificationCenter.default.addObserver(self, selector: #selector(self.vcxInitialized), name: NSNotification.Name(rawValue: "vcxInitialized"), object: nil)
            isInitialized = MobileSDK.shared().sdkInited
        }
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        NotificationCenter.default.removeObserver(self)
    }

    @objc func handleSingleTap(_ sender: UITapGestureRecognizer) {
        self.view.endEditing(true)
    }
    
    @IBAction func scanQR(_ sender: UIButton) {
        if(!isInitialized) {
            print("Please wait for VCX to initialize!")
            return;
        }
        readerVC.delegate = self
        readerVC.completionBlock = { (result: QRCodeReaderResult?) in
            let invite = result?.value as String?
            print(invite ?? "")
            self.addNewConnectionFromQRCode(invite ?? "")
        }
        readerVC.modalPresentationStyle = .formSheet
        present(readerVC, animated: true, completion: nil)
    }
    
    private func addNewConnectionFromQRCode(_ data: String) {
        let connectValues = CMConnection.parsedInvite(data);

        if var storageRequests = LocalStorage.getObjectForKey("requests", shouldCreate: false) as? [String: Any] {
            let label = connectValues?["label"];
            let goal = connectValues?["goal"] ?? "New connection";
            let profileUrl = connectValues?["profileUrl"];
            let uuid = UUID().uuidString
            
            let newRequest:[String:Any] = [
                "name": label as? String ?? "",
                "goal": goal as? String ?? "",
                "profileUrl": profileUrl as? String ?? "",
                "uuid": uuid,
                "type": "null",
                "data": CMUtilities.toJsonString(connectValues)
            ];
            
            storageRequests[uuid] = newRequest;
            LocalStorage.store("requests", andObject: storageRequests);
            
            self.requests = storageRequests;
            self.tableView.reloadData();
        }
        return
    }
    
    
    @objc func vcxInitialized() {
        self.infoLbl.text = "VCX initialized!"
        isInitialized = true;
        let delayInSeconds: Double = 15.0
        DispatchQueue.main.asyncAfter(deadline: .now() + delayInSeconds, execute: {
            self.infoLbl.text = ""
        })
    }
    
    @IBAction func addNewConnection(_ sender: Any) {
        do {
        let connectionText = addConnConfigTextView.text ?? ""
        if connectionText.count > 3 && connectionText != "enter code here" {
            let connectValues = CMConnection.parsedInvite(connectionText);

            if var storageRequests = LocalStorage.getObjectForKey("requests", shouldCreate: false) as? [String: Any] {
                let label = connectValues?["label"];
                let goal = connectValues?["goal"] ?? "New connection";
                let profileUrl = connectValues?["profileUrl"];
                let uuid = UUID().uuidString
//                let data = CMUtilities.dict(toJsonString: connectValues) ?? "";
                
                let messageDictionary = connectValues;
                let jsonData = try JSONSerialization.data(withJSONObject: messageDictionary as Any, options: [])
                let jsonString = String(data: jsonData, encoding: String.Encoding.ascii)!
                
                let newRequest:[String:Any] = [
                    "name": label as? String ?? "",
                    "goal": goal as? String ?? "",
                    "profileUrl": profileUrl as? String ?? "",
                    "uuid": uuid,
                    "type": "null",
                    "data": CMUtilities.toJsonString(connectValues)
                ];
                
                storageRequests[uuid] = newRequest;
                LocalStorage.store("requests", andObject: storageRequests);
                
                self.requests = storageRequests;
                self.tableView.reloadData();
            }
        }
        } catch {
            print(error)
        }
    }
    
    @IBAction func checkMessages(_ sender: Any) {
        CMMessage.downloadAllMessages { messages, error in
            for message in messages ?? [] {
                let msg = message as! [String : String];
                let type = msg["type"];
                
                if type == "credential-offer" {
                    let name = msg["claim_name"]
                    let uid = msg["uid"]
                    
                    if var storageRequests = LocalStorage.getObjectForKey("requests", shouldCreate: false) as? [String: Any] {
                        let uuid = UUID().uuidString;
                        
                        let newRequest = [
                            "name": name ?? "",
                            "goal": "Credential Offer",
                            "uuid": uuid,
                            "type": type ?? "",
                            "data": CMUtilities.dict(toJsonString: message as? [AnyHashable : Any]) ?? ""
                        ] as [String:String?]?;
                        
                        storageRequests[uuid] = newRequest;
                        LocalStorage.store("requests", andObject: storageRequests);
                        
                        if (msg["pwDid"] != nil) && (msg["uid"] != nil) {
                            CMMessage.updateStatus(msg["pwDid"]!, messageId: msg["uid"]!) { _,_ in  }
                        }
                        
                        self.requests = storageRequests;
                        self.tableView.reloadData();
                    }
                }
                if type == "committed-question" {
                    let payload = msg["payload"];
                    let payloadDict = CMUtilities.json(toDictionary: payload);

                    if var storageRequests = LocalStorage.getObjectForKey("requests", shouldCreate: false) as? [String: Any] {
                        let uuid = UUID().uuidString
                        
                        let newRequest:[String:String] = [
                            "name": payloadDict?["question_text"] as? String ?? "",
                            "goal": "Question",
                            "uuid": uuid,
                            "type": type ?? "",
                            "data": CMUtilities.dict(toJsonString: message as? [AnyHashable : Any]) ?? ""
                        ];
                        
                        storageRequests[uuid] = newRequest;
                        LocalStorage.store("requests", andObject: storageRequests);
                        
                        if (msg["pwDid"] != nil) && (msg["uid"] != nil) {
                            CMMessage.updateStatus(msg["pwDid"]!, messageId: msg["uid"]!) { _,_ in  }
                        }
                        
                        self.requests = storageRequests;
                        self.tableView.reloadData();
                    }
                }
                if type == "presentation-request" {
                    let payload = msg["payload"];
                    let payloadDict = CMUtilities.json(toDictionary: payload);

                    if var storageRequests = LocalStorage.getObjectForKey("requests", shouldCreate: false) as? [String: Any] {
                        let uuid = UUID().uuidString
                        
                        let newRequest:[String:String] = [
                            "name": payloadDict?["comment"] as? String ?? "",
                            "goal": "Proof Request",
                            "uuid": uuid,
                            "type": type ?? "",
                            "data": CMUtilities.dict(toJsonString: message as? [AnyHashable : Any]) ?? ""
                        ];
                        
                        storageRequests[uuid] = newRequest;
                        LocalStorage.store("requests", andObject: storageRequests);
                        
                        if (msg["pwDid"] != nil) && (msg["uid"] != nil) {
                            CMMessage.updateStatus(msg["pwDid"]!, messageId: msg["uid"]!) { _,_ in  }
                        }
                        
                        self.requests = storageRequests;
                        self.tableView.reloadData();
                    }
                }
            }
        }
    }
    
    @objc private func newConnection(_ data: String, completionHandler: @escaping CompletionHandler) -> Any? {
//        CMConnection.handle(_:data, connectionType:ConnectionType.QR.rawValue, phoneNumber:"") { result, error in
//            if error != nil {
//                return completionHandler(false, error);
//            }
//            return completionHandler(true, nil);
//        }
        
        CMConnection.handle(data, connectionType:ConnectionType.QR.rawValue, phoneNumber:"") { result, error in
            if error != nil {
                return completionHandler(false, error);
            }
            return completionHandler(true, nil);
        };
        return completionHandler(false, nil);
    }
    
    private func acceptCredential(_ data: String, completionHandler: @escaping CompletionHandler) -> Any? {
        CMCredential.acceptCredentila(fromMessage:data) { result, error in
            if error != nil {
                return completionHandler(false, error);
            }
            return completionHandler(true, nil);
        }
    }
    
    private func rejectCredential(_ data: String, completionHandler: @escaping CompletionHandler) -> Any? {
        CMCredential.rejectCredentila(fromMessage:data) { result, error in
            if error != nil {
                return completionHandler(false, error);
            }
            return completionHandler(true, nil);
        }
    }
    
    private func sendProof(_ data: String, completionHandler: @escaping CompletionHandler) -> Any? {
        CMProofRequest.send(fromMessage:data) { result, error in
            if error != nil {
                return completionHandler(false, error);
            }
            return completionHandler(true, nil);
        }
    }
    
    private func rejectProof(_ data: String, completionHandler: @escaping CompletionHandler) -> Any? {
        CMProofRequest.reject(fromMessage:data) { result, error in
            if error != nil {
                return completionHandler(false, error);
            }
            return completionHandler(true, nil);
        }
    }
    
    func convertToDictionary(text: String) -> [String: Any]? {
        if let data = text.data(using: .utf8) {
            do {
                return try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
            } catch {
                print(error.localizedDescription)
            }
        }
        return nil
    }
    
    private func answer(_ data: String, completionHandler: @escaping CompletionHandler) -> Any? {
        let msg = convertToDictionary(text: data)
        let payload = msg?["payload"];
        let payloadDict = convertToDictionary(text: payload as! String)
        let responses = payloadDict?["valid_responses"] as? [[String : String]];
        
        let pwDidMes = msg?["pwDid"];
        let questionConnection = CMConnection.getByPwDid(pwDidMes as? String);
        
        let alert = UIAlertController(
            title: payloadDict?["question_text"] as? String,
            message: payloadDict?["question_detail"] as? String,
            preferredStyle: UIAlertController.Style.alert
        );
        
        for response in responses ?? [] {
            alert.addAction(
                UIAlertAction(
                    title: response["text"],
                    style: UIAlertAction.Style.default
                ) {_ in
                    CMMessage.answerQuestion(questionConnection!, message: payload as! String, answer: CMUtilities.dict(toJsonString: response)) { result, error in
                        LocalStorage.addEvent(toHistory: NSString.localizedStringWithFormat("%@ - Answer question", payloadDict?["question_text"] as! CVarArg) as String);
                        return completionHandler(result, error);
                    }
                }
            )
        }
        self.present(alert, animated: true)
        return completionHandler(false, nil);
    }
    
    private func requestByIndex(_ index: Int) -> Any? {
        let requestsIDs = Array(requests.keys);
        if requestsIDs.count > 0 {
            let requestsID = requestsIDs[index];
            return requests[requestsID];
        }
        return [:]
    }
    
    private func switchRequestToHistoryView(_ uuid: String, completionHandler: @escaping CompletionHandler) -> Any? {
        var storageRequests = requests;
        print("switchRequestToHistoryView", storageRequests, uuid)
        for key in Array(storageRequests.keys) {
            if key == uuid {
                storageRequests.removeValue(forKey: key);
            }
        }
        LocalStorage.store("requests", andObject: storageRequests);
        self.requests = storageRequests;
        self.tableView.reloadData();
        return completionHandler(true, nil);
    }
    
    private func addRejectAction(_ name: String) {
        LocalStorage.addEvent(toHistory: "\(name) - Rejected")
        self.tableView.reloadData();
        return
    }
}

extension HomeViewController: UITextViewDelegate, UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 110;
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1;
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return requests.count;
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell: CustomTableViewCell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! CustomTableViewCell;
        let request = self.requestByIndex(indexPath.row) as? [String: Any] ?? [:];
        
        let type = request["type"] as? String ?? "";
        let name = request["name"] as? String ?? "";
        let goal = request["goal"] as? String ?? "";
        let uuid = request["uuid"] as? String ?? "";
        let data = request["data"] as? String ?? "";

        if (type == "null") {
            let logoPath = request["logoUrl"] as? String ?? "";
            cell.updateCellAttributes(
                title: name,
                subtitle: goal,
                logoUrl: logoPath);
            cell.addAceptCallback(acceptCallback: { () -> () in
                _ = self.newConnection(data) { result, _ in
                    if (result) {
                        _ = self.switchRequestToHistoryView(uuid) { _, _ in }
                    }
                }
            });
            cell.addRejectCallback(rejectCallback: { () -> () in
                _ = self.switchRequestToHistoryView(uuid) { _, _ in }
                _ = self.addRejectAction(uuid)
            });
        }
        if (type == "credential-offer") {
            cell.updateCellAttributes(
                title: name,
                subtitle: goal,
                logoUrl: "");
            cell.addAceptCallback(acceptCallback: { () -> () in
//                _ = self.switchRequestToHistoryView(uuid) { _, _ in };
                _ = self.acceptCredential(data) { result, error in
                    _ = self.switchRequestToHistoryView(uuid) { _, _ in };
                }
            });
            cell.addRejectCallback(rejectCallback: { () -> () in
                _ = self.rejectCredential(data) { result, error in
                    if (result) {
                        _ = self.switchRequestToHistoryView(uuid) { _, _ in }
                    }
                }
            });
        }
        if (type == "committed-question") {
            cell.updateCellAttributes(
                title: name,
                subtitle: goal,
                logoUrl: "");
            cell.addAceptCallback(acceptCallback: { () -> () in
                _ = self.answer(data) { result, error in
                    if (result) {
                        _ = self.switchRequestToHistoryView(uuid) { _, _ in };
                    }
                }
            });
            cell.addRejectCallback(rejectCallback: { () -> () in
                _ = self.switchRequestToHistoryView(uuid) { _, _ in }
            });
        }
        if (type == "presentation-request") {
            cell.updateCellAttributes(
                title: name,
                subtitle: goal,
                logoUrl: "");
            cell.addAceptCallback(acceptCallback: { () -> () in
                _ = self.sendProof(data) { result, error in
                    if (result) {
                        _ = self.switchRequestToHistoryView(uuid) { _, _ in };
                    }
                }
            });
            cell.addRejectCallback(rejectCallback: { () -> () in
                _ = self.rejectProof(data) { result, error in
                    if (result) {
                        _ = self.switchRequestToHistoryView(uuid) { _, _ in }
                    }
                }
            });
        }
        return cell;
    }
}
