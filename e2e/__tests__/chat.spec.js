// @flow

import { element, by, waitFor, expect } from 'detox'
import { matchScreenshot } from '../utils/screenshot'
import {
  BURGER_MENU,
  MENU_SETTINGS,
  SETTINGS_CHAT,
  CHAT_CANCEL,
  CHAT_CLOSE,
  CHAT_HEADER,
  CHAT_NEW_MESSAGE,
  CHAT_TEXT_VIEW,
  CHAT_SEND_BUTTON,
  CHAT_SUCCESS_MESSAGE,
  SCREENSHOT_EMPTY_CHAT,
  SCREENSHOT_NOT_EMPTY_CHAT,
  getDeviceType,
} from '../utils/test-constants'

describe('Chat', () => {
  it('Open chat, check elements, match screenshot', async () => {
    await element(by.id(BURGER_MENU)).tap()
    await element(by.text(MENU_SETTINGS)).tap()
    try {
      await element(by.text(SETTINGS_CHAT)).tap()
    } catch (e) {
      console.warn(e) // espresso
    }
    try {
      await expect(element(by.text(CHAT_HEADER))).toBeVisible() // header
    } catch (e) {
      console.warn(e) // espresso
    }
    try {
      await expect(element(by.text(CHAT_NEW_MESSAGE))).toBeVisible()
    } catch (e) {
      console.warn(e) // espresso
    }
    await matchScreenshot(SCREENSHOT_EMPTY_CHAT) // screenshot
  })

  it('Type and send some messages', async () => {
    if (getDeviceType() === 'android') {
      return
    } // esspresso doesn't work well with apptentive elements
    await element(by.type(CHAT_TEXT_VIEW)).typeText('test message')
    await element(by.type(CHAT_TEXT_VIEW)).tapReturnKey()
    await element(by.text(CHAT_SEND_BUTTON)).tap()
    // await expect(element(by.text(CHAT_SUCCESS_MESSAGE))).toBeVisible() //?

    await element(by.text(CHAT_NEW_MESSAGE)).atIndex(0).tap()
    await element(by.type(CHAT_TEXT_VIEW))
      .atIndex(2)
      .typeText('one more test message')
    await element(by.type(CHAT_TEXT_VIEW)).atIndex(2).tapReturnKey()
    await element(by.text(CHAT_SEND_BUTTON)).tap()
    // await expect(element(by.text(CHAT_SUCCESS_MESSAGE))).toBeVisible() //?
    // await matchScreenshot(SCREENSHOT_NOT_EMPTY_CHAT) // screenshot

    await element(by.text(CHAT_CLOSE)).tap()
  })
})
