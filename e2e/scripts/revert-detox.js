#!/usr/bin/env node

const fs = require('fs')
const promisify = require('util').promisify
const { exec } = require('child-process-async')
const readFile = promisify(fs.readFile)
;(async function() {
  const envContent = await readFile('.env', 'utf8')
  if (envContent.trim() !== 'detox=no') {
    await exec('echo "detox=no" > .env')
    await exec('git add .env')
  }
})()
