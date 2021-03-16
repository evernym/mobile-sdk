// @flow

import { device, element, by } from 'detox'
import { tapOn, readVisibleText, wait } from '../utils/detox-selectors'
import { SOVRIN_TOKEN_AMOUNT_TEST_ID } from '../../app/my-connections/my-connections-constants'
import { matchScreenshot } from '../utils/screenshot'
import { mintTokens } from '../utils/mint-tokens'
import { BigNumber } from 'bignumber.js'
import { WALLET_HEADER_TOKEN_BALANCE_ID } from '../../app/wallet/type-wallet'
import { convertSovrinAtomsToSovrinTokens } from '../../app/sovrin-token/sovrin-token-converter'

describe.skip('Token functionality', () => {
  it('should show token history', async () => {
    await tapOn('tab-bar-settings-icon')
    await tapOn(SOVRIN_TOKEN_AMOUNT_TEST_ID)
    await matchScreenshot('token-dashboard.png', {
      ignoreAreas: [walletAddressArea, tokenBalanceArea],
    })
    await tapOn('token-copy-to-clipboard-label')
    const paymentAddress = await readVisibleText('token-payment-address')
    const currentTokenBalance = await readVisibleText(
      WALLET_HEADER_TOKEN_BALANCE_ID
    )
    const sovAtomsToMint = '1000'
    await mintTokens(paymentAddress, sovAtomsToMint)
    // close token screen
    await tapOn('wallet-header-close-image')
    // open token screen again
    await tapOn(SOVRIN_TOKEN_AMOUNT_TEST_ID)

    // TODO:KS wait is a code smell in e2e tests, and we need to fix it
    // we don't have any indicator on UI, so that detox wait for it to finish
    // we have to add this wait, we have to remove this, and add something on
    // UI, so detox can figure this out by itself
    // wait for wallet balance to update
    await wait(2000)

    const updatedTokenBalance = new BigNumber(currentTokenBalance)
      .plus(new BigNumber(convertSovrinAtomsToSovrinTokens(sovAtomsToMint)))
      .toFixed()
      .toString()

    await expect(element(by.id(WALLET_HEADER_TOKEN_BALANCE_ID))).toHaveText(
      updatedTokenBalance
    )
  })

  const walletAddressArea = {
    iphone5s: {
      top: 272,
      left: 19,
      width: 282,
      height: 131,
    },
    iphone7: {
      top: 272,
      left: 19,
      width: 337,
      height: 106,
    },
    iphone8: {
      top: 272,
      left: 19,
      width: 337,
      height: 106,
    },
    iphonex: {
      top: 296,
      left: 19,
      width: 337,
      height: 110,
    },
    iphonexsmax: {
      top: 296,
      left: 19,
      width: 337,
      height: 110,
    },
  }

  const tokenBalanceArea = {
    iphone5s: {
      top: 68,
      left: 45,
      width: 230,
      height: 40,
    },
    iphone7: {
      top: 68,
      left: 45,
      width: 280,
      height: 40,
    },
    iphone8: {
      top: 68,
      left: 45,
      width: 280,
      height: 40,
    },
    iphonex: {
      top: 92,
      left: 45,
      width: 280,
      height: 40,
    },
    iphonexsmax: {
      top: 92,
      left: 45,
      width: 280,
      height: 40,
    },
  }
})
