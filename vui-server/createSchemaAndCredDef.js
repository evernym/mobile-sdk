let vcx = require('node-vcx-wrapper');
let ffi = require('ffi');
let fs = require('fs');

function getRandomInt (min, max) {
    min = Math.ceil(min)
    max = Math.floor(max)
    return Math.floor(Math.random() * (max - min)) + min
}

async function run() {
    const myffi = ffi.Library('/usr/lib/libnullpay.so', {nullpay_init: ['void', []]});
    await myffi.nullpay_init();
    await vcx.initVcx("vcxconfig.json");

    const version = `${getRandomInt(1, 1000)}.${getRandomInt(1, 1000)}.${getRandomInt(1, 1000)}`
    const schemaData = {
        "data": {
          "attrNames": [
            "First name",
            "Last name",
            "Date of birth",
            "Credit rating"
          ],
          "name": "CreditRating",
          "version": version 
        },
        "paymentHandle": 0,
        "sourceId": "CreditRatingSchema"
      };

    let schema;
    let schemaId;
    try {
        schema = await vcx.Schema.create(schemaData);
        schemaId = await schema.getSchemaId();
        console.log("Schema created: ", schemaId);
    }
    catch(error) {
        console.log("ERROR: ",error)
    }

    const data = {
        name: 'CreditRatingCredDef',
        paymentHandle: 0,
        revocation: false,
        revocationDetails: {
            tailsFile: 'tails.txt',
        },
        schemaId: schemaId,
        sourceId: 'CreditRatingCredDef'
    };
    try {
        const cred_def = await vcx.CredentialDef.create(data);
        const cred_def_id = await cred_def.getCredDefId();
        console.log ("Credential definition created: ", cred_def_id)
        const serCredDef = await cred_def.serialize();
	const serialized_cred_def_file_path = `./serialized_cred_def.json`;
        fs.writeFileSync(serialized_cred_def_file_path, JSON.stringify(serCredDef, null, 4));
	console.log('Credential definition was serialized to a file ',serialized_cred_def_file_path);
	process.exit(0);
    }
    catch(error) {
      console.log("ERROR: ",error)
    }
}

run(); 
