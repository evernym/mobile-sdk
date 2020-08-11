//
//  ProofDetailsViewController.m
//  CMeSdkObjc
//
//  Created by Predrag Jevtic on 6/17/20.
//  Copyright © 2020 Norman Jarvis. All rights reserved.
//

#import "ProofDetailsViewController.h"
#import "ProofAttributeTableViewCell.h"
#import "CMUtilities.h"
#import "CMProofRequest.h"

@interface ProofDetailsViewController ()

@property (weak, nonatomic) IBOutlet UILabel *titleLbl;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UITextField* activeTextField;
@end

@implementation ProofDetailsViewController

@synthesize connection, proof, decriptedProof, proofAttributes, proofFields;

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    decriptedProof = [CMUtilities jsonToDictionary: proof[@"payload"][@"@msg"]];
}
- (void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear: animated];
    
    proofFields = [decriptedProof[@"proof_request_data"][@"requested_attributes"] allKeys];
    [self.tableView reloadData];

    [CMProofRequest autofillAttributes: proof andConnection: connection withCompletionHandler: ^(NSDictionary *proofAttributes, NSError *error) {
        if(proofAttributes) {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.proofAttributes = proofAttributes;
                [self.tableView reloadData];
            });
        }
    }];
}

- (NSInteger)tableView: (UITableView *)tableView numberOfRowsInSection: (NSInteger)section {
    NSArray* attributes = self.proof[@"attributes"];
    return attributes ? [attributes count] : 0;
}

- (UITableViewCell *)tableView: (UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ProofAttributeTableViewCell* cell = [tableView dequeueReusableCellWithIdentifier: @"proofAttributeCell" forIndexPath: indexPath];
    NSString* attrKey = proofFields[indexPath.row];
    NSDictionary* attribute = decriptedProof[@"proof_request_data"][attrKey];
    NSLog(@"proof cell %@", attribute);
    cell.attributeValueTextField = _activeTextField;
    [cell updateAttribute: attrKey fieldName: attribute[@"name"] andValue: proofAttributes[attrKey]];
    return cell;
}

- (IBAction)sendProofRequest:(id)sender {
    [CMProofRequest sendProofRequest: proof proofAttributes: proofAttributes andConnection: connection withCompletionHandler:^(NSString *successMessage, NSError *error) {
        if(error) {
            [CMUtilities printError: error];
        }
        UIAlertController* alert = [UIAlertController alertControllerWithTitle: @"Proof Request sent!" message: @"Proof request has been signed and sent successfully." preferredStyle: UIAlertControllerStyleAlert];

        UIAlertAction* defaultAction = [UIAlertAction actionWithTitle: @"OK" style: UIAlertActionStyleDefault handler: ^(UIAlertAction * action) {}];
        [alert addAction: defaultAction];
        [self presentViewController: alert animated: YES completion: nil];
    }];
}

@end
