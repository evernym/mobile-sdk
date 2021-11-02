# Simple Sponsor

This is a simple HTTP server performing generation and signing of tokens which can be used for provisioning of a cloud agents.

#### Prerequisite

- Sponsor registered in Evernym's Cloud Service [see](../../docs/3.Initialization.md#sponsor-ie-you-onboarding-with-evernyms-cloud-service).
- Ngrok
- Python3

#### Preparation

1. Rename `server.conf.sample` file to `server.conf` or create a new one and change the following fields with your Sponsor information (note: do not use quotes for values):
    * `sponsor_id` - an ID given to you from Evernym's Support Team after the Sponsor onboarding process is complete.
    * `seed` - seed used for generation of your `DID/Verkey` pair used for Sponsor onboarding.
    * `verkey` - generate `Verkey`
1. Start Ngrok with `ngrok http 4321` to get public address for your server (`4321` is default port which can be changed in `server.conf` file).

#### Run

##### In Docker

1. `docker build -f Dockerfile -t simple-sponsor .`
1. `docker run -it -p 4321:4321 simple-sponsor`

##### Locally

1. Install [Libindy](https://github.com/hyperledger/indy-sdk#installing-the-sdk)
1. Install python dependencies with `pip3 install -r requirements.txt`
1. Start server with `python3 server.py`

#### How to use

Started server provides `/generate` POST endpoint without query params and the following body:
```
{
    `sponseeId`: 'string'  - a unique identifier of a requester
}
```

Example: 
* url - `http://b620a27d5ce0.ngrok.io/generate`
* body - `{'sponseeId': '545516d9-9c5d-4bae-84c6-a74989499cc5'}`

You need to call this endpoint in your application to get provision token.

#### Flow

1. A client application generates a unique identifier and put it into a request as `sponseeId` to generate a provision token.
2. Sponsor backend generates token and optionally do mapping of `sponseeId` to some internal info's (like push notification endpoint - not included in current sample)
3. Sponsor backend returns generated token (`sponseeId` is just set by the value from the original request).

