package test.java.Tests;

import appModules.AppInjector;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;
import org.json.JSONObject;
import org.openqa.selenium.Keys;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import test.java.appModules.AppUtils;
import test.java.appModules.VASApi;
import test.java.pageObjects.HomePage;
import test.java.pageObjects.ProofRequestPage;
import test.java.pageObjects.CustomValuesPage;
import test.java.utility.AppDriver;
import test.java.utility.Helpers;
import test.java.utility.IntSetup;
import test.java.utility.LocalContext;
import test.java.utility.Config;

import java.util.Arrays;
import java.util.List;

public class ProofCasesTest extends IntSetup {
	Injector injector = Guice.createInjector(new AppInjector());

	private AppUtils objAppUtlis = injector.getInstance(AppUtils.class);
	private HomePage homePage = injector.getInstance(HomePage.class);
	private ProofRequestPage proofRequestPage = injector.getInstance(ProofRequestPage.class);
	private CustomValuesPage customValuesPage = injector.getInstance(CustomValuesPage.class);
	private LocalContext context = LocalContext.getInstance();

	private VASApi VAS;
	private String DID;
	private String issuerDID = (Config.Device_Type == "android") ? Config.DEMO_VERITY_ISSUER_DID_ANDROID : Config.DEMO_VERITY_ISSUER_DID_IOS;

	private String header = "Proof Request";

	@BeforeClass
	public void BeforeClassSetup() throws Exception {
		DID = context.getValue("DID");

		driverApp = AppDriver.getDriver();
		objAppUtlis.openApp(driverApp);

		VAS = VASApi.getInstance();
	}

	@Test
	public void shareProofRequestContainingGroupedAttributes() throws Exception {
		/*
		 * Proof request contains grouped attributes which must be filled from the same credential
		 * {"names": ["FirstName", "LastName"]},
		 * */
		String attribute1 = "FirstName";
		String attribute2 = "LastName";

		List<JSONObject> requestedAttributes = Arrays.asList(
				new JSONObject().put("names", Arrays.asList(attribute1, attribute2))
		);
		String proofName = Helpers.randomString();

		AppUtils.DoSomethingEventually(
				() -> VAS.requestProof(DID, proofName, requestedAttributes, null),
				() -> AppUtils.waitForElement(driverApp, () -> proofRequestPage.header(driverApp, header)).isDisplayed()
		);

		proofRequestPage.attributeName(driverApp, attribute1).isDisplayed();
		proofRequestPage.attributeName(driverApp, attribute2).isDisplayed();
		proofRequestPage.selectedCredentialIcon(driverApp).isDisplayed();

		objAppUtlis.shareProof(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.proofSharedEvent(driverApp, proofName)).isDisplayed();
	}

	@Test(dependsOnMethods = "shareProofRequestContainingGroupedAttributes")
	public void rejectProofRequestContainingMissingAttributes() throws Exception {
		/*
		 * Proof request contains attribute which can not be provided
		 * {"name": "Missing attribute", "self_attest_allowed": false}
		 * */
		String attribute = "Missing attribute";
		List<JSONObject> requestedAttributes = Arrays.asList(
				new JSONObject()
						.put("name", attribute)
						.put("self_attest_allowed", false)
		);
		String proofName = Helpers.randomString();

		AppUtils.DoSomethingEventually(
			() -> VAS.requestProof(DID, proofName, requestedAttributes, null),
			() -> AppUtils.waitForElement(driverApp, () -> proofRequestPage.missingCredentialsError(driverApp)).isDisplayed()
		);

		proofRequestPage.okButton(driverApp).click();

		proofRequestPage.attributeName(driverApp, attribute).isDisplayed();
		proofRequestPage.notFoundError(driverApp).isDisplayed();
		proofRequestPage.notFoundIcon(driverApp).isDisplayed();

		objAppUtlis.rejectProof(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.proofRequestRejectedEvent(driverApp, proofName)).isDisplayed();
	}

	@Test(dependsOnMethods = "rejectProofRequestContainingMissingAttributes")
	public void rejectProofRequestContainingMissingGroupedAttributes() throws Exception {
		/*
		 * Proof request contains grouped attributes which cannot be filled credential
		 * {"names": ["FirstName", "Missing attribute"]},
		 * */
		String attribute1 = "FirstName";
		String attribute2 = "Missing attribute";

		List<JSONObject> requestedAttributes = Arrays.asList(
				new JSONObject().put("names", Arrays.asList(attribute1, attribute2))
		);
		String proofName = Helpers.randomString();

		AppUtils.DoSomethingEventually(
			() -> VAS.requestProof(DID, proofName, requestedAttributes, null),
			() -> AppUtils.waitForElement(driverApp, () -> proofRequestPage.missingCredentialsError(driverApp)).isDisplayed()
		);

		proofRequestPage.okButton(driverApp).click();

		proofRequestPage.attributeName(driverApp, attribute1 + "," + attribute2).isDisplayed();
		proofRequestPage.notFoundError(driverApp).isDisplayed();
		proofRequestPage.notFoundIcon(driverApp).isDisplayed();

		objAppUtlis.rejectProof(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.proofRequestRejectedEvent(driverApp, proofName)).isDisplayed();
	}

	@Test(dependsOnMethods = "rejectProofRequestContainingMissingGroupedAttributes")
	public void shareProofRequestContainingSelfAttestedAttributes() throws Exception {
		/*
		 * Proof request contains attribute which can not be provided
		 * {"name": "Missing attribute"}
		 * */
		String attribute = "Test attribute";
		String value = "Custom Value";
		List<JSONObject> requestedAttributes = Arrays.asList(
				new JSONObject()
						.put("name", attribute)
						.put("self_attest_allowed", true)
		);
		String proofName = Helpers.randomString();

		AppUtils.DoSomethingEventually(
				() -> VAS.requestProof(DID, proofName, requestedAttributes, null),
				() -> AppUtils.waitForElement(driverApp, () -> proofRequestPage.header(driverApp, header)).isDisplayed()
		);

		proofRequestPage.missingAttributePlaceholder(driverApp).isDisplayed();
		proofRequestPage.arrowForwardIcon(driverApp).isDisplayed();
		proofRequestPage.missingAttributePlaceholder(driverApp).click();

		customValuesPage.title(driverApp).click();
		customValuesPage.description(driverApp).click();
		customValuesPage.attributeNameLabel(driverApp, attribute).click();
		customValuesPage.customValueInput(driverApp).sendKeys(value);

		if (Config.iOS_Devices.contains(Config.Device_Type))
		{
			customValuesPage.customValueInput(driverApp).sendKeys(Keys.RETURN);
		}
		else {
			AndroidDriver androidDriver = (AndroidDriver) driverApp;
			androidDriver.pressKeyCode(AndroidKeyCode.KEYCODE_ENTER);
		}

		proofRequestPage.attributeValue(driverApp, value).isDisplayed();

		objAppUtlis.shareProof(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.proofSharedEvent(driverApp, proofName)).isDisplayed();
	}

	@Test(dependsOnMethods = "shareProofRequestContainingSelfAttestedAttributes")
	public void shareProofRequestFromCredentialButCanBeSelfAttested() throws Exception {
		/*
		 * Proof request contains attribute which can be provided from credential and also can be self-attested
		 * {"name": "Missing attribute"}
		 * */
		String attribute = "Status";
		List<JSONObject> requestedAttributes = Arrays.asList(
				new JSONObject()
						.put("name", attribute)
						.put("self_attest_allowed", true)
		);
		String proofName = Helpers.randomString();

		AppUtils.DoSomethingEventually(
				() -> VAS.requestProof(DID, proofName, requestedAttributes, null),
				() -> AppUtils.waitForElement(driverApp, () -> proofRequestPage.header(driverApp, header)).isDisplayed()
		);

		objAppUtlis.shareProof(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.proofSharedEvent(driverApp, proofName)).isDisplayed();
	}

	@Test(dependsOnMethods = "shareProofRequestFromCredentialButCanBeSelfAttested")
	public void shareProofRequestContainingPredicates() throws Exception {
		/*
		 * Proof request contains grouped attributes which must be filled from the same credential
		 * {"name": "Years", "p_type":">=", "p_value": 20},
		 * */
		String attribute = "Years";

		List<JSONObject> requestedPredicates = Arrays.asList(
				new JSONObject()
						.put("name", attribute)
						.put("p_type", ">=")
						.put("p_value", 20)
		);
		String proofName = Helpers.randomString();

		AppUtils.DoSomethingEventually(
				() -> VAS.requestProof(DID, proofName, null, requestedPredicates),
				() -> AppUtils.waitForElement(driverApp, () -> proofRequestPage.header(driverApp, header)).isDisplayed()
		);

		proofRequestPage.attributeName(driverApp, attribute).isDisplayed();
		proofRequestPage.selectedCredentialIcon(driverApp).isDisplayed();

		objAppUtlis.shareProof(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.proofSharedEvent(driverApp, proofName)).isDisplayed();
	}

	@Test(dependsOnMethods = "shareProofRequestContainingPredicates")
	public void rejectProofRequestContainingMissingPredicate() throws Exception {
		/*
		 * Proof request contains grouped attributes which cannot be filled credential
		 * {"name": "Years", "p_type":">=", "p_value": 60},
		 * */
		String attribute = "Years";

		List<JSONObject> requestedPredicates = Arrays.asList(
				new JSONObject()
						.put("name", attribute)
						.put("p_type", ">=")
						.put("p_value", 60)
		);

		String proofName = Helpers.randomString();

		AppUtils.DoSomethingEventually(
			() -> VAS.requestProof(DID, proofName, null, requestedPredicates),
			() -> AppUtils.waitForElement(driverApp, () -> proofRequestPage.missingCredentialsError(driverApp)).isDisplayed()
		);

		proofRequestPage.okButton(driverApp).click();

		proofRequestPage.attributeName(driverApp, attribute).isDisplayed();
		proofRequestPage.unresolvedPredicateError(driverApp, "Greater than or equal to 60").isDisplayed();
		proofRequestPage.notFoundIcon(driverApp).isDisplayed();

		objAppUtlis.rejectProof(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.proofRequestRejectedEvent(driverApp, proofName)).isDisplayed();
	}

	@Test(dependsOnMethods = "rejectProofRequestContainingMissingPredicate")
	public void shareProofRequestContainingAttributesWithSchemaCredDefRestrictions() throws Exception {
		/*
		 * Proof request contains grouped attributes which must be filled from the same credential
		 * {"name": "FirstName", "restrictions":[{"cred_def_id":"cred_def_id"}]"},
		 * {"name": "LastName", "restrictions":[{"schema_id":"schema_id"}]"}
		 * */
		List<JSONObject> requestedAttributes = Arrays.asList(
				new JSONObject()
						.put("name", "FirstName")
						.put("restrictions", Arrays.asList(
								new JSONObject().put("cred_def_id", context.getValue("credDefId"))
						)),
				new JSONObject()
						.put("name", "LastName")
						.put("restrictions", Arrays.asList(
								new JSONObject().put("schema_id", context.getValue("schemaId"))
						))
		);
		String proofName = Helpers.randomString();

		AppUtils.DoSomethingEventually(
			() -> VAS.requestProof(DID, proofName, requestedAttributes, null),
			() -> AppUtils.waitForElement(driverApp, () -> proofRequestPage.header(driverApp, header)).isDisplayed()
		);

		objAppUtlis.shareProof(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.proofSharedEvent(driverApp, proofName)).isDisplayed();
	}

	@Test(dependsOnMethods = "shareProofRequestContainingAttributesWithSchemaCredDefRestrictions")
	public void shareProofRequestContainingAttributesWithDIDRestrictions() throws Exception {
		/*
		 * Proof request contains grouped attributes which must be filled from the same credential
		 * {"name": "Years", "restrictions":[*** one DID ***]"},
		 * {"name": "Status", "restrictions":[*** list of DIDs ***]"}
		 * */
		List<JSONObject> requestedAttributes = Arrays.asList(
				new JSONObject()
						.put("name", "Years")
						.put("restrictions", Arrays.asList(
								new JSONObject().put("issuer_did", issuerDID)
						)),
				new JSONObject()
						.put("name", "Status")
						.put("restrictions", Arrays.asList(
								new JSONObject().put("issuer_did", issuerDID),
								new JSONObject().put("issuer_did", "PMzJsfuq4YYPAKHLSrdP4R"),
								new JSONObject().put("issuer_did", "PMzJsfuq4YYPAKHLSrdP4S")
						))
		);
		String proofName = Helpers.randomString();

		AppUtils.DoSomethingEventually(
			() -> VAS.requestProof(DID, proofName, requestedAttributes, null),
			() -> AppUtils.waitForElement(driverApp, () -> proofRequestPage.header(driverApp, header)).isDisplayed()
		);

		objAppUtlis.shareProof(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.proofSharedEvent(driverApp, proofName)).isDisplayed();
	}

	@AfterClass
	public void AfterClass() {
		driverApp.closeApp();
	}

}
