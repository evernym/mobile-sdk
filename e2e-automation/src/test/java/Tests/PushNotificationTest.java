package test.java.Tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.NoSuchElementException;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import appModules.AppInjector;
import test.java.appModules.AppUtils;
import test.java.appModules.VASApi;
import test.java.utility.IntSetup;
import test.java.funcModules.ConnectionModules;
import test.java.pageObjects.ConnectionHistoryPage;
import test.java.pageObjects.ConnectionDetailPage;
import test.java.utility.Helpers;
import test.java.utility.RetryAnalyzer;
import test.java.utility.LocalContext;
import test.java.utility.AppDriver;
import test.java.utility.BrowserDriver;
import test.java.pageObjects.HomePage;
import test.java.pageObjects.MenuPage;
import test.java.pageObjects.MyConnectionsPage;
import test.java.pageObjects.CredentialPage;
import test.java.utility.Constants;
import test.java.utility.Config;

import java.time.Duration;


public class PushNotificationTest extends IntSetup {
    Injector injector = Guice.createInjector(new AppInjector());
    private AppUtils objAppUtlis = injector.getInstance(AppUtils.class);
    private ConnectionModules objConnectionModules = injector.getInstance(ConnectionModules.class);
    private HomePage homePage = injector.getInstance(HomePage.class);
    private MenuPage menuPage = injector.getInstance(MenuPage.class);
    private CredentialPage credentialPage = injector.getInstance(CredentialPage.class);

    private VASApi VAS = VASApi.getInstance();
    private LocalContext context = LocalContext.getInstance();

    private final String appClosed = "closed";
    private final String appBackground = "background";

    String connectionName = Helpers.randomString();
    String invitationType = "connection-invitation";

    @BeforeClass
    public void BeforeClassSetup() throws Exception {
        System.out.println("Push Notification Test Suite has been started!");
        driverApp = AppDriver.getDriver();
        driverBrowser = BrowserDriver.getDriver();

        if ((Config.Device_Type.equals("iOS") || Config.Device_Type.equals("awsiOS"))) {
            return;
        }

        AppUtils.DoSomethingEventually(
                () -> objConnectionModules.getConnectionInvitation(driverBrowser, driverApp, connectionName, invitationType),
                () -> objConnectionModules.acceptConnectionInvitation(driverApp)
        );

        AppUtils.waitForElement(driverApp, () -> homePage.connectedEvent(driverApp, connectionName)).isDisplayed();
        Thread.sleep(3000);
    }

    @DataProvider(name = "appStates")
    public Object[][] getAppStates() {
        return new Object[][] {
                { appClosed },
                { appBackground },
        };
    }

    // this test doesn't work after connection test!
    @Test(dataProvider = "appStates")
    public void checkNotification(String appState) throws Exception {
        if ((Config.Device_Type.equals("iOS") || Config.Device_Type.equals("awsiOS"))) {
            return;
        }

        String credentialName = Helpers.randomString();

        // close app or put it to background
        switch(appState) {
            case appClosed:
                driverBrowser.closeApp(); // case 1: app is closed
//                driverApp.closeApp();
                break;
            case appBackground:
                driverBrowser.runAppInBackground(Duration.ofSeconds(-1)); // case 2: app is in background
//                driverApp.runAppInBackground(Duration.ofSeconds(-1));
                break;
        }

        Thread.sleep(10000);
        VAS.sendCredentialOffer(context.getValue("DID"), "PMzJsfuq4YYPAKHLSrdP4Q:3:CL:185320:tag", Constants.values, credentialName);
        Thread.sleep(7000);

        ((AndroidDriver) driverApp).openNotifications();

        // TODO: try to sleep while `isDisplayed == false` here

        homePage.credentialOfferNotification(driverApp).click();

        try {
            objAppUtlis.unlockApp(driverApp);
        } catch (Exception ex) {
            System.out.println("Unlocking is not needed here!");
        }

        try {
            homePage.newMessage(driverApp).click(); // not needed? intermittent case
        } catch (Exception ex) {
            System.out.println("New message tapping is not needed here!");
        }

        objAppUtlis.acceptCredential(driverApp);

        homePage.recentEventsSection(driverApp).isDisplayed();

        AppUtils.waitForElement(driverApp, () -> homePage.credentialIssuedEvent(driverApp, credentialName)).isDisplayed();
    }

    @AfterClass
    public void AfterClass() {
        BrowserDriver.closeApp();
        driverApp.closeApp();
        System.out.println("Push Notification Test Suite has been finished!");
    }
}
