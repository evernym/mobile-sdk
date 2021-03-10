// @flow

import detox, { device } from 'detox'
import { storeBootedDeviceId } from '../utils/screenshot'
import { setDeviceType } from '../utils/test-constants'
import { unlock } from '../utils/lock-unlock'

jest.setTimeout(600000)
const config = require('../../package.json').detox

beforeAll(async () => {
  await detox.init(config, { launchApp: false })
  await device.launchApp({
    permissions: { camera: 'YES', photos: 'YES', notifications: 'YES' },
  })
  await storeBootedDeviceId()
  setDeviceType(device.getPlatform())

  await unlock() // moved here to run before each `describe` only, not before each `it`
  // console.log('DETOX SETUP HAS BEEN FINISHED')
})

beforeEach(async () => {})

afterEach(async () => {})

afterAll(async () => {
  // await device.terminateApp()
  await detox.cleanup()
})
