package test.java.Tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.openqa.selenium.NoSuchElementException;
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

import java.util.concurrent.TimeUnit;

/**
 * The ConnectionTest class is a Test class which holds test method related to
 * connection
 */
public class ConnectionTest extends IntSetup {
	Injector injector = Guice.createInjector(new AppInjector());

	private AppUtils objAppUtils = injector.getInstance(AppUtils.class);
	private ConnectionModules objConnectionModules = injector.getInstance(ConnectionModules.class);
	private HomePage homePage = injector.getInstance(HomePage.class);
	private MenuPage menuPage = injector.getInstance(MenuPage.class);
	private MyConnectionsPage myConnectionsPage = injector.getInstance(MyConnectionsPage.class);
	private ConnectionHistoryPage connectionHistoryPage = injector.getInstance(ConnectionHistoryPage.class);
	private ConnectionDetailPage connectionDetailPage = injector.getInstance(ConnectionDetailPage.class);
	private LocalContext context = LocalContext.getInstance();

	private String connectionName;
	private String connectionInvitation = "connection-invitation";
	private String oobInvitation = "out-of-band-invitation";
	private boolean isDisplayed = false;

	@BeforeClass
	public void BeforeClassSetup() {
		System.out.println("Connection Test Suite has been started!");
		driverApp = AppDriver.getDriver();
		driverApp.launchApp();
	}

	@DataProvider(name = "data1")
	public Object[][] createData() {
		return new Object[][] {
				{ connectionInvitation },
				{ oobInvitation },
		};
	}

	@Test(dataProvider = "data1")
	public void rejectConnectionTest(String invitationType) throws Exception {
		driverBrowser = BrowserDriver.getDriver();

		AppUtils.DoSomethingEventually(
				() -> objConnectionModules.getConnectionInvitation(driverBrowser, driverApp, Helpers.randomString(), invitationType),
				() -> objConnectionModules.acceptPushNotificationRequest(driverApp),
				() -> objConnectionModules.rejectConnectionInvitation(driverApp)
		);

		Thread.sleep(1000);
		BrowserDriver.closeApp();
	}

	@Test(dataProvider = "data1", dependsOnMethods = "rejectConnectionTest")
//	@Test(dataProvider = "data1")
	public void setUpConnectionTest(String invitationType) throws Exception {
		connectionName = invitationType;

		driverBrowser = BrowserDriver.getDriver();

		AppUtils.DoSomethingEventually(
				() -> objConnectionModules.getConnectionInvitation(driverBrowser, driverApp, connectionName, invitationType),
				() -> objConnectionModules.acceptConnectionInvitation(driverApp)
		);

		// wait until connection get completed
		AppUtils.waitForElement(driverApp, () -> homePage.connectedEvent(driverApp, connectionName)).isDisplayed();

		Thread.sleep(1000);
		BrowserDriver.closeApp();
  	}

	@Test(dependsOnMethods = "setUpConnectionTest")
	public void validateMyConnectionRecordAppeared() throws Exception {
		objAppUtils.openApp(driverApp);

		homePage.burgerMenuButton(driverApp).click();
		menuPage.myConnectionsButton(driverApp).click();
		myConnectionsPage.testConnection(driverApp, connectionName).click();
	}

	@Test(dependsOnMethods = "validateMyConnectionRecordAppeared")
	public void validateConnectionHistory() throws Exception {
		AppUtils.DoSomethingEventually(
				() -> connectionHistoryPage.backButton(driverApp).isDisplayed()
		);
		connectionHistoryPage.connectionLogo(driverApp).isDisplayed();
		connectionHistoryPage.connectionName(driverApp, connectionName).isDisplayed();
		connectionHistoryPage.connectedRecord(driverApp).isDisplayed();
		connectionHistoryPage.connectedRecordDescription(driverApp, connectionName).isDisplayed();
	}

	@Test(dependsOnMethods = "validateConnectionHistory")
	public void validateConnectionDetails() throws Exception {
		connectionHistoryPage.threeDotsButton(driverApp).isDisplayed();
		connectionHistoryPage.threeDotsButton(driverApp).click();

		connectionDetailPage.close_Button(driverApp).isDisplayed();
		connectionDetailPage.delete_Button(driverApp).isDisplayed();

		connectionDetailPage.close_Button(driverApp).click();
		connectionHistoryPage.backButton(driverApp).click();
	}

	@AfterClass
	public void AfterClass() {
		context.setValue("connectionName", connectionName);
		driverApp.closeApp();
		System.out.println("Connection Test Suite has been finished!");
	}
}
