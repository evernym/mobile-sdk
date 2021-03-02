//
//  ProofDetailsViewController.swift
//  CMeSdkSwift
//
//  Created by Predrag Jevtic on 04/02/2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

import UIKit

class ProofDetailsViewController: UIViewController {

    @IBOutlet var titleLbl: UILabel!
    @IBOutlet var tableView: UITableView!
    @IBOutlet var activeTextField: UITextField!

    var connection: [String: Any]?
    var proof: [String: Any]?
    var proofAttributes:  [String: Any]?
    var decriptedProof:  [String: Any]?
    var proofFields:  [Any]?

    override func viewDidLoad() {
        super.viewDidLoad()

        guard
            let payload = self.proof?["payload"] as? [String: Any],
            let msg = payload["@msg"] as? String
        else { return }

        self.decriptedProof = CMUtilities.json(toDictionary: msg) as? [String: Any]
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)

//        proofFields = [decriptedProof[@"proof_request_data"][@"requested_attributes"] allKeys];
//        [self.tableView reloadData];
//
//        [CMProofRequest autofillAttributes: proof andConnection: connection withCompletionHandler: ^(NSDictionary *proofAttributes, NSError *error) {
//        if(proofAttributes) {
//        dispatch_async(dispatch_get_main_queue(), ^{
//        self.proofAttributes = proofAttributes;
//        [self.tableView reloadData];
//        });
//        }
//        }];
    }
}
