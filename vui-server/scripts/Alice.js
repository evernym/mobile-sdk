let vcx = require('node-vcx-wrapper');
let ffi = require('ffi');
let fs = require('fs');
const readlineSync = require('readline-sync')

function getRandomInt (min, max) {
  min = Math.ceil(min)
  max = Math.floor(max)
  return Math.floor(Math.random() * (max - min)) + min
} 
const {Schema, CredentialDef, Connection, Credential, DisclosedProof, IssuerCredential, Proof, ProofState, StateType, Error, rustAPI} = vcx;

function sleepPromise(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function run() {
  const myffi = ffi.Library('/usr/lib/libnullpay.so', {nullpay_init: ['void', []]});
  await myffi.nullpay_init();
  await vcx.initVcx("vcxconfig.Alice.json");
  console.log('Input Faber.js invitation details')
  const details = readlineSync.question('Enter your invite details: ')
  const jdetails = JSON.parse(details)

  console.log('#10 Convert to valid json and string and create a connection to faber')
  const connectionToFaber = await Connection.createWithInvite({ id: 'faber', invite: JSON.stringify(jdetails) })
  await connectionToFaber.connect({ data: '{"use_public_did": true}' })
  let connectionstate = await connectionToFaber.getState()
  while (connectionstate !== StateType.Accepted) {
    await sleepPromise(2000)
    await connectionToFaber.updateState()
    connectionstate = await connectionToFaber.getState()
  }

  console.log('#11 Wait for faber.py to issue a credential offer')
  await sleepPromise(10000)
  const offers = await Credential.getOffers(connectionToFaber)
  console.log(`Alice found ${offers.length} credential offers.`)
  console.log(JSON.stringify(offers))

  // Create a credential object from the credential offer
  const credential = await Credential.create({ sourceId: 'credential', offer: JSON.stringify(offers[0]) })

  console.log('#15 After receiving credential offer, send credential request')
  await credential.sendRequest({ connection: connectionToFaber, payment: 0 })

  console.log('#16 Poll agency and accept credential offer from faber')
  let credentialState = await credential.getState()
  while (credentialState !== StateType.Accepted) {
    await sleepPromise(2000)
    await credential.updateState()
    credentialState = await credential.getState()
  }

  console.log('#22 Poll agency for a proof request')
  const requests = await DisclosedProof.getRequests(connectionToFaber)

  console.log('#23 Create a Disclosed proof object from proof request')
  const proof = await DisclosedProof.create({ sourceId: 'proof', request: JSON.stringify(requests[0]) })

  console.log('#24 Query for credentials in the wallet that satisfy the proof request')
  const credentials = await proof.getCredentials()

  // Use the first available credentials to satisfy the proof request
  for (let i = 0; i < Object.keys(credentials.attrs).length; i++) {
    const attr = Object.keys(credentials.attrs)[i];
    credentials.attrs[attr] = {
      credential: credentials.attrs[attr][0]
    }
  }

  console.log('#25 Generate the proof')
  await proof.generateProof({ selectedCreds: credentials, selfAttestedAttrs: {} })

  console.log('#26 Send the proof to faber')
  await proof.sendProof(connectionToFaber)

  let proofState = await proof.getState()
  while (proofState !== StateType.Accepted) {
    console.log("proofState: ",proofState);
    await sleepPromise(2000)
    await proof.updateState()
    proofState = await proof.getState()
  }
  console.log('Proof is verified.');
  process.exit(0);
}

run()
