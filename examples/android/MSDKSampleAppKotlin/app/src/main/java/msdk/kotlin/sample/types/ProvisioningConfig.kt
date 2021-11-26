package msdk.kotlin.sample.types

import org.json.JSONException
import org.json.JSONObject


object ProvisioningConfig {
    fun builder(): ProvisioningConfigBuilder {
        return ProvisioningConfigBuilder()
    }

    class ProvisioningConfigBuilder {
        private var agencyEndpoint: String? = null
        private var agencyDid: String? = null
        private var agencyVerkey: String? = null
        private var walletName: String? = null
        private var walletKey: String? = null
        private var logo: String? = null
        private var name: String? = null
        private var protocolType: String? = null

        /**
         * Set agency endpoint.
         *
         * @param agencyEndpoint agency endpoint
         * @return [ProvisioningConfigBuilder] instance
         */
        fun withAgencyEndpoint(agencyEndpoint: String): ProvisioningConfigBuilder {
            this.agencyEndpoint = agencyEndpoint
            return this
        }

        /**
         * Set agency did.
         *
         * @param agencyDid agency did
         * @return [ProvisioningConfigBuilder] instance
         */
        fun withAgencyDid(agencyDid: String): ProvisioningConfigBuilder {
            this.agencyDid = agencyDid
            return this
        }

        /**
         * Set agency verkey.
         *
         * @param agencyVerkey agency verkey
         * @return [ProvisioningConfigBuilder] instance
         */
        fun withAgencyVerkey(agencyVerkey: String): ProvisioningConfigBuilder {
            this.agencyVerkey = agencyVerkey
            return this
        }

        /**
         * Set wallet name.
         *
         * @param walletName wallet name
         * @return [ProvisioningConfigBuilder] instance
         */
        fun withWalletName(walletName: String): ProvisioningConfigBuilder {
            this.walletName = walletName
            return this
        }

        /**
         * Set logo
         *
         * @param logo logo
         * @return this
         */
        fun withLogo(logo: String?): ProvisioningConfigBuilder {
            this.logo = logo
            return this
        }

        /**
         * Set app name
         *
         * @param name name
         * @return this
         */
        fun withName(name: String?): ProvisioningConfigBuilder {
            this.name = name
            return this
        }

        /**
         * Set wallet key
         *
         * @param walletKey walletKey
         * @return this
         */
        fun withWalletKey(walletKey: String?): ProvisioningConfigBuilder {
            this.walletKey = walletKey
            return this
        }

        /**
         * Set protocol type
         *
         * @param protocolType protocol type
         * @return this
         */
        fun withProtocolType(protocolType: String?): ProvisioningConfigBuilder {
            this.protocolType = protocolType
            return this
        }

        /**
         * Creates provisioning config from settings.
         *
         * @return provisioning config
         */
        @Throws(JSONException::class)
        fun build(): String {
            val agencyConfig = JSONObject()
            agencyConfig.put("agency_endpoint", agencyEndpoint)
            agencyConfig.put("agency_did", agencyDid)
            agencyConfig.put("agency_verkey", agencyVerkey)
            agencyConfig.put("wallet_name", walletName)
            agencyConfig.put("wallet_key", walletKey)
            agencyConfig.put("protocol_type", "3.0")
            agencyConfig.put("logo", logo)
            agencyConfig.put("name", name)
            agencyConfig.put("protocol_type", this.protocolType);
            return agencyConfig.toString()
        }
    }
}