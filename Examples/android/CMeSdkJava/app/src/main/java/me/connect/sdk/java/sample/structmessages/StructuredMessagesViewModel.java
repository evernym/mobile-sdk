package me.connect.sdk.java.sample.structmessages;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.Executors;

import me.connect.sdk.java.Messages;
import me.connect.sdk.java.StructuredMessages;
import me.connect.sdk.java.message.Message;
import me.connect.sdk.java.message.MessageType;
import me.connect.sdk.java.message.StructuredMessageHolder;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.StructuredMessage;

public class StructuredMessagesViewModel extends AndroidViewModel {
    private final Database db;
    private LiveData<List<StructuredMessage>> structMessages;

    public StructuredMessagesViewModel(@NonNull Application application) {
        super(application);
        db = Database.getInstance(application);
    }

    public LiveData<List<StructuredMessage>> getStructuredMessages() {
        if (structMessages == null) {
            structMessages = db.structuredMessageDao().getAll();
        }
        return structMessages;
    }

    public SingleLiveData<Boolean> getNewStructuredMessages() {
        SingleLiveData<Boolean> data = new SingleLiveData<>();
        checkStructMessages(data);
        return data;
    }

    private void checkStructMessages(SingleLiveData<Boolean> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Connection> connections = db.connectionDao().getAllAsync();
            for (Connection c : connections) {
                Messages.getPendingMessages(c.serialized, MessageType.QUESTION).handle((res, throwable) -> {
                    if (res != null) {
                        for (Message msg : res) {
                            StructuredMessageHolder holder = StructuredMessages.extract(msg);
                            if (!db.structuredMessageDao().checkMessageExists(holder.getId(), c.id)) {
                                StructuredMessage sm = new StructuredMessage();
                                sm.connectionId = c.id;
                                sm.messageId = msg.getUid();
                                sm.entryId = holder.getId();
                                sm.questionText = holder.getQuestionText();
                                sm.questionDetail = holder.getQuestionDetail();
                                sm.answers = holder.getResponses();
                                sm.type = holder.getType();
                                sm.serialized = msg.getPayload();
                                db.structuredMessageDao().insertAll(sm);
                            }
                        }
                    }
                    liveData.postValue(true);
                    return null;
                });
            }
        });
    }

    public SingleLiveData<Boolean> answerMessage(int messageId, String answer) {
        SingleLiveData<Boolean> data = new SingleLiveData<>();
        answerStructMessage(messageId, answer, data);
        return data;
    }

    private void answerStructMessage(int messageId, String answer, SingleLiveData<Boolean> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            StructuredMessage sm = db.structuredMessageDao().getById(messageId);

            Connection con = db.connectionDao().getById(sm.connectionId);
            StructuredMessages.answer(con.serialized, sm.messageId, sm.type, sm.serialized, answer).handle((res, err) -> {
                if (err == null) {
                    sm.selectedAnswer = answer;
                    db.structuredMessageDao().update(sm);
                }
                liveData.postValue(err == null);
                return null;
            });
        });
    }


}
