//
//  MessageTableViewCell.swift
//  CMeSdkSwift
//
//  Created by Predrag Jevtic on 04/02/2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

import UIKit

class MessageTableViewCell: UITableViewCell {
    @IBOutlet var titleLbl: UILabel!
    @IBOutlet var typeLbl: UILabel!
    @IBOutlet var logoImageView: UIImageView!
    @IBOutlet var logoWidthConstraint: NSLayoutConstraint!

    var message: [String: Any]?
    var messageObjects: [Any]?

    func updateCellContent(message: [String: Any], sender: [String: Any]) {
        self.message = message
        guard
            let decryptedPayload = message["decryptedPayload"] as? String,
            let messageData = CMUtilities.json(toDictionary: decryptedPayload) as? [String: Any],
            let msg = messageData["@msg"] as? String,
            let msgObjects = CMUtilities.json(toArray: msg)
        else { return }

        print("message data \(String(describing: messageObjects))")

        //        loadImage(sender)

        let messageType = message["type"] as? String ?? ""

        //    if([@[@"credOffer", @"cred"] containsObject: messageType]) {
        //        [self populateCredentialCell];
        //        return;
        //    }
        //    if([messageType isEqual: @"aries"]) {
        //        NSDictionary* messageObj = [CMUtilities jsonToDictionary: messageData[@"@msg"]];
        //        NSData* sigData = [CMUtilities decode64String: messageObj[@"connection~sig"][@"sig_data"]];
        //        NSString* signature = [[NSString alloc] initWithData: sigData encoding: NSASCIIStringEncoding];
        //        self.typeLbl.text = @"Credential Request";
        //        NSLog(@"aries %@", signature);
        //    }
        //    if([messageType isEqual: @"credReq"]) {
        //        //        self.titleLbl.text = [NSString stringWithFormat:@"Values: %lu", [values count]];
        //        NSLog(@"credenialReq %@", message);
        //        self.typeLbl.text = @"Credential Request";
        //        return;
        //    }
        //
        //    if([messageType isEqual: @"proofReq"]) {
        //        NSDictionary* data = [CMUtilities jsonToDictionary: messageData[@"@msg"]];
        //        self.titleLbl.text = data[@"name"];
        //        self.typeLbl.text = @"Proof request - Press to populate";
        //        self.logoImageView.hidden = true;
        //        self.logoWidthConstraint.constant = 0;
        //        return;
        //    }
    }

    func loadImage(_ sender: [String: Any]){

        guard
            let logoPath = sender["logoUrl"] as? String,
            let logoUrl = URL(string: logoPath)
        else {
            self.logoWidthConstraint.constant = 0
            return
        }
        self.logoImageView.isHidden = false;
        logoImageView.layer.cornerRadius = 30
        DispatchQueue.global(qos: .userInitiated).async {
            let data = try? Data(contentsOf: logoUrl)
            guard let imgData = data else { return }

            DispatchQueue.main.async {
                self.logoWidthConstraint.constant = 60
                self.logoImageView.image = UIImage(data: imgData)
            }
        }
    }

}

//-(void)populateCredentialCell {
//    NSDictionary* credentialData = [CMUtilities jsonToDictionary: [CMUtilities jsonToDictionary: message[@"decryptedPayload"]][@"@msg"]];
//    NSMutableDictionary* values = [@{} mutableCopy];
//    if ([[credentialData allKeys] containsObject: @"libindy_cred"]){
//        values = [CMUtilities jsonToDictionary: credentialData[@"libindy_cred"]][@"values"];
//    }
//    if ([[credentialData allKeys] containsObject: @"libindy_offer"]){
//        //        values = [CMUtilities jsonToDictionary: credentialData[@"libindy_offer"]][@"values"];
//    }
//    self.titleLbl.text = [NSString stringWithFormat:@"Values: %lu", (unsigned long)[values count]];
//    self.typeLbl.text = @"Credential";
//}
