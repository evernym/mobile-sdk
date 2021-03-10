/**
 * @jest-environment node
 */

// @flow

import { element, by, waitFor, expect } from 'detox'
import {
  BURGER_MENU,
  MENU_SETTINGS,
  SETTINGS_PASSCODE,
  PIN_CODE_INPUT_BOX,
  TEST_PASS_CODE,
  TEST_PASS_CODE_CHANGED,
  SETTINGS_CHAT,
  CHAT_CANCEL,
  CHAT_CLOSE,
  SETTINGS_ABOUT,
  ABOUT_BACK_ARROW,
  SETTINGS_ONFIDO,
  ONFIDO_BACK_ARROW,
} from '../utils/test-constants'
import {
  getInvitation,
  createSchema,
  createClaimDef,
  sendClaimOffer,
} from '../utils/api'
import { intersection } from 'ramda'
import chalk from 'chalk'

let connectionId
let schema
let credDef
let credential

describe('Test suite title', () => {
  it('Create schema', async () => {
    let rawSchema = {
      name: 'Profile Info',
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
    }
    // data:
    // { id: 231,
    //   name: 'Evernym QA-RC1591890098494',
    //   schemaId: 'VLWUDvadHTZ2LsiXmhhvsR:2:Evernym QA-RC1591890098494:1.0',
    //   fields: [ [Object], [Object], [Object] ] } }
    schema = await createSchema(rawSchema).catch(console.error)
  })
  it('Create cred def', async () => {
    // data:
    // { id: 199,
    //   name: 'Test Cred Def',
    //   price: 0,
    //   schema:
    //    { id: 238,
    //      name: 'Evernym QA-RC1591892599221',
    //      schemaId: 'VLWUDvadHTZ2LsiXmhhvsR:2:Evernym QA-RC1591892599221:1.0' } } }
    credDef = await createClaimDef('Test Cred Def', schema, '0').catch(
      console.error
    )
  })
  it('Create credential', async () => {
    // credential = await sendClaimOffer('Profile Info', )
  })
  it('Create proof request', async () => {})
})
