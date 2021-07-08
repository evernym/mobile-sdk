//
//  HomeViewController.swift
//  CMeSdkSwift
//
//  Created by Evernym on 06.07.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

import QRCodeReaderViewController

class HomeViewController: UIViewController {
    
    @IBOutlet var tableView: UITableView!
    @IBOutlet var addConnectionBtn: UIButton!
    @IBOutlet var infoLbl: UILabel!
    @IBOutlet var checkMessages: UIButton!
    @IBOutlet var scanQR: UIButton!
    @IBOutlet var addConnConfigTextView: UITextView!
    
    var requests: [String: Any] = [:]
    var isInitialized = false;
    
    typealias CompletionHandler = (_ result:Bool, _ error:Error?) -> Void

    override func viewDidLoad() {
        super.viewDidLoad();
        self.tableView.delegate = self;
        self.tableView.dataSource = self;
        self.addConnConfigTextView.delegate = self;
        self.addConnConfigTextView.layer.cornerRadius = 5;
        self.addConnectionBtn.layer.cornerRadius = 5;
        self.scanQR.layer.cornerRadius = 5;
        self.checkMessages.layer.cornerRadius = 5;
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated);
        if let storageRequests = LocalStorage.getObjectForKey("requests", shouldCreate: false) as? [String: Any] {
            self.requests = storageRequests;
            self.tableView.reloadData();
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
    
    @IBAction func addNewConnection(_ sender: Any) {
        let connectionText = addConnConfigTextView.text ?? ""
        if connectionText.count > 3 && connectionText != "enter code here" {
            let connectValues = CMConnection.parsedInvite(connectionText);

            if var storageRequests = LocalStorage.getObjectForKey("requests", shouldCreate: false) as? [String: Any] {
                let label = connectValues?["label"];
                let goal = connectValues?["goal"] ?? "New connection";
                let profileUrl = connectValues?["profileUrl"];
                let uuid = UUID().uuidString
                
                let newRequest:[String:String] = [
                    "name": label as? String ?? "",
                    "goal": goal as? String ?? "",
                    "profileUrl": profileUrl as? String ?? "",
                    "uuid": uuid,
                    "type": "null",
                    "data": CMUtilities.dict(toJsonString: connectValues) ?? ""
                ];
                
                storageRequests[uuid] = newRequest;
                LocalStorage.store("requests", andObject: storageRequests);
                
                self.requests = storageRequests;
                self.tableView.reloadData();
            }
        }
    }
    
    @IBAction func checkMessages(_ sender: Any) {
        CMMessage.downloadAllMessages { messages, error in
            for message in messages ?? [] {
                let msg = message as! [String : String];
                let type = msg["type"];
                
                if type == "credential-offer" {
                    let payload = msg["payload"];
                    let payloadArr = CMUtilities.json(toArray: payload) as? [[String:String]];
                    let payloadDict = payloadArr?[0];
                    
                    if var storageRequests = LocalStorage.getObjectForKey("requests", shouldCreate: false) as? [String: Any] {
                        let uuid = UUID().uuidString;
                        
                        let newRequest = [
                            "name": payloadDict?["claim_name"] ?? "",
                            "goal": "Credential Offer",
                            "uuid": payloadDict?["uid"] ?? "",
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
                            "uuid": payloadDict?["uid"] as? String ?? "",
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
                            "uuid": payloadDict?["uid"] as? String ?? "",
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
    
    private func newConnection(_ data: String, completionHandler: @escaping CompletionHandler) -> Any? {
        CMConnection.handle(_:data, connectionType:ConnectionType.QR.rawValue, phoneNumber:"") { result, error in
            if error != nil {
                return completionHandler(false, error);
            }
            return completionHandler(true, nil);
        }
        
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
    
    private func answer(_ data: String, completionHandler: @escaping CompletionHandler) -> Any? {
        let message = CMUtilities.json(toDictionary: data);
        let payload = message?["payload"];
        let payloadDict = CMUtilities.json(toDictionary: payload as? String);
        let responses = payloadDict?["valid_responses"] as? [[String : String]];
        
        let pwDidMes = message?["pwDid"];
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
        return completionHandler(false, nil);
    }
    
    private func requestByIndex(_ index: Int) -> Any? {
        let requestsIDs = Array(requests.keys);
        let requestsID = requestsIDs[index];
        return requests[requestsID];
    }
    
    private func switchRequestToHistoryView(_ uuid: String) {
        if var storageRequests = LocalStorage.getObjectForKey("requests", shouldCreate: false) as? [String:String] {
            for index in 0...storageRequests.keys.count {
                let keys = Array(storageRequests.keys)
                let key = keys[index];
                if key == uuid {
                    storageRequests.removeValue(forKey: key);
                }
            }
            LocalStorage.store("requests", andObject: storageRequests);
            self.requests = storageRequests;
            self.tableView.reloadData();
        }
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
        return 3;
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
                logoUrl: logoPath) {
                self.newConnection(data) { result, _ in
                    if (result) {
                        self.switchRequestToHistoryView(uuid);
                    }
                }
            } rejectCallback: {
                self.switchRequestToHistoryView(uuid);
            }

        }
        if (type == "credential-offer") {
            cell.updateCellAttributes(
                title: name,
                subtitle: goal,
                logoUrl: "") {
                self.acceptCredential(data) { result, _ in
                    if (result) {
                        self.switchRequestToHistoryView(uuid);
                    }
                }
            } rejectCallback: {
                self.rejectCredential(data) { result, _ in
                    if (result) {
                        self.switchRequestToHistoryView(uuid);
                    }
                }
            }

        }
        if (type == "committed-question") {
            cell.updateCellAttributes(
                title: name,
                subtitle: goal,
                logoUrl: "") {
                self.answer(data) { result, error in
                    if (result) {
                        self.switchRequestToHistoryView(uuid);
                    }
                }
            } rejectCallback: {
                self.switchRequestToHistoryView(uuid);
            }

        }
        if (type == "presentation-request") {
            cell.updateCellAttributes(
                title: name,
                subtitle: goal,
                logoUrl: "") {
                self.sendProof(data) { result, error in
                    if (result) {
                        self.switchRequestToHistoryView(uuid);
                    }
                }
            } rejectCallback: {
                self.rejectProof(data) { result, error in
                    if (result) {
                        self.switchRequestToHistoryView(uuid);
                    }
                }
            }

        }
        return cell;
    }
}
