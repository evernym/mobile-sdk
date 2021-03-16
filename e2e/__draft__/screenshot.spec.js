// @flow

import { element, by } from 'detox'
import { matchScreenshot } from '../utils/screenshot'
import {
  SCREENSHOT_HOME,
  SCREENSHOT_CONNECTIONS,
  SCREENSHOT_SETTINGS,
  BURGER_MENU,
  MENU_MY_CONNECTIONS,
  MENU_SETTINGS,
} from '../utils/test-constants'

describe('Screenshot-based test suite', () => {
  it('Home screenshot check', async () => {
    await matchScreenshot(SCREENSHOT_HOME)
  })

  it('My connections screenshot check', async () => {
    await element(by.id(BURGER_MENU)).tap()
    await element(by.text(MENU_MY_CONNECTIONS)).tap()
    await matchScreenshot(SCREENSHOT_CONNECTIONS)
  })

  it('Settings screenshot check', async () => {
    await element(by.id(BURGER_MENU)).tap()
    await element(by.text(MENU_SETTINGS)).tap()
    await matchScreenshot(SCREENSHOT_SETTINGS)
  })
})
