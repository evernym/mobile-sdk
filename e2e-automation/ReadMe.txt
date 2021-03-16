Macos test automation setup:

0. Installation steps:
1. JDK 8
2. Android SDK
3. Android Studio 3.1.4 or higher
4. Xcode 10.0
5. Node.js 8.6.0
6. Appium 1.15.1 or higher

7. Steps for Appium:
7.0. Create Android emulator
7.1. Start Android emulator
7.2. Make sure that Appium's chromedriver version matches Chrome's version
7.3. Start Appium server
7.4. `cd ConnectMe/e2e-automation`
7.5. Run tests: `mvn test`

8. Steps for Detox:
8.0. Create iOS simulator or use default one
8.1. Start iOS simulator
8.2. `cd ConnectMe/e2e`
8.3. `brew update`
8.4. `brew tap wix/brew && brew install applesimutils`
8.5. `brew install imagemagick && brew install GraphicsMagick`
8.6. `yarn global add detox-cli`
8.7. `yarn pod:dev:install`
8.8. Run tests: `yarn e2e -b debug -e qa -t home`

`.bash_profile` additions:
export JAVA_HOME=/Library/Java/JavaVirtualMachines/%JDK_FOLDER%/Contents/Home
export ANDROID_HOME=/Users/%USER_NAME%/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
export PATH="/usr/local/bin:/usr/local/sbin:$PATH"