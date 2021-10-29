package msdk.kotlin.sample.messages

import msdk.kotlin.sample.handlers.Connections.ConnectionMetadata
import org.json.JSONObject

class OutOfBandInvitation(
    var invitation: String?,
    var attachment: JSONObject?,
    var userMeta: ConnectionMetadata?,
    var existingConnection: String?
) {

    class OutOfBandInviteBuilder {
        private var invitation: String? = null
        private var attachment: JSONObject? = null
        private var existingConnection: String? = null
        private var userMeta: ConnectionMetadata? = null
        fun withInvitation(invitation: String): OutOfBandInviteBuilder {
            this.invitation = invitation
            return this
        }

        fun withAttachment(attach: JSONObject): OutOfBandInviteBuilder {
            attachment = attach
            return this
        }

        fun withUserMeta(userMeta: ConnectionMetadata?): OutOfBandInviteBuilder {
            this.userMeta = userMeta
            return this
        }

        fun withExistingConnection(existingConnection: String?): OutOfBandInviteBuilder {
            this.existingConnection = existingConnection
            return this
        }

        fun build(): OutOfBandInvitation {
            return OutOfBandInvitation(
                invitation,
                attachment,
                userMeta,
                existingConnection
            )
        }
    }

    companion object {
        fun builder(): OutOfBandInviteBuilder {
            return OutOfBandInviteBuilder()
        }
    }

}