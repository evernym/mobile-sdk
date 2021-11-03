package msdk.kotlin.sample.types;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdateMessageStatusBody {
    List<MessageToUpdate> messages;

    public UpdateMessageStatusBody () {
        this.messages = new ArrayList<>();
    }

    public static class MessageToUpdate {
        String pairwiseDID;
        List<String> uids;

        public MessageToUpdate(String pairwiseDID, String uid) {
            this.pairwiseDID = pairwiseDID;
            this.uids = Collections.singletonList(uid);
        }
    }

    public @NonNull
    UpdateMessageStatusBody addMessage(@NonNull String pairwiseDID, @NonNull String uid) {
        this.messages.add(new MessageToUpdate(pairwiseDID, uid));
        return this;
    }

    public @NonNull
    String toJSON() throws JSONException {
        JSONArray result = new JSONArray();
        for (MessageToUpdate messageToUpdate: this.messages) {
            result.put(
                    new JSONObject()
                            .put("pairwiseDID", messageToUpdate.pairwiseDID)
                            .put("uids", new JSONArray(messageToUpdate.uids))
            );
        }
        return result.toString();
    }
}
