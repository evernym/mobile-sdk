# VCX library framework 

If you want to use iOS implementation as your starting point, easiest solution to integrate VCX library into your existing project or new one is to use [Cocoapods](https://cocoapods.org).

Since our VCX library is still not publicly available, you will need to use our private repo which can be downloaded here: https://github.com/evernym/mobile-sdk/releases 

1. Downloading one of the files: 
    
    - vcx.libvcxall_[version number]_universal.zip or 
    - vcx.libvcxpartial_[version number]_universal.zip

2. Unzip and copy content of the file from above to **vcx** folder in root of mobile SDK. 

3. Go to examples/iOS/CMeSdkObjc or examples/iOS/CMeSdkSwift and from terminal run: 
`pod install --repo-update`.

4. When script completes, your Xcode workspace (CMeSdkSwift.xcworkspace) should contain Vcx cocapod library in this place inside your Xcode (in Project Navigator list on left, ⌘1): 
   
   ```
   - Pods
     - Development Pods
       - vcx
         - Frameworks
         - Pod
         - Support Files

![Pod structure in Xcode](/wiki-images/pod-structure.png)


> **Note**: 

You can change path to the vcx folder to better suite your needs. In that case, you need to update path in 3 places: 

- vcx.podspec path in your Podfile in xcode project folder (currently is `pod 'vcx', :path => '../../../vcx.podspec'`)
- Move vcx.pospec to the folder where you pointed in Podifle
- if you want to use new versions of VCX library, update path to the file and version on MobileSDK server in vcx.podspec, in the root of the MobileSDK folder (currently is `0.0.165` and path is `s.source = { :git => 'git@github.com:evernym/VCX-Cocoapods.git' }`)
- Move vcx folder (with downloaded library) to the same folder where your vcx.podspec is moved
