// @flow

import { element, by, waitFor } from 'detox'
import {
  BURGER_MENU,
  SCAN_BUTTON,
  MENU_MY_CONNECTIONS,
  MY_CONNECTIONS_CONNECTION,
} from '../utils/test-constants'
import { waitForElementAndTap } from '../utils/detox-selectors'

const TIMEOUT = 15000

describe('Delete connection', () => {
  it('Case 1: go to my connections, enter test connection and delete it', async () => {
    await waitForElementAndTap('id', BURGER_MENU, TIMEOUT)

    await waitForElementAndTap('text', MENU_MY_CONNECTIONS, TIMEOUT)

    await waitForElementAndTap('text', MY_CONNECTIONS_CONNECTION, TIMEOUT)
  })
})
