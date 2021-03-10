// @flow

import { element, by, waitFor } from 'detox'
import {
  BURGER_MENU,
  MENU_CONTAINER,
  MENU_HOME,
  MENU_MY_CONNECTIONS,
  MENU_MY_CREDENTIALS,
  MENU_SETTINGS,
  getDeviceType,
} from '../utils/test-constants'

describe('Menu screen', () => {
  it('Case 1: go to menu, find all necessary elements', async () => {
    await element(by.id(BURGER_MENU)).tap()

    // check menu view
    await waitFor(element(by.id(MENU_CONTAINER)))
      .toBeVisible()
      .withTimeout(5000)

    // check home button
    if (getDeviceType() === 'ios') {
      await waitFor(element(by.text(MENU_HOME)))
        .toBeVisible()
        .withTimeout(5000)
    } else {
      await waitFor(element(by.text(MENU_HOME)).atIndex(1))
        .toBeVisible()
        .withTimeout(5000)
    }

    // check my connections button
    await waitFor(element(by.text(MENU_MY_CONNECTIONS)))
      .toBeVisible()
      .withTimeout(5000)

    // check my credentials button
    await waitFor(element(by.text(MENU_MY_CREDENTIALS)))
      .toBeVisible()
      .withTimeout(5000)

    // check settings button
    await waitFor(element(by.text(MENU_SETTINGS)))
      .toBeVisible()
      .withTimeout(5000)
  })
  it('Case 2: check all available buttons', async () => {
    await element(by.text(MENU_MY_CONNECTIONS)).tap()
    await element(by.id(BURGER_MENU)).tap()
    await element(by.text(MENU_MY_CREDENTIALS)).tap()
    await element(by.id(BURGER_MENU)).tap()
    await element(by.text(MENU_SETTINGS)).tap()
    await element(by.id(BURGER_MENU)).tap()
    await element(by.text(MENU_HOME)).tap()
  })
})
