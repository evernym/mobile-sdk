// @flow
import gm from 'gm'
import pixelmatch from 'pixelmatch'
import { promisify } from 'util'
import { exec } from 'child-process-async'
import fs from 'fs'
import { pathExists, move, remove } from 'fs-extra'
import chalk from 'chalk'
import { flatten, compose, values, filter, prop, tap, head } from 'ramda'
import { getDeviceType, IOS, ANDROID } from './test-constants'

const PNG = require('pngjs').PNG
const JPEG = require('jpeg-js')

const iPhone7 = 'iPhone7'.toLowerCase()
const iPhone8 = 'iPhone8'.toLowerCase()
const iPhoneX = 'iPhoneX'.toLowerCase()
const iPhone5s = 'iPhone5s'.toLowerCase()
const iPhoneXSMax = 'iPhoneXSMax'.toLowerCase()
const Nexus5X = 'Nexus5X'.toLowerCase()

// all images are switched from JPG to PNG because pixelmatch works with PNG only and GraphicsMagick works with both

const usePixelmatch = true // set to false to use GraphicsMagick

const SIZE = {
  [iPhoneX]: {
    width: 1125,
    height: 2436,
    cropHeight: 90,
    scaleFactor: {
      width: 3,
      height: 3,
    },
  },
  [iPhone7]: {
    width: 750,
    height: 1334,
    cropHeight: 39,
    scaleFactor: {
      width: 2,
      height: 2,
    },
  },
  [iPhone8]: {
    width: 750,
    height: 1334,
    cropHeight: 39,
    scaleFactor: {
      width: 2,
      height: 2,
    },
  },
  [iPhone5s]: {
    width: 640,
    height: 1136,
    cropHeight: 38,
    scaleFactor: {
      width: 2,
      height: 2,
    },
  },
  [iPhoneXSMax]: {
    width: 1242,
    height: 2688,
    cropHeight: 100,
    scaleFactor: {
      width: 3.3,
      height: 3.3,
    },
  },
  [Nexus5X]: {
    width: 1080,
    height: 1920,
    cropHeight: 50,
    scaleFactor: {
      width: 2,
      height: 2,
    },
  },
}

const SIMULATOR_NAME_MAP = {
  [iPhone7]: 'iPhone 7',
  [iPhone8]: 'iPhone 8',
  [iPhoneX]: 'iPhone X',
  [iPhone5s]: 'iPhone 5s',
  [iPhoneXSMax]: 'iPhone XS Max',
  [Nexus5X]: 'Nexus_5X_API_29',
}

const COMPARE_ERROR_TOLERANCE = 0.0001
const defaultSimulator = iPhone8
const baseDirectory = 'e2e/screenshots'
const { SIMULATOR = defaultSimulator, UPDATE } = process.env
const simulator = SIMULATOR ? SIMULATOR.toLowerCase() : defaultSimulator
let bootedDeviceId = 'booted'

const BOOTED = 'Booted'
const SHUTDOWN = 'Shutdown'
export type Device = {
  state: typeof BOOTED | typeof SHUTDOWN,
  name: string,
  udid: string,
}

const isBooted = (device: Device) => {
  const { name, state } = device

  return (
    name.toLowerCase() === SIMULATOR_NAME_MAP[simulator].toLowerCase() &&
    state === BOOTED
  )
}

export const storeBootedDeviceId = async () => {
  const { stdout } = await exec('xcrun simctl list devices --json')
  const list = JSON.parse(stdout)
  bootedDeviceId = compose(
    prop('udid'),
    head,
    filter(isBooted),
    flatten,
    values,
    prop('devices')
  )(list)
}

export const getBootedDeviceId = () => bootedDeviceId

function getNewScreenshotPath(name: string) {
  return `${baseDirectory}/tmp/${simulator}/${name}`
}

function getExistingScreenshotPath(name: string) {
  return `${baseDirectory}/screenshots/${simulator}/${name}`
}

function getDiffPath(name: string) {
  return `${baseDirectory}/diff/${simulator}/${name}`
}

const diff = promisify(gm.compare)
const diffOptions = (file) => ({ file, tolerance: COMPARE_ERROR_TOLERANCE })

// it uses GraphicsMagick and it is async
const areSame = async (image1: string, image2: string, diffImagePath: string) =>
  await diff(image1, image2, diffOptions(diffImagePath))

// it uses pixelmatch and it is sync
const areSameNew = (image1: string, image2: string, diffImagePath: string) => {
  const img1 = PNG.sync.read(fs.readFileSync(image1))
  const img2 = PNG.sync.read(fs.readFileSync(image2))
  const { width, height } = img1
  const delta = new PNG({ width, height })

  let res = pixelmatch(img1.data, img2.data, delta.data, width, height, {
    threshold: 0.85,
  })

  fs.writeFileSync(diffImagePath, PNG.sync.write(delta))

  return res
}

// removes header of simulator that contains date and battery icon
// which messes up our screenshot comparison
async function removeHeader(image: string) {
  return new Promise((resolve, reject) => {
    const { width, height, cropHeight } = SIZE[simulator]
    gm(image)
      .crop(width, height - cropHeight, 0, cropHeight)
      .write(image, (error) => {
        if (error) {
          reject(error)
        }

        resolve()
      })
  })
}

type ScreenShotOptions = {
  ignoreAreas: Array<{
    [simulatorName: string]: {
      top: number,
      left: number,
      width: number,
      height: number,
    },
  }>,
}

async function mask(path: *, area: *, name: *) {
  // get area of mask
  const { top, left, width, height } = area[simulator]
  const { scaleFactor, cropHeight } = SIZE[simulator]
  // top left corner coordinate of mask
  const x0 = left * scaleFactor.width
  const y0 = top * scaleFactor.height - cropHeight
  // bottom right coordinate of mask
  const x1 = x0 + width * scaleFactor.width
  const y1 = y0 + height * scaleFactor.height

  await new Promise((resolve, reject) => {
    gm(path)
      .drawRectangle(x0, y0, x1, y1)
      .write(path, (error) => {
        if (error) {
          reject(error)
        }

        resolve()
      })
  })
}

export async function matchScreenshot(
  name: string,
  options?: ScreenShotOptions
) {
  // // TODO:KS Add support for screenshot testing for Android as well
  // if (getDeviceType() === ANDROID) {
  //   return
  // }

  const newScreenshot = getNewScreenshotPath(name)

  if (getDeviceType() === IOS) {
    // ios screenshot capturing
    await exec(`xcrun simctl io ${bootedDeviceId} screenshot ${newScreenshot}`)
  } else {
    // android screenshot capturing
    await exec(`adb shell /system/bin/screencap -p /sdcard/screenshot.png`)
    await exec(`adb pull /sdcard/screenshot.png ${newScreenshot}`)
  }
  await removeHeader(newScreenshot)

  if (options) {
    // if we want to ignore some areas that will always have dynamic value
    // for example: we might have date field that will always be different
    for (const ignoreArea of options.ignoreAreas) {
      await mask(newScreenshot, ignoreArea, name)
    }
  }

  const existingScreenshot = getExistingScreenshotPath(name)

  const exists = await pathExists(existingScreenshot)
  if (!exists) {
    await move(newScreenshot, existingScreenshot)
    console.log(chalk.green(`New screenshot captured at ${existingScreenshot}`))

    return
  }

  if (UPDATE) {
    await remove(existingScreenshot)
    await move(newScreenshot, existingScreenshot)
    console.log(chalk.green(`Updated screenshot ${existingScreenshot}`))

    return
  }

  const diffImagePath = getDiffPath(name)

  let numDiffPixels
  let result

  if (usePixelmatch) {
    numDiffPixels = areSameNew(existingScreenshot, newScreenshot, diffImagePath)
    console.log(chalk.cyan(numDiffPixels))
  } else {
    result = await areSame(existingScreenshot, newScreenshot, diffImagePath)
    console.log(chalk.cyan(result))
  }

  // $FlowFixMe
  if (usePixelmatch ? numDiffPixels > 0 : !result) {
    console.log(
      chalk.red(
        `Existing screenshot at '${existingScreenshot}' and new screenshot at '${newScreenshot}' are not same. Please see difference at ${diffImagePath}. \nIf you want to keep new screenshot, then run test command with -u flag.`
      )
    )

    throw new Error(
      `Failed to match screenshot ${name}. Check diff ${diffImagePath}`
    )
  }
}
