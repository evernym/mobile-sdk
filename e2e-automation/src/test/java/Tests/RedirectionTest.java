package test.java.Tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.openqa.selenium.NoSuchElementException;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import appModules.AppInjector;
import test.java.appModules.AppUtils;
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
import test.java.utility.Config;

import java.time.Duration;


public class RedirectionTest extends IntSetup {
    Injector injector = Guice.createInjector(new AppInjector());

    private AppUtils objAppUtils = injector.getInstance(AppUtils.class);
    private ConnectionModules objConnectionModules = injector.getInstance(ConnectionModules.class);
    private HomePage homePage = injector.getInstance(HomePage.class);
    private LocalContext context = LocalContext.getInstance();

    private String connectionInvitationLink;
    private String oobInvitationLink;
    private final String appClosed = "closed";
    private final String appBackground = "background";

    @BeforeClass
    public void BeforeClassSetup() {
        System.out.println("Redirection Test Suite has been started!");
        driverApp = AppDriver.getDriver();
        connectionInvitationLink = context.getValue("connection-invitation");
        oobInvitationLink = context.getValue("out-of-band-invitation");
    }

    // connection-invitation | oob-invitation
    // app is closed | app is running in background
    // same invitation | different invitation with the same public DID - how can I change it?

    @DataProvider(name = "invitationLinksAndAppStates")
    public Object[][] getInvitationLinksAndAppStates() {
        return new Object[][] {
                { connectionInvitationLink, appClosed },
                { oobInvitationLink, appClosed },
                { connectionInvitationLink, appBackground },
                { oobInvitationLink, appBackground },
        };
    }

    @Test(dataProvider = "invitationLinksAndAppStates")
    public void redirectConnection(String link, String appState) throws Exception {
        if ((Config.Device_Type.equals("iOS") || Config.Device_Type.equals("awsiOS"))) {
            return;
        }

        driverBrowser = BrowserDriver.getDriver();
//        driverApp.close(); // test ios

        // close app or put it to background
        switch(appState) {
            case appClosed:
//                driverApp.close(); // 404 error android - it is closed
//                driverBrowser.close(); // ?
                break;
            case appBackground:
                driverApp.runAppInBackground(Duration.ofSeconds(-1));
                break;
        }

        // open deep link
        objConnectionModules.openDeepLink(driverBrowser, driverApp, link);

        // check conditions
        homePage.homeHeader(driverApp).isDisplayed();

        BrowserDriver.closeApp();
        driverApp.closeApp();
    }

    @AfterClass
    public void AfterClass() {
        System.out.println("Redirection Test Suite has been finished!");
    }
}
