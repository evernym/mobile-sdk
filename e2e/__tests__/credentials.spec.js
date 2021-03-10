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
  MY_CREDENTIALS_HEADER,
  MY_CREDENTIALS_DETAILS_HEADER,
  MY_CREDENTIALS_DETAILS_ISSUED_BY,
  MY_CREDENTIALS_BACK_ARROW,
  MY_CREDENTIALS_DELETE,
  SCREENSHOT_MY_CREDENTIALS_LIST,
  SCREENSHOT_MY_CREDENTIALS_ENTRY,
  SCREENSHOT_MY_CREDENTIALS_LIST_ONE_DELETED,
  ALLOW_BUTTON,
  CONNECT_BUTTON,
  HOME_NEW_MESSAGE,
  CLAIM_OFFER_ACCEPT,
  CLAIM_OFFER_REJECT,
  PROOF_REQUEST_SEND,
  PROOF_REQUEST_REJECT,
  CONNECTION_SUBMENU_BUTTON,
  CONNECTION_DELETE_BUTTON,
  MY_CONNECTIONS_CONNECTION,
} from '../utils/test-constants'
import {
  VAS,
  VASconfig,
  getDeferred,
  CLAIM_OFFER_ADDRESS,
  CLAIM_OFFER_CONTACT,
} from '../utils/api_new'

//$FlowFixMe
require('tls').DEFAULT_ECDH_CURVE = 'auto'

const TIMEOUT = 30000

// // Start server to listen VAS responses
const instance = new VAS(VASconfig)

describe('My credentials screen', () => {
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

    try {
      await waitForElementAndTap('text', ALLOW_BUTTON, TIMEOUT)
    } catch (e) {
      console.log('Permissions have already been granted!')
    }

    await waitForElementAndTap('text', CONNECT_BUTTON, TIMEOUT)

    await new Promise((r) => setTimeout(r, TIMEOUT)) // sync

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
  })

  it('Case 2.1: create and accept address credential', async () => {
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
          global.CLAIM_OFFER_ADDRESS_CRED_DEF_ID,
          {
            street: 'Grove',
            city: 'Phoenix',
            state: 'Arizona',
            country: 'USA',
          },
          CLAIM_OFFER_ADDRESS
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

    await waitForElementAndTap('text', CLAIM_OFFER_ACCEPT, TIMEOUT)

    await instance.issueCredential(global.DID)
  })

  it('Case 2.2: create and accept contact credential', async () => {
    await instance.sendCredentialOffer(
      global.DID,
      global.CLAIM_OFFER_CONTACT_CRED_DEF_ID,
      {
        'physical address': 'test address',
        'mailing address': 'another test address',
        email: 'test@gmail.com',
        phone: '5554433',
      },
      CLAIM_OFFER_CONTACT
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
          global.CLAIM_OFFER_CONTACT_CRED_DEF_ID,
          {
            'physical address': 'test address',
            'mailing address': 'another test address',
            email: 'test@gmail.com',
            phone: '5554433',
          },
          CLAIM_OFFER_CONTACT
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

    await waitForElementAndTap('text', CLAIM_OFFER_ACCEPT, TIMEOUT)

    await instance.issueCredential(global.DID)
  })

  it('Case 3: go to my credentials, find all necessary elements', async () => {
    await element(by.id(BURGER_MENU)).tap()
    await element(by.text(MENU_MY_CREDENTIALS)).tap()

    // check credentials header
    await waitFor(element(by.text(MY_CREDENTIALS_HEADER)))
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

    await matchScreenshot(SCREENSHOT_MY_CREDENTIALS_LIST) // screenshot
  })

  it('Case 4: check credential details', async () => {
    await element(by.text(CLAIM_OFFER_ADDRESS)).tap()

    await expect(element(by.text(MY_CREDENTIALS_DETAILS_HEADER))).toBeVisible()

    await expect(
      element(by.text(MY_CREDENTIALS_DETAILS_ISSUED_BY))
    ).toBeVisible()

    await expect(element(by.text(CLAIM_OFFER_ADDRESS))).toBeVisible()

    await matchScreenshot(SCREENSHOT_MY_CREDENTIALS_ENTRY) // screenshot

    await waitForElementAndTap('id', MY_CREDENTIALS_BACK_ARROW, TIMEOUT)
  })

  it('Case 5: delete all credentials', async () => {
    for (const i of [
      CLAIM_OFFER_ADDRESS, // 2.1
      CLAIM_OFFER_CONTACT, // 2.2
      // CLAIM_OFFER_MIXED, // 2.3
    ]) {
      await element(by.text(i)).swipe('left')

      await waitForElementAndTap('text', MY_CREDENTIALS_DELETE, TIMEOUT)

      await element(by.text(MY_CREDENTIALS_DELETE)).atIndex(0).tap()

      await new Promise((r) => setTimeout(r, 1000)) // sync

      await expect(element(by.text(i))).toNotExist()
    }
  })

  it('Case 5: delete connection', async () => {
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
