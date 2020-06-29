package me.connect.sdk.java;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class AgencyConfig {
    private AgencyConfig() {
    }

    public static final String DEFAULT = "{\"agency_url\":\"http://agency.evernym.com\",\"agency_did\":\"DwXzE7GdE5DNfsrRXJChSD\",\"agency_verkey\":\"844sJfb2snyeEugKvpY7Y4jZJk9LT6BnS6bnuKoiqbip\",\"agent_seed\":null,\"enterprise_seed\":null}";

    public static String setConfigParameters(String agencyConfig, String walletName, String walletKey, Context context) throws JSONException {
        JSONObject config = new JSONObject(agencyConfig);
        config.put("wallet_name", walletName);
        config.put("wallet_key", walletKey);
        config.put("storage_config", "\"storage_config\": \"{\\\"path\\\":\\\"" + context.getFilesDir().getAbsolutePath() + "/.indy_client/wallet\\\"}\"");
        return config.toString();
    }
}
