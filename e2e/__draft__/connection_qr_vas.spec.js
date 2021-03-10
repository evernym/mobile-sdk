/**
 * @jest-environment node
 */

// @flow

import https from 'https'
import http from 'http'
import fs from 'fs'
import { post, get } from 'axios'
import R from 'ramda'
import { v4 as uuidv4 } from 'uuid'
import { exec } from 'child-process-async'
import chalk from 'chalk'

import { element, by, waitFor, expect, device } from 'detox'
import { waitForElementAndTap } from '../utils/detox-selectors'
import {
  HOME_CONTAINER,
  QR_CODE_INPUT_ENV_SWITCH,
  QR_CODE_ENV_SWITCH_URL,
  QR_CODE_NATIVE_ALERT_SWITCH_TEXT,
  OK_TEXT_ALERT,
  NATIVE_ALERT_OK_MATCHER,
  TEST_PASS_CODE,
  INVITATION_SUCCESS_MODAL_CONTINUE,
  INVITATION_ACCEPT,
  PIN_CODE_INPUT_BOX,
  HOME_HEADER,
  HOME_NEW_MESSAGE,
  CLAIM_OFFER_ACCEPT,
  CLAIM_OFFER_REJECT,
  PROOF_REQUEST_SEND,
  PROOF_REQUEST_REJECT,
  PROOF_REQUEST_GENERATE,
  ALLOW_BUTTON,
  CONNECT_BUTTON,
  SCAN_BUTTON,
  GENERAL_SCROLL_VIEW,
  PROOF_REQUEST_MISSING_ATTRIBUTE_BASE,
  BURGER_MENU,
  MENU_MY_CONNECTIONS,
  MY_CONNECTIONS_CONTAINER,
  MY_CONNECTIONS_HEADER,
  MY_CONNECTIONS_CONNECTION,
  CONNECTION_ENTRY_HEADER,
  VIEW_CREDENTIAL,
  CREDENTIAL_HEADER,
  VIEW_PROOF,
  PROOF_HEADER,
  CLOSE_BUTTON,
  SCREENSHOT_INVITATION,
  SCREENSHOT_CLAIM_OFFER_PROFILE_INFO,
  SCREENSHOT_CLAIM_OFFER_ADDRESS,
  SCREENSHOT_CLAIM_OFFER_CONTACT,
  SCREENSHOT_CLAIM_OFFER_MIXED,
  SCREENSHOT_PROOF_TEMPLATE_SINGLE_CLAIM_FULFILLED,
  SCREENSHOT_PROOF_TEMPLATE_TWO_CLAIM_FULFILLED,
  SCREENSHOT_TEST_CONNECTION,
  SCREENSHOT_HOME_SMALL_HISTORY,
  SCREENSHOT_HOME_BIG_HISTORY,
  BACK_ARROW,
  MENU_HOME,
  CONNECTION_SUBMENU_BUTTON,
  CONNECTION_SUBMENU_CLOSE_BUTTON,
  CONNECTION_DELETE_BUTTON,
  PROOF_REQUEST_SCROLL_VIEW,
  PROOF_REQUEST_ATTRIBUTE_INPUT,
  MENU_MY_CREDENTIALS,
  MY_CREDENTIALS_DELETE,
} from '../utils/test-constants'
import { matchScreenshot } from '../utils/screenshot'
import {
  VAS,
  VASconfig,
  getDeferred,
  CLAIM_OFFER_PROFILE_INFO,
  CLAIM_OFFER_ADDRESS,
  CLAIM_OFFER_CONTACT,
  CLAIM_OFFER_MIXED,
  PROOF_TEMPLATE_SINGLE_CLAIM_FULFILLED,
  PROOF_TEMPLATE_TWO_CLAIM_FULFILLED,
  PROOF_TEMPLATE_MISSING_ATTRIBUTES,
} from '../utils/api_new'
import type { InvitationType, QRType } from '../utils/api_new'

//$FlowFixMe
require('tls').DEFAULT_ECDH_CURVE = 'auto'

const TIMEOUT = 45000

// // Start server to listen VAS responses
const instance = new VAS(VASconfig)

describe('Test connections, credentials and proofs', () => {
  it('Case 1.1: set up environment and establish connection by scanning QR code', async () => {
    // Configure ngrok
    let url = await instance.setupNgrok()

    // // Register server endpoint in VAS - run it once
    let result1 = await instance.registerEndpoint(
      url // it changes
    )
    console.warn(result1)

    // // CreateRelationship request :: VAS returns RelationshipCreated
    let [
      relationshipThreadID,
      DID,
      result2,
    ] = await instance.createRelationship('Evernym QA-RC')
    console.warn([relationshipThreadID, DID, result2])

    global.DID = DID // put this into global variable to use in next cases

    let jsonData = await instance.relationshipInvitation(
      relationshipThreadID,
      DID,
      'connection-invitation',
      'ARIES_V1_QR'
    )
    console.warn(jsonData)

    const { resolve, promise: invitationPushed } = getDeferred()
    const server = http
      .createServer(function (request, response) {
        response.writeHead(200, { 'Content-Type': 'application/json' })
        response.write(jsonData.trim())
        response.end()
        resolve && resolve()
      })
      .listen(1337)
    console.log(
      chalk.greenBright('Invitation server is listening on port 1337...')
    )

    await waitForElementAndTap('text', SCAN_BUTTON, TIMEOUT)

    await invitationPushed

    server.close()
    console.log(chalk.redBright('Invitation server has been stopped.'))

    try {
      await waitForElementAndTap('text', ALLOW_BUTTON, TIMEOUT)
    } catch (e) {
      console.log('Permissions have already been granted!')
    }

    await waitForElementAndTap('text', CONNECT_BUTTON, TIMEOUT)

    await new Promise((r) => setTimeout(r, TIMEOUT)) // sync
  })

  xit('Case 1.2: create schemas and credential definitions', async () => {
    // // CLAIM_OFFER_PROFILE_INFO

    //$FlowFixMe
    global.CLAIM_OFFER_PROFILE_INFO_SCHEMA_ID = await instance
      .createSchema(CLAIM_OFFER_PROFILE_INFO, ['name', 'gender', 'height'])
      .then((res) => res[0])

    //$FlowFixMe
    global.CLAIM_OFFER_PROFILE_INFO_CRED_DEF_ID = await instance
      .createCredentialDef(
        CLAIM_OFFER_PROFILE_INFO,
        global.CLAIM_OFFER_PROFILE_INFO_SCHEMA_ID
      )
      .then((res) => res[0])

    // // CLAIM_OFFER_ADDRESS

    //$FlowFixMe
    global.CLAIM_OFFER_ADDRESS_SCHEMA_ID = await instance
      .createSchema(CLAIM_OFFER_ADDRESS, ['street', 'city', 'state', 'country'])
      .then((res) => res[0])

    //$FlowFixMe
    global.CLAIM_OFFER_ADDRESS_CRED_DEF_ID = await instance
      .createCredentialDef(
        CLAIM_OFFER_ADDRESS,
        global.CLAIM_OFFER_ADDRESS_SCHEMA_ID
      )
      .then((res) => res[0])

    // // CLAIM_OFFER_CONTACT

    //$FlowFixMe
    global.CLAIM_OFFER_CONTACT_SCHEMA_ID = await instance
      .createSchema(CLAIM_OFFER_CONTACT, [
        'physical address',
        'mailing address',
        'email',
        'phone',
      ])
      .then((res) => res[0])

    //$FlowFixMe
    global.CLAIM_OFFER_CONTACT_CRED_DEF_ID = await instance
      .createCredentialDef(
        CLAIM_OFFER_CONTACT,
        global.CLAIM_OFFER_CONTACT_SCHEMA_ID
      )
      .then((res) => res[0])

    // // CLAIM_OFFER_MIXED

    //$FlowFixMe
    global.CLAIM_OFFER_MIXED_SCHEMA_ID = await instance
      .createSchema(CLAIM_OFFER_MIXED, [
        'mailing address',
        'email',
        'phone',
        'city',
        'state',
        'name',
        'gender',
        'height',
      ])
      .then((res) => res[0])

    //$FlowFixMe
    global.CLAIM_OFFER_MIXED_CRED_DEF_ID = await instance
      .createCredentialDef(
        CLAIM_OFFER_MIXED,
        global.CLAIM_OFFER_MIXED_SCHEMA_ID
      )
      .then((res) => res[0])
  })

  xit('Case 2.1: create and reject profile credential', async () => {
    await instance.sendCredentialOffer(
      global.DID,
      global.CLAIM_OFFER_PROFILE_INFO_CRED_DEF_ID,
      {
        name: 'Alex',
        gender: 'male',
        height: '180',
      },
      CLAIM_OFFER_PROFILE_INFO
    )

    // catch intermittent failure with new message absence
    try {
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    } catch (e) {
      console.warn(e)
      await element(by.text('No new notifications.')).swipe('down')
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    }

    await waitForElementAndTap('text', CLAIM_OFFER_REJECT, TIMEOUT)
  })

  xit('Case 2.2: create and accept profile credential', async () => {
    await instance.sendCredentialOffer(
      global.DID,
      global.CLAIM_OFFER_PROFILE_INFO_CRED_DEF_ID,
      {
        name: 'Alex',
        gender: 'male',
        height: '180',
      },
      CLAIM_OFFER_PROFILE_INFO
    )

    // catch intermittent failure with new message absence
    try {
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    } catch (e) {
      console.warn(e)
      await element(by.text('No new notifications.')).swipe('down')
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    }

    await matchScreenshot(SCREENSHOT_CLAIM_OFFER_PROFILE_INFO) // screenshot profile info

    await waitForElementAndTap('text', CLAIM_OFFER_ACCEPT, TIMEOUT)

    await instance.issueCredential(global.DID)
  })

  xit('Case 2.3: create and accept address credential', async () => {
    await instance.sendCredentialOffer(
      global.DID,
      global.CLAIM_OFFER_ADDRESS_CRED_DEF_ID,
      {
        street: 'Grove',
        city: 'Phoenix',
        state: 'Arizona',
        country: 'USA',
      },
      CLAIM_OFFER_ADDRESS
    )

    // catch intermittent failure with new message absence
    try {
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    } catch (e) {
      console.warn(e)
      await element(by.text('No new notifications.')).swipe('down')
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    }

    await matchScreenshot(SCREENSHOT_CLAIM_OFFER_ADDRESS) // screenshot address

    await waitForElementAndTap('text', CLAIM_OFFER_ACCEPT, TIMEOUT)

    await instance.issueCredential(global.DID)
  })

  xit('Case 2.4: create and accept contact credential', async () => {
    await instance.sendCredentialOffer(
      global.DID,
      global.CLAIM_OFFER_CONTACT_CRED_DEF_ID,
      {
        'physical address': 'test address',
        'mailing address': 'another test address',
        email: 'test@gmail.com',
        phone: '555-44-33',
      },
      CLAIM_OFFER_CONTACT
    )

    // catch intermittent failure with new message absence
    try {
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    } catch (e) {
      console.warn(e)
      await element(by.text('No new notifications.')).swipe('down')
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    }

    await matchScreenshot(SCREENSHOT_CLAIM_OFFER_CONTACT) // screenshot contact

    await waitForElementAndTap('text', CLAIM_OFFER_ACCEPT, TIMEOUT)

    await instance.issueCredential(global.DID)
  })

  xit('Case 2.5: create and accept mixed credential', async () => {
    await instance.sendCredentialOffer(
      global.DID,
      global.CLAIM_OFFER_MIXED_CRED_DEF_ID,
      {
        'mailing address': 'another test address',
        email: 'test@gmail.com',
        phone: '555-44-33',
        city: 'Phoenix',
        state: 'Arizona',
        name: 'Alex',
        gender: 'male',
        height: '170',
      },
      CLAIM_OFFER_MIXED
    )

    // catch intermittent failure with new message absence
    try {
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    } catch (e) {
      console.warn(e)
      await element(by.text('No new notifications.')).swipe('down')
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    }

    await matchScreenshot(SCREENSHOT_CLAIM_OFFER_MIXED) // screenshot mixed

    await waitForElementAndTap('text', CLAIM_OFFER_ACCEPT, TIMEOUT)

    await instance.issueCredential(global.DID)
  })

  xit('Case 3.1: create and reject proof request', async () => {
    await instance.presentProof(
      global.DID,
      PROOF_TEMPLATE_SINGLE_CLAIM_FULFILLED,
      ['name', 'gender'],
      true
    )

    // catch intermittent failure with new message absence
    try {
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    } catch (e) {
      console.warn(e)
      await element(by.text('No new notifications.')).swipe('down')
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    }

    await waitForElementAndTap('text', PROOF_REQUEST_REJECT, TIMEOUT)
  })

  xit('Case 3.2: create and send proof request', async () => {
    await instance.presentProof(
      global.DID,
      PROOF_TEMPLATE_SINGLE_CLAIM_FULFILLED,
      ['name', 'gender'],
      true
    )

    // catch intermittent failure with new message absence
    try {
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    } catch (e) {
      console.warn(e)
      await element(by.text('No new notifications.')).swipe('down')
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    }

    await matchScreenshot(SCREENSHOT_PROOF_TEMPLATE_SINGLE_CLAIM_FULFILLED) // screenshot first proof

    await waitForElementAndTap('text', PROOF_REQUEST_SEND, TIMEOUT)
  })

  xit('Case 3.3: create and send another proof request', async () => {
    await instance.presentProof(
      global.DID,
      PROOF_TEMPLATE_TWO_CLAIM_FULFILLED,
      ['city', 'state'],
      true
    )

    // catch intermittent failure with new message absence
    try {
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    } catch (e) {
      console.warn(e)
      await element(by.text('No new notifications.')).swipe('down')
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    }

    await matchScreenshot(SCREENSHOT_PROOF_TEMPLATE_TWO_CLAIM_FULFILLED) // screenshot second proof

    await waitForElementAndTap('text', PROOF_REQUEST_SEND, TIMEOUT)
  })

  it('Case 3.4: create and send self-attested proof request', async () => {
    // we have a single input field for all missed attributes - probalby it's a bug
    await instance.presentProof(
      global.DID,
      PROOF_TEMPLATE_MISSING_ATTRIBUTES,
      ['missing attribute 1', 'missing attribute 2', 'missing attribute 3'],
      true
    )

    // catch intermittent failure with new message absence
    try {
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    } catch (e) {
      console.warn(e)
      await element(by.text('No new notifications.')).swipe('down')
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    }

    await waitForElementAndTap('text', OK_TEXT_ALERT, TIMEOUT)

    await element(by.type(PROOF_REQUEST_SCROLL_VIEW)).atIndex(0).swipe('down')

    try {
      await element(
        by.id(PROOF_REQUEST_MISSING_ATTRIBUTE_BASE.concat('1'))
      ).tap()
      await element(by.type(PROOF_REQUEST_ATTRIBUTE_INPUT))
        .atIndex(0)
        .typeText('test attribute 1')
    } catch (e) {
      try {
        await element(by.type(PROOF_REQUEST_SCROLL_VIEW))
          .atIndex(0)
          .swipe('down')
        await element(
          by.id(PROOF_REQUEST_MISSING_ATTRIBUTE_BASE.concat('1'))
        ).tap()
        await element(by.type(PROOF_REQUEST_ATTRIBUTE_INPUT))
          .atIndex(0)
          .typeText('test attribute 1')
      } catch (e) {
        await element(by.type(PROOF_REQUEST_SCROLL_VIEW)).atIndex(0).swipe('up')
        await element(
          by.id(PROOF_REQUEST_MISSING_ATTRIBUTE_BASE.concat('1'))
        ).tap()
        await element(by.type(PROOF_REQUEST_ATTRIBUTE_INPUT))
          .atIndex(0)
          .typeText('test attribute 1')
      }
    }
    await element(by.type(PROOF_REQUEST_ATTRIBUTE_INPUT))
      .atIndex(0)
      .tapReturnKey()
    await element(by.text('Done')).tap()

    try {
      await element(
        by.id(PROOF_REQUEST_MISSING_ATTRIBUTE_BASE.concat('2'))
      ).tap()
      await element(by.type(PROOF_REQUEST_ATTRIBUTE_INPUT))
        .atIndex(0)
        .typeText('test attribute 2')
    } catch (e) {
      try {
        await element(by.type(PROOF_REQUEST_SCROLL_VIEW))
          .atIndex(0)
          .swipe('down')
        await element(
          by.id(PROOF_REQUEST_MISSING_ATTRIBUTE_BASE.concat('2'))
        ).tap()
        await element(by.type(PROOF_REQUEST_ATTRIBUTE_INPUT))
          .atIndex(0)
          .typeText('test attribute 2')
      } catch (e) {
        await element(by.type(PROOF_REQUEST_SCROLL_VIEW)).atIndex(0).swipe('up')
        await element(
          by.id(PROOF_REQUEST_MISSING_ATTRIBUTE_BASE.concat('2'))
        ).tap()
        await element(by.type(PROOF_REQUEST_ATTRIBUTE_INPUT))
          .atIndex(0)
          .typeText('test attribute 2')
      }
    }
    await element(by.type(PROOF_REQUEST_ATTRIBUTE_INPUT))
      .atIndex(0)
      .tapReturnKey()
    await element(by.text('Done')).tap()

    try {
      await element(
        by.id(PROOF_REQUEST_MISSING_ATTRIBUTE_BASE.concat('3'))
      ).tap()
      await element(by.type(PROOF_REQUEST_ATTRIBUTE_INPUT))
        .atIndex(0)
        .typeText('test attribute 3')
    } catch (e) {
      try {
        await element(by.type(PROOF_REQUEST_SCROLL_VIEW))
          .atIndex(0)
          .swipe('down')
        await element(
          by.id(PROOF_REQUEST_MISSING_ATTRIBUTE_BASE.concat('3'))
        ).tap()
        await element(by.type(PROOF_REQUEST_ATTRIBUTE_INPUT))
          .atIndex(0)
          .typeText('test attribute 3')
      } catch (e) {
        await element(by.type(PROOF_REQUEST_SCROLL_VIEW)).atIndex(0).swipe('up')
        await element(
          by.id(PROOF_REQUEST_MISSING_ATTRIBUTE_BASE.concat('3'))
        ).tap()
        await element(by.type(PROOF_REQUEST_ATTRIBUTE_INPUT))
          .atIndex(0)
          .typeText('test attribute 3')
      }
    }
    await element(by.type(PROOF_REQUEST_ATTRIBUTE_INPUT))
      .atIndex(0)
      .tapReturnKey()
    await element(by.text('Done')).tap()

    await waitForElementAndTap('text', PROOF_REQUEST_SEND, TIMEOUT)
  })

  xit('Case 4: check my connections screenshot with test connection', async () => {
    await waitForElementAndTap('id', BURGER_MENU, TIMEOUT)

    await waitForElementAndTap('text', MENU_MY_CONNECTIONS, TIMEOUT)

    await matchScreenshot(SCREENSHOT_TEST_CONNECTION) // screenshot
  })

  xit('Case 5: drill down to connection and check its elements', async () => {
    await waitForElementAndTap('text', MY_CONNECTIONS_CONNECTION, TIMEOUT)

    await element(by.type(GENERAL_SCROLL_VIEW))
      .atIndex(0)
      .swipe('down', 'fast', 0.5)

    await expect(element(by.text(CONNECTION_ENTRY_HEADER))).toBeVisible()

    await element(by.text(VIEW_CREDENTIAL)).atIndex(3).tap()

    await expect(element(by.text(CREDENTIAL_HEADER))).toBeVisible()

    await expect(
      element(by.text(CLAIM_OFFER_PROFILE_INFO)).atIndex(0)
    ).toBeVisible()

    await waitForElementAndTap('text', CLOSE_BUTTON, TIMEOUT)

    await element(by.type(GENERAL_SCROLL_VIEW))
      .atIndex(0)
      .swipe('up', 'fast', 0.5)

    await element(by.text(VIEW_PROOF)).atIndex(0).tap()

    await expect(element(by.text(PROOF_HEADER))).toBeVisible()

    await expect(
      // element(by.text(PROOF_TEMPLATE_MISSING_ATTRIBUTES)).atIndex(0)
      element(by.text(PROOF_TEMPLATE_TWO_CLAIM_FULFILLED)).atIndex(0)
    ).toBeVisible()

    await waitForElementAndTap('text', CLOSE_BUTTON, TIMEOUT)

    await element(by.id(BACK_ARROW)).tap()
  })

  xit('Case 6: delete existing connection, credentials and tear down environment', async () => {
    await waitForElementAndTap('id', BURGER_MENU, TIMEOUT)

    await waitForElementAndTap('text', MENU_MY_CREDENTIALS, TIMEOUT)

    for (const i of [
      CLAIM_OFFER_PROFILE_INFO,
      CLAIM_OFFER_ADDRESS,
      CLAIM_OFFER_CONTACT,
      CLAIM_OFFER_MIXED,
    ]) {
      await element(by.text(i)).swipe('left')

      await waitForElementAndTap('text', MY_CREDENTIALS_DELETE, TIMEOUT)

      await element(by.text(MY_CREDENTIALS_DELETE)).atIndex(0).tap()

      await new Promise((r) => setTimeout(r, 1000)) // sync

      await expect(element(by.text(i))).toNotExist()
    }

    await waitForElementAndTap('id', BURGER_MENU, TIMEOUT)

    await waitForElementAndTap('text', MENU_MY_CONNECTIONS, TIMEOUT)

    await waitForElementAndTap('text', MY_CONNECTIONS_CONNECTION, TIMEOUT)

    await waitForElementAndTap('id', CONNECTION_SUBMENU_BUTTON, TIMEOUT) // open connection menu

    await waitForElementAndTap('id', CONNECTION_SUBMENU_CLOSE_BUTTON, TIMEOUT) // close it

    await waitForElementAndTap('id', CONNECTION_SUBMENU_BUTTON, TIMEOUT) // open menu again

    await waitForElementAndTap('id', CONNECTION_DELETE_BUTTON, TIMEOUT) // delete connection

    await expect(element(by.text(MY_CONNECTIONS_CONNECTION))).toNotExist()

    await instance.endpointServer.close()
    console.log(chalk.redBright('VAS server has been stopped.'))
    await instance.shutdownNgrok()
  })
})

// // Dev Team 1
// const VASconfig = {
//   verityUrl: 'https://vas-team1.pdev.evernym.com/api/',
//   domainDID: 'XNRkA8tboikwHD3x1Yh7Uz',
//   apiKey: 'HZ3Ak6pj9ryFASKbA9fpwqjVh42F35UDiCLQ13J58Xoh:4Wf6JtGy9enwwXVKcUgADPq7Pnf9T2YZ8LupMEVxcQQf98uuRYxWGHLAwXWp8DtaEYHo4cUeExDjApMfvLJQ48Kp'
// }

// // Dev RC
// const VASconfig = {
//   verityUrl: 'https://vas.pdev.evernym.com/api/',
//   domainDID: '32djqLcu9WGsZL4MwyAjVn',
//   apiKey:
//     'C6jtgbRwzTHp1T1mQSFGDf2YTdHeN1kj2sJr7VJbvT5P:4iBetiYFD998So2APxqRRdjYFg7qhjQvLnkwpJ6vDBszoWGuRpj75YKvLJKBhsXSQtPvXTGghCyKaPMLJEVwX6v7',
// }
