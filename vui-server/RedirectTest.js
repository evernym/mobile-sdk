let vcx = require('node-vcx-wrapper');
let ffi = require('ffi');
let fs = require('fs');

const {Schema, CredentialDef, Connection, IssuerCredential, Proof, StateType, Error, rustAPI} = vcx;

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

const connection_type = 'QR'

const connectionArgs =
{
  "connection_type" : connection_type,
  "phone" : '+381648055416',
  "use_public_did" : true
}

async function run() {
    await vcx.initVcx("vcxconfig.json");

    const connection1 = await Connection.create({id: 'Connection 1'});
    await connection1.connect({ data : JSON.stringify(connectionArgs) });
    await connection1.updateState();
    let details = await connection1.inviteDetails(true);
    if (connection_type === 'QR') {
        console.log("*** Create QR code out of this invite details externally and scan it with ConnectMe app ***\n");
    }
    if (connection_type === 'SMS') {
        console.log("SMS connection invite has been sent. Please accept it in ConnectMe app. Invite details:\n")
    }
    console.log(details);
    
    let connection_state = await connection1.getState();
    while (connection_state !== StateType.Accepted) {
        await sleep(2000);
        await connection1.updateState();
        connection_state = await connection1.getState();
	    console.log("connection state: " ,connection_state);
    }
    console.log('Connection 1 was accepted!');

    const connection2 = await Connection.create({id: 'Connection 2'});
    await connection1.connect({ data : JSON.stringify(connectionArgs) });
    await connection1.updateState();
    let details = await connection1.inviteDetails(true);
    if (connection_type === 'QR') {
        console.log("*** Create QR code out of this invite details externally and scan it with ConnectMe app ***\n");
    }
    if (connection_type === 'SMS') {
        console.log("SMS connection invite has been sent. Please accept it in ConnectMe app. Invite details:\n")
    }
    console.log(details);

    connection_state = await connection2.getState();
    while (connection_state !== StateType.Redirected) {
        await sleep(2000);
        await connection2.updateState();
        connection_state = await connection2.getState();
            console.log("connection state: " ,connection_state);
    }
    console.log('Connection 2 was redirected!');
    console.log('Redirect details: ');
    console.log(await connection2.getRedirectDetails());
    
}

run();
