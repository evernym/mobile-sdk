package test.java.appModules;

import static org.testng.Assert.assertEquals;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.Assert;
import org.testng.Reporter;

import io.restassured.http.Method;
import io.restassured.specification.RequestSpecification;
import test.java.utility.Config;

/** 
 * The RestApi class is to implement rest api calls used for ConnectMe app
 * 
 */
public class RestApi {

	private static final String CONFIG_FILE_NAME = "config.properties";

	/**
	 * to do post call with the request
	 * @param bodyParam-body of the request
	 * @param path-path for request
	 * @param param- any param in request
	 * @return response of the post in the form of map
	 */	
	
	public HashMap<String, String> post(String bodyParam, String path, String param) throws Exception {
		HashMap<String, String> map = new HashMap<String, String>();
		String env = Config.VerityUI_URL;
		if (param != "") {
			RestAssured.baseURI = env + path + param;
			System.out.println(RestAssured.baseURI);
		} else {
			RestAssured.baseURI = env + path;
		}
		Response ResponseRest = RestAssured
										.given()
										.auth()
										.basic("demo", "ktjo6iKiJsn7EGlCCZj07qKw3")
										.relaxedHTTPSValidation()
										.contentType("application/json")
										.body(bodyParam)
										.when()
										.post("");
		String bodyAsString = ResponseRest.getBody().asString();
		int statusCodeResponse = ResponseRest.getStatusCode();
		if(statusCodeResponse>201){
			System.out.println(bodyAsString);
			Reporter.log("Response: "+bodyAsString);
			Assert.assertTrue(false);
		}
		System.out.println(bodyAsString);
		map = jsonToMap(bodyAsString);
		return map;
	}

	/**
	 * to do get call with the request
	 * @param path-path for request
	 * @param param- any param in request
	 * @return response of the post in the form of map
	 */	
	public HashMap<String, String> get(String path, String param) throws Exception {
		// Specify the base URL to the RESTful web service
		HashMap<String, String> map = new HashMap<String, String>();
		String env = Config.VerityUI_URL;
		RestAssured.baseURI = env + path;
		RequestSpecification httpRequest = RestAssured
													.given()
													.auth()
													.basic("demo", "ktjo6iKiJsn7EGlCCZj07qKw3")
													.relaxedHTTPSValidation();
		httpRequest.accept("application/json");
		Response response = httpRequest.request(Method.GET, "/" + param);
		String responseBody = response.getBody().asString();
		System.out.println("Response Body is =>  " + responseBody);
		map = jsonToMap(responseBody);
		return map;
	}
	
	/**
	 * converts a json string to HashMap
	 * @param json-json string which need to be converted
	 */
	public HashMap<String, String> jsonToMap(String json) throws Exception {
		HashMap<String, String> map = new HashMap<String, String>();
		JSONObject jObject = new JSONObject(json);
		Iterator<?> keys = jObject.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			String value = jObject.get(key).toString();
			map.put(key, value);
		}
		return map;
	}
	
	/**
	 * get value for a key in  map
	 * @param responseMap-map in which we want to iterate
	 * @param mapkey-key for which we want to reterive value
	 */
	public String getKeyValue(Map<String, String> responseMap, String mapkey) throws Exception {
		String returnValue = null;
		for (Map.Entry<String, String> entry : responseMap.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof String) {
				{
					if (key.equals(mapkey)) {
						returnValue = (String) value;
					}

				}
			} else if (value instanceof Map) {
				Map<String, String> subMap = (Map<String, String>) value;
				getKeyValue(subMap, mapkey);
			} else {
				throw new IllegalArgumentException(String.valueOf(value));
			}

		}
		return returnValue;
	}

	/**
	 * To match the value of a key in map
	 * @param responseMap-map in which we want to iterate
	 * @param mapkey-key for which we want to reterive value
	 * @param validateValue- the value which we expects
	 */
	public void responseMatcher(Map<String, String> responseMap, String mapkey, String validateValue) throws Exception {
		for (Map.Entry<String, String> entry : responseMap.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof String) {
				{
					if (key.equals(mapkey)) {
						assertEquals(validateValue, value);

					}

				}
			} else if (value instanceof Map) {
				Map<String, String> subMap = (Map<String, String>) value;
				responseMatcher(subMap, mapkey, validateValue);
			} else {
				throw new IllegalArgumentException(String.valueOf(value));
			}
		}
	}

	public static void writeConfig(String property, String value) throws Exception {
		OutputStream out = new FileOutputStream(CONFIG_FILE_NAME);
		Properties prop = new Properties();
		prop.setProperty(property, value);
		prop.store(out, "");
		out.close();
	}

	public static String readConfig(String property) throws Exception {
		InputStream in = new FileInputStream(CONFIG_FILE_NAME);
		Properties prop = new Properties();
		prop.load(in);
		return prop.getProperty(property);
	}

	/**
	 * sends connections invite by post call
	 * @return  response map for post call
	 */
	public HashMap<String, String> sendConnectionInvite() throws Exception {
		String bodyParam, path;
		bodyParam = "{ \"name\": \"Alex\", \"phone\": \"8327364896\", \"sms\": true}";
		path = "/api/v1/connections";
		HashMap<String, String> response = post(bodyParam, path, "");
		return response;
	}

	/**
	 * checks connections status by get call
	 * @param connectionID-connection id for which we want to know status
	 * @return  response map for get call
	 */
	public HashMap<String, String> checkStatusConnection(String connectionID) throws Exception {
		String param, path;
		HashMap<String, String> response;
		param = connectionID;
		path = "/api/v1/connections";
		response = get(path, param);
		return response;
	}

	/**
	 * checks status by get call
	 * @param id-id for which we want to know status
	 * @param type-type for which we want to know status(like proof,claim)
	 * @return  response map for get call
	 */
	public HashMap<String, String> checkStatus(String id, String type) throws Exception {
		String param, path;
		HashMap<String, String> response;
		param = id;
		path = "/api/v1/" + type + "";
		response = get(path, param);
		return response;
	}

	/**
	 * polls for a particular connection
	 * @param connectionID-connection id for which polling will be peformed
	 * @return  response map for post call
	 */
	public HashMap<String, String> pollConnection(String connectionID) throws Exception {
		String param, path;
		HashMap<String, String> response;
		param = connectionID + "/update_state";
		path = "/api/v1/connections/";
		response = post("", path, param);
		System.out.println(response);
		return response;
	}

	/**
	 * polls for a particular particular status
	 * @param id-id for which polliong will be peformed
	 * @param type-type for which polliong will be peformed(like connection,proof,claim)
	 * @return  response map for post call
	 */
	public void poll(String value, String validateValue,String id,String type) throws Exception {	
		for(int i=0;i<40;i++)
		{
		if (!value.equals(validateValue))
		    {
			Thread.sleep(5000);
			HashMap<String, String> resposneMap = checkStatus(id, type);
		    value=getKeyValue(resposneMap, "state");

		   }
	     }
		Thread.sleep(10000);
		assertEquals(value, validateValue,"State of the request type "+type+" is: "+value);
	}
	
	
	
	/**
	 * creates a schema for credentials
	 * @return  response map for post call
	 */
	public HashMap<String, String> createSchema(String attribute) throws Exception {
		String bodyParam, path;
		bodyParam = "{\"fields\":["+attribute+"]}";
		path = "/api/v1/schemas";
		HashMap<String, String> response = post(bodyParam, path, "");
		return response;
	}

	/**
	 * creates credentials for a schema
	 * @param  seqNO-schema for which credentials will be created
	 * @return  response map for post call
	 */
	public HashMap<String, String> createCredentialDef(String seqNO,String price) throws Exception {
		String bodyParam, path;
		bodyParam = "{\"name\": \"AutoCred\",\r\n  \"schema\": \"" + seqNO + "\",\"price\":\""+price+"\"}";//Note we need to make credential name as param 
		path = "/api/v1/credential-defs";
		HashMap<String, String> response = post(bodyParam, path, "");
		return response;
	}
	
	/**
	 * sends credentials to ConnectMe app
	 * @param  connectionID-connection for which credentials will be send
	 * @param  credentialsDefID-credentials which will be send
	 * @param  attribute-attribute which need to be send
	 * @return  response map for post call
	 */
	public HashMap<String, String> sendCredential(String connectionID, String credentialsDefID, String attribute, String price) throws Exception {
		String bodyParam, path;
		int connectionIDInt=Integer.parseInt(connectionID);
		int credentialsDefIDInt=Integer.parseInt(credentialsDefID);
		bodyParam = "{\"data\":["+attribute+"],\"connection\":"
				+ connectionIDInt + ",\"credentialDef\":" + credentialsDefIDInt + ",\"price\":\""+price+"\"}";
		path = "/api/v1/credentials";
		HashMap<String, String> response = post(bodyParam, path, "");
		return response;
	}
	
	/**
	 * issue's a credential
	 * @param  credentialID-credential for which we need to issue
	 * @return  response map for post call
	 */
	public HashMap<String, String> issueCredential(String credentialID) throws Exception {
		String param, path;
		HashMap<String, String> response;
		param = credentialID + "/send";
		path = "/api/v1/credentials/";
		response = post("", path, param);
		return response;
	}
	
	/**
	 * creates a proof
	 * @param fields-fields in proofs
	 * @return  response map for post call
	 */
	public HashMap<String, String> createProof(String fields) throws Exception {
		String bodyParam, path;
		bodyParam = "{\"fields\":["+fields+"],\"name\":\"AutoProof\"}";
		path = "/api/v1/proof-defs";
		HashMap<String, String> response = post(bodyParam, path, "");
		return response;
	}

	/**
	 * sends proof to ConnectMe app
	 * @param  connectionID-connection for which proof will be send
	 * @param  proofID-proof which will be send
	 * @return response map for post call
	 */
	public HashMap<String, String> sendProof(String connectionID, String proofID) throws Exception {
		String bodyParam, path;
		bodyParam = "{\"proofDef\":"
				+ proofID + ",\"connection\":" + connectionID + "}";
		path = "/api/v1/proofs";
		HashMap<String, String> response = post(bodyParam, path, "");
		return response;
	}
}
