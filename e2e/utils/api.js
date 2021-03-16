// @flow
/**
 * @jest-environment node
 */

import { post, get } from 'axios'
import imap from 'imap-simple'
import moment from 'moment'
import R from 'ramda'
import https from 'https'
import fs from 'fs'
import { exec } from 'child-process-async'
import chalk from 'chalk'

//$FlowFixMe
require('tls').DEFAULT_ECDH_CURVE = 'auto'

const httpsConfig = {
  timeout: 300000,
  httpsAgent: new https.Agent({
    ca: fs.readFileSync('devops/ca.crt'),
  }),
  auth: {
    username: 'demo',
    password: process.env.VUI_PWD,
  },
}

function getVerityApi() {
  //$FlowFixMe
  let api = `https://vui.p${process.env.environment}.evernym.com` // we don't use any environment except `qa` and `dev`
  console.log(api)
  if (!api) {
    return null
  }

  if (api) {
    api = api.trim()
    if (api[api.length - 1] === '/') {
      api = api.slice(0, api.length - 1)
    }

    return `${api}/api/v1`
  }
}

const verityBaseUrl = getVerityApi()

if (!verityBaseUrl) {
  throw new Error(
    'We did not find url in VERITY_API environment variable. Please set an environment variable with the name VERITY_API and set the value to verity-ui base url. On *nix based terminal, you can run `$ export VERITY_API=https://5be99b12.ngrok.io` You can get the url from the machine which is running verity ui server.'
  )
}

// if (!process.env.SMS_INBOX_PASSWORD) {
//   throw new Error(
//     'SMS_INBOX_PASSWORD environment variable not found. On *nix based terminal you can run `$ export SMS_INBOX_PASSWORD=password` to set environment variable for current session. Or you could add it your source file.'
//   )
// }

const inboxConfig = {
  imap: {
    user: 'number.2018@yahoo.com',
    password: process.env.MAIL_PWD,
    host: 'imap.mail.yahoo.com',
    port: 993,
    tls: true,
    authTimeout: 60000,
  },
}

//$FlowFixMe
const devAgencyUrl = `https://agency.p${process.env.environment}.evernym.com`

const today = moment().format('MMMM DD, YYYY')
const emailSearchCriteria = ['UNSEEN', ['SINCE', today]]
const emailFetchOptions = {
  markSeen: false,
  bodies: ['TEXT', 'HEADER'],
}

export async function getInvitation() {
  const [invitationId, qrCode] = await post(
    `${verityBaseUrl}/connections`,
    {
      name: 'Alex',
      phone: '18327364896',
      sms: true,
    },
    httpsConfig
  ).then((res) => [res.data.id, res.data.qrCode])

  console.log(chalk.magentaBright(invitationId))
  console.log(chalk.cyan(qrCode))

  const { stdout } = await exec(
    `echo "${qrCode}" | cut -c 23- | base64 --decode > tmp.png && zbarimg tmp.png && rm tmp.png`
  )
  console.log(chalk.cyan(stdout))
  const jsonData = stdout.substr(8)

  await new Promise((r) => setTimeout(r, 60000)) // wait before fetching latest message

  // not putting `await` here because we don't want to block here
  // it should run asynchronously, we will wait for inbox to open
  // when we need to get the email, not before that
  const [connection, inbox] = await imap
    .connect(inboxConfig)
    .then((connection) => {
      return [connection, connection.openBox('INBOX')]
    })
    .catch(console.error)

  await inbox.then((res) => console.log(res)).catch(console.error)
  const emails = await connection.search(emailSearchCriteria, emailFetchOptions)
  const latestEmailBody = R.compose(
    R.prop('body'),
    R.head,
    R.filter(R.eqProps('which', { which: 'TEXT' })),
    R.flatten,
    R.prop('parts'),
    R.head,
    R.sortWith([R.descend(R.prop('seqNo'))])
  )(emails)
  // const appLink = 'https://link.comect.me/?t='
  const appLink = 'https://connectme.app.link/?t='
  const appLinkIndex = latestEmailBody.indexOf(appLink)
  const tokenSize = 8
  const token = latestEmailBody.slice(
    appLinkIndex + appLink.length,
    appLinkIndex + appLink.length + tokenSize
  )

  await new Promise((r) => setTimeout(r, 30000)) // wait until latest message will be processed
  await connection.end() // close mail server connection

  // no need to wait for invitation to be fetched
  // we can call this function only to get invitation token
  // and when we need actual invitation data, then we can resolve for
  // fetchingInvitation or use await to get invitation data
  // this can be used directly by qr-code screen
  const fetchingInvitation = await get(
    `${devAgencyUrl}/agency/url-mapper/${token}`
  )
    .then(R.compose(get, R.path(['data', 'url'])))
    .catch(console.error)

  console.log(chalk.cyan(connection))
  console.log(chalk.cyan(inbox))
  console.log(chalk.cyan(token))
  console.log(chalk.cyan(fetchingInvitation))

  return [
    token,
    invitationId,
    fetchingInvitation,
    `${appLink}${token}`,
    jsonData,
  ]
}

export const CLAIM_OFFER_PROFILE_INFO = 'Profile Info'
export const CLAIM_OFFER_ADDRESS = 'Address'
export const CLAIM_OFFER_CONTACT = 'Contact'
export const CLAIM_OFFER_MIXED = 'Profile Address & Contact'

// generate schemas
export const rawSchemas = [
  {
    name: CLAIM_OFFER_PROFILE_INFO,
    fields: [
      {
        name: 'name',
        type: 0,
        constraints: [],
      },
      {
        name: 'gender',
        type: 0,
        constraints: [],
      },
      {
        name: 'height',
        type: 0,
        constraints: [],
      },
    ],
  },
  {
    name: CLAIM_OFFER_ADDRESS,
    fields: [
      {
        name: 'street',
        type: 0,
        constraints: [],
      },
      {
        name: 'city',
        type: 0,
        constraints: [],
      },
      {
        name: 'state',
        type: 0,
        constraints: [],
      },
      {
        name: 'country',
        type: 0,
        constraints: [],
      },
    ],
  },
  {
    name: CLAIM_OFFER_CONTACT,
    fields: [
      {
        name: 'physical address',
        type: 0,
        constraints: [],
      },
      {
        name: 'mailing address',
        type: 0,
        constraints: [],
      },
      {
        name: 'email',
        type: 0,
        constraints: [],
      },
      {
        name: 'phone',
        type: 0,
        constraints: [],
      },
    ],
  },
  {
    name: CLAIM_OFFER_MIXED,
    fields: [
      {
        name: 'mailing address',
        type: 0,
        constraints: [],
      },
      {
        name: 'email',
        type: 0,
        constraints: [],
      },
      {
        name: 'phone',
        type: 0,
        constraints: [],
      },
      {
        name: 'city',
        type: 0,
        constraints: [],
      },
      {
        name: 'state',
        type: 0,
        constraints: [],
      },
      {
        name: 'name',
        type: 0,
        constraints: [],
      },
      {
        name: 'gender',
        type: 0,
        constraints: [],
      },
      {
        name: 'height',
        type: 0,
        constraints: [],
      },
    ],
  },
]

const claimDefData = {
  [CLAIM_OFFER_PROFILE_INFO]: [
    {
      name: 'name',
      type: 0,
      value: 'automated test name',
    },
    {
      name: 'gender',
      type: 0,
      value: 'automated test gender',
    },
    {
      name: 'height',
      type: 0,
      value: 'automated test height',
    },
  ],
  [CLAIM_OFFER_ADDRESS]: [
    {
      name: 'street',
      type: 0,
      value: 'automated test street',
    },
    {
      name: 'city',
      type: 0,
      value: 'automated test city',
    },
    {
      name: 'state',
      type: 0,
      value: 'automated test state',
    },
    {
      name: 'country',
      type: 0,
      value: 'automated test country',
    },
  ],
  [CLAIM_OFFER_CONTACT]: [
    {
      name: 'physical address',
      type: 0,
      value: 'automated test address',
    },
    {
      name: 'mailing address',
      type: 0,
      value: 'automated test mailing',
    },
    {
      name: 'email',
      type: 0,
      value: 'automated test email',
    },
    {
      name: 'phone',
      type: 0,
      value: 'automated test phone',
    },
  ],
  [CLAIM_OFFER_MIXED]: [
    {
      name: 'mailing address',
      type: 0,
      value: 'mixed claim mailing',
    },
    {
      name: 'email',
      type: 0,
      value: 'mixed claim email',
    },
    {
      name: 'phone',
      type: 0,
      value: 'mixed claim phone',
    },
    {
      name: 'city',
      type: 0,
      value: 'mixed claim city',
    },
    {
      name: 'state',
      type: 0,
      value: 'mixed claim state',
    },
    {
      name: 'name',
      type: 0,
      value: 'mixed claim name',
    },
    {
      name: 'gender',
      type: 0,
      value: 'mixed claim gender',
    },
    {
      name: 'height',
      type: 0,
      value: 'mixed claim height',
    },
  ],
}

let claimDefs = []

export const createSchema = ({ fields }: any) =>
  post(`${verityBaseUrl}/schemas`, { fields }, httpsConfig)

export const createClaimDef = (
  name: string,
  { data: schema }: any,
  price: string = '0'
) =>
  post(
    `${verityBaseUrl}/credential-defs`,
    {
      name: name,
      schema: `${schema.schemaId}`,
      price: price,
    },
    httpsConfig
  )

function addCreatedClaimDef({ data: claimDef }) {
  claimDefs.push(claimDef)

  return claimDefs
}
// put await on claimDefsWaiting and get the array of claim defs
const claimDefsWaiting = rawSchemas.map((rawSchema) =>
  createSchema(rawSchema)
    .then(R.partial(createClaimDef, [rawSchema.name]))
    .then(addCreatedClaimDef)
    .catch(console.error)
)

const getClaimDefs = async () => {
  if (claimDefs.length < rawSchemas.length) {
    await Promise.all(claimDefsWaiting)
  }

  return claimDefs
}

type ClaimOfferName =
  | typeof CLAIM_OFFER_PROFILE_INFO
  | typeof CLAIM_OFFER_ADDRESS
  | typeof CLAIM_OFFER_CONTACT
  | typeof CLAIM_OFFER_MIXED

export async function sendClaimOffer(
  claimOfferName: ClaimOfferName,
  connection: number,
  price: string = '0'
) {
  const storedClaimDefs = await getClaimDefs()
  const credentialDef = R.compose(
    R.prop('id'),
    R.head,
    R.filter(R.propEq('name', claimOfferName))
  )(storedClaimDefs)
  const data = claimDefData[claimOfferName]

  return post(
    `${verityBaseUrl}/credentials`,
    {
      credentialDef,
      connection,
      data,
      price,
    },
    httpsConfig
  )
}

export const PROOF_TEMPLATE_SINGLE_CLAIM_FULFILLED =
  'Automated Single claim fulfilled'
export const PROOF_TEMPLATE_TWO_CLAIM_FULFILLED =
  'Automated Two claim fulfilled'
export const PROOF_TEMPLATE_MISSING_ATTRIBUTES =
  'Automated Missing attributes multiple claims'
const proofTemplateData = [
  {
    name: PROOF_TEMPLATE_SINGLE_CLAIM_FULFILLED,
    fields: [
      {
        name: 'name',
      },
      {
        name: 'gender',
      },
    ],
  },
  {
    name: PROOF_TEMPLATE_TWO_CLAIM_FULFILLED,
    fields: [
      {
        name: 'name',
      },
      {
        name: 'gender',
      },
      {
        name: 'street',
      },
      {
        name: 'city',
      },
      {
        name: 'state',
      },
    ],
  },
  {
    name: PROOF_TEMPLATE_MISSING_ATTRIBUTES,
    fields: [
      {
        name: 'name',
      },
      {
        name: 'gender',
      },
      {
        name: 'street',
      },
      {
        name: 'city',
      },
      {
        name: 'missing attribute 1',
      },
      {
        name: 'missing attribute 2',
      },
      {
        name: 'missing attribute 3',
      },
    ],
  },
]

let proofTemplates = []
const proofTemplateCreated = ({ data }) => {
  proofTemplates.push(data)

  return proofTemplates
}

let proofTemplatesWaiting = proofTemplateData.map((proofTemplate) =>
  post(`${verityBaseUrl}/proof-defs`, proofTemplate, httpsConfig)
    .then(proofTemplateCreated)
    .catch(console.error)
)

const getProofTemplates = async () => {
  if (proofTemplates.length < proofTemplateData.length) {
    await Promise.all(proofTemplatesWaiting)
  }

  return proofTemplates
}

type ProofRequestName =
  | typeof PROOF_TEMPLATE_SINGLE_CLAIM_FULFILLED
  | typeof PROOF_TEMPLATE_TWO_CLAIM_FULFILLED
  | typeof PROOF_TEMPLATE_MISSING_ATTRIBUTES

export async function sendProofRequest(
  proofRequestName: ProofRequestName,
  connection: number
) {
  const proofTemplates = await getProofTemplates()
  const proofTemplateId = R.compose(
    R.prop('id'),
    R.head,
    R.filter(R.propEq('name', proofRequestName))
  )(proofTemplates)

  return post(
    `${verityBaseUrl}/proofs`,
    {
      proofDef: proofTemplateId,
      connection,
    },
    httpsConfig
  )
}
