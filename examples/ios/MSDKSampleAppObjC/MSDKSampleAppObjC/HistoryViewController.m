//
//  HistoryViewController.m
//  MSDKSampleAppObjC
//
//  Created by Evernym on 25.06.2021.
//  Copyright © 2021 Norman Jarvis. All rights reserved.
//

#import "HistoryViewController.h"
#import "LocalStorage.h"

@interface HistoryViewController ()
@property NSDictionary* history;
@property (nonatomic, readwrite, strong) IBOutlet UITableView *tableView;

@end
@implementation HistoryViewController
@synthesize history;

- (void)viewDidLoad {
    [super viewDidLoad];
    self.tableView.dataSource = self;
    self.tableView.delegate = self;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear: animated];
    history = [LocalStorage getObjectForKey: @"history" shouldCreate: false];
    [self.tableView reloadData];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [history.allKeys count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cell"];
    NSDictionary *historyDict = history[history.allKeys[indexPath.row]];
    NSString *name = [historyDict objectForKey: @"name"];
    cell.textLabel.text = name;

    return cell;
}

@end
