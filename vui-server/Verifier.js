let vcx = require('node-vcx-wrapper');
let ffi = require('ffi');
let fs  = require('fs');

const {Schema, CredentialDef, Connection, IssuerCredential, Proof, ProofState, StateType, Error, rustAPI} = vcx;

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function run() {
    await vcx.initVcx("./vcxconfig.json");
   
    let serialized_connection = fs.readFileSync('./serialized_connection.json');
    let connectionToHolder = await Connection.deserialize(JSON.parse(serialized_connection));

    const proofAttributes = [
        {'name': 'First name'},
        {'name': 'Last name'},
        {'name': 'Date of birth'},
        {'name': 'Credit rating'}
    ];

    console.log("Create a Proof object");
    const proof = await Proof.create({
        sourceId: "213",
        attrs: proofAttributes,
        name: 'proofForHolder',
        revocationInterval: {}
    });
    
    console.log("Request proof of the credit rating from the holder");
    await proof.requestProof(connectionToHolder);

    console.log("Poll the agency on every 2 seconds and wait for the holder to provide a proof");
    let proofState = await proof.getState();
    let counter = 0;
    while (proofState == StateType.OfferSent) {
        await sleep(2000);
        await proof.updateState();
        proofState = await proof.getState();
	counter = counter + 1;
	console.log("Polling attempt: ",counter,", proof state: ",proofState);
    }

    console.log("Process the proof provided by the holder");
    let proof_data = await proof.getProof(connectionToHolder);
    let valid = proof_data.proofState;
    if (valid === ProofState.Verified) {
        console.log("Proof is verified")
    } else {
        console.log("Could not verify proof")
    }
    console.log("Proof object: ", JSON.stringify(proof_data, null, 4));
    process.exit(0);

}

run()
