/**
 * @jest-environment node
 */

// @flow

import http from 'http'
import chalk from 'chalk'
import { element, by, waitFor, expect } from 'detox'
import { waitForElementAndTap } from '../utils/detox-selectors'
import { matchScreenshot } from '../utils/screenshot'
import {
  BURGER_MENU,
  SCAN_BUTTON,
  MENU_MY_CONNECTIONS,
  MENU_MY_CREDENTIALS,
  MY_CONNECTIONS_CONTAINER,
  MY_CONNECTIONS_HEADER,
  SCREENSHOT_TEST_CONNECTION,
  MY_CONNECTIONS_CONNECTION,
  GENERAL_SCROLL_VIEW,
  CONNECTION_ENTRY_HEADER,
  VIEW_CREDENTIAL,
  CREDENTIAL_HEADER,
  CLOSE_BUTTON,
  VIEW_PROOF,
  PROOF_HEADER,
  BACK_ARROW,
  ALLOW_BUTTON,
  CONNECT_BUTTON,
  HOME_NEW_MESSAGE,
  CLAIM_OFFER_ACCEPT,
  CLAIM_OFFER_REJECT,
  PROOF_REQUEST_SEND,
  PROOF_REQUEST_REJECT,
  CONNECTION_SUBMENU_BUTTON,
  CONNECTION_DELETE_BUTTON,
  MY_CREDENTIALS_DELETE,
  SCREENSHOT_CLAIM_OFFER_PROFILE_INFO,
  SCREENSHOT_PROOF_TEMPLATE_SINGLE_CLAIM_FULFILLED,
  SCREENSHOT_ALLOW_NOTIFICATIONS,
} from '../utils/test-constants'
import {
  VAS,
  VASconfig,
  getDeferred,
  CLAIM_OFFER_PROFILE_INFO,
  PROOF_TEMPLATE_SINGLE_CLAIM_FULFILLED,
} from '../utils/api_new'

//$FlowFixMe
require('tls').DEFAULT_ECDH_CURVE = 'auto'

const TIMEOUT = 30000

// // Start server to listen VAS responses
const instance = new VAS(VASconfig)

describe('My connections screen', () => {
  it('Case 1: create new connection, schemas and cred defs', async () => {
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

    // await matchScreenshot(SCREENSHOT_ALLOW_NOTIFICATIONS) // screenshot

    try {
      await waitForElementAndTap('text', ALLOW_BUTTON, TIMEOUT)
    } catch (e) {
      console.log('Permissions have already been granted!')
    }

    await waitForElementAndTap('text', CONNECT_BUTTON, TIMEOUT)

    await new Promise((r) => setTimeout(r, TIMEOUT)) // sync

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
  })

  it('Case 2.1: create and reject profile credential', async () => {
    await instance.sendCredentialOffer(
      global.DID,
      global.CLAIM_OFFER_PROFILE_INFO_CRED_DEF_ID,
      {
        name: 'Bob',
        gender: 'male',
        height: '190',
      },
      CLAIM_OFFER_PROFILE_INFO
    )

    try {
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    } catch (e) {
      console.warn(e)
      await element(by.text('No new notifications.')).swipe('down')
      try {
        await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
      } catch (e) {
        await instance.sendCredentialOffer(
          global.DID,
          global.CLAIM_OFFER_PROFILE_INFO_CRED_DEF_ID,
          {
            name: 'Bob',
            gender: 'male',
            height: '190',
          },
          CLAIM_OFFER_PROFILE_INFO
        )
        try {
          await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
        } catch (e) {
          console.warn(e)
          await element(by.text('No new notifications.')).swipe('down')
          await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
        }
      }
    }

    await waitForElementAndTap('text', CLAIM_OFFER_REJECT, TIMEOUT)
  })

  it('Case 2.2: create and accept profile credential', async () => {
    await instance.sendCredentialOffer(
      global.DID,
      global.CLAIM_OFFER_PROFILE_INFO_CRED_DEF_ID,
      {
        name: 'Bob',
        gender: 'male',
        height: '170',
      },
      CLAIM_OFFER_PROFILE_INFO
    )

    try {
      await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
    } catch (e) {
      console.warn(e)
      await element(by.text('No new notifications.')).swipe('down')
      try {
        await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
      } catch (e) {
        await instance.sendCredentialOffer(
          global.DID,
          global.CLAIM_OFFER_PROFILE_INFO_CRED_DEF_ID,
          {
            name: 'Bob',
            gender: 'male',
            height: '170',
          },
          CLAIM_OFFER_PROFILE_INFO
        )
        try {
          await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
        } catch (e) {
          console.warn(e)
          await element(by.text('No new notifications.')).swipe('down')
          await waitForElementAndTap('text', HOME_NEW_MESSAGE, TIMEOUT)
        }
      }
    }

    await matchScreenshot(SCREENSHOT_CLAIM_OFFER_PROFILE_INFO) // screenshot profile info

    await waitForElementAndTap('text', CLAIM_OFFER_ACCEPT, TIMEOUT)

    await instance.issueCredential(global.DID)
  })

  it('Case 3.1: create and reject proof request', async () => {
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

  it('Case 3.2: create and send proof request', async () => {
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

  it('Case 4: go to my connections, find all necessary elements', async () => {
    await waitForElementAndTap('id', BURGER_MENU, TIMEOUT)

    await waitForElementAndTap('text', MENU_MY_CONNECTIONS, TIMEOUT)

    // check connections view
    await waitFor(element(by.id(MY_CONNECTIONS_CONTAINER)))
      .toBeVisible()
      .withTimeout(TIMEOUT)

    // check connections header
    await waitFor(element(by.text(MY_CONNECTIONS_HEADER)))
      .toBeVisible()
      .withTimeout(TIMEOUT)

    // check menu button
    await waitFor(element(by.id(BURGER_MENU)))
      .toBeVisible()
      .withTimeout(TIMEOUT)

    // check camera button
    await waitFor(element(by.text(SCAN_BUTTON)))
      .toBeVisible()
      .withTimeout(TIMEOUT)

    await matchScreenshot(SCREENSHOT_TEST_CONNECTION) // screenshot
  })

  it('Case 5: drill down to connection and check its elements', async () => {
    await waitForElementAndTap('text', MY_CONNECTIONS_CONNECTION, TIMEOUT)

    await element(by.type(GENERAL_SCROLL_VIEW))
      .atIndex(0)
      .swipe('down', 'fast', 0.5)

    await expect(element(by.text(CONNECTION_ENTRY_HEADER))).toBeVisible()

    await element(by.text(VIEW_CREDENTIAL)).tap()

    await expect(element(by.text(CREDENTIAL_HEADER))).toBeVisible()

    await expect(
      element(by.text(CLAIM_OFFER_PROFILE_INFO)).atIndex(0)
    ).toBeVisible()

    await waitForElementAndTap('text', CLOSE_BUTTON, TIMEOUT)

    await element(by.type(GENERAL_SCROLL_VIEW))
      .atIndex(0)
      .swipe('up', 'fast', 0.5)

    await element(by.text(VIEW_PROOF)).tap()

    await expect(element(by.text(PROOF_HEADER))).toBeVisible()

    await expect(
      element(by.text(PROOF_TEMPLATE_SINGLE_CLAIM_FULFILLED)).atIndex(0)
    ).toBeVisible()

    await waitForElementAndTap('text', CLOSE_BUTTON, TIMEOUT)

    await element(by.id(BACK_ARROW)).tap()
  })

  it('Case 6: delete connection and credential', async () => {
    await waitForElementAndTap('id', BURGER_MENU, TIMEOUT)

    await waitForElementAndTap('text', MENU_MY_CREDENTIALS, TIMEOUT)

    await element(by.text(CLAIM_OFFER_PROFILE_INFO)).swipe('left') // open credential menu

    await waitForElementAndTap('text', MY_CREDENTIALS_DELETE, TIMEOUT)

    await element(by.text(MY_CREDENTIALS_DELETE)).atIndex(0).tap()

    await new Promise((r) => setTimeout(r, 1000)) // sync

    await expect(element(by.text(CLAIM_OFFER_PROFILE_INFO))).toNotExist()

    await waitForElementAndTap('id', BURGER_MENU, TIMEOUT)

    await waitForElementAndTap('text', MENU_MY_CONNECTIONS, TIMEOUT)

    await waitForElementAndTap('text', MY_CONNECTIONS_CONNECTION, TIMEOUT)

    await waitForElementAndTap('id', CONNECTION_SUBMENU_BUTTON, TIMEOUT) // open connection menu

    await waitForElementAndTap('id', CONNECTION_DELETE_BUTTON, TIMEOUT) // delete connection

    await expect(element(by.text(MY_CONNECTIONS_CONNECTION))).toNotExist()

    await instance.endpointServer.close()
    console.log(chalk.redBright('VAS server has been stopped.'))
    await instance.shutdownNgrok()
  })
})
