let vcx = require('node-vcx-wrapper');
let ffi = require('ffi');
let fs = require('fs');

const {Schema, CredentialDef, Connection, IssuerCredential, Proof, StateType, Error, rustAPI} = vcx;

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function run() {
    const myffi = ffi.Library('/usr/lib/libnullpay.so', {nullpay_init: ['void', []]});
    await myffi.nullpay_init();
    await vcx.initVcx("vcxconfig.json");
    
    const connectionToHolder = await Connection.create({id: 'Holder'});
    await connectionToHolder.connect({data:'{"use_public_did": true}'});
    await connectionToHolder.updateState();
    const details = await connectionToHolder.inviteDetails(true);
    console.log("*** Create QR code out of this invite externally and scan it with Holder's ConnectMe app ***\n");
    console.log(details);
    
    let connection_state = await connectionToHolder.getState();
    while (connection_state !== StateType.Accepted) {
        await sleep(2000);
        await connectionToHolder.updateState();
        connection_state = await connectionToHolder.getState();
    }
    console.log('Connection was accepted!');
    //console.log('TEST:\n',JSON.stringify(await connectionToHolder.serialize(), null, 4));
    const serialized_connection = await connectionToHolder.serialize();
    // console.log("SERIALIZED",serialized_connection);
   //  console.log("STRINGIFIED",JSON.stringify(serialized_connection, null, 4));
    const connection_file_path = `./serialized_connection.json`;
    fs.writeFileSync(connection_file_path, JSON.stringify(serialized_connection, null, 4));
    console.log('Connection was serialized to a file!');
}

run();
 
