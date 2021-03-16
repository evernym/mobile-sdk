// @flow

import { element, by, waitFor, expect } from 'detox'
import { unlock } from '../utils/lock-unlock'
import { matchScreenshot } from '../utils/screenshot'
import {
  SCREENSHOT_HOME,
  SCREENSHOT_CONNECTIONS,
  SCREENSHOT_CREDENTIALS,
  SCREENSHOT_SETTINGS,
  SCREENSHOT_MENU,
  BURGER_MENU,
  MENU_MY_CONNECTIONS,
  MENU_MY_CREDENTIALS,
  MENU_SETTINGS,
} from '../utils/test-constants'

// PLEASE DO NOT REMOVE or RENAME this file
// We are using this file to run initial setup of tests
// Since detox launches app inside simulator for every test file
// that means for every test we would need to keep checking whether
// lock was setup or not, if not setup and if yes, then which lock
// and then unlock
// Obviously, all of this functionality can be made into a function
// but there is another reason why we need to have this file
// Detox, by default uninstalls the app to run tests from every file
// so if there are 10 test files, then detox would install, uninstall
// app inside simulator 10 times.
// That would increase 20 seconds in every test, which is not good at all
// Since we don't want to keep testing lock/unlock functionality
// When we could spend that time in testing other stuff
// Detox solved this problem recently, and gave an option to use --reuse
// which would not uninstall the app, but just launch the app again.
// However, detox did not come up with full solution.
// If we launch our tests with --reuse option, then app would not be
// launched if simulator already does not have detox build installed on it.
// That means to use --reuse option of detox cli, we need to manually
// install the build first on simulator and then run detox test
// There is no command provided by detox or other simulator utils which
// can tell us if app is already installed or not. So, we don't know if
// we need to install the app.
// Also, there is no simple install command to install app, detox does not
// expose any command to us to install the app.
// Even if it did, we can't be sure that existing installation of app
// on the simulator belongs to the current test run. It could be old build
// So, we need to be sure of just one installation only for this test session
// All of the above reasons comes to the point where we need to do below
// - Delete previous installed app
// - run app setup once, where we set environment, choose lock, and go to home
// - start running all other tests, by not uninstalling app and saving time

describe('One time initial setup and base screenshots check', () => {
  // it('Set environment, set lock, go to home', async () => {
  //   await unlock()
  // })

  it('Home screenshot check', async () => {
    await matchScreenshot(SCREENSHOT_HOME)
  })

  it('Menu screenshot check', async () => {
    await element(by.id(BURGER_MENU)).tap()
    await matchScreenshot(SCREENSHOT_MENU)
  })

  it('My connections screenshot check', async () => {
    await element(by.text(MENU_MY_CONNECTIONS)).tap()
    await matchScreenshot(SCREENSHOT_CONNECTIONS)
  })

  it('My credentials screenshot check', async () => {
    await element(by.id(BURGER_MENU)).tap()
    await element(by.text(MENU_MY_CREDENTIALS)).tap()
    await matchScreenshot(SCREENSHOT_CREDENTIALS)
  })

  it('Settings screenshot check', async () => {
    await element(by.id(BURGER_MENU)).tap()
    await element(by.text(MENU_SETTINGS)).tap()
    await matchScreenshot(SCREENSHOT_SETTINGS)
  })
})
