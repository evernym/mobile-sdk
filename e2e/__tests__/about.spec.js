// @flow

import { element, by, waitFor, expect } from 'detox'
import { matchScreenshot } from '../utils/screenshot'
import {
  BURGER_MENU,
  MENU_SETTINGS,
  SETTINGS_ABOUT,
  ABOUT_BACK_ARROW,
  ABOUT_HEADER,
  ABOUT_TAC_BUTTON_HEADER,
  ABOUT_PP_BUTTON_HEADER,
  SCREENSHOT_ABOUT_MAIN,
  SCREENSHOT_ABOUT_TAC,
  SCREENSHOT_ABOUT_PP,
} from '../utils/test-constants'

describe('About', () => {
  it('check about', async () => {
    await element(by.id(BURGER_MENU)).tap()
    await element(by.text(MENU_SETTINGS)).tap()
    await element(by.text(SETTINGS_ABOUT)).tap()
    await expect(element(by.text(ABOUT_HEADER))).toBeVisible() // header
    await expect(element(by.text('built by'))).toBeVisible()
    await expect(element(by.text('powered by'))).toBeVisible()
    await matchScreenshot(SCREENSHOT_ABOUT_MAIN) // screenshot
  })

  it('check terms and conditions', async () => {
    await element(by.text(ABOUT_TAC_BUTTON_HEADER)).tap()
    await expect(
      element(by.text(ABOUT_TAC_BUTTON_HEADER)).atIndex(0)
    ).toBeVisible()
    await matchScreenshot(SCREENSHOT_ABOUT_TAC) // screenshot

    await element(by.id(ABOUT_BACK_ARROW)).tap()
  })

  it('check privacy policy', async () => {
    await element(by.text(ABOUT_PP_BUTTON_HEADER)).tap()
    await expect(
      element(by.text(ABOUT_PP_BUTTON_HEADER)).atIndex(0)
    ).toBeVisible()
    await matchScreenshot(SCREENSHOT_ABOUT_PP) // screenshot

    await element(by.id(ABOUT_BACK_ARROW)).tap()
    await element(by.id(ABOUT_BACK_ARROW)).tap()
  })
})
