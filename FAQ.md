<details>
  <summary>What is CAS?</summary>

  CAS stands for Consumer Agency Service. This service is hosted by Evernym. It creates and hosts cloud agents for individual app installs. Evernym's production CAS is hosted at `https://agency.evernym.com`. To communicate with CAS, developers would also need to know CAS's `DID` and `verification key`. To know `DID` and `verification key` of Evernym's CAS or EAS, developers can make a GET API call to `https://agency.evernym.com/agency/`.
</details>

<details>
  <summary>What is EAS?</summary>

  Enterprise Agency Service (EAS) hosted by Evernym is responsible for creating and hosting cloud agent for Enterprise wallets. Mobile SDK, almost always interact with CAS, not EAS.
</details>

<details>
  <summary>What is the difference between Evernym Agency Service and the cloud agent?</summary>

  Evernym's Agency Service creates and hosts individual cloud agents for each unique app install of Connect.Me, or each instance of our mobile SDK.
  Cloud agents primary value is for storing and forwarding messages, while the edge agent (the app on the phone) comes on and offline. It also provides push notification services.
</details>

<details>
  <summary>Can I use mobile SDK in Xamarin?</summary>

  Yes, mobile SDK can be used in Xamarin. However, Evernym does not provide .NET wrapper for mobile SDK. Mobile SDK contains native Android and iOS wrappers. These native Android and iOS wrappers can be used in Xamarin in same way as other native plugin/modules of Android and iOS are used.
</details>

<details>
  <summary>Can I use mobile SDK in Flutter?</summary>

  Yes, mobile SDK can be used in Flutter. However, mobile SDK does not contain flutter bindings for mobile SDK. Mobile SDK can be integrated in Flutter as other native Android and iOS plugins are used.
</details>

<details>
  <summary>How to get started with mobile SDK?</summary>

- Get access to mobile SDK. It should be available to download from `release tab`
- Familiarize yourself with [basic concepts](./0.Base%20Concepts.md)
- Integrate mobile SDK in your app using this [guide](./1.ProjectSetup.md)
- Run through other markdown files from #2 to #9

</details>

<details>
  <summary>How to send a credential with an image/photo?</summary>

Check this [guide](./9.%20CredentialsWithAttachments.md)
</details>

<details>
  <summary>How to send a credential with an attachment?</summary>

Check this [guide](./9.%20CredentialsWithAttachments.md)
</details>

<details>
  <summary>How to send a message with custom action?</summary>

Check this [guide](./6.StructuredMessages.md)
</details>
