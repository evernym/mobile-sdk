package test.java.Tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.appium.java_client.android.AndroidDriver;
import org.json.JSONObject;
import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import appModules.AppInjector;
import test.java.appModules.AppUtils;
import test.java.appModules.VASApi;
import test.java.funcModules.ConnectionModules;
import test.java.utility.IntSetup;
import test.java.utility.AppDriver;
import test.java.utility.BrowserDriver;
import test.java.pageObjects.HomePage;
import test.java.pageObjects.MenuPage;
import test.java.pageObjects.CredentialPage;
import test.java.utility.Constants;
import test.java.utility.LocalContext;

import java.time.Duration;


public class DummyTest extends IntSetup {
    Injector injector = Guice.createInjector(new AppInjector());
    private AppUtils objAppUtlis = injector.getInstance(AppUtils.class);
    private ConnectionModules objConnectionModules = injector.getInstance(ConnectionModules.class);
    private HomePage homePage = injector.getInstance(HomePage.class);
    private MenuPage menuPage = injector.getInstance(MenuPage.class);
    private CredentialPage credentialPage = injector.getInstance(CredentialPage.class);

    private VASApi VAS = VASApi.getInstance();
    private LocalContext context = LocalContext.getInstance();

    private void validateCredentialView(String header, String title, String credentialName, JSONObject values) throws Exception {
        credentialPage.header(driverApp, header).isDisplayed();
        credentialPage.title(driverApp, title).isDisplayed();
//        credentialPage.credentialSenderName(driverApp, connectionName).isDisplayed();
        credentialPage.credentialSenderLogo(driverApp).isDisplayed();
        credentialPage.credentialName(driverApp, credentialName).isDisplayed();

        for (String attribute : values.keySet()) {
            if (attribute.contains("_link")) { // attachment case
                attribute = attribute.replace("_link", "");
                try {
                    credentialPage.credentialAttributeName(driverApp, attribute).isDisplayed();
                } catch (Exception e) {
                    AppUtils.pullScreenUp(driverApp);
                    credentialPage.credentialAttributeName(driverApp, attribute).isDisplayed();
                }
            } else {
                try {
                    credentialPage.credentialAttributeName(driverApp, attribute).isDisplayed();
                    credentialPage.credentialAttributeValue(driverApp, values.getString(attribute)).isDisplayed();
                } catch (Exception e) {
                    AppUtils.pullScreenUp(driverApp);
                    credentialPage.credentialAttributeName(driverApp, attribute).isDisplayed();
                    credentialPage.credentialAttributeValue(driverApp, values.getString(attribute)).isDisplayed();
                }
            }
        }
    }

    @BeforeClass
    public void BeforeClassSetup() throws Exception {
        driverApp = AppDriver.getDriver();
        driverBrowser = BrowserDriver.getDriver();
//        driverApp.launchApp();
    }

//    @Test(enabled = false)
//    public void dummyCase1() throws Exception {
//        homePage.burgerMenuButton(driverApp).click();
//        menuPage.myCredentialsButton(driverApp).click();
//        Thread.sleep(1000);
//        AppUtils.pullScreenUp(driverApp);
//        AppUtils.pullScreenDown(driverApp);
//    }
//
//    @Test(enabled = false)
//    public void dummyCase2() throws Exception {
//        final JSONObject values = new JSONObject()
//                .put("Photo_link", Constants.valuesAttachment.getString("Attachment_link"))
//                .put("PDF_link", Constants.valuesAttachment2.getString("Attachment_link"))
//                .put("DOCX_link", Constants.valuesAttachment3.getString("Attachment_link"))
//                .put("CSV_link", Constants.valuesAttachment4.getString("Attachment_link"));
//
//        homePage.newMessage(driverApp).click();
////        Thread.sleep(1000);
////        AppUtils.pullScreenUp(driverApp);
//
//        validateCredentialView("Credential Offer", "Issued by", "kqufstlwra", values);
//
//    }

    @Test(enabled = true)
    public void dummyCase3() throws Exception {
        String connectionName = test.java.utility.Helpers.randomString();
        String credentialName = test.java.utility.Helpers.randomString();
        String invitationType = "connection-invitation";
        String header = "Credential Offer";

//        objAppUtlis.openApp(driverApp); // not needed
//
//        AppUtils.DoSomethingEventually(
//                () -> objConnectionModules.getConnectionInvitation(driverBrowser, driverApp, connectionName, invitationType),
//                () -> objConnectionModules.acceptConnectionInvitation(driverApp)
//        );
//
//        // wait until connection get completed
//        AppUtils.waitForElement(driverApp, () -> homePage.connectedEvent(driverApp, connectionName)).isDisplayed();
//        Thread.sleep(2000);

        driverBrowser.closeApp(); // case 1: app is closed
//        driverBrowser.runAppInBackground(Duration.ofSeconds(-1)); // case 2: app is in background
//        driverApp.closeApp();


        Thread.sleep(10000);

//        VAS.sendCredentialOffer(context.getValue("DID"), "PMzJsfuq4YYPAKHLSrdP4Q:3:CL:185320:tag", Constants.values, credentialName);
        VAS.sendCredentialOffer("CXP1jQnW8XBuiA6nUkNfrr", "PMzJsfuq4YYPAKHLSrdP4Q:3:CL:185320:tag", Constants.values, credentialName);

        Thread.sleep(7000);

        ((AndroidDriver) driverApp).openNotifications();
        homePage.credentialOfferNotification(driverApp).click();

        objAppUtlis.unlockApp(driverApp);
//        homePage.newMessage(driverApp).click(); //not needed?

        objAppUtlis.acceptCredential(driverApp);

        homePage.recentEventsSection(driverApp).isDisplayed();

        AppUtils.waitForElement(driverApp, () -> homePage.credentialIssuedEvent(driverApp, credentialName)).isDisplayed();
    }

    @AfterClass
    public void AfterClass() {
//        driverApp.closeApp();
    }
}
