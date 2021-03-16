#!/usr/bin/env node

const yargs = require('yargs')
const fs = require('fs')
const promisify = require('util').promisify
const path = require('path')
const del = require('del')
const { exec, spawn } = require('child-process-async')
const { pathExists, move, remove } = require('fs-extra')
const chalk = require('chalk')
const shell = require('shelljs')

const readFile = promisify(fs.readFile)
const readDir = promisify(fs.readdir)
// this is the path relative to package.json
// const testsDirectory = 'e2e/__tests__' // removed due to `-d` parameter
let testsDirectory

const detoxConfig = require('../package.json').detox.configurations

const args = yargs
  .option('b', {
    alias: 'build',
    describe: 'build before running test',
    choices: ['debug', 'release'],
    string: true,
  })
  .option('a', {
    alias: 'android',
    describe: 'run tests against android',
    default: false,
    boolean: true,
  })
  .option('s', {
    alias: 'simulators',
    describe: 'simulators on which tests need to run',
    choices: [
      'iphone5s',
      'iphone7',
      'iphone8',
      'iphonex',
      'iphonexsmax',
      'nexus5x',
    ], // android check
    default: ['iphonexsmax'],
    array: true,
  })
  .option('u', {
    alias: 'update',
    describe: 'whether to update failing screenshots with new ones',
    default: false,
    boolean: true,
  })
  .option('t', {
    alias: 'testToRun',
    describe: 'which test to run',
    string: true,
  })
  .option('d', {
    alias: 'draft',
    describe: 'run tests from __draft__ folder',
    default: false,
    boolean: true,
  })
  .option('e', {
    alias: 'environment',
    describe: 'which environment to use QA Test1 or DEV-RC',
    string: true,
    choices: [
      'dev',
      'sandbox',
      'staging',
      'prod',
      'demo',
      'qatest1',
      'qa',
      'devteam1',
      'devteam2',
      'devteam3',
      'devrc',
    ],
    default: ['qa'],
  })
  .option('k', {
    alias: 'skip',
    describe:
      'skip even the initial test to lock setup and just launch existing installed app',
    boolean: true,
    default: false,
  })
  .parserConfiguration({
    ['populate--']: true,
  })
  .completion()
  .strict(false)
  .example(
    'yarn e2e -u -b release -s iphone7 iphonex -t token -- -- -r',
    'Run e2e with update, build release mode, on simulators, with detox config as -r, and test file to run'
  )
  .help().argv
;(async function (done) {
  testsDirectory = args.draft ? 'e2e/__draft__' : 'e2e/__tests__'
  await runBuildIfNeeded(args)
  const exitCode = await runTests(args)
  console.log('Result exit code: ' + exitCode)
  process.exit(exitCode)
  done()
})()

async function runBuildIfNeeded(args) {
  // TODO:KS Need to consider android as well
  // const debugBuildPath = detoxConfig['ios.sim.debug'].binaryPath
  const debugBuildPath = args.android
    ? detoxConfig['android.emu.debug'].binaryPath
    : detoxConfig['ios.sim.debug'].binaryPath // check android
  console.log(debugBuildPath)
  // TODO:KS Need to consider android as well
  const releaseBuildPath = args.android
    ? detoxConfig['android.emu.release'].binaryPath
    : detoxConfig['ios.sim.release'].binaryPath // check android
  console.log(debugBuildPath)
  const releaseBuildExist = await pathExists(releaseBuildPath)
  const debugBuildExist = await pathExists(debugBuildPath)
  let needToGenerateBuild = args.build ? true : !debugBuildExist
  let isDetoxEnvChanged = false

  // we need to be sure that developer is
  // running metro bundler and build with detox env as yes
  // if, then set correct info in .env file
  // and then ask user to run packager again
  const envContent = await readFile('.env', 'utf8')
  if (envContent.trim() !== 'detox=yes') {
    await exec('echo "detox=yes" > .env')
    isDetoxEnvChanged = true
    // Also, since previous build might not be using correct detox setting
    // so, we need to generate build again with correct detox env
    needToGenerateBuild = true
  }

  if (needToGenerateBuild) {
    // remove builds if already exists
    ;(debugBuildExist || releaseBuildExist) &&
      (await del(path.dirname(debugBuildPath)))

    const buildType = args.build ? args.build : 'debug'
    // TODO:KS Need to consider android as well
    // const buildConfig = `ios.sim.${buildType}`
    const buildConfig = args.android
      ? `android.emu.${buildType}`
      : `ios.sim.${buildType}` // android check
    const detoxArgs = ['build', `-c`, `${buildConfig}`]
    const detoxCommand = `detox ${detoxArgs.join(' ')}`
    console.log(chalk.green(`Running command ${detoxCommand}`))
    const build = spawn(`detox`, detoxArgs, {
      stdio: 'inherit',
    })
    // wait for build to finish
    await build
    // tell user that build has finished
    console.log(chalk.green(`Successfully built using ${detoxCommand}`))

    if (isDetoxEnvChanged) {
      console.log(
        chalk.bgRed(
          `.env file did not have correct config for detox. We have updated .env file with appropriate information needed. Stop already running packager and re-run it via npm start or yarn start`
        )
      )
      throw new Error(
        'Read above message for detailed information on what to do.'
      )
    }
  }
}

async function runTests(args) {
  // TODO:KS Need to consider android as well
  const simCommandNameMap = {
    iphone5s: 'iPhone 5s',
    iphone7: 'iPhone 7',
    iphonex: 'iPhone X',
    iphonexsmax: 'iPhone XS Max',
    iphone8: 'iPhone 8',
    // nexus5x: 'Nexus_5X_API_29', // android check
    nexus5x: 'Nexus_5X_API_R',
  }

  // set environment variables that are going to be used by our tests
  if (args.update) {
    shell.env['UPDATE'] = 'YES'
  }
  // set environment on which app needs to run, prod, staging, dev etc.
  shell.env['environment'] = args.environment

  for (const sim of args.simulators) {
    // TODO:KS Need to consider android as well
    // config to run from package.json
    // const detoxConfig = `ios.sim.${args.build || 'debug'}`
    const detoxConfig = args.android
      ? `android.emu.${args.build || 'debug'}`
      : `ios.sim.${args.build || 'debug'}` // android check
    // simulator name for OS
    const simName = simCommandNameMap[sim]
    // set env variable so that our tests can identify running simulator
    shell.env['SIMULATOR'] = sim.toUpperCase()
    // create args for detox
    // override -n (name) of config with sim name
    const initialTestArgs = ['test', '-n', simName, '-c', detoxConfig]
    if (!args.skip) {
      const initialTestRun = spawn(
        'detox',
        [...initialTestArgs, 'e2e/__tests__/initial.spec.js'], // path to initial test is absolute - we must use it for any test folder
        { stdio: 'inherit' }
      )
      // wait for initial test run to finish
      const { stdout, stderr, exitCode } = await initialTestRun

      if (exitCode) {
        return exitCode
      }
    }

    // if (!args.skip) {
    //   const connectionTestRun = spawn(
    //     'detox',
    //     [...initialTestArgs, 'e2e/__tests__/connection_invitation.spec.js'], // path to connection invitation test is absolute - we must use it for any test folder
    //     { stdio: 'inherit' }
    //   )
    //   // wait for initial test run to finish
    //   const { stdout, stderr, exitCode } = await connectionTestRun

    //   if (exitCode) {
    //     return exitCode
    //   }
    // }

    const extraArgs = []
    // is there single test that user wants to run
    if (args.testToRun) {
      extraArgs.push(`${testsDirectory}/${args.testToRun}.spec.js`)
    } else {
      // if we have not specified a single test to run
      // then we need to all tests inside __tests__ directory
      // just not initial.spec.js
      // because that is already run
      const fileNames = await readDir(testsDirectory)
      extraArgs.push(
        ...fileNames
          .map((file) => {
            if (
              file === 'initial.spec.js' ||
              file === 'connection_invitation.spec.js'
            )
              return ''
            return `${testsDirectory}/${file}`
          })
          .filter((_) => _)
      )
    }

    // Now we can go ahead and run all functional tests
    // because lock setup is done now
    // Now, since we know app was fresh installed only for this session
    // we can use --reuse(-r) option to speed up our test run
    const detoxRunArgs = [
      ...initialTestArgs,
      ...extraArgs,
      '-r',
      ...(args['--'] || []),
    ]
    const testRun = spawn('detox', detoxRunArgs, { stdio: 'inherit' })
    // wait for test run to finish
    const { stdout, stderr, exitCode } = await testRun

    return exitCode
  }
}
