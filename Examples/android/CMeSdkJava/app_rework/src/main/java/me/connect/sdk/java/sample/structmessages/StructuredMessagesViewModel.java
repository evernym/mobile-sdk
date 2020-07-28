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
import me.connect.sdk.java.message.MessageType;
import me.connect.sdk.java.message.StructuredMessageHolder;
import me.connect.sdk.java.message.StructuredMessageHolder.Response;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.StructuredMessage;

public class StructuredMessagesViewModel extends AndroidViewModel {
    private final Database db;
    private MutableLiveData<List<StructuredMessage>> structMessages;

    public StructuredMessagesViewModel(@NonNull Application application) {
        super(application);
        db = Database.getInstance(application);
    }

    public LiveData<List<StructuredMessage>> getStructuredMessages() {
        if (structMessages == null) {
            structMessages = new MutableLiveData<>();
        }
        loadStructuredMessages();
        return structMessages;
    }

    private void loadStructuredMessages() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<StructuredMessage> data = db.structuredMessageDao().getAll();
            structMessages.postValue(data);
        });
    }

    public SingleLiveData<Boolean> getNewStructuredMessages() {
        SingleLiveData<Boolean> data = new SingleLiveData<>();
        checkStructMessages(data);
        return data;
    }

    private void checkStructMessages(SingleLiveData<Boolean> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Connection> connections = db.connectionDao().getAll();
            for (Connection c : connections) {
                Messages.getPendingMessages(c.serialized, MessageType.QUESTION).handle((res, throwable) -> {
                    if (res != null) {
                        for (String msg : res) {
                            StructuredMessageHolder holder = StructuredMessages.extract(msg);
                            if (!db.structuredMessageDao().checkMessageExists(holder.getId(), c.id)) {
                                StructuredMessage sm = new StructuredMessage();
                                sm.connectionId = c.id;
                                sm.messageId = holder.getMessageId();
                                sm.entryId = holder.getId();
                                sm.questionText = holder.getQuestionText();
                                sm.questionDetail = holder.getQuestionDetail();
                                sm.answers = holder.getResponses();
                                sm.serialized = msg;
                                db.structuredMessageDao().insertAll(sm);
                                loadStructuredMessages();
                            }
                        }
                    }
                    liveData.postValue(true);
                    return null;
                });
            }
        });
    }

    public SingleLiveData<Boolean> answerMessage(int messageId, String nonce) {
        SingleLiveData<Boolean> data = new SingleLiveData<>();
        answerStructMessage(messageId, nonce, data);
        return data;
    }

    private void answerStructMessage(int messageId, String nonce, SingleLiveData<Boolean> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            StructuredMessage sm = db.structuredMessageDao().getById(messageId);

            Connection con = db.connectionDao().getById(sm.connectionId);
            StructuredMessages.answer(con.serialized, sm.messageId, nonce).handle((res, err) -> {
                if (res != null) {
                    String sa = "";
                    for (Response r : sm.answers) {
                        if (r.getNonce().equals(nonce)) {
                            sa = r.getText();
                        }
                        sm.selectedAnswer = sa;
                        db.structuredMessageDao().update(sm);
                    }
                }
                loadStructuredMessages();
                liveData.postValue(err == null);
                return null;
            });
        });
    }


}
