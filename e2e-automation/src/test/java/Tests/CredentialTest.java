package test.java.Tests;

import appModules.AppInjector;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.appium.java_client.TouchAction;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.java.appModules.AppUtils;
import test.java.utility.IntSetup;
import test.java.appModules.VASApi;
import test.java.pageObjects.HomePage;
import test.java.pageObjects.MenuPage;
import test.java.pageObjects.CredentialPage;
import test.java.pageObjects.MyCredentialsPage;
import test.java.pageObjects.ConnectionHistoryPage;
import test.java.pageObjects.ConnectionDetailPage;
import test.java.funcModules.ConnectionModules;

import test.java.utility.LocalContext;
import test.java.utility.Helpers;
import test.java.utility.RetryAnalyzer;
import test.java.utility.Tuple;
import test.java.utility.Constants;
import test.java.utility.AppDriver;

import java.util.List;

public class CredentialTest extends IntSetup {
	Injector injector = Guice.createInjector(new AppInjector());

	private AppUtils objAppUtlis = injector.getInstance(AppUtils.class);
	private HomePage homePage = injector.getInstance(HomePage.class);
	private MenuPage menuPage = injector.getInstance(MenuPage.class);
	private CredentialPage credentialPage = injector.getInstance(CredentialPage.class);
	private MyCredentialsPage myCredentialsPage = injector.getInstance(MyCredentialsPage.class);
	private ConnectionHistoryPage connectionHistoryPage = injector.getInstance(ConnectionHistoryPage.class);
	private ConnectionModules objConnectionModules = injector.getInstance(ConnectionModules.class);

	private LocalContext context = LocalContext.getInstance();

	private String credentialName = Helpers.randomString();
	private String credentialNameMany = Helpers.randomString();
	private String credentialNameAttachment = Helpers.randomString();
	private String credentialNameAll = Helpers.randomString();
	private String connectionName;
	private VASApi VAS;

	private List<String> attrs4 = Helpers.fourAttributes();
//	private List<String> attrs2 = Helpers.twoAttributes();
	private List<String> attrsAll = Helpers.allAttachmentsAttributes();
	private List<String> attrsMany = Helpers.nAttributes(125);
	private JSONObject valuesMany = new JSONObject();
	private String DID;
	private boolean isDisplayed = false;
	private String header = "Credential Offer";

	private Tuple[] parametersList = {
			new Tuple(credentialName, attrs4, "schemaId", "credDefId"),
			new Tuple(credentialNameMany, attrsMany, "schemaIdMany", "credDefIdMany"),
//			new Tuple(credentialNameAttachment, attrs2, "schemaIdAttachment", "credDefIdAttachment")
			new Tuple(credentialNameAttachment, attrsAll, "schemaIdAll", "credDefIdAll")
	};

	private void validateCredentialView(String header, String title, String credentialName, JSONObject values) throws Exception {
		credentialPage.header(driverApp, header).isDisplayed();
		credentialPage.title(driverApp, title).isDisplayed();
		credentialPage.credentialSenderName(driverApp, connectionName).isDisplayed();
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

	private void createSchemaAndCredDef(String credentialName, List<String> attributes, String schemaKey, String credDefKey) throws Exception {
		JSONObject schemaResponse;
		String schemaId;
		JSONObject credDefResponse;
		String credDefId;

		// create schema
		try {
			schemaResponse = VAS.createSchema(credentialName, "1.0", attributes);
			schemaId = schemaResponse.getString("schemaId");
			context.setValue(schemaKey, schemaId);
		} catch (Exception ex) {
			schemaResponse = VAS.createSchema(credentialName, "1.0", attributes);
			schemaId = schemaResponse.getString("schemaId");
			context.setValue(schemaKey, schemaId);
		}

		// create cred def
		try {
			credDefResponse = VAS.createCredentialDef(credentialName, schemaResponse.getString("schemaId"));
			credDefId = credDefResponse.getString("credDefId");
			context.setValue(credDefKey, credDefId);
		} catch (Exception ex) {
			credDefResponse = VAS.createCredentialDef(credentialName, schemaResponse.getString("schemaId"));
			credDefId = credDefResponse.getString("credDefId");
			context.setValue(credDefKey, credDefId);
		}
	}

	@BeforeClass
	public void BeforeClassSetup() throws Exception {
		DID = context.getValue("DID");
		connectionName = context.getValue("connectionName");

		driverApp = AppDriver.getDriver();
		objAppUtlis.openApp(driverApp);

		VAS = VASApi.getInstance();

		for (Tuple parameters: parametersList) {
			try {
				createSchemaAndCredDef(parameters.a, parameters.b, parameters.c, parameters.d);
			} catch (Exception ex) {
				System.err.println(ex.toString());
				createSchemaAndCredDef(parameters.a, parameters.b, parameters.c, parameters.d);
			}
		}
	}

	@DataProvider(name = "data2")
	public Object[][] createData() {
		return new Object[][] {
				{Constants.valuesAttachment, Helpers.randomString()},
				{Constants.valuesAttachment2, Helpers.randomString()},
				{Constants.valuesAttachment3, Helpers.randomString()},
		};
	}

	@Test
	public void acceptCredentialFromHome() throws Exception {
		AppUtils.DoSomethingEventually(
				() -> VAS.sendCredentialOffer(DID, context.getValue("credDefId"), Constants.values, credentialName),
				() -> AppUtils.waitForElement(driverApp, () -> credentialPage.header(driverApp, header)).isDisplayed()
		);

//		validateCredentialView("Credential Offer", "Issued by", credentialName, Constants.values); FIXME: android failure - swipe is needed
		objAppUtlis.acceptCredential(driverApp);

		homePage.recentEventsSection(driverApp).isDisplayed();
//		homePage.issuingCredentialEvent(driverApp, credentialName).isDisplayed(); FIXME: intermittent failure

        AppUtils.waitForElement(driverApp, () -> homePage.credentialIssuedEvent(driverApp, credentialName)).isDisplayed();
	}

	@Test(dependsOnMethods = "acceptCredentialFromHome")
	public void validateMyCredentialRecordAppeared() throws Exception {
		homePage.burgerMenuButton(driverApp).click();
		menuPage.myCredentialsButton(driverApp).click();
		myCredentialsPage.testCredential(driverApp, credentialName).isDisplayed();
	}

	@Test(dependsOnMethods = "validateMyCredentialRecordAppeared")
	public void validateCredentialDetails() throws Exception {
		myCredentialsPage.testCredential(driverApp, credentialName).click();
		validateCredentialView("Credential Details", "Issued by", credentialName, Constants.values);
		credentialPage.backArrow(driverApp).click();
	}

	@Test(dependsOnMethods = "validateCredentialDetails")
	public void validateConnectionHistory() throws Exception {
		objConnectionModules.openConnectionHistory(driverApp, connectionName);

		connectionHistoryPage.acceptedCredentialRecord(driverApp).isDisplayed();
		connectionHistoryPage.acceptedCredentialViewButton(driverApp).click();

		validateCredentialView("My Credential", "Accepted Credential", credentialName, Constants.values);
		credentialPage.closeButton(driverApp).click();
		connectionHistoryPage.backButton(driverApp).click();

		homePage.burgerMenuButton(driverApp).click();
		menuPage.homeButton(driverApp).click();
	}

	@Test(dependsOnMethods = "validateConnectionHistory")
	public void rejectCredentialOffer() throws Exception {
		AppUtils.DoSomethingEventually(
				() -> VAS.sendCredentialOffer(DID, context.getValue("credDefId"), Constants.values, credentialName),
				() -> AppUtils.waitForElement(driverApp, () -> credentialPage.header(driverApp, header)).isDisplayed()
		);

//		validateCredentialView("Credential Offer", "Issued by", credentialName, Constants.values);
		objAppUtlis.rejectCredential(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.credentialRejectedEvent(driverApp, credentialName)).isDisplayed();
		objConnectionModules.openConnectionHistory(driverApp, connectionName);
		connectionHistoryPage.rejectedCredentialRecord(driverApp, credentialName).isDisplayed();
		connectionHistoryPage.backButton(driverApp).click();

		homePage.burgerMenuButton(driverApp).click();
		menuPage.homeButton(driverApp).click();
	}

	@Test(dependsOnMethods = "rejectCredentialOffer")
	public void acceptManyAttributesCredential() throws Exception {
		for (String item: attrsMany)
			valuesMany.put(item, Helpers.randomString());

		AppUtils.DoSomethingEventually(
				() -> VAS.sendCredentialOffer(DID, context.getValue("credDefIdMany"), valuesMany, credentialNameMany),
				() -> AppUtils.waitForElement(driverApp, () -> credentialPage.header(driverApp, header)).isDisplayed()
		);

		objAppUtlis.acceptCredential(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.credentialIssuedEvent(driverApp, credentialNameMany)).isDisplayed();
	}

//	TODO: return this test after VAS issues fixing
//	@Test(dataProvider = "data2", dependsOnMethods = "acceptManyAttributesCredential")
//	public void acceptAttachmentCredential(JSONObject values, String comment) throws Exception {
//		AppUtils.DoSomethingEventually(
//				() -> VAS.sendCredentialOffer(DID, context.getValue("credDefIdAttachment"), values, comment),
//				() -> credentialPage.header(driverApp, header).isDisplayed()
//		);
//
//		validateCredentialView("Credential Offer", "Issued by", comment, values);
//
//		objAppUtlis.acceptCredential(driverApp);
//
//		AppUtils.waitForElement(driverApp, () -> homePage.credentialIssuedEvent(driverApp, comment)).isDisplayed();
//	}

	@Test(dependsOnMethods = "acceptManyAttributesCredential")
	public void acceptMultiAttachmentCredential() throws Exception {
		final JSONObject values = new JSONObject()
//				.put("Label", "All Attachments Credential")
				.put("Photo_link", Constants.valuesAttachment.getString("Attachment_link"))
				.put("PDF_link", Constants.valuesAttachment2.getString("Attachment_link"))
				.put("DOCX_link", Constants.valuesAttachment3.getString("Attachment_link"))
				.put("CSV_link", Constants.valuesAttachment4.getString("Attachment_link"));

		AppUtils.DoSomethingEventually(
				() -> VAS.sendCredentialOffer(DID, context.getValue("credDefIdAll"), values, credentialNameAll),
				() -> AppUtils.waitForElement(driverApp, () -> credentialPage.header(driverApp, header)).isDisplayed()
		);

		validateCredentialView("Credential Offer", "Issued by", credentialNameAll, values);

		objAppUtlis.acceptCredential(driverApp);

		AppUtils.waitForElement(driverApp, () -> homePage.credentialIssuedEvent(driverApp, credentialNameAll)).isDisplayed();
	}

	@AfterClass
	public void AfterClass() {
		context.setValue("credentialName", credentialName);
		context.setValue("credentialNameMany", credentialNameMany);
		context.setValueList("attrsMany", attrsMany);
		driverApp.closeApp();
	}

}
