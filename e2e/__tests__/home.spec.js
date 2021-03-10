// @flow

// import { matchScreenshot } from '../utils/screenshot'
// import { SCREENSHOT_HOME } from '../utils/test-constants'

import { element, by, waitFor } from 'detox'
import {
  HOME_CONTAINER,
  HOME_HEADER,
  BURGER_MENU,
  SCAN_BUTTON,
  QR_CODE_SCANNER_CLOSE_BUTTON,
  getDeviceType,
} from '../utils/test-constants'

describe('Home screen', () => {
  it('show home, find all necessary elements', async () => {
    // // it doesn't work in pipeline: CM-2552
    // await matchScreenshot(SCREENSHOT_HOME)

    // check home view
    await waitFor(element(by.id(HOME_CONTAINER)))
      .toBeVisible()
      .withTimeout(5000)

    // check home header
    if (getDeviceType() === 'ios') {
      await waitFor(element(by.text(HOME_HEADER)))
        .toBeVisible()
        .withTimeout(5000)
    } else {
      await waitFor(element(by.text(HOME_HEADER)).atIndex(1))
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

  it('open and close menu and scanner', async () => {
    await element(by.id(BURGER_MENU)).tap()
    await element(by.text(SCAN_BUTTON)).tap()
    // await element(by.id(QR_CODE_SCANNER_CLOSE_BUTTON)).tap()
  })
})
