//
//  HistoryViewController.swift
//  MSDKSampleAppSwift
//
//  Created by Evernym on 06.07.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

class HistoryViewController: UIViewController {
    
    @IBOutlet var tableView: UITableView!
    
    var history: [String: Any] = [:]

    override func viewDidLoad() {
        super.viewDidLoad();
        tableView.delegate = self;
        tableView.dataSource = self;
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated);
        if let storageHistory = LocalStorage.getObjectForKey("history", shouldCreate: false) as? [String: Any] {
            self.history = storageHistory;
            self.tableView.reloadData();
        }
    }
    
    private func historyEventByIndex(_ index: Int) -> Any? {
        let historyIDs = Array(history.keys);
        let historyID = historyIDs[index];
        return history[historyID];
    }
}

extension HistoryViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return history.count;
    }
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1;
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath);
        let historyEvent = self.historyEventByIndex(indexPath.row) as? [String: Any] ?? [:];
        cell.textLabel?.text = historyEvent["name"] as? String;
        return cell;
    }
}

