/**
 * @jest-environment node
 */

// @flow

import { element, by, waitFor } from 'detox'
import { getText } from 'detox-getprops'

describe.skip('Backup test suite', () => {
  it('Case 1: positive backup tests', async () => {
    await waitFor(element(by.id('burger-menu')))
      .toBeVisible()
      .withTimeout(5000)
    await element(by.id('burger-menu')).tap()

    await waitFor(element(by.text('Settings')))
      .toBeVisible()
      .withTimeout(5000)
    await element(by.text('Settings')).tap()

    await waitFor(element(by.text('Create a Backup')))
      .toBeVisible()
      .withTimeout(5000)
    await element(by.text('Create a Backup')).tap()
    // issue with list item with testID
    // await waitFor(element(by.id('settings-backup-data-wallet'))).toBeVisible().withTimeout(5000)
    // await element(by.id('settings-backup-data-wallet')).tap()

    await waitFor(element(by.id('show-recovery-passphrase')))
      .toBeVisible()
      .withTimeout(5000)
    const phrase = await getText(element(by.id('show-recovery-passphrase')))
    console.log(phrase)

    await waitFor(element(by.id('submit-recovery-passphrase')))
      .toBeVisible()
      .withTimeout(5000)
    await element(by.id('submit-recovery-passphrase')).tap()

    await waitFor(element(by.id('verify-passphrase-container-text-input')))
      .toBeVisible()
      .withTimeout(5000)
    await element(by.id('verify-passphrase-container-text-input')).typeText(
      phrase + '\n'
    )

    // await waitFor(element(by.text('Downloaded .zip Backup')))
    //   .toBeVisible()
    //   .withTimeout(5000)
    // await element(by.text('Downloaded .zip Backup')).tap()

    await waitFor(element(by.id('export-encrypted-backup')))
      .toBeVisible()
      .withTimeout(5000)
    await element(by.id('export-encrypted-backup')).tap()

    const elementType = '_UISizeTrackingView'
    // this is top visible tappable element in hierarchy
    await waitFor(element(by.type(elementType)))
      .toBeVisible()
      .withTimeout(15000)
    // try to press `Copy` - it doesn't work
    await element(by.type(elementType)).tapAtPoint({ x: 50, y: 200 })
    // try to press `Save to Files` - it doesn't work
    await element(by.type(elementType)).tapAtPoint({ x: 125, y: 200 })

    await waitFor(element(by.id('backup-complete-submit-button')))
      .toBeVisible()
      .withTimeout(5000)
    await element(by.id('backup-complete-submit-button')).tap()
  })

  it('Case 2: negative backup test', async () => {})
})
