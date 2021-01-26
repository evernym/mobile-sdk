package me.connect.sdk.java;

import org.json.JSONException;
import org.json.JSONObject;

public class AgencyConfig {
    private AgencyConfig() {
    }

    public static final String DEFAULT = "{\"agency_url\":\"http://agency.evernym.com\",\"agency_did\":\"DwXzE7GdE5DNfsrRXJChSD\",\"agency_verkey\":\"844sJfb2snyeEugKvpY7Y4jZJk9LT6BnS6bnuKoiqbip\",\"agent_seed\":null,\"enterprise_seed\":null}";

    public static String setConfigParameters(String agencyConfig, String walletName, String walletKey,
                                             String walletPath, String protocolType) throws JSONException {
        JSONObject config = new JSONObject(agencyConfig);
        config.put("wallet_name", walletName);
        config.put("wallet_key", walletKey);
        JSONObject storageConfig = new JSONObject().put("path", walletPath);
        config.put("storage_config", storageConfig.toString());
        config.put("protocol_type", protocolType);
        return config.toString();
    }
}
