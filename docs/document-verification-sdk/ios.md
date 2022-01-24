# iOS

## Integration

- Copy `ios/Frameworks/MIDSAssistSDK.xcframework` and `ios/Frameworks/MIDSVerificationSDK.xcframework` from [react-native-white-label](https://gitlab.com/evernym/mobile/react-native-white-label-app) repo
- Add these frameworks as embedded framework from Xcode
- In your Podfile, ensure that you are using `use_frameworks!`
- Add below dependencies in your Podfile

  ```ruby
  pod 'JumioMobileSDK', '~>3.9.0' # Use Netverify, Authentication, Document Verification and BAM Checkout together in your app
  pod 'JumioMobileSDK/Netverify', '~>3.9.0' # Use full Netverify and Authentication functionality
  pod 'JumioMobileSDK/NetverifyBase', '~>3.9.0' # For Fastfill, Netverify basic functionality
  pod 'JumioMobileSDK/NetverifyNFC', '~>3.9.0' # For Fastfill, Netverify functionality with NFC extraction
  pod 'JumioMobileSDK/NetverifyBarcode', '~>3.9.0' # For Fastfill, Netverify functionality with barcode extraction
  pod 'JumioMobileSDK/NetverifyFace+iProov', '~>3.9.0' # For Fastfill, Netverify functionality with Identity Verification, Authentication
  pod 'JumioMobileSDK/NetverifyFace+Zoom', '~>3.9.0' # For Fastfill, Netverify functionality with Identity Verification, Authentication
  pod 'JumioMobileSDK/DocumentVerification', '~>3.9.0' # Use Document Verification functionality
  pod 'JumioMobileSDK/BAMCheckout', '~>3.9.0' # Use BAM Checkout functionality
  pod 'JumioMobileSDK/NetverifyFace+iProov', `~>3.9.0` # For Fastfill, Netverify functionality with Identity Verification, Authentication
  ```

## Get SDK Token

  <details>
    <summary>See code</summary>

```swift

let demoVerityFlowBaseUrl = 
  "https://simple-verifier-backend.pps.evernym.com/Prod/issuer-service/get-mc-sdk-token"
let prodVerityFlowBaseUrl =
    "https://simple-verifier-backend.evernym.com/Prod/issuer-service/get-mc-sdk-token"
    
// Create a URLRequest for an API endpoint
let url = URL(string: demoVerityFlowBaseUrl)!
var request = URLRequest(url: url)

// Configure request authentication
request.setValue(
    "Token token=<add-device-check-jwt-token-here>", 
    forHTTPHeaderField: "Authorization"
)
request.setValue(
    "Put your domainDID that has been approved by Evernym", 
    forHTTPHeaderField: "domainDID"
)

// empty body data
let bodyData = "{}"
request.httpMethod = "POST"
request.httpBody = bodyData

// Create the HTTP request
let session = URLSession.shared
let task = session.dataTask(with: request) { (data, response, error) in

    if let error = error {
      // Handle HTTP request error
    } else if let data = data {
      // Handle HTTP request response
      // We get JSON response in above call
      // the format is as below
      {
        result: "{"sdkToken":"<sometokenvalue>","apiDataCenter":"SG"}"
      }
    } else {
      // Handle unexpected error
    }
}

// start HTTP request

task.resume()

```

  </details>

### Handle errors while getting sdk token

In case API call fails, we get 500 as status code and below in response body

```javascript
  {
    body: '{"errorMessage":"{\"error\":\"{\"data\":\"{\"Errors\":[]}\"}\",\"message\":\"error messages\"}"}'
  }
```

## Init SDK with token

Once we have the token from above API call. We can now init the SDK on app side.

<details>
  <summary></summary>

```swift
  import MIDSAssistSDK
  
  class MIDSDocumentVerification: NSObject {
    static var enrollmentManagerInstance: MIDSEnrollmentManager!
    static var enrollmentManager: MIDSEnrollmentManager = getEnrollmentManagerInstance()
    var currentScanView: MIDSCustomScanViewController?
    var verifyInfoView: ConfirmScannedView!
    var loader: LoaderView!
      
    static func getEnrollmentManagerInstance() -> MIDSEnrollmentManager {
        if enrollmentManagerInstance == nil {
            enrollmentManagerInstance = MIDSEnrollmentManager.shared()
        }
        return enrollmentManagerInstance
    }
    
    func initMIDSSDK(_ token: String,
                    withDataCenter dataCenter: String,) -> Void {
        DispatchQueue.main.async {
          // we will add delegate methods to soon in below section as per need
          MIDSDocumentVerification.enrollmentManager.enrollmentDelegate = self
          let dataCenter = self.getDataCenter(dataCenter: dataCenter)
          MIDSDocumentVerification.enrollmentManager.initializeMIDSVerifySDK(sdkToken: token, dataCenter: dataCenter)
        }
    }

    func getDataCenter(dataCenter: String) -> MIDSDataCenter {
      switch dataCenter {
      case "SG":
        return .MIDSDataCenterSG;
      case "US":
        return .MIDSDataCenterUS;
      case "EU":
        return .MIDSDataCenterEU
      default:
        return .MIDSDataCenterSG;
      }
    }
    
  }

```

</details>

### Handle success or error of SDK init

Once we have called the SDK method. The success or error are sent to delegate attached on the class. These delegates are need to be created by us.

<details>
  <summary>See code</summary>

```swift
extension MIDSDocumentVerification: MIDSEnrollmentDelegate {
  
  func midsEnrollmentManager(didFinishInitializationSuccess status: Bool) {
    // Initialization success
  }
  
  func midsEnrollmentManager(didFinishInitializationWithError error: MIDSVerifyError) {
      handleMIDSError(error: error)
  }
  
  func midsEnrollmentManager(didDetermineNextScanViewController scanViewController: MIDSCustomScanViewController, isFallback: Bool) {
    // TODO
  }
    
  func resetScanner(_ sender:UIButton!) {
      currentScanView?.dismiss(animated: true)
  }

  func midsEnrollmentManager(didStartBiometricAnalysis scanViewController: MIDSCustomScanViewController) {}
  
  func midsEnrollmentManager(customScanViewControllerWillPresentIProovController scanViewController: MIDSCustomScanViewController) {}
  
  func midsEnrollmentManager(customScanViewControllerWillPrepareIProovController scanViewController: MIDSCustomScanViewController) {
    // TODO
  }
  
  func midsEnrollmentManager(didCaptureAllParts status: Bool) {
    currentScanView = nil
  }
}

```

</details>

## Get countries list

Once we know that initialization was success. We can get the supported countries list to show to the user.

```swift
func getCountryList(_ resolve: @escaping RCTResponseSenderBlock,
                            rejecter reject: @escaping RCTResponseSenderBlock) -> Void {
    let countryList = MIDSDocumentVerification.enrollmentManager.getCountryList()
    var countries = [String: String]()
    for country in countryList {
      if let countryName = country.countryName, let countryCode = country.countryCode {
        countries[countryName] = countryCode
      }
    }

    resolve([countries])
  }
```

## Get Document as per selected country

Once we show to the user the selected country. And user selects a country. We need to get a list of available documents to scan for this particular country.

```swift
func getDocumentTypes(_ countryCode: String) -> Void {
    let documentTypes = MIDSDocumentVerification.enrollmentManager.getDocumentTypes(countryCode: countryCode)
  }
```

### Handle empty document list per selected country

There can be a case where there is no supported document for that country to scan. Please show appropriate error to the user.

```swift
func startMIDSSDKScan(_ documentType: String,
                              policyVersion version: String,) -> Void {
    DispatchQueue.main.async {
      MIDSDocumentVerification.enrollmentManager.startScan(document: documentType, privacyPolicyVersion: version, userBiometricConsent: true)
    }
  }
```

## Start scan screen

Now, we should see a scan screen. Since this screen is used to scan document and a document may need to be scanned from both sides. We will use below delegate method to control what happens at each step. Below are the steps that may happen and we can configure scan scan on the basis of our needs.

- Check if we get any error for starting scan screen
- If error in error delegate method, then terminate SDK
- If our `midsEnrollmentManager(didDetermineNextScanViewController` delegate method gets called. This means that start scan screen was successful.
- We present the scanViewController for scanning the document
- Once user scans the document successfully, then our delegate method `midsEnrollmentManager:shouldDisplayConfirmationWith` will be called. Here we need to show a confirmation screen to the user and the scanned document image that will be used to issue credential. Also, we can show user `confirm` and `retry` buttons. We will add details on action handlers for `confirm` and `retry`.
- In some countries for some types of document, we may need to scan both sides of the document. We need to handle this case in our delegate method `midsEnrollmentManager:didDetermineNextScanViewController`
- Once the document scan is successful, we need to show a `Loader` view
- Now, we will wait for face scan delegate to be called to hide `Loader` view and show `face scan` view controller

### Configure SDK for different events on scan screen

The below file contains detail implementation of above delegate methods and handling

<details>
  <summary>See code for delegate handlers</summary>

```swift
extension MIDSDocumentVerification: MIDSEnrollmentDelegate {
  
  func midsEnrollmentManager(scanViewController: MIDSCustomScanViewController, shouldDisplayNoUSAddressFoundHint message: String, confirmation: @escaping () -> Void) {
      NSLog("no US address")
  }

  func midsEnrollmentManager(didFinishInitializationSuccess status: Bool) {
    
  }
  
  func midsEnrollmentManager(didFinishInitializationWithError error: MIDSVerifyError) {
      handleMIDSError(error: error)
  }
  
  func midsEnrollmentManager(didDetermineNextScanViewController scanViewController: MIDSCustomScanViewController, isFallback: Bool) {
    self.currentScanView = scanViewController
    
    scanViewController.modalPresentationStyle = .fullScreen
    scanViewController.customScanViewController?.modalPresentationStyle = .fullScreen

    let myButton = UIButton(type: .roundedRect)
    // Position Button
    myButton.frame = CGRect(x: (UIApplication.shared.keyWindow?.bounds.width)! - 100, y: 60, width: 100, height: 50)
    // Set text on button
    myButton.setTitle("×", for: .normal)
    myButton.setTitleColor(UIColor.gray, for: .normal)
    myButton.titleLabel?.font = UIFont.boldSystemFont(ofSize: 50)
    // Set button background color
    myButton.backgroundColor = UIColor.white.withAlphaComponent(0)

    // Set button action
    myButton.addTarget(self, action: #selector(resetScanner(_:)), for: UIControl.Event.touchUpInside)

    scanViewController.customScanViewController?.view.addSubview(myButton)

    if  scanViewController.customScanViewController?.currentScanMode() == .faceCapture || scanViewController.customScanViewController?.currentScanMode() == .faceIProov {
      UIApplication.shared.windows.first?.rootViewController?.dismiss(animated: true, completion:{ () -> Void in
        UIApplication.shared.windows.first?.rootViewController?.present(scanViewController, animated: true)
      })
        
      return
    }

    UIApplication.shared.windows.first?.rootViewController?.dismiss(animated: false)
    UIApplication.shared.windows.first?.rootViewController?.present(scanViewController, animated: false)
  }
    
    func resetScanner(_ sender:UIButton!) {
        currentScanView?.dismiss(animated: true, completion: {
          
        })
    }
    
  func midsEnrollmentManager(didCancelWithError error: MIDSVerifyError) {
    handleMIDSError(error: error)
  }

  func midsEnrollmentManager(scanViewController: MIDSCustomScanViewController, shouldDisplayHelpWithText message: String, animationView: UIView) {
    scanViewController.customScanViewController?.retryScan()
  }
  
  func midsEnrollmentManager(shouldDisplayConfirmationWith view: UIView, text: String, currentStep: Int, totalSteps: Int, retryEnabled: Bool, confirmEnabled: Bool, confirmation: (() -> Void)?, retake: (() -> Void)?) {
    
    if confirmEnabled {
        if (verifyInfoView != nil){
            self.verifyInfoView.removeFromSuperview()
        }

        verifyInfoView = ConfirmScannedView()
        verifyInfoView.inflate()
        
        if let frame = currentScanView?.view.bounds {
            view.frame = CGRect(x: 0,
                                y: 0,
                                width: frame.size.width * 0.9,
                                height: frame.size.height * 0.4)
        }
        view.center = verifyInfoView.getView().center
        verifyInfoView.getView().addSubview(view)
        
        verifyInfoView.addConfirmationHandler(action: confirmation, confirm: confirmEnabled)
        verifyInfoView.addRetakeHandler(action: retake, retake: retryEnabled)

        let myButton = UIButton(type: .roundedRect)
        myButton.frame = CGRect(x: (UIApplication.shared.keyWindow?.bounds.width)! - 100, y: 30, width: 100, height: 50)
        myButton.setTitle("×", for: .normal)
        myButton.setTitleColor(UIColor.gray, for: .normal)
        myButton.titleLabel?.font = UIFont.boldSystemFont(ofSize: 50)
        myButton.backgroundColor = UIColor.white.withAlphaComponent(0)
        myButton.addTarget(self, action: #selector(resetScanner(_:)), for: UIControl.Event.touchUpInside)
        verifyInfoView.getView().addSubview(myButton)
        
        currentScanView?.view.addSubview(verifyInfoView.getView())
    }
  }
    
  func midsEnrollmentManager(didStartBiometricAnalysis scanViewController: MIDSCustomScanViewController) {}
  
  func midsEnrollmentManager(customScanViewControllerWillPresentIProovController scanViewController: MIDSCustomScanViewController) {}
  
  func midsEnrollmentManager(customScanViewControllerWillPrepareIProovController scanViewController: MIDSCustomScanViewController) {
    DispatchQueue.main.async {
        guard let appDelegate = UIApplication.shared.delegate,
            let window = appDelegate.window else {
            return
        }

        self.loader = LoaderView()
        self.loader.inflate()

        if let view = scanViewController.customOverlayLayer {
            view.addSubview(self.loader.getView())
        } else {
            window?.addSubview(self.loader.getView())
        }
    }
  }
  
  func midsEnrollmentManager(didCaptureAllParts status: Bool) {
    currentScanView = nil
  }
}

```

</details>

## Get workflow ID after successful document scan

Once the process for scan is finished. The document is being processed and SDK returns the workflow ID that can be used to get the credential.

```swift
  // Add this delegate method in same extension as above
  func midsEnrollmentManager(didFinishScanningWith reference: String, accountID: String?, authenticationResult: Bool?)  {
    UIApplication.shared.windows.first?.rootViewController?.dismiss(animated: true, completion: {
        MIDSDocumentVerification.enrollmentManager.terminateSDK()
    })
  }

```

## Make a connection with server

- First we need to make connection with Evernym server. This process is same as defined in [Connections](./../5.Connections.md) documentation.
- Below is the detail for the API that can be used to download connection invitation from Evernym server without user interaction. This step can be done before starting the SDK or in the background to avoid spending time establishing connection after user is done scanning. If we establish connection in background, then user only to needs to wait while we send workflowID to Evernym server.

```swift
  let demoVerityFlowBaseUrl =
  "https://simple-verifier-backend.pps.evernym.com/Prod/issuer-service/create-invitation"
  String prodVerityFlowBaseUrl =
  "https://simple-verifier-backend.evernym.com/Prod/issuer-service/create-invitation"
  
  // Send post request to one of the above URL
  // The headers needs to be the same as we sent in API call to get SDK token, refer above steps

  // Handle invitation and establish connection as described in Connections documentation

```

## Send workflow ID to server to get credential for the scanned document

Once the connection is established, now we can send the workflow ID to server and get the credential issued. Before sending the workflow ID, we need to make sure that we have created a credDefId for the credential that we want to issue to our users. Follow verity guidelines to get domainDID, and creating credential definitions.

Below code assumes that we know credDefIds.

```swift

  let demoVerityFlowBaseUrl =
  "https://simple-verifier-backend.pps.evernym.com/Prod/issuer-service/issue-credential"
  String prodVerityFlowBaseUrl =
  "https://simple-verifier-backend.evernym.com/Prod/issuer-service/issue-credential"
  
  // Send post request to one of the above URL
  // The headers needs to be the same as we sent in API call to get SDK token, refer above steps

  // In previous requests, our body was empty JSON "{}". For this API call, we need below JSON as body

  JSONSerialization.data(
    workflowId,
    connectionDID,
    country,
    // document name string "PASSPORT" | "DRIVING_LICENSE" | "IDENTITY_CARD" | "VISA"
    document,
    credDefId,
    // this is the device check token
    hardwareToken,
    platform: "android",
  })

```

### Handle credential offer message

Once above API call is successful. We can check for credential offer message as described in [Messages](./../4.MessagesFlow.md).
Once we get credential offer message, then we can accept the credential offer and show to user as described in [Credentials](./../6.Credentials.md).
