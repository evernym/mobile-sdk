// @flow

import { element, by, waitFor, expect } from 'detox'
import {
  BURGER_MENU,
  MENU_SETTINGS,
  SETTINGS_PASSCODE,
  PIN_CODE_INPUT_BOX,
  TEST_PASS_CODE,
  TEST_PASS_CODE_CHANGED,
  CLOSE_BUTTON,
  NEW_BACK_ARROW,
  getDeviceType,
} from '../utils/test-constants'

describe('Passcode', () => {
  it('change passcode negative', async () => {
    await element(by.id(BURGER_MENU)).tap()
    await element(by.text(MENU_SETTINGS)).tap()
    await element(by.text(SETTINGS_PASSCODE)).tap()
    await element(by.id(PIN_CODE_INPUT_BOX))
      .atIndex(0)
      .replaceText(TEST_PASS_CODE_CHANGED)
    await expect(element(by.text('Wrong passcode! Please try again')))
    await element(by.id(NEW_BACK_ARROW)).tap()
  })

  it('change passcode positive', async () => {
    await element(by.text(SETTINGS_PASSCODE)).tap()
    await element(by.id(PIN_CODE_INPUT_BOX))
      .atIndex(0)
      .replaceText(TEST_PASS_CODE)
    await element(by.id(PIN_CODE_INPUT_BOX))
      .atIndex(0)
      .replaceText(TEST_PASS_CODE_CHANGED)
    if (getDeviceType() === 'android') {
      await element(by.id(PIN_CODE_INPUT_BOX))
        .atIndex(0)
        .replaceText(TEST_PASS_CODE_CHANGED)
    }
    await element(by.text(CLOSE_BUTTON)).tap()
  })
})
