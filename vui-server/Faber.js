let vcx = require('node-vcx-wrapper');
let ffi = require('ffi');
let fs = require('fs');

function getRandomInt (min, max) {
  min = Math.ceil(min)
  max = Math.floor(max)
  return Math.floor(Math.random() * (max - min)) + min
} 
const {Schema, CredentialDef, Connection, IssuerCredential, Proof, ProofState, StateType, Error, rustAPI} = vcx;

function sleepPromise(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function run() {
  const myffi = ffi.Library('/usr/lib/libnullpay.so', {nullpay_init: ['void', []]});
  await myffi.nullpay_init();
	
  await vcx.initVcx("vcxconfig.Faber.json");
  const version = `${getRandomInt(1, 1001)}.${getRandomInt(1, 1001)}.${getRandomInt(1, 1001)}`
  const schemaData = {
        "data": {
          "attrNames": ["Legal Entity Identifier","Name","Address","Tax number","Registration date"],
          "name": "Business Registry Record",
         version 
        },
        "paymentHandle": 0,
        "sourceId": "1"
      }; 
  console.log(`#3 Create a new schema on the ledger: ${JSON.stringify(schemaData, null, 2)}`)
  const schema = await Schema.create(schemaData)
  const schemaId = await schema.getSchemaId()
  console.log(`Created schema with id ${schemaId}`)

  console.log('#4 Create a new credential definition on the ledger')
  const data = {
    name: 'DemoCredential123',
    paymentHandle: 0,
    revocation: false,
    revocationDetails: {
      tailsFile: 'tails.txt'
    },
    schemaId: schemaId,
    sourceId: 'testCredentialDefSourceId123'
  }
  const credDef = await CredentialDef.create(data)
  const credDefId = await credDef.getCredDefId()
  const credDefHandle = credDef.handle
  console.log(`Created credential with id ${credDefId} and handle ${credDefHandle}`)

  console.log('#5 Create a connection to alice and print out the invite details')
  const connectionToAlice = await Connection.create({ id: 'alice' })
  await connectionToAlice.connect('{}')
  await connectionToAlice.updateState()
  const details = await connectionToAlice.inviteDetails(false)
  console.log('\n\n**invite details**')
  console.log("**You'll ge queried to paste this data to alice side of the demo. This is invitation to connect.**")
  console.log("**It's assumed this is obtained by Alice from Faber by some existing secure channel.**")
  console.log('**Could be on website via HTTPS, QR code scanned at Faber institution, ...**')
  console.log('\n******************\n\n')
  console.log(JSON.stringify(JSON.parse(details)))
  console.log('\n\n******************\n\n')

  console.log('#6 Polling agency and waiting for alice to accept the invitation. (start alice.py now)')
  let connectionState = await connectionToAlice.getState()
  while (connectionState !== StateType.Accepted) {
    await sleepPromise(2000)
    await connectionToAlice.updateState()
    connectionState = await connectionToAlice.getState()
  }
  console.log('Connection to alice was Accepted!')

  const schemaAttrs = {
      "Name": "Acme Corp",
      "Legal Entity Identifier": "0039487214",
      "Tax number": "702914556",
      "Address": "682 New Creek Road",
      "Registration date": "23.12.2017"}

  console.log('#12 Create an IssuerCredential object using the schema and credential definition')

  const credentialForAlice = await IssuerCredential.create({
    attr: schemaAttrs,
    sourceId: 'alice_degree',
    credDefHandle,
    credentialName: 'cred',
    price: '0'
  })

  console.log('#13 Issue credential offer to alice')
  await credentialForAlice.sendOffer(connectionToAlice)
  await credentialForAlice.updateState()

  console.log('#14 Poll agency and wait for alice to send a credential request')
  let credentialState = await credentialForAlice.getState()
  while (credentialState !== StateType.RequestReceived) {
    await sleepPromise(2000)
    await credentialForAlice.updateState()
    credentialState = await credentialForAlice.getState()
    console.log("credentialState: ",credentialState);
  }

  console.log('#17 Issue credential to alice')
  await credentialForAlice.sendCredential(connectionToAlice)

  console.log('#18 Wait for alice to accept credential')
  await credentialForAlice.updateState()
  credentialState = await credentialForAlice.getState()
  while (credentialState !== StateType.Accepted) {
    await sleepPromise(2000)
    await credentialForAlice.updateState()
    credentialState = await credentialForAlice.getState()
    console.log("credentialState: ",credentialState);
  }

  const proofAttributes = [
          {
            "name": "Legal Entity Identifier", restrictions: [ {issuer_id: 'Ya3qqZKLbzLHFEH5JZYMa3'} ]
          },
          {
            "name": "Tax number"
          },
          {
            "name": "Address"
          },
          {
            "name": "Registration date"
          },
	  {
            "name": "Name"
          }
        ]

  console.log('#19 Create a Proof object')
  const proof = await Proof.create({
    sourceId: '213',
    attrs: proofAttributes,
    name: 'proofForAlice',
    revocationInterval: {}
  })

  console.log('#20 Request proof of degree from alice')
  await proof.requestProof(connectionToAlice)

  console.log('#21 Poll agency and wait for alice to provide proof')
  let proofState = await proof.getState()
  console.log("proof_state: ",proofState);
  while (proofState == StateType.OfferSent) {
    await sleepPromise(2000)
    await proof.updateState()
    proofState = await proof.getState()
    console.log("proofState: ",proofState);
  }

  console.log('#27 Process the proof provided by alice')
  await proof.getProof(connectionToAlice)

  console.log('#28 Check if proof is valid')
  if (proof.proofState === ProofState.Verified) {
    console.log('Proof is verified')
  } else {
    console.log('Proof is NOT verified')
  };
  process.exit(0);
}

run()
