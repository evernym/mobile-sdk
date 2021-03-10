package test.java.appModules;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Method;
import io.restassured.response.Response;
import org.json.JSONException;
import org.json.JSONObject;
import test.java.utility.Config;

import java.util.*;

import static test.java.utility.Helpers.UUID4;

public class VASApi {
	private static VASApi instance;

	private String VAS_SERVER_ENDPOINT = Config.VAS_Server_Link;
	private String VERITY_URL;
	private String VERITY_DOMAIN_DID;
	private String VERITY_API_KEY;

	private VASApi() {
		switch (Config.Env_Type) {
			case QA:
				System.out.println("Using QA VAS.");
				VERITY_URL = Config.QA_VERITY_URL;
				VERITY_DOMAIN_DID = Config.QA_VERITY_DOMAIN_DID;
				VERITY_API_KEY = Config.QA_VERITY_API_KEY;
				break;
			case Demo:
				System.out.println("Using Demo VAS.");
				VERITY_URL = Config.DEMO_VERITY_URL;
				VERITY_DOMAIN_DID = (Config.Device_Type == "android") ? Config.DEMO_VERITY_DOMAIN_DID_ANDROID : Config.DEMO_VERITY_DOMAIN_DID_IOS;
				VERITY_API_KEY = (Config.Device_Type == "android") ? Config.DEMO_VERITY_API_KEY_ANDROID : Config.DEMO_VERITY_API_KEY_IOS;
				break;
			case DevTeam1:
				System.out.println("Using DevTeam1 VAS.");
				VERITY_URL = Config.DEVTEAM1_VERITY_URL;
				VERITY_DOMAIN_DID = Config.DEVTEAM1_VERITY_DOMAIN_DID;
				VERITY_API_KEY = Config.DEVTEAM1_VERITY_API_KEY;
				break;
		}
	}

	public static VASApi getInstance() {
		if (instance == null) {
			instance = new VASApi();
			instance.registerEndpoint();
		}
		return instance;
	}

	private JSONObject post(String path, String bodyParam) {
		RestAssured.baseURI = VERITY_URL + VERITY_DOMAIN_DID + path;

		System.out.println("Send POST Request on VAS");
		System.out.println("POST Request Path is => " + path);
		System.out.println("POST Request Body is => " + bodyParam);

		Response response = RestAssured
				.given()
				.header(new Header("X-API-KEY", VERITY_API_KEY))
				.relaxedHTTPSValidation()
				.contentType("application/json")
				.body(bodyParam)
				.when()
				.post();

		String responseBody = response.getBody().asString();
		System.out.println("POST Response Body is => " + responseBody);
		return new JSONObject(responseBody);
	}

	private JSONObject getLastVASResponse(String thread) {
		RestAssured.baseURI = VAS_SERVER_ENDPOINT + '/' + thread;

		System.out.println("Send GET Request on VAS");

		Response response = RestAssured
				.given()
				.relaxedHTTPSValidation()
				.request(Method.GET);

		String responseBody = response.getBody().asString();
		System.out.println("GET Response Body is =>  " + responseBody);

		JSONObject result = new JSONObject();
		try {
			result = new JSONObject(responseBody);
		} catch (JSONException ex) {
			// ignore
		}

		return result;
	}

	private JSONObject getVASResponse(String expectedType, String thread) throws Exception {
		for (int i = 0; i < 10; i++) {
			Thread.sleep(10000);
			JSONObject VASResponse = getLastVASResponse(thread);
			if (VASResponse.has("@type") && VASResponse.getString("@type").equals(expectedType)) {
				return VASResponse;
			}
		}
		return new JSONObject();
	}

	public void createIssuer(String relationshipThreadID) {
		System.out.println("Create issuer: " + VERITY_DOMAIN_DID);

		JSONObject body =
				new JSONObject()
						.put("@id", UUID4())
						.put("@type", "did:sov:123456789abcdefghi1234;spec/issuer-setup/0.6/create")
						.put("domainDID", VERITY_DOMAIN_DID);

		String path = "/issuer-setup/0.6/" + UUID4();

		post(path, body.toString());
	}

	public void getIdentifier(String relationshipThreadID) {
		JSONObject body =
				new JSONObject()
						.put("@id", UUID4())
						.put("@type", "did:sov:123456789abcdefghi1234;spec/issuer-setup/0.6/current-public-identifier");

		String path = "/issuer-setup/0.6/" + UUID4();

		post(path, body.toString());
	}

	private void registerEndpoint() {
		System.out.println("registerEndpoint VAS_SERVER_ENDPOINT: " + VAS_SERVER_ENDPOINT);

		JSONObject body =
				new JSONObject()
						.put("@id", UUID4())
						.put("@type", "did:sov:123456789abcdefghi1234;spec/configs/0.6/UPDATE_COM_METHOD")
						.put("comMethod",
								new JSONObject()
										.put("id", "webhook")
										.put("type", 2)
										.put("value", VAS_SERVER_ENDPOINT)
										.put("packaging", new JSONObject().put("pkgType", "plain")
										)
						);
		String path = "/configs/0.6/" + UUID4();

		post(path, body.toString());
	}

	public JSONObject createRelationship(String label) throws Exception {
		JSONObject body =
				new JSONObject()
						.put("@id", UUID4())
						.put("@type", "did:sov:123456789abcdefghi1234;spec/relationship/1.0/create")
						.put("label", label);

		String thread = UUID4();
		String path = "/relationship/1.0/" + thread;

		post(path, body.toString());

		JSONObject result = new JSONObject();
		JSONObject VASResponse = getVASResponse("did:sov:123456789abcdefghi1234;spec/relationship/1.0/created", thread);

		result.put("relationshipThreadID", VASResponse.getJSONObject("~thread").getString("thid"));
		result.put("DID", VASResponse.getString("did"));

		return result;
	}

	public JSONObject createConnectionInvitation(String relationshipThreadID,
	                                             String DID,
												 String invitationType) throws Exception {
		JSONObject body =
				new JSONObject()
						.put("@id", UUID4())
						.put("@type", String.format("did:sov:123456789abcdefghi1234;spec/relationship/1.0/%s", invitationType))
						.put("~for_relationship", DID)
						.put("shortInvite", true);

		String path = "/relationship/1.0/" + relationshipThreadID;

		post(path, body.toString());

		JSONObject result = new JSONObject();
		JSONObject VASResponse = getVASResponse("did:sov:123456789abcdefghi1234;spec/relationship/1.0/invitation", relationshipThreadID);

		result.put("inviteURL", VASResponse.getString("shortInviteURL"));

		return result;
	}

	public JSONObject createSchema(String name,
	                               String version,
	                               List<String> attrNames) throws Exception {
		JSONObject body =
				new JSONObject()
						.put("@id", UUID4())
						.put("@type", "did:sov:123456789abcdefghi1234;spec/write-schema/0.6/write")
						.put("name", name)
						.put("version", version)
						.put("attrNames", attrNames);

		String thread = UUID4();
		String path = "/write-schema/0.6/" + thread;

		post(path, body.toString());

		JSONObject result = new JSONObject();
		JSONObject VASResponse = getVASResponse("did:sov:123456789abcdefghi1234;spec/write-schema/0.6/status-report", thread);

		result.put("schemaId", VASResponse.getString("schemaId"));

		return result;
	}

	public JSONObject createCredentialDef(String name,
	                                      String schemaID) throws Exception {
		JSONObject body =
				new JSONObject()
						.put("@id", UUID4())
						.put("@type", "did:sov:123456789abcdefghi1234;spec/write-cred-def/0.6/write")
						.put("name", name + "_" + UUID4())
						.put("tag", "tag")
						.put("schemaId", schemaID)
						.put("revocationDetails",
								new JSONObject().put("support_revocation", false)
						);

		String thread = UUID4();
		String path = "/write-cred-def/0.6/" + thread;

		post(path, body.toString());

		Thread.sleep(10000);

		JSONObject result = new JSONObject();
		JSONObject VASResponse = getVASResponse("did:sov:123456789abcdefghi1234;spec/write-cred-def/0.6/status-report", thread);

		result.put("credDefId", VASResponse.getString("credDefId"));

		return result;
	}

	public JSONObject sendCredentialOffer(String DID,
	                                String credDefID,
	                                JSONObject values,
	                                String comment) {
		JSONObject body =
				new JSONObject()
						.put("@id", UUID4())
						.put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/issue-credential/1.0/offer")
						.put("~for_relationship", DID)
						.put("cred_def_id", credDefID)
						.put("credential_values", values)
						.put("price", "0")
						.put("comment", comment)
						.put("auto_issue", true);

		String thread = UUID4();
		String path = "/issue-credential/1.0/" + thread;

		post(path, body.toString());

		JSONObject result = new JSONObject();
		result.put("threadId", thread);

		return result;
	}

	public void requestProof(String DID,
	                         String name,
	                         List<JSONObject> attributes,
	                         List<JSONObject> predicates) {
		JSONObject body =
				new JSONObject()
						.put("@id", UUID4())
						.put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/present-proof/1.0/request")
						.put("~for_relationship", DID)
						.put("name", name)
						.put("proof_attrs", attributes)
						.put("proof_predicates", predicates);

		String path = "/present-proof/1.0/" + UUID4();

		post(path, body.toString());
	}

	public void askQuestion(String DID,
	                         String text,
	                         String detail,
	                         List<String> validResponses) {
		JSONObject body =
				new JSONObject()
						.put("@id", UUID4())
						.put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/committedanswer/1.0/ask-question")
						.put("~for_relationship", DID)
						.put("text", text)
						.put("detail", detail)
						.put("valid_responses", validResponses);

		String path = "/committedanswer/1.0/" + UUID4();

		post(path, body.toString());
	}
}
