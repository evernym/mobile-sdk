// @flow

import { element, by, waitFor, expect, device } from 'detox'
import { matchScreenshot } from '../utils/screenshot'
import { unlock } from '../utils/lock-unlock'
import {
  BURGER_MENU,
  MENU_SETTINGS,
  SETTINGS_ONFIDO,
  ONFIDO_BACK_ARROW,
  ONFIDO_CUSTOM_BACK_ARROW,
  ONFIDO_ACCEPT_BUTTON,
  ALLOW_BUTTON,
  SCREENSHOT_ONFIDO_DOC_SELECTION,
  SCREENSHOT_ONFIDO_PASSPORT,
  SCREENSHOT_ONFIDO_LICENSE,
  SCREENSHOT_ONFIDO_NIC,
  SCREENSHOT_ONFIDO_RPC,
} from '../utils/test-constants'

describe('Onfido', () => {
  it('Check onfido general screen', async () => {
    await element(by.id(BURGER_MENU)).tap()
    await element(by.text(MENU_SETTINGS)).tap()
    await element(by.text(SETTINGS_ONFIDO)).tap()
    try {
      await element(by.text(ALLOW_BUTTON)).tap()
    } catch (e) {
      console.log('Push notifications are already allowed!')
    }
    await element(by.text(ONFIDO_ACCEPT_BUTTON)).tap()
    await matchScreenshot(SCREENSHOT_ONFIDO_DOC_SELECTION) // screenshot
    await expect(element(by.text('Identity verification'))).toBeVisible()
    await expect(element(by.text('Select a document'))).toBeVisible()

    // // workaround
    // await element(by.text('Passport')).tap()
    // await new Promise((r) => setTimeout(r, 5000)) // sync issue
    // await device.terminateApp()
    // await new Promise((r) => setTimeout(r, 5000)) // sync issue
    // await device.launchApp()
    // await unlock()
    // await element(by.id(BURGER_MENU)).tap()
    // await element(by.text(MENU_SETTINGS)).tap()
    // await element(by.text(SETTINGS_ONFIDO)).tap()
    // await element(by.text(ONFIDO_ACCEPT_BUTTON)).tap()
  })

  it('Check passport', async () => {
    await element(by.text('Passport')).tap()
    await expect(element(by.text('Passport photo page'))).toBeVisible()
    await matchScreenshot(SCREENSHOT_ONFIDO_PASSPORT) // screenshot

    await element(by.label(ONFIDO_CUSTOM_BACK_ARROW)).tap()
  })

  it('Check license', async () => {
    await element(by.text("Driver's License")).tap()
    await expect(element(by.text('Select issuing country'))).toBeVisible()
    await element(by.text('United States')).tap()
    await expect(element(by.text("Front of driver's license"))).toBeVisible()
    await matchScreenshot(SCREENSHOT_ONFIDO_LICENSE) // screenshot

    await element(by.label(ONFIDO_CUSTOM_BACK_ARROW)).tap()
    await element(by.label(ONFIDO_CUSTOM_BACK_ARROW)).tap()
  })

  it('Check NIC', async () => {
    await element(by.text('National Identity Card')).tap()
    await expect(element(by.text('Select issuing country'))).toBeVisible()
    await element(by.text('United States')).tap()
    await expect(element(by.text('Front of card'))).toBeVisible()
    await matchScreenshot(SCREENSHOT_ONFIDO_NIC) // screenshot

    await element(by.label(ONFIDO_CUSTOM_BACK_ARROW)).tap()
    await element(by.label(ONFIDO_CUSTOM_BACK_ARROW)).tap()
  })

  it('Check RPC', async () => {
    await element(by.text('Residence Permit Card')).tap()
    await expect(element(by.text('Select issuing country'))).toBeVisible()
    await element(by.text('United States')).tap()
    await expect(element(by.text('Front of permit'))).toBeVisible()
    await matchScreenshot(SCREENSHOT_ONFIDO_RPC) // screenshot
    await element(by.label(ONFIDO_CUSTOM_BACK_ARROW)).tap()
    await element(by.label(ONFIDO_CUSTOM_BACK_ARROW)).tap()

    await element(by.label(ONFIDO_CUSTOM_BACK_ARROW)).tap()
    await element(by.id(ONFIDO_BACK_ARROW)).tap()
  })
})
