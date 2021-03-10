// @flow

import fs from 'fs'
import { promisify } from 'util'
import { exec } from 'child-process-async'
import { pathExists, move, remove } from 'fs-extra'
import chalk from 'chalk'
import nodeSSH from 'node-ssh'
import { StringDecoder } from 'string_decoder'

import { wait } from './detox-selectors'

const writeFile = promisify(fs.writeFile)
const readFile = promisify(fs.readFile)
const ssh = new nodeSSH()

export async function mintTokens(paymentAddress: string, atoms: string) {
  const environment = process.env.environment || 'qatest1'
  const configPath = `e2e/config/ssh-${environment}`
  const serverNameFile = `${configPath}/ssh-server-name`
  const pemFilePath = `${configPath}/Evernym-QA-Pool.pem`

  const serverNameExists = await pathExists(serverNameFile)
  const pemFileExists = await pathExists(pemFilePath)
  if (!serverNameExists || !pemFileExists) {
    try {
      await writeFile(serverNameFile, '')
    } catch (e) {
      console.log(
        chalk.redBright(
          `Could not create ssh-server-name file. ${serverNameFile}`
        )
      )
    }
    console.log(
      chalk.bgRed(
        `One of the required files for ssh to mint token is missing from environment folder. Find an email with subject "AWS Indy-CLI Instance for minting tokens and setting fees". Download attachment and put it in appropriate environment folder inside e2e/config/ssh-*. This folder needs to have two files Evernym-QA-Pool.pem, and ssh-server-name. Evernym-QA-Pool.pem should be inside the attachment of email, ssh-server-name file is created for you. You need to put username@ip.adr.res.ss inside ssh-server-name file.`
      )
    )

    throw new Error('Missing required files')
  }

  // connect to ssh machine
  const serverName = await readFile(serverNameFile, 'utf8')
  const [userName, hostName] = serverName.trim().split('@')
  const sshServer = await ssh.connect({
    host: hostName,
    username: userName,
    privateKey: pemFilePath,
  })

  try {
    const mintFileName = await createUploadMintFile(
      paymentAddress,
      atoms,
      configPath
    )
    // In some cases TAA can change, if TAA changes
    // then we need to set and remove TAA again to mint tokens
    // if we need to change TAA, then set setRemoveTaa flag to true
    const setRemoveTaa = false

    let taaRemoveFileName = ''
    let taaSetFileName = ''
    if (setRemoveTaa) {
      taaRemoveFileName = await incrementTaaRemoveVersionOnSSH(configPath)
      taaSetFileName = await incrementTaaSetVersionOnSSH(configPath)
    }

    const shell = await ssh.requestShell()
    const remoteShellData = new StringDecoder('utf8')
    shell.on('data', chunk => remoteShellData.write(chunk))

    if (setRemoveTaa) {
      shell.write(`indy-cli ${taaRemoveFileName}\n`)
      await exit(1500, shell)
    }

    // mint tokens
    shell.write(`indy-cli --config amlconfig.json ${mintFileName}\n`)
    await exit(3000, shell)

    if (setRemoveTaa) {
      shell.write(`indy-cli ${taaSetFileName}\n`)
      await exit(1500, shell)
    }

    console.log(remoteShellData.end())
  } finally {
    sshServer.dispose()
  }
}

async function createUploadMintFile(
  paymentAddress: *,
  atoms: *,
  configPath: *
) {
  const mintFileTemplate = `pool connect tp
wallet open tw key=tw
load-plugin library=/usr/lib/libsovtoken.so initializer=sovtoken_init
did use V4SGRU86Z58d6TV7PBUe6f

# Prepare Transaction
ledger mint-prepare outputs=(${paymentAddress},${atoms})

# Sign mint transaction with Trustee 1
did use V4SGRU86Z58d6TV7PBUe6f
ledger sign-multi

# Sign mint transaction with Trustee 2
did use LnXR1rPnncTPZvRdmJKhJQ
ledger sign-multi

# Sign mint transaction with Trustee 3
did use PNQm3CwyXbN5e39Rw3dXYx
ledger sign-multi

# Send custom multi-sig transaction
did use V4SGRU86Z58d6TV7PBUe6f
ledger custom context

# View Balance
ledger get-payment-sources payment_address=${paymentAddress}
`

  const mintFileName = 'mint-e2e-test'
  const mintFilePath = `${configPath}/${mintFileName}`

  await writeFile(mintFilePath, mintFileTemplate)
  await ssh.putFile(mintFilePath, `./${mintFileName}`)

  return mintFileName
}

async function incrementTaaSetVersionOnSSH(configPath: *) {
  const taaSetFileName = 'taaset'
  const e2eTAASetFilePath = `${configPath}/${taaSetFileName}`
  await ssh.getFile(e2eTAASetFilePath, taaSetFileName)
  const newVersion = await getIncrementedVersion(e2eTAASetFilePath)
  const newTaaSetContent = `# Connect and load wallet
pool connect tp
wallet open tw key=tw
load-plugin library=/usr/lib/libsovtoken.so initializer=sovtoken_init
did use V4SGRU86Z58d6TV7PBUe6f

# Set TAA
ledger txn-author-agreement version=${newVersion}: text="Transaction Author Agreement V2.0"
`

  await writeFile(e2eTAASetFilePath, newTaaSetContent)
  await ssh.putFile(e2eTAASetFilePath, `./${taaSetFileName}`)

  return taaSetFileName
}

async function incrementTaaRemoveVersionOnSSH(configPath: *) {
  const taaRemoveFileName = 'taaremove'
  const e2eTAARemoveFilePath = `${configPath}/${taaRemoveFileName}`
  await ssh.getFile(e2eTAARemoveFilePath, taaRemoveFileName)
  const newVersion = await getIncrementedVersion(e2eTAARemoveFilePath)
  const newTaaRemoveContent = `# Connect and load wallet
pool connect tp
wallet open tw key=tw
load-plugin library=/usr/lib/libsovtoken.so initializer=sovtoken_init
did use V4SGRU86Z58d6TV7PBUe6f

# Set TAA
ledger txn-author-agreement version=${newVersion} text=""
`

  await writeFile(e2eTAARemoveFilePath, newTaaRemoveContent)
  await ssh.putFile(e2eTAARemoveFilePath, `./${taaRemoveFileName}`)

  return taaRemoveFileName
}

async function getIncrementedVersion(taaFilePath: *) {
  const taaFileContents = await readFile(taaFilePath, 'utf8')
  // get version
  const [major, minor, fix] = taaFileContents
    .trim()
    .split('\n')
    .slice(-1)[0]
    .split(' ')
    .filter(_ => _)
    .filter(x => x.startsWith('version'))[0]
    .split('=')[1]
    .split('.')
    .map(x => parseInt(x, 10))

  // increment version by 2
  const newVersion = `${major}.${minor}.${fix + 2}`

  return newVersion
}

async function exit(delay: *, shell: *) {
  await wait(delay)
  shell.write('exit\n')
  await wait(500)
}
