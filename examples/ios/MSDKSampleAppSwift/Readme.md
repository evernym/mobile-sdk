# Mobile SDK Swift iOS Sample

This is a simple iOS mobile application written in Swift using Mobile SDK which represents a digital wallet of a Holder side in the Verifiable Credentials model.

The application provides the base functionality of our [ConnectMe](https://gitlab.com/evernym/mobile/connectme) mobile application but covers the main flows and can be used against [TryConnectMe](https://try.connect.me/#/) demo tutorial.

#### Prerequisite

* Devices
    * 64 Bit devices only
    * Only these architectures are supported
        * arm64, arm7, x86, x86_64

      **Note:** Depending on the architecture, you should choose the version
        * `vcx 0.0.227` for phones
        * `vcx 0.0.228` for simulators

* OS
    * iOS 10+
* Sponsor registered in Evernym's Cloud Service [see](/docs/3.Initialization.md#sponsor-ie-you-onboarding-with-evernyms-cloud-service).

#### Environment

By default, the applications points to [**Production**](../../../environments/ProductionEnvironment.md) environment.
* Cloud Agent and Pool Ledger Genesis Transactions can be changed in the [file](../common/Config.m) containing main application settings.

In order to run the application you **MUST** provide an endpoint ([**sponsorServerURL**](../common/Config.m)) of your Sponsor Server which application will call to generate a provisioning tokens.\
A minimal Sponsor Server sample can be found [here](/examples/simple-sponsor).
