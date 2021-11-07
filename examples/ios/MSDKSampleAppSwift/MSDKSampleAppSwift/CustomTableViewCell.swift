//
//  CustomTableViewCell.swift
//  MSDKSampleAppSwift
//
//  Created by Evernym on 07.07.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

class CustomTableViewCell: UITableViewCell {
    @IBOutlet var logo: UIImageView!;
    @IBOutlet var title: UILabel!;
    @IBOutlet var subtitle: UILabel!;
    @IBOutlet var accept: UIButton!;
    @IBOutlet var reject: UIButton!;
    
    typealias ActionBlock = () -> Void;
    var _acceptCallback: ActionBlock? = nil;
    var _rejectCallback: ActionBlock? = nil;
    
    override func awakeFromNib() {
        super.awakeFromNib()
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
    }
    
    @objc func callAcceptBlock() {
        _acceptCallback!();
    }
    
    @objc func callRejectBlock() {
        _rejectCallback!();
    }
    
    func updateCellAttributes(title: String, subtitle: String, logoUrl: String) {
        self.title.text = title;
        self.subtitle.text = subtitle;
        
        if logoUrl != "" {
            let url = URL(string: logoUrl)
            let data = try? Data(contentsOf: url!)
            self.logo.image = UIImage(data: data!)
        }
    }
    
    func addAceptCallback(acceptCallback: @escaping ActionBlock) {
        _acceptCallback = acceptCallback;
        self.accept.addTarget(self, action: #selector(callAcceptBlock), for: UIControl.Event.touchUpInside)
    }
    
    func addRejectCallback(rejectCallback: @escaping ActionBlock) {
        _rejectCallback = rejectCallback;
        self.reject.addTarget(self, action: #selector(callRejectBlock), for: UIControl.Event.touchUpInside)
    }
    
    func loadImage(_ logoPath: String){
        guard
            let logoUrl = URL(string: logoPath)
        else {
            return
        }

        self.logo.isHidden = false;
        logo.layer.cornerRadius = 30
        DispatchQueue.global(qos: .userInitiated).async {
            let data = try? Data(contentsOf: logoUrl)
            guard let imgData = data else { return }

            DispatchQueue.main.async {
                self.logo.image = UIImage(data: imgData)
            }
        }
    }
    
}
