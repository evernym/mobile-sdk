// @flow

import { element, by, waitFor, device } from 'detox'
import {
  BURGER_MENU,
  SCAN_BUTTON,
  QR_CODE_SCANNER_CLOSE_BUTTON,
  SETTINGS_CONTAINER,
  SETTINGS_HEADER,
  SETTINGS_CREATE_BACKUP,
  BACKUP_CLOSE,
  SETTINGS_BIOMETRICS,
  BIOMETRICS_OK,
  SETTINGS_PASSCODE,
  PASSCODE_BACK_ARROW,
  SETTINGS_CHAT,
  CHAT_CANCEL,
  CHAT_CLOSE,
  SETTINGS_ABOUT,
  ABOUT_BACK_ARROW,
  SETTINGS_ONFIDO,
  ONFIDO_BACK_ARROW,
  getDeviceType,
} from '../utils/test-constants'

describe('Settings screen', () => {
  it('Case 1: go to settings, find all necessary elements', async () => {
    await element(by.id(BURGER_MENU)).tap()
    await element(by.text('Settings')).tap()

    // check settings view
    await waitFor(element(by.id(SETTINGS_CONTAINER)))
      .toBeVisible()
      .withTimeout(5000)

    // check settings header
    if (getDeviceType() === 'ios') {
      await waitFor(element(by.text(SETTINGS_HEADER)))
        .toBeVisible()
        .withTimeout(5000)
    } else {
      await waitFor(element(by.text(SETTINGS_HEADER)).atIndex(1))
        .toBeVisible()
        .withTimeout(5000)
    }

    // check menu button
    await waitFor(element(by.id(BURGER_MENU)))
      .toBeVisible()
      .withTimeout(5000)

    // check camera button
    await waitFor(element(by.text(SCAN_BUTTON)))
      .toBeVisible()
      .withTimeout(5000)
  })
  it('Case 2: check all available buttons', async () => {
    await element(by.text(SCAN_BUTTON)).tap()
    await element(by.id(QR_CODE_SCANNER_CLOSE_BUTTON)).tap()
    // await element(by.text(SETTINGS_CREATE_BACKUP)).tap()
    // await element(by.id(BACKUP_CLOSE)).tap()
    await element(by.text(SETTINGS_BIOMETRICS)).tap()
    await element(by.text(BIOMETRICS_OK)).tap()
    await element(by.text(SETTINGS_PASSCODE)).tap()
    await element(by.id(PASSCODE_BACK_ARROW)).tap()
    if (getDeviceType() === 'ios') {
      await element(by.text(SETTINGS_CHAT)).tap()
      await element(by.text(CHAT_CLOSE)).tap()
    } else {
      // android taps this button but throws strange espresso exception
      try {
        await element(by.text(SETTINGS_CHAT)).tap()
      } catch (e) {
        console.warn(e)
      } finally {
        await device.pressBack()
      }
    }
    await element(by.text(SETTINGS_ABOUT)).tap()
    await element(by.id(ABOUT_BACK_ARROW)).tap()
    // await element(by.text(SETTINGS_ONFIDO)).tap()
    // await element(by.id(ONFIDO_BACK_ARROW)).tap()
  })
})
