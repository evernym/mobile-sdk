//
//  ConnectionDetailsViewController.swift
//  CMeSdkSwift
//
//  Created by Predrag Jevtic on 04/02/2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

import UIKit

class ConnectionDetailsViewController: UIViewController {

    @IBOutlet var tableView: UITableView!
    var connection: [String: Any]?
    var msgSender: [String: Any]?
    var messages: [Any] = []

    override func viewDidLoad() {
        super.viewDidLoad()
        self.title = CMConnection.connectionName(connection)
    }

    @IBAction func checkForMessages(_ sender: Any) {
        // NOTE: same method will be used when receiving push notification
        // Only difference is:
        // in push notification, we will also receive messageID, which will be used for downloading details for that specific messageID
        // here (without push notification) we will download all messages for given connection
        guard let connection = self.connection else { return }
        
        CMMessage.downloadMessages(connection, andType: Received, andMessageID: nil) { (messages, error) in
            print("Received Messages",  messages as Any)
            guard
                let downloadedMessages = messages,
                downloadedMessages.count >= 1
            else { return }
            
            for message in downloadedMessages {
                let decryptedMessage = (message as! [String: Any])["decryptedPayload"] as! String;
                let uid = (message as! [String: Any])["uid"] as! String
                guard var decryptedMessageJson = (try? JSONSerialization.jsonObject(with: Data(decryptedMessage.utf8), options: JSONSerialization.ReadingOptions.mutableContainers)) as? [String: Any] else {
                    print("Could not convert to json")
                    return
                }
                decryptedMessageJson["uid"] = uid;
                let messageType = decryptedMessageJson["@type"] as! [String: String]
                let messageTypeName = messageType["name"]!
                if (messageTypeName == "credential-offer") {
//                    CMCredential.acceptCredOffer(decryptedMessageJson, forConnection: connection) { (message, error) in
//
//                    }
                } else if (["proof_request", "proof-request", "presentation-request"].contains(messageTypeName)) {
                    
                }
            }
        }
    }

    func openMessageControllerForType(_ messageType: String, message: [String: Any]) {
        switch messageType {
        case "proofReq":
            guard let decryptedPayload = message["decryptedPayload"] as? String else { return }
            var proofObj = message
            proofObj["payload"] = CMUtilities.json(toDictionary: decryptedPayload)
            self.performSegue(withIdentifier: "openProofDetails", sender: proofObj)
            return

//        case "credOffer":
//            CMCredential.acceptCredOffer(message, forConnection: connection) { (credentialDetails, error) in
//                print("credential offer results ", credentialDetails ?? "", error?.localizedDescription ?? "")
//                DispatchQueue.main.async {
//                    self.goBack()
//                }
//            }

        default:
            break
        }
    }

    func goBack() {
        self.navigationController?.popViewController(animated: true)
    }

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        switch segue.identifier {
        case "openProofDetails":
            guard
                let proofVC = segue.destination as? ProofDetailsViewController,
                let proofSender = sender as? [String: Any]

            else { return }
            proofVC.proof = proofSender
            proofVC.connection = self.connection

        default:
            print("Prepare for segue: \(String(describing: sender))")
        }
    }
}

extension ConnectionDetailsViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return messages.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        guard
            let cell =  tableView.dequeueReusableCell(withIdentifier: "messageCell", for: indexPath) as? MessageTableViewCell,
            let msg = messages[indexPath.row] as? [String : Any],
            let sender = self.msgSender
        else { return UITableViewCell() }

        cell.updateCellContent(message: msg, sender: sender)
        return cell;
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let message = messages[indexPath.row]

        print("message is \(message)")
        guard
            let msg = message as? [String: Any],
            let msgType = msg["type"] as? String
        else { return }
        self.openMessageControllerForType(msgType, message: msg)
    }
}
