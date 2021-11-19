//
//  HomeViewController.swift
//  MSDKSampleAppSwift
//
//  Created by Evernym on 06.07.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

import AVFoundation
import UIKit
import QRCodeReader

class HomeViewController: UIViewController, QRCodeReaderViewControllerDelegate, UITextDropDelegate {
    lazy var readerVC: QRCodeReaderViewController = {
        let builder = QRCodeReaderViewControllerBuilder {
            $0.reader = QRCodeReader(metadataObjectTypes: [.qr], captureDevicePosition: .back)
            
            $0.showTorchButton        = true
            $0.showSwitchCameraButton = true
            $0.showCancelButton       = false
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
    
    let CREDENTIAL_OFFER = "credential-offer"
    let PRESENTATION_REQUEST = "presentation-request"
    let COMMITTED_QUESTION = "committed-question"
    let OOB = "OOB"
    
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
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        super.touchesBegan(touches, with: event)
        self.addConnConfigTextView.endEditing(true)
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
    
    @objc func vcxInitialized() {
        self.infoLbl.text = "VCX initialized!"
        isInitialized = true;
        let delayInSeconds: Double = 15.0
        DispatchQueue.main.asyncAfter(deadline: .now() + delayInSeconds, execute: {
            self.infoLbl.text = ""
        })
    }
    
    @objc private func createActionWithInvitation(_ data:String) {
        ConnectionInvitation.parsedInvite(data) { connectValues, error in
            let label = (connectValues?["label"] ?? "New connection") as! String
            let goal = (connectValues?["goal"] ?? "New connection") as! String
            let profileUrl = (connectValues?["profileUrl"] ?? "") as! String
            
            self.createAction(
                label,
                profileUrl: profileUrl,
                goal: goal,
                type: self.OOB,
                data: Utilities.toJsonString(connectValues),
                additionalData: "",
                pwDid: ""
            )
        }
    }
    
    private func createAction(_ label: String,
                              profileUrl: String,
                              goal: String,
                              type: String,
                              data: String,
                              additionalData: String,
                              pwDid: String
                              ) {
        if var storageRequests = LocalStorage.getObjectForKey("requests", shouldCreate: false) as? [String: Any] {
            let uuid = UUID().uuidString
            
            let newRequest:[String:Any] = [
                "name": label,
                "goal": goal,
                "profileUrl": profileUrl,
                "uuid": uuid,
                "type": type,
                "data": data,
                "additionalData": additionalData,
                "pwDid": pwDid
            ];
            
            storageRequests[uuid] = newRequest
            LocalStorage.store("requests", andObject: storageRequests)
            
            self.requests = storageRequests
            self.tableView.reloadData()
        }
    }
    
    @IBAction func addNewConnection(_ sender: Any) {
        let connectionText = addConnConfigTextView.text ?? ""
        if connectionText.count > 3 && connectionText != "enter code here" {
            self.createActionWithInvitation(connectionText)
        }
    }
    
    @IBAction func scanQR(_ sender: UIButton) {
        if(!isInitialized) {
            print("Please wait for VCX to initialize!")
            return;
        }
        readerVC.delegate = self
        readerVC.completionBlock = { (result: QRCodeReaderResult?) in
            let invite = (result?.value ?? "") as String
            print(invite)
            self.createActionWithInvitation(invite)
        }
        readerVC.modalPresentationStyle = .formSheet
        present(readerVC, animated: true, completion: nil)
    }
    
    @IBAction func checkMessages(_ sender: Any) {
        Message.downloadAllMessages { messages, error in
            for message in messages ?? [] {
                let msg = message as! [String : String];
                let type = msg["type"];
                
                if type == self.CREDENTIAL_OFFER {
                    self.handleReceivedCredentialOffer(msg, completionHandler: { _, _ in })
                }
                
                if type == self.PRESENTATION_REQUEST {
                    self.handleReceivedProofrequest(msg, completionHandler: { _, _ in })
                }
                
                if type == "committed-question" {
                    self.handleReceivedQuestion(msg)
                }
            }
        }
    }
    
    private func handleReceivedCredentialOffer(_ message: [String : String], completionHandler: @escaping CompletionHandler) {
        self.messageStatusUpdate(message)
        let pwDid = message["pwDid"]  ?? ""
        let payload = message["payload"] ?? ""
        let payloadArr = convertToArray(text:payload)
        let payloadDict = payloadArr?.first
        let name = (payloadDict?["claim_name"] ?? "Offer") as! String
        
        Credential.create(withOffer: payload) { createdOffer, _ in
            self.createAction(
                name,
                profileUrl: "",
                goal: "Credential Offer",
                type: self.CREDENTIAL_OFFER,
                data: payload,
                additionalData: Utilities.dict(toJsonString: createdOffer),
                pwDid: pwDid
            )
            
            return completionHandler(true, nil)
        }
    }
    
    private func handleReceivedProofrequest(_ message: [String : String], completionHandler: @escaping CompletionHandler) {
        self.messageStatusUpdate(message)
        let pwDid = message["pwDid"]  ?? ""
        let payload = message["payload"] ?? ""
        let payloadDict = convertToDictionary(text:payload)
        let name = payloadDict?["comment"] as? String ?? ""
        
        ProofRequest.create(withRequest: payload) { request, _ in
            self.createAction(
                name,
                profileUrl: "",
                goal: "Proof Request",
                type: self.PRESENTATION_REQUEST,
                data: payload,
                additionalData: Utilities.dict(toJsonString: request),
                pwDid: pwDid
            )
            
            return completionHandler(true, nil)
        }
    }
    
    private func handleReceivedQuestion(_ message: [String : String]) {
        self.messageStatusUpdate(message)
        let pwDid = message["pwDid"]  ?? ""
        let payload = message["payload"] ?? ""
        let payloadDict = convertToDictionary(text:payload)
        let name = payloadDict?["question_text"] as? String ?? ""
        self.createAction(
            name,
            profileUrl: "",
            goal: "Question",
            type: self.COMMITTED_QUESTION,
            data: Utilities.toJsonString(message),
            additionalData: payload,
            pwDid: pwDid
        )
    }
    
    @objc private func handleAcceptAction (_ data: String,
                                           forType: String,
                                           pwDid: String,
                                           additionalData: String,
                                           name: String,
                                           completionHandler: @escaping CompletionHandler) -> Any? {
        if forType == self.OOB {
            ConnectionHandler.handleConnectionInvitation(data) { result, error in
                return completionHandler(true, nil)
            }
        }
        if forType == self.CREDENTIAL_OFFER {
            CredentialOffersHandler.acceptCredentialOffer(pwDid,
                                                          attachment: data,
                                                          createdOffer: Utilities.json(toDictionary: additionalData),
                                                          fromMessage: true
            ) { _, _ in
                return completionHandler(true, nil)
            }
        }
        if forType == self.PRESENTATION_REQUEST {
            ProofRequestsHandler.acceptProofRequest(pwDid,
                                                    attachment: Utilities.json(toDictionary: data),
                                                    request: Utilities.json(toDictionary: additionalData),
                                                    name: name
            ) { _, _ in
                return completionHandler(true, nil)
            }
        }
        if forType == self.COMMITTED_QUESTION {
            _ = self.answer(data) { result, error in
                return completionHandler(result, nil)
            }
        }
        return completionHandler(false, nil)
    }
    
    @objc private func handleRejectAction (_ data: String,
                                           forType: String,
                                           pwDid: String,
                                           additionalData: String,
                                           name: String,
                                           completionHandler: @escaping CompletionHandler) -> Any? {
        if forType == self.OOB {
            return completionHandler(true, nil)
        }
        if forType == self.CREDENTIAL_OFFER {
            CredentialOffersHandler.rejectCredentialOffer(pwDid,
                                                          attachment: Utilities.json(toDictionary: data),
                                                          createdOffer: Utilities.json(toDictionary: additionalData)
            ) { _, _ in
                return completionHandler(true, nil)
            }
        }
        if forType == self.PRESENTATION_REQUEST {
            ProofRequestsHandler.rejectProofRequest(pwDid,
                                                    request: additionalData,
                                                    name: name
            ) { _, _ in
                return completionHandler(true, nil)
            }
        }
        if forType == self.COMMITTED_QUESTION {
            return completionHandler(true, nil)
        }
        return completionHandler(false, nil)
    }
    
    @objc private func answer(_ data: String, completionHandler: @escaping CompletionHandler) -> Any? {
        let msg = convertToDictionary(text: data)
        let payload = msg?["payload"];
        let payloadDict = convertToDictionary(text: payload as! String)
        let responses = payloadDict?["valid_responses"] as? [[String : String]];
        
        let pwDidMes = msg?["pwDid"];
        let questionConnection = ConnectionInvitation.getConnectionByPwDid(pwDidMes as? String ?? "");
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
                    Message.answerQuestion(questionConnection, message: payload as! String, answer: Utilities.dict(toJsonString: response)) { result, error in
                        LocalStorage.addEvent(toHistory: NSString.localizedStringWithFormat("%@ - Answer question", payloadDict?["question_text"] as! CVarArg) as String);
                        return completionHandler(result, error);
                    }
                }
            )
        }
        self.present(alert, animated: true)
        return completionHandler(false, nil);
    }
    
    @objc private func messageStatusUpdate(_ message: [String : String]) {
        Message.updateStatus(message["pwDid"]!, messageId: message["uid"]!) { _,_ in  }
    }
    
    private func convertToDictionary(text: String) -> [String: Any]? {
        if let data = text.data(using: .utf8) {
            do {
                return try JSONSerialization.jsonObject(with: data, options: []) as? [String: Any]
            } catch {
                print(error.localizedDescription)
            }
        }
        return nil
    }
    
    private func convertToArray(text: String) -> [[String: Any]]? {
        if let data = text.data(using: .utf8) {
            do {
                return try JSONSerialization.jsonObject(with: data, options: []) as? [[String: Any]]
            } catch {
                print(error.localizedDescription)
            }
        }
        return nil
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
        let logoPath = request["logoUrl"] as? String ?? "";
        let pwDid = request["pwDid"] as? String ?? "";
        let additionalData = request["additionalData"] as? String ?? "";

        cell.updateCellAttributes(title: name, subtitle: goal, logoUrl: logoPath)
        cell.addAceptCallback(acceptCallback: { () -> () in
            _ = self.handleAcceptAction(data,
                                        forType: type,
                                        pwDid: pwDid,
                                        additionalData: additionalData,
                                        name: name
            ) { _, _ in
                _ = self.switchRequestToHistoryView(uuid) { _, _ in }
            }
        })
        cell.addRejectCallback(rejectCallback: { () -> () in
            _ = self.handleRejectAction(data,
                                        forType: type,
                                        pwDid: pwDid,
                                        additionalData: additionalData,
                                        name: name
            ) { _, _ in
                _ = self.switchRequestToHistoryView(uuid) { _, _ in }
                if type == self.OOB || type == self.COMMITTED_QUESTION {
                    self.addRejectAction(name)
                }
            }
            
        });
        
        return cell;
    }
}
