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
        for i in 0...6 {
            CMMessage.downloadMessages(connection, andType: CMMessageStatusType(rawValue: CMMessageStatusType.RawValue(i)), andMessageID: nil) { (messages, error) in
                print("Received Messages",  messages as Any, i)
                guard
                    let downloadedMessages = messages,
                    downloadedMessages.count > 1
                else { return }

                self.messages = downloadedMessages.filter {
                    guard
                        let msg = $0 as? [String: Any],
                        let msgType = msg["ty[e"] as? String

                    else { return false }

                    return !["ACCEPT_CONN_REQ", "connReq", "connReqAnswer"].contains(msgType)
                }

                self.tableView.reloadData()
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

        case "credOffer":
            CMCredential.acceptCredOffer(message, forConnection: connection) { (credentialDetails, error) in
                print("credential offer results ", credentialDetails ?? "", error?.localizedDescription ?? "")
                DispatchQueue.main.async {
                    self.goBack()
                }
            }

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
