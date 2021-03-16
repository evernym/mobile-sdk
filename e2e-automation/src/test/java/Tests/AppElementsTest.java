package test.java.Tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import appModules.AppInjector;
import test.java.appModules.AppUtils;
import test.java.utility.Config;
import test.java.utility.IntSetup;
import test.java.appModules.AppiumUtils;
import test.java.utility.AppDriver;

public class AppElementsTest extends IntSetup {
    Injector injector = Guice.createInjector(new AppInjector());

    private AppUtils objAppUtils = injector.getInstance(AppUtils.class);
    private test.java.pageObjects.HomePage objHomePage = injector.getInstance(test.java.pageObjects.HomePage.class);
    private test.java.pageObjects.MenuPage objMenuPage = injector.getInstance(test.java.pageObjects.MenuPage.class);
    private test.java.pageObjects.MyConnectionsPage objConnectionsPage = injector.getInstance(test.java.pageObjects.MyConnectionsPage.class);
    private test.java.pageObjects.MyCredentialsPage objCredentialsPage = injector.getInstance(test.java.pageObjects.MyCredentialsPage.class);
    private test.java.pageObjects.SettingsPage objSettingsPage = injector.getInstance(test.java.pageObjects.SettingsPage.class);
    private test.java.pageObjects.BiometricsPage objBiometricsPage = injector.getInstance(test.java.pageObjects.BiometricsPage.class);
    private test.java.pageObjects.PasscodePage objPasscodePage = injector.getInstance(test.java.pageObjects.PasscodePage.class);
    private test.java.pageObjects.ChatPage objChatPage = injector.getInstance(test.java.pageObjects.ChatPage.class);
    private test.java.pageObjects.AboutPage objAboutPage = injector.getInstance(test.java.pageObjects.AboutPage.class);
    private test.java.pageObjects.OnfidoPage objOnfidoPage = injector.getInstance(test.java.pageObjects.OnfidoPage.class);
    private test.java.pageObjects.QrScannerPage qrScannerPage = injector.getInstance(test.java.pageObjects.QrScannerPage.class);

    @BeforeClass
    public void BeforeClassSetup() throws Exception {
        driverApp = AppDriver.getDriver();
        objAppUtils.openApp(driverApp);
    }

    @Test
    public void checkHome() throws Exception {
        // Home
        objHomePage.homeHeader(driverApp).isDisplayed();
        objHomePage.burgerMenuButton(driverApp).isDisplayed();
        objHomePage.scanButton(driverApp).isDisplayed();
    }

    @Test(dependsOnMethods = "checkHome")
    public void checkMenu() throws Exception {
        objHomePage.burgerMenuButton(driverApp).click(); // go to Menu
        objMenuPage.menuContainer(driverApp).isDisplayed();
        objMenuPage.connectMeBanner(driverApp).isDisplayed();
        objMenuPage.connectMeLogo(driverApp).isDisplayed();
        objMenuPage.builtByFooter(driverApp).isDisplayed();
        objMenuPage.versionFooter(driverApp).isDisplayed();

        // Avatar
        objMenuPage.userAvatar(driverApp).click();
        try {
            objMenuPage.okButton(driverApp).click();
            objMenuPage.menuAllowButton(driverApp).click();
        }
        catch (NoSuchElementException e) {
            System.out.println("Permissions already have been granted!");
        }
        finally {
            Thread.sleep(1000);
            // FIXME
            if((Config.Device_Type.equals("android")||Config.Device_Type.equals("awsAndroid"))) {
                ((AndroidDriver) driverApp).pressKeyCode(AndroidKeyCode.BACK);
            } else {
                AppiumUtils.findElement(
                        driverApp,
                        "//XCUIElementTypeButton[@name=\"Cancel\"]",
                        "Cancel Button"
                ).click();
            }
        }
        objMenuPage.homeButton(driverApp).click();

        // My Connections
        objHomePage.burgerMenuButton(driverApp).click();
        objMenuPage.myConnectionsButton(driverApp).click();
        objConnectionsPage.myConnectionsHeader(driverApp).isDisplayed();
        objHomePage.scanButton(driverApp).isDisplayed();

        // My Credentials
        objHomePage.burgerMenuButton(driverApp).click();
        objMenuPage.myCredentialsButton(driverApp).click();
        objCredentialsPage.myCredentialsHeader(driverApp).isDisplayed();
        objHomePage.scanButton(driverApp).isDisplayed();

        // Settings
        objHomePage.burgerMenuButton(driverApp).click();
        objMenuPage.settingsButton(driverApp).click();

        // Go Back Home
        objHomePage.burgerMenuButton(driverApp).click();
        objMenuPage.homeButton(driverApp).click();

        Thread.sleep(1000);
    }

    @Test(dependsOnMethods = "checkMenu")
    public void checkQrScanner() throws Exception {
        objHomePage.scanButton(driverApp).isDisplayed();
        objHomePage.scanButton(driverApp).click();
        try {
            qrScannerPage.scannerAllowButton(driverApp).click();
        }
        catch (NoSuchElementException e) {
            System.out.println("Permissions already have been granted!");
        }
        finally {
            Thread.sleep(1000);
            qrScannerPage.scannerCloseButton(driverApp).click();
            Thread.sleep(1000);
        }
    }

    @Test(dependsOnMethods = "checkQrScanner")
    public void checkSettings() throws Exception {
        objHomePage.burgerMenuButton(driverApp).click(); // go to Menu
        objMenuPage.settingsButton(driverApp).click(); // go to Settings

        objSettingsPage.settingsContainer(driverApp).isDisplayed();
        objSettingsPage.settingsHeader(driverApp).isDisplayed();

        // Biometrics
        objSettingsPage.biometricsButton(driverApp).click();
        objBiometricsPage.okButton(driverApp).click();

        // Change Passcode
        objSettingsPage.passcodeButton(driverApp).click();
        objPasscodePage.backArrow(driverApp).click();

        // Chat
        objSettingsPage.chatButton(driverApp).click();
        objChatPage.backArrow(driverApp).click();

        // About
        objSettingsPage.aboutButton(driverApp).click();
        objAboutPage.backArrow(driverApp).click();

//        // Onfido
//        objSettingsPage.onfidoButton(driverApp).click();
//        objOnfidoPage.backArrow(driverApp).click();
    }

    @AfterClass
    public void AfterClass() {
        driverApp.closeApp();
    }
}
