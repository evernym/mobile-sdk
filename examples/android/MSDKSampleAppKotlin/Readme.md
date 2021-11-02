# Mobile SDK Kotlin Android Sample

This is a simple android mobile application written in Kotlin using Mobile SDK which represents a digital wallet of a Holder side in the Verifiable Credentials model.

The application provides the base functionality of our [ConnectMe](https://gitlab.com/evernym/mobile/connectme) mobile application but covers the main flows and can be used against [TryConnectMe](https://try.connect.me/#/) demo tutorial.

#### Prerequisite

* Devices
   * Both 32 bit and 64 bit devices
   * Only these architectures are supported
      * arm64, arm7, x86, x86_64
* OS
   * Android 6+ (API version 23+)
* Sponsor registered in Evernym's Cloud Service [see](/docs/3.Initialization.md#sponsor-ie-you-onboarding-with-evernyms-cloud-service).


#### Environment

By default, the applications points to [**Production**](../../../environments/ProductionEnvironment.md) environment.
* Cloud Agent can be changed in the [file](./app/src/main/java/msdk/kotlin/sample/Constants.kt) containing main application settings. 
* Pool Ledger Genesis Transactions can be changed in the [file](./app/src/main/res/raw/genesis.txt)

In order to run the application you **MUST** provide an endpoint ([SERVER_URL](./app/src/main/java/msdk/kotlin/sample/Constants.kt)) of your Sponsor Server which application will call to generate a provisioning tokens.\
A minimal Sponsor Server sample can be found [here](/examples/simple-sponsor).
