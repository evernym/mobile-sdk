<a href="https://www.evernym.com/"><img src="https://pbs.twimg.com/profile_images/1022255393395929088/0eYH-Os__400x400.jpg" title="Evernym" style="width: 150px"></a>

# Connect.Me Mobile SDK

Welcome to the Connect.Me Mobile SDK!

<!--What should go here?> Evernym's mobile sdk repo with links to aar and cocoapod and starter files. --> 

Connecting and exchanging encrypted data between an Agency (server side) and an Edge Client (mobile application) consists of several steps and is supported by a secured, encrypted protocol library (VCX). 

After project setup and adding dependency libraries, perform the the following steps: 

1. [Initialize the mobile SDK](1.ProjectSetup.md) - VCX library in app runtime 
2. [Initialize the wallet](2.InitializingTheWallet.md) and communicate with a specific Agency (web server)
3. [Accept the invitation and establish a connection](3.Connections.md) with the entity  
4. [Accept the offered credential](4.Credentials.md)
5. [Accept and respond to a proof request](5.Proofs.md)
6. [Exchange secured structured messages](6.StructuredMessages.md)
7. [Connection Redirection](7.ConnectionRedirection.md)
   
![Mobile SDK Flow](wiki-images/ConnectMeMobileSDK.png)


<!--To be created in response to customer feedback ## FAQ
 
- **How do I do *specifically* so and so?**
    - No problem! Just do this. -->

### Helpful links
- <a href="https://github.com/evernym/mobile-starter" target="_blank">Mobile Starter Kit</a> &#8212; Current repo of mobile starter kit
- <a href="https://drive.google.com/drive/folders/1-ySuVqU7q79jG2epoVJH4bFU1CqWVGnR?usp=sharing" target="_blank">
    Starter files</a> &#8212; Current location of AAR and Cocoapods
- <a href=" https://docs.google.com/document/d/1HAa27qArYlU0NO1VbEjA8ANXmVHl-b7fxa40e21I5L8/edit" target="_blank">
    Old SDK docs</a>
- <a href=" https://docs.google.com/document/d/1HAa27qArYlU0NO1VbEjA8ANXmVHl-b7fxa40e21I5L8/edit" target="_blank">
    Connector App</a> &#8212; Helpful resource to see how an older version of Connect.Me works
