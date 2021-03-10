// @flow
/**
 * @jest-environment node
 */

import https from 'https'
import http from 'http'
import fs from 'fs'
import { post, get } from 'axios'
import R from 'ramda'
import { v4 as uuidv4 } from 'uuid'
import { exec } from 'child-process-async'
import chalk, { magentaBright } from 'chalk'
import ngrok from 'ngrok'

export type InvitationType = 'connection-invitation' | 'out-of-band-invitation'
export type QRType = 'ARIES_V1_QR' | 'ARIES_OUT_OF_BAND'

export const CLAIM_OFFER_PROFILE_INFO = 'Profile Info'
export const CLAIM_OFFER_ADDRESS = 'Address'
export const CLAIM_OFFER_CONTACT = 'Contact'
export const CLAIM_OFFER_MIXED = 'Profile Address & Contact'

export const PROOF_TEMPLATE_SINGLE_CLAIM_FULFILLED =
  'Automated Single claim fulfilled'
export const PROOF_TEMPLATE_TWO_CLAIM_FULFILLED =
  'Automated Two claim fulfilled'
export const PROOF_TEMPLATE_MISSING_ATTRIBUTES =
  'Automated Missing attributes multiple claims'

const ngrokToken = '1iJaXfMlZOJKhCqqLHSn09L6fkq_3rPWVgnjc8rDUsV4qbeJo' // evernym's pipeline
// const ngrokToken = '1iHZRRRaUTHxjpkECKB8mMJgEur_4iyr4c1chCR8KwtXV7euu' // local

// QA RC
export const VASconfig = {
  verityUrl: 'https://vas.pqa.evernym.com/api/',
  verityPublicDID: 'D6tuzxJe4Vpyz2XwTwnf7T',
  verityPublicVerKey: '7bZHdWn2KNyD36iRxQSLqikFKmjFYfAyBjYJqw76Tfqg',
  domainDID: 'PofY18gShVSS4wfN5pmYjB',
  verityAgentVerKey: 'Tz4Z41bUAJJJgMCm1WhkjqLq7nFVP2bLC9WFXrbwEj6',
  sdkVerKeyId: 'KGtd7qrDmudHSRuc8ox5dP',
  sdkVerKey: 'AxgDQMEvACUxYE6oEpYSNC43EyawKpBSfD19xwx8kkko',
  version: '0.2',
  apiKey:
    'AxgDQMEvACUxYE6oEpYSNC43EyawKpBSfD19xwx8kkko:2WCxXCjFhrpRUtz93XQZxsqGcqaBpPnmkvJa8FEH16HPEnMXCAzChVsCdqcNh9bYieBCYma77pZAMKqtXdzADu3z',
}

// // Dev Team 1
// export const VASconfig = {
//   verityUrl: 'https://vas-team1.pdev.evernym.com/api/',
//   domainDID: 'XNRkA8tboikwHD3x1Yh7Uz',
//   apiKey: 'HZ3Ak6pj9ryFASKbA9fpwqjVh42F35UDiCLQ13J58Xoh:4Wf6JtGy9enwwXVKcUgADPq7Pnf9T2YZ8LupMEVxcQQf98uuRYxWGHLAwXWp8DtaEYHo4cUeExDjApMfvLJQ48Kp'
// }

export class VAS {
  verityConfig: any
  httpsConfig: any
  verityUrl: string
  domainDID: string
  endpointServer: any
  responseTimeout: number

  constructor(verityConfig: any) {
    global.responses = {}
    this.verityConfig = verityConfig
    this.httpsConfig = {
      timeout: 180000,
      httpsAgent: new https.Agent({}),
      headers: {
        'X-API-KEY': this.verityConfig['apiKey'],
      },
    }
    this.verityUrl = this.verityConfig['verityUrl']
    this.domainDID = this.verityConfig['domainDID']
    this.responseTimeout = 15000

    this.endpointServer = http
      .createServer(function (request, response) {
        const { headers, method, url } = request
        let body = ''
        request
          .on('error', (err) => {
            console.error(err)
          })
          .on('data', (chunk) => {
            body += chunk.toString()
          })
          .on('end', () => {
            body = JSON.parse(body)
            global.lastResponse = body
            if (body['@type']) {
              global.responses[`${body['@type']}`] = body
            }
            console.log(
              '----------------\n',
              `Headers: ${JSON.stringify(headers)}\n`,
              `Method: ${method}\n`,
              `URL: ${url}\n`,
              `Body: ${JSON.stringify(global.lastResponse, null, 2)}\n`,
              // `All Responses: ${JSON.stringify(global.responses, null, 2)}\n`, // DEBUG
              '----------------\n'
            )
          })
      })
      .listen(1338)
    console.log(chalk.greenBright('VAS server is listening on port 1338...'))
  }

  async setupNgrok(): string {
    // await ngrok.kill() // kills ngrok process
    // const { res } = await exec(`pkill ngrok`) // for sure
    // console.log(chalk.yellowBright(res))
    // ------
    await ngrok.authtoken(ngrokToken)
    const url = await ngrok.connect(1338)
    console.log(chalk.yellowBright(url))

    //$FlowFixMe
    return url
  }

  async shutdownNgrok() {
    await ngrok.disconnect() // stops all
    await ngrok.kill() // kills ngrok process
    // const { res } = await exec(`pkill ngrok`) // for sure
    // console.log(chalk.yellowBright(res))
  }

  async registerEndpoint(endpointUrl: string): string {
    const result = await post(
      `${this.verityUrl}${this.domainDID}/configs/0.6/${uuidv4()}`,
      {
        '@id': uuidv4(),
        '@type':
          'did:sov:123456789abcdefghi1234;spec/configs/0.6/UPDATE_COM_METHOD',
        comMethod: {
          id: 'webhook',
          type: 2,
          value: endpointUrl, // it changes everytime you run ngrok
          packaging: {
            pkgType: 'plain',
          },
        },
      },
      this.httpsConfig
    )
      .catch((err) => console.error(err))
      .then((res) => res.data)

    //$FlowFixMe
    return result
  }

  async createRelationship(label: string): Array<string> {
    const result = await post(
      `${this.verityUrl}${this.domainDID}/relationship/1.0/${uuidv4()}`,
      {
        '@type': 'did:sov:123456789abcdefghi1234;spec/relationship/1.0/create',
        '@id': uuidv4(),
        label: label,
      },
      this.httpsConfig
    )
      .catch((err) => console.error(err))
      .then((res) => res.data)

    await new Promise((r) => setTimeout(r, this.responseTimeout))

    // const relationshipThreadID = global.lastResponse['~thread']['thid']
    const relationshipThreadID =
      global.responses[
        'did:sov:123456789abcdefghi1234;spec/relationship/1.0/created'
      ]['~thread']['thid'] // new approach
    console.log(chalk.magentaBright(`THREAD ID: ${relationshipThreadID}`))
    // const DID = global.lastResponse['did']
    const DID =
      global.responses[
        'did:sov:123456789abcdefghi1234;spec/relationship/1.0/created'
      ]['did'] // new approach
    console.log(chalk.magentaBright(`RELATIONSHIP DID: ${DID}`))

    //$FlowFixMe
    return [relationshipThreadID, DID, result]
  }

  async relationshipInvitation(
    relationshipThreadID: string,
    DID: string,
    invitationType: InvitationType,
    qrType: QRType
  ): string {
    const result = await post(
      `${this.verityUrl}${this.domainDID}/relationship/1.0/${relationshipThreadID}`,
      {
        '@type': `did:sov:123456789abcdefghi1234;spec/relationship/1.0/${invitationType}`,
        '@id': uuidv4(),
        '~forRelationship': DID, // wrong
        // '~for_relationship': DID,
        // 'shortInvite': true // QA VAS should be upgraded for this!
      },
      this.httpsConfig
    )
      .catch((err) => console.error(err))
      .then((res) => res.data)

    await new Promise((r) => setTimeout(r, this.responseTimeout))

    // let link = global.lastResponse['inviteURL']
    let link =
      global.responses[
        'did:sov:123456789abcdefghi1234;spec/relationship/1.0/invitation'
      ]['inviteURL'] // new approach
    console.log(chalk.cyanBright(link))
    link = link.substr(45) // this depends on verity environment used
    console.log(chalk.cyanBright(link))
    const { stdout } = await exec(`echo "${link}" | base64 --decode`)
    console.log(chalk.cyanBright(stdout))
    const payload = stdout

    const jsonData = JSON.stringify({
      payload: JSON.parse(payload),
      type: qrType,
      version: '1.0',
      original: payload,
    })
    console.log(chalk.magentaBright(jsonData))

    // this method should also return deeplink got by shortInvite to establish connection using:
    // `adb shell am start -a android.intent.action.VIEW -d "https://connectme.app.link/?t=https://vty.im/e4s9w" me.connect` for android
    // `xcrun simctl openurl booted "https://connectme.app.link/?t=https://vty.im/e4s9w"` for ios

    //$FlowFixMe
    return jsonData
  }

  async createSchema(name: string, attributes: Array<string>): Array<string> {
    const result = await post(
      `${this.verityUrl}${this.domainDID}/write-schema/0.6/${uuidv4()}`,
      {
        '@type': 'did:sov:123456789abcdefghi1234;spec/write-schema/0.6/write',
        '@id': uuidv4(),
        name: `${name}_${uuidv4()}`,
        version: '1.0',
        attrNames: attributes,
      },
      this.httpsConfig
    )
      .catch((err) => console.error(err))
      .then((res) => res.data)

    await new Promise((r) => setTimeout(r, this.responseTimeout * 2))

    // const schemaID = global.lastResponse['schemaId']
    let schemaID
    try {
      schemaID =
        global.responses[
          'did:sov:123456789abcdefghi1234;spec/write-schema/0.6/status-report'
        ]['schemaId'] // new approach
    } catch (e) {
      await new Promise((r) => setTimeout(r, this.responseTimeout))
      schemaID =
        global.responses[
          'did:sov:123456789abcdefghi1234;spec/write-schema/0.6/status-report'
        ]['schemaId']
    }
    console.log(chalk.magentaBright(`SCHEMA ID: ${schemaID}`))

    //$FlowFixMe
    return [schemaID, result]
  }

  async createCredentialDef(name: string, schemaID: string): Array<string> {
    const result = await post(
      `${this.verityUrl}${this.domainDID}/write-cred-def/0.6/${uuidv4()}`,
      {
        '@type': 'did:sov:123456789abcdefghi1234;spec/write-cred-def/0.6/write',
        '@id': uuidv4(),
        name: `${name}_${uuidv4()}`,
        tag: 'tag',
        schemaId: schemaID,
        revocationDetails: {
          support_revocation: false,
          tails_file: 'string',
          max_creds: 100,
        },
      },
      this.httpsConfig
    )
      .catch((err) => console.error(err))
      .then((res) => res.data)

    await new Promise((r) => setTimeout(r, this.responseTimeout * 2))

    // const credDefID = global.lastResponse['credDefId']
    let credDefID
    try {
      credDefID =
        global.responses[
          'did:sov:123456789abcdefghi1234;spec/write-cred-def/0.6/status-report'
        ]['credDefId'] // new approach
    } catch (e) {
      await new Promise((r) => setTimeout(r, this.responseTimeout))
      credDefID =
        global.responses[
          'did:sov:123456789abcdefghi1234;spec/write-cred-def/0.6/status-report'
        ]['credDefId']
    }
    console.log(chalk.magentaBright(`CRED DEF ID: ${credDefID}`))

    //$FlowFixMe
    return [credDefID, result]
  }

  async sendCredentialOffer(
    DID: string,
    credDefID: string,
    values: any,
    comment: string
  ): string {
    const result = await post(
      `${this.verityUrl}${this.domainDID}/issue-credential/1.0/${uuidv4()}`,
      {
        '@type':
          'did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/issue-credential/1.0/offer',
        '@id': uuidv4(),
        '~for_relationship': DID,
        cred_def_id: credDefID,
        credential_values: values,
        price: '0',
        comment: comment,
      },
      this.httpsConfig
    )
      .catch((err) => console.error(err))
      .then((res) => res.data)

    await new Promise((r) => setTimeout(r, this.responseTimeout * 2)) // sync

    //$FlowFixMe
    return result
  }

  async issueCredential(DID: string): string {
    // await new Promise((r) => setTimeout(r, this.responseTimeout)) // sync

    // const credThreadID = global.lastResponse['~thread']['thid']
    const credThreadID =
      global.responses[
        'did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/issue-credential/1.0/sent'
      ]['~thread']['thid']
    console.log(`CRED THREAD ID: ${credThreadID}`)

    const result = await post(
      `${this.verityUrl}${this.domainDID}/issue-credential/1.0/${credThreadID}`,
      {
        '@type':
          'did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/issue-credential/1.0/issue',
        '@id': uuidv4(),
        '~for_relationship': DID,
      },
      this.httpsConfig
    )
      .catch((err) => console.error(err))
      .then((res) => res.data)

    await new Promise((r) => setTimeout(r, this.responseTimeout * 4)) // sync

    //$FlowFixMe
    return result
  }

  async presentProof(
    DID: string,
    name: string,
    attributes: Array<string>,
    self_attest_allowed: boolean
  ): string {
    const result = await post(
      `${this.verityUrl}${this.domainDID}/present-proof/1.0/${uuidv4()}`,
      {
        '@type':
          'did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/present-proof/1.0/request',
        '@id': uuidv4(),
        '~for_relationship': DID,
        name: name,
        proof_attrs: [
          {
            names: attributes,
            self_attest_allowed: self_attest_allowed,
          },
        ],
      },
      this.httpsConfig
    )
      .catch((err) => console.error(err))
      .then((res) => res.data)

    await new Promise((r) => setTimeout(r, this.responseTimeout * 2)) // sync

    //$FlowFixMe
    return result
  }
}

// TODO: Implement different method for self-attested proofs or rework this one ^^^

export function getDeferred() {
  let resolve
  let reject

  //$FlowFixMe
  const promise = new Promise((res, rej) => {
    resolve = res
    reject = rej
  })

  return { resolve, reject, promise }
}
