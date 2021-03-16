// @flow

import detox from 'detox'
import { exec } from 'child-process-async'
import { getBootedDeviceId } from './screenshot'

const { element, by, expect, device, waitFor } = detox

export const tapOn = async (id: string) => {
  const fetchedElement = element(by.id(id))
  await fetchedElement.tap()
}

// courtesy of https://github.com/wix/detox/issues/445#issuecomment-514801808
export const readVisibleText = async (testID: string) => {
  try {
    await expect(element(by.id(testID))).toHaveText(
      '_you_cant_possible_have_this_text_'
    )
    throw new Error('are you kidding me?')
  } catch (error) {
    if (device.getPlatform() === 'ios') {
      const start = `accessibilityLabel was "`
      const end = '" on '
      const errorMessage = error.message.toString()
      const [, restMessage] = errorMessage.split(start)
      const [label] = restMessage.split(end)
      return label
    } else {
      const start = 'Got:'
      const end = '}"'
      const errorMessage = error.message.toString()
      const [, restMessage] = errorMessage.split(start)
      const [label] = restMessage.split(end)
      const value = label.split(',')
      var combineText = value.find((i: string) => i.includes('text=')).trim()
      const [, elementText] = combineText.split('=')
      return elementText
    }
  }
}

export function wait(delay: *): Promise<void> {
  return new Promise(function (resolve) {
    setTimeout(resolve, delay)
  })
}

export const ID_MATCHER = 'id'
export const TEXT_MATCHER = 'text'
export const TYPE_MATCHER = 'type'

type MatcherType = typeof ID_MATCHER | typeof TEXT_MATCHER | typeof TYPE_MATCHER

export const waitForElementAndTap = async (
  matcher_type: MatcherType,
  matcher_data: string,
  timeout: number
) => {
  switch (matcher_type) {
    case 'id':
      const e1 = element(by.id(matcher_data))
      await waitFor(e1).toBeVisible().withTimeout(timeout)
      await e1.tap()
      break
    case 'text':
      const e2 = element(by.text(matcher_data))
      await waitFor(e2).toBeVisible().withTimeout(timeout)
      await e2.tap()
      break
    case 'type':
      const e3 = element(by.type(matcher_data))
      await waitFor(e3).toBeVisible().withTimeout(timeout)
      await e3.tap()
      break
  }
}
