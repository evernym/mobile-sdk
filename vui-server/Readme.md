# Vui Server for developers


Before running Docker image for the first time, please download files listed bellow and copy them to folder **libs**. 

- indy-cli_1.15.0-bionic_amd64.deb
- libindy_1.15.0-bionic_amd64.deb
- libnullpay_1.15.0-bionic_amd64.deb
- libsovtoken_1.0.5_amd64.deb
- libvcx_0.8.72140829-d437603a5-bionic_amd64.deb
- node-vcx-wrapper_0.8.72143220-d437603a5_amd64.tgz

Download links are here: 

https://github.com/evernym/mobile-sdk/releases

## Docker image generation: 

```bash
docker build -t structuredmessage .
```

## Running server: 

```bash
docker run -it structuredmessage /bin/bash
```

After running above script, you will end up in inside virtual machine in terminal. 
There you can execute each if the steps separatelly. 

## Clear current build and re-build docker image: 

```bash
docker system prune -a 
```
Warning: It will clean up all docker images you have installed on your machine. 

## Creating connection: 
  
```
node EstablishAndSerializeConnection.js
```

If connection is established successfully, you will see this message bellow QR code: 

```
Connection was accepted!
Connection was serialized to a file!
```
Also, you will be able to enter command for executing other commands. 

In case connection is not established on cloud agent side, the code will stay the same and message about Accepting connection will not appear. In that case, please check your environment setup and make sure your genesis file and server environment points to DEMO: 

Genesis file for Demo Environment is here: 
https://raw.githubusercontent.com/sovrin-foundation/sovrin/stable/sovrin/pool_transactions_sandbox_genesis

For full list of available environments you can check here: 
https://github.com/sovrin-foundation/connector-app/blob/master/app/store/config-store.js 

## Sending structured message (question): 

```
node AskQuestion.js
```