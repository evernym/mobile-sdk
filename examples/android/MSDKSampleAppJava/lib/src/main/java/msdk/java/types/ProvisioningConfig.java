package msdk.java.types;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import org.json.JSONException;
import org.json.JSONObject;

import msdk.java.handlers.Initialization;

public class ProvisioningConfig {
    public static ProvisioningConfigBuilder builder() {
        return new ProvisioningConfigBuilder();
    }

    public static final class ProvisioningConfigBuilder {
        private String agencyEndpoint;
        private String agencyDid;
        private String agencyVerkey;
        private String walletName;
        private String walletKey;
        private String logo;
        private String name;

        private ProvisioningConfigBuilder() {
        }

        /**
         * Set agency endpoint.
         *
         * @param agencyEndpoint agency endpoint
         * @return {@link ProvisioningConfigBuilder} instance
         */
        public @NonNull
        ProvisioningConfigBuilder withAgencyEndpoint(@NonNull String agencyEndpoint) {
            this.agencyEndpoint = agencyEndpoint;
            return this;
        }

        /**
         * Set agency did.
         *
         * @param agencyDid agency did
         * @return {@link ProvisioningConfigBuilder} instance
         */
        public @NonNull
        ProvisioningConfigBuilder withAgencyDid(@NonNull String agencyDid) {
            this.agencyDid = agencyDid;
            return this;
        }

        /**
         * Set agency verkey.
         *
         * @param agencyVerkey agency verkey
         * @return {@link ProvisioningConfigBuilder} instance
         */
        public @NonNull
        ProvisioningConfigBuilder withAgencyVerkey(@NonNull String agencyVerkey) {
            this.agencyVerkey = agencyVerkey;
            return this;
        }

        /**
         * Set wallet name.
         *
         * @param walletName wallet name
         * @return {@link ProvisioningConfigBuilder} instance
         */
        public @NonNull
        ProvisioningConfigBuilder withWalletName(@NonNull String walletName) {
            this.walletName = walletName;
            return this;
        }

        /**
         * Set logo
         *
         * @param logo logo
         * @return this
         */
        public @NonNull
        ProvisioningConfigBuilder withLogo(String logo) {
            this.logo = logo;
            return this;
        }

        /**
         * Set app name
         *
         * @param name name
         * @return this
         */
        public @NonNull
        ProvisioningConfigBuilder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set wallet key
         *
         * @param walletKey walletKey
         * @return this
         */
        public @NonNull
        ProvisioningConfigBuilder withWalletKey(String walletKey) {
            this.walletKey = walletKey;
            return this;
        }

        /**
         * Creates provisioning config from settings.
         *
         * @return provisioning config
         */
        public @NonNull
        String build() throws JSONException {
            JSONObject agencyConfig = new JSONObject();
            agencyConfig.put("agency_endpoint", this.agencyEndpoint);
            agencyConfig.put("agency_did", this.agencyDid);
            agencyConfig.put("agency_verkey", this.agencyVerkey);
            agencyConfig.put("wallet_name", this.walletName);
            agencyConfig.put("wallet_key", this.walletKey);
            agencyConfig.put("protocol_type", "3.0");
            agencyConfig.put("logo", this.logo);
            agencyConfig.put("name", this.name);
            return agencyConfig.toString();
        }
    }
}
