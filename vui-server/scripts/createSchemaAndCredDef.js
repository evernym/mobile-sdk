let vcx = require('node-vcx-wrapper');
let ffi = require('ffi');

async function run() {
    const myffi = ffi.Library('/usr/lib/libnullpay.so', {nullpay_init: ['void', []]});
    await myffi.nullpay_init();
    await vcx.initVcx("vcxconfig.json");

    const schemaData = {
        "data": {
          "attrNames": [
            "First name",
            "Last name",
            "Date of birth",
            "Credit rating"
          ],
          "name": "CreditRating",
          "version": "1.0.4"
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
        let schema_lookup = await vcx.Schema.lookup({"sourceId":"CreditRatingSchema", "schemaId":schemaId});
        console.log("Schema lookup: ", schema_lookup); 
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
        console.log("Serialized cred def:\n", serCredDef);
        console.log("\n\nCHANGE SERIALIZED CREDENTIAL DEFINITION IN Issuer.js SCRIPT")
    }
    catch(error) {
      console.log("ERROR: ",error)
    }
}

run(); 
