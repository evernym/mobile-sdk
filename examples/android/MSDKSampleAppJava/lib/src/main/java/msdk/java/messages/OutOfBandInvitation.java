package msdk.java.messages;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import msdk.java.handlers.Connections;

public class OutOfBandInvitation {
    public String invitation;
    public JSONObject attachment;
    public Connections.ConnectionMetadata userMeta;
    public String existingConnection;

    public OutOfBandInvitation(
            String invitation,
            JSONObject attach,
            Connections.ConnectionMetadata userMeta,
            String existingConnection
    ) {
        this.invitation = invitation;
        this.attachment = attach;
        this.existingConnection = existingConnection;
        this.userMeta = userMeta;
    }

    public static class OutOfBandInviteBuilder {
        private String invitation;
        private JSONObject attachment;
        private String existingConnection;
        private Connections.ConnectionMetadata userMeta;

        private OutOfBandInviteBuilder() {
        }

        public @NonNull
        OutOfBandInviteBuilder withInvitation(@NonNull String invitation) {
            this.invitation = invitation;
            return this;
        }

        public @NonNull
        OutOfBandInviteBuilder withAttachment(@NonNull JSONObject attach) {
            this.attachment = attach;
            return this;
        }

        public @NonNull
        OutOfBandInviteBuilder withUserMeta(Connections.ConnectionMetadata userMeta) {
            this.userMeta = userMeta;
            return this;
        }

        public @NonNull
        OutOfBandInviteBuilder withExistingConnection(String existingConnection) {
            this.existingConnection = existingConnection;
            return this;
        }

        public @NonNull
        OutOfBandInvitation build() {
            return new OutOfBandInvitation(
                    invitation,
                    attachment,
                    userMeta,
                    existingConnection
            );
        }
    }

    public static OutOfBandInviteBuilder builder() {
        return new OutOfBandInviteBuilder();
    }
}
