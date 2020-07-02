let vcx = require('node-vcx-wrapper');
let ffi = require('ffi');
let fs = require('fs');

const {
  Schema,
  CredentialDef,
  Connection,
  IssuerCredential,
  Proof,
  StateType,
  Error,
  rustAPI
} = vcx;

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function run() {
    const myffi = ffi.Library('/usr/lib/libnullpay.so', {nullpay_init: ['void', []]});
    await myffi.nullpay_init();
    await vcx.initVcx("vcxconfig.json");

    let serialized_connection = fs.readFileSync('./serialized_connection.json');
    let connectionToHolder = await Connection.deserialize(JSON.parse(serialized_connection));
    
    var serializedCredDef = 
   { version: '1.0',
   data: 
   { id: 'Ya3qqZKLbzLHFEH5JZYMa3:3:CL:79298:tag1',
     tag: 'tag1',
     name: 'CreditRatingCredDef',
     source_id: 'CreditRatingCredDef',
     issuer_did: 'Ya3qqZKLbzLHFEH5JZYMa3',
     cred_def_payment_txn: null,
     rev_reg_def_payment_txn: null,
     rev_reg_delta_payment_txn: null,
     rev_reg_id: null,
     rev_reg_def: null,
     rev_reg_entry: null,
     tails_file: 'tails.txt',
     state: 1 } } 
    var credDef = await CredentialDef.deserialize(serializedCredDef);
    var credDefHandle = credDef.handle;

    const attrs = {
        'First name': 'John',
        'Last name': 'Doe',
        'Date of birth': '01.01.1970',
        'Credit rating': 'BB-',
    };

    let credential = await IssuerCredential.create({
                sourceId: '123',
                credDefHandle: credDefHandle,
                attr: attrs,
                credentialName: 'Customer Record',
                price: '0'
              })
    await credential.sendOffer(connectionToHolder);
    await credential.updateState();
    console.log("Credential offer sent");

    console.log("Poll the agency and wait for the holder to send a credential request");
    let credential_state = await credential.getState();
    while (credential_state !== StateType.RequestReceived) {
        await sleep(2000);
        await credential.updateState();
        credential_state = await credential.getState();
    }

    console.log("Issue a credential to a holder");
    await credential.sendCredential(connectionToHolder);


    console.log("Wait for the holder to accept the credential");
    await credential.updateState();
    credential_state = await credential.getState();
    while (credential_state !== StateType.Accepted) {
        sleep(2000);
        await credential.updateState();
        credential_state = await credential.getState();
    }
    

}

run()
