package test.java.Tests;

import appModules.AppInjector;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.java.utility.AppDriver;
import test.java.appModules.AppUtils;
import test.java.utility.IntSetup;
import test.java.appModules.VASApi;
import test.java.pageObjects.HomePage;
import test.java.pageObjects.MenuPage;
import test.java.pageObjects.MyConnectionsPage;
import test.java.pageObjects.MyCredentialsPage;
import test.java.pageObjects.CredentialPage;
import test.java.pageObjects.ConnectionHistoryPage;
import test.java.pageObjects.ConnectionDetailPage;
import test.java.utility.LocalContext;


public class DeletionTest extends IntSetup {
    Injector injector = Guice.createInjector(new AppInjector());

    private AppUtils objAppUtils = injector.getInstance(AppUtils.class);
    private LocalContext context = LocalContext.getInstance();
    private HomePage homePage = injector.getInstance(HomePage.class);
    private MenuPage menuPage = injector.getInstance(MenuPage.class);
    private MyConnectionsPage myConnectionsPage = injector.getInstance(MyConnectionsPage.class);
    private MyCredentialsPage myCredentialsPage = injector.getInstance(MyCredentialsPage.class);
    private CredentialPage credentialPage = injector.getInstance(CredentialPage.class);
    private ConnectionHistoryPage connectionHistoryPage = injector.getInstance(ConnectionHistoryPage.class);
    private ConnectionDetailPage connectionDetailPage = injector.getInstance(ConnectionDetailPage.class);

    private String connectionInvitation = "connection-invitation";
    private String oobInvitation = "out-of-band-invitation";
    private boolean isDisplayed = false;
    private String credentialName;
    private String credentialNameMany;

    @BeforeClass
    public void BeforeClassSetup() throws Exception{
        driverApp = AppDriver.getDriver();
        objAppUtils.openApp(driverApp);
        credentialName = context.getValue("credentialName");
        credentialNameMany = context.getValue("credentialNameMany");
    }

    @Test
    public void deleteEmptyConnection() throws Exception {
        homePage.burgerMenuButton(driverApp).click();
        menuPage.myConnectionsButton(driverApp).click();
        myConnectionsPage.testConnection(driverApp, connectionInvitation).click();
        connectionHistoryPage.threeDotsButton(driverApp).click();
        connectionDetailPage.delete_Button(driverApp).click();
        Thread.sleep(1000);

        AppUtils.isNotDisplayed(
                () -> myConnectionsPage.testConnection(driverApp, connectionInvitation).isDisplayed()
        );
    }

    @Test(dependsOnMethods = "deleteEmptyConnection")
    public void deleteCredentialFromExistingConnection() throws Exception {
        homePage.burgerMenuButton(driverApp).click();
        menuPage.myCredentialsButton(driverApp).click();
        // TODO: move this logic to helper
        try {
            myCredentialsPage.testCredential(driverApp, credentialNameMany).click();
        } catch (Exception ex) {
            AppUtils.pullScreenUp(driverApp);
            myCredentialsPage.testCredential(driverApp, credentialNameMany).click();
        }
        connectionHistoryPage.threeDotsButton(driverApp).click();
        credentialPage.deleteButton(driverApp).click();
        Thread.sleep(1000);

        AppUtils.isNotDisplayed(
                () -> myCredentialsPage.testCredential(driverApp, credentialNameMany).isDisplayed()
        );
    }

    @Test(dependsOnMethods = "deleteCredentialFromExistingConnection")
    public void deleteNotEmptyConnection() throws Exception {
        homePage.burgerMenuButton(driverApp).click();
        menuPage.myConnectionsButton(driverApp).click();
        myConnectionsPage.testConnection(driverApp, oobInvitation).click();
        connectionHistoryPage.threeDotsButton(driverApp).click();
        connectionDetailPage.delete_Button(driverApp).click();
        Thread.sleep(1000);

        AppUtils.isNotDisplayed(
                () -> myConnectionsPage.testConnection(driverApp, oobInvitation).isDisplayed()
        );
    }

    @Test(dependsOnMethods = "deleteNotEmptyConnection")
    public void deleteCredentialFromDeletedConnection() throws Exception {
        homePage.burgerMenuButton(driverApp).click();
        menuPage.myCredentialsButton(driverApp).click();
        // TODO: move this logic to helper
        try {
            myCredentialsPage.testCredential(driverApp, credentialName).click();
        } catch (Exception ex) {
            AppUtils.pullScreenUp(driverApp);
            myCredentialsPage.testCredential(driverApp, credentialName).click();
        }
        connectionHistoryPage.threeDotsButton(driverApp).click();
        credentialPage.deleteButton(driverApp).click();
        Thread.sleep(1000);

        AppUtils.isNotDisplayed(
                () -> myCredentialsPage.testCredential(driverApp, credentialName).isDisplayed()
        );
    }

    @AfterClass
    public void AfterClass() {
        driverApp.quit();
    }
}
