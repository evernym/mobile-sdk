# VCX for Python

This is a simple HTTP server performing generation and signing of tokens which can be used for provisioning of a cloud agents.

#### Prerequisite

- Sponsor registered in Evernym's Cloud Service
- Ngrok
- Python3

#### Run

##### In Docker

1. `docker build -f Dockerfile -t simple-sponsor .`
1. `docker run -it -p 4321:4321 simple-sponsor`

##### Locally

1. Install [Libindy](https://github.com/hyperledger/indy-sdk#installing-the-sdk)
1. Install python dependencies with `pip3 install -r requirements.txt`
1. Rename `server.conf.sample` file to `server.conf` or create a new one and change the following fields with your Sponsor information:
    * `sponsor_id` - an ID given to you from Evernym's Support Team after the Sponsor onboarding process is complete.
    * `seed` - seed used for generation of your `DID/Verkey` pair used for Sponsor onboarding.
    * `verkey` - generate `Verkey`
1. Start Ngrok with `ngrok http 4321` to get public address for your server (`4321` is default port which can be changed in `server.conf` file).
1. Start server with `python3 server.py`
    * It provides `/generate` POST endpoint which should be called to get provision token (example: `http://b620a27d5ce0.ngrok.io/generate`).
    * You need to set this endpoint (`http://b620a27d5ce0.ngrok.io/generate`) in your application.