package test.java.Tests;

import appModules.AppInjector;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import test.java.appModules.AppUtils;
import test.java.utility.IntSetup;
import test.java.appModules.VASApi;
import test.java.utility.LocalContext;
import test.java.utility.Helpers;
import test.java.pageObjects.ProofRequestPage;
import test.java.pageObjects.MenuPage;
import test.java.pageObjects.ConnectionHistoryPage;
import test.java.pageObjects.HomePage;
import test.java.funcModules.ConnectionModules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import test.java.utility.AppDriver;

public class ProofDemoTest extends IntSetup {
	Injector injector = Guice.createInjector(new AppInjector());

	private AppUtils objAppUtlis = injector.getInstance(AppUtils.class);
	private HomePage homePage = injector.getInstance(HomePage.class);
	private ProofRequestPage proofRequestPage = injector.getInstance(ProofRequestPage.class);
	private MenuPage menuPage = injector.getInstance(MenuPage.class);
	private ConnectionHistoryPage connectionHistoryPage = injector.getInstance(ConnectionHistoryPage.class);
	private ConnectionModules objConnectionModules = injector.getInstance(ConnectionModules.class);
	private LocalContext context = LocalContext.getInstance();

	private String header = "Proof Request";
	private String proofName = Helpers.randomString();
	private String proofNameImage = "Image Proof";
	private String proofNameMany = "Big Proof";
	private String proofNameDiff = "Different Credentials Proof";
	private VASApi VAS;
	private String DID;
	private String connectionName;
	private List<String> attrsMany;

	private List<JSONObject> requestedAttributes = Arrays.asList(
			new JSONObject().put("name", "FirstName"),
			new JSONObject().put("name", "Years")
	);

	private List<JSONObject> requestedAttributesImage = Arrays.asList(
			new JSONObject().put("name", "Photo_link")
	);

	private List<JSONObject> requestedAttributesDiff = Arrays.asList(
			new JSONObject().put("name", "LastName"),
			new JSONObject().put("name", "Status"),
			new JSONObject().put("name", "PDF_link")
	);

	private List<JSONObject> requestedAttributesMany = new ArrayList<>();

	private void validateProofRequestView(String header, String title, String proofName, List<JSONObject> values) throws Exception {
		proofRequestPage.header(driverApp, header).isDisplayed();
		proofRequestPage.title(driverApp, title).isDisplayed();
		System.out.println(connectionName);
		proofRequestPage.proofRequestSenderName(driverApp, connectionName).isDisplayed();
		proofRequestPage.proofRequestSenderLogo(driverApp).isDisplayed();
		proofRequestPage.proofRequestName(driverApp, proofName).isDisplayed();

		for (JSONObject attribute : values) {
			String value = attribute.getString("name");
			if (value.contains("_link")) { // attachment case
				attribute.put("name", value.replace("_link", ""));
			}
			else {
				proofRequestPage.attributeName(driverApp, value).isDisplayed();
			}
		}
	}

	@BeforeClass
	public void BeforeClassSetup() throws Exception {
		DID = context.getValue("DID");
		connectionName = context.getValue("connectionName");
		context.setValue("proofName", proofName);
		attrsMany = context.getValueList("attrsMany");

		driverApp = AppDriver.getDriver();
		objAppUtlis.openApp(driverApp);

		VAS = VASApi.getInstance();
	}

	@Test
	public void acceptProofRequestFromHome() throws Exception {
		AppUtils.DoSomethingEventually(
				() -> VAS.requestProof(DID, proofName, requestedAttributes, null),
//				() -> proofRequestPage.header(driverApp, "Proof Request").isDisplayed()
				() -> AppUtils.waitForElement(driverApp, () -> proofRequestPage.header(driverApp, header)).isDisplayed()
		);

		validateProofRequestView(header, "Requested by", proofName, requestedAttributes);
		objAppUtlis.shareProof(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.proofSharedEvent(driverApp, proofName)).isDisplayed();
	}

	@Test(dependsOnMethods = "acceptProofRequestFromHome")
	public void validateConnectionHistory() throws Exception {
		objConnectionModules.openConnectionHistory(driverApp, connectionName);
//		// TODO: move this logic to helper
//		try {
			connectionHistoryPage.sharedProofRecord(driverApp, proofName).isDisplayed();
//		} catch (Exception ex) {
//			AppUtils.pullScreenUp(driverApp);
//			connectionHistoryPage.sharedProofRecord(driverApp, proofName).isDisplayed();
//		}
		connectionHistoryPage.viewProofRequestDetailsButton(driverApp).click();

		validateProofRequestView(header, "You shared this information", proofName, requestedAttributes);
		proofRequestPage.closeButton(driverApp).click();
		connectionHistoryPage.backButton(driverApp).click();
		homePage.burgerMenuButton(driverApp).click();
		menuPage.homeButton(driverApp).click();
	}

	@Test(dependsOnMethods = "validateConnectionHistory")
	public void rejectProofRequest() throws Exception {
		AppUtils.DoSomethingEventually(
				() -> VAS.requestProof(DID, proofName, requestedAttributes, null),
//				() -> proofRequestPage.header(driverApp, "Proof Request").isDisplayed()
				() -> AppUtils.waitForElement(driverApp, () -> proofRequestPage.header(driverApp, header)).isDisplayed()
		);

		validateProofRequestView(header, "Requested by", proofName, requestedAttributes);
		objAppUtlis.rejectProof(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.proofRequestRejectedEvent(driverApp, proofName)).isDisplayed();
		objConnectionModules.openConnectionHistory(driverApp, connectionName);
		connectionHistoryPage.rejectedProofRequestRecord(driverApp, proofName).isDisplayed();
		connectionHistoryPage.backButton(driverApp).click();

		homePage.burgerMenuButton(driverApp).click();
		menuPage.homeButton(driverApp).click();
	}

	@Test(dependsOnMethods = "rejectProofRequest")
	public void acceptProofRequestWithImage() throws Exception {
		AppUtils.DoSomethingEventually(
				() -> VAS.requestProof(DID, proofNameImage, requestedAttributesImage, null),
//				() -> proofRequestPage.header(driverApp, "Proof Request").isDisplayed()
				() -> AppUtils.waitForElement(driverApp, () -> proofRequestPage.header(driverApp, header)).isDisplayed()
		);

		validateProofRequestView(header, "Requested by", proofNameImage, requestedAttributesImage);
		objAppUtlis.shareProof(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.proofSharedEvent(driverApp, proofNameImage)).isDisplayed();
	}

	@Test(dependsOnMethods = "acceptProofRequestWithImage")
	public void acceptProofRequestMany() throws Exception {
		for (String item: attrsMany)
			requestedAttributesMany.add(new JSONObject().put("name", item));

		AppUtils.DoSomethingEventually(
				() -> VAS.requestProof(DID, proofNameMany, requestedAttributesMany, null),
//				() -> proofRequestPage.header(driverApp, "Proof Request").isDisplayed()
				() -> AppUtils.waitForElement(driverApp, () -> proofRequestPage.header(driverApp, header)).isDisplayed()
		);

		objAppUtlis.shareProof(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.proofSharedEvent(driverApp, proofNameMany)).isDisplayed();
	}

	@Test(dependsOnMethods = "acceptProofRequestMany")
	public void acceptProofRequestDifferentCredentials() throws Exception {
		AppUtils.DoSomethingEventually(
				() -> VAS.requestProof(DID, proofNameDiff, requestedAttributesDiff, null),
//				() -> proofRequestPage.header(driverApp, "Proof Request").isDisplayed()
				() -> AppUtils.waitForElement(driverApp, () -> proofRequestPage.header(driverApp, header)).isDisplayed()
		);

		validateProofRequestView(header, "Requested by", proofNameDiff, requestedAttributesDiff);
		objAppUtlis.shareProof(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.proofSharedEvent(driverApp, proofNameDiff)).isDisplayed();
	}

	@AfterClass
	public void AfterClass() {
		driverApp.closeApp();
	}

}
