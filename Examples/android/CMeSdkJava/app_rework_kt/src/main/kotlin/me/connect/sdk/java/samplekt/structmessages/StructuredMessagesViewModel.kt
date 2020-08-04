package me.connect.sdk.java.samplekt.structmessages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.StructuredMessage
import me.connect.sdk.java.Messages
import me.connect.sdk.java.StructuredMessages
import me.connect.sdk.java.message.MessageType
import java.util.concurrent.Executors


class StructuredMessagesViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Database.getInstance(application)
    private val structMessages by lazy {
        MutableLiveData<List<StructuredMessage>>()
    }

    fun getStructuredMessages(): LiveData<List<StructuredMessage>> {
        loadStructuredMessages()
        return structMessages
    }

    private fun loadStructuredMessages() {
        Executors.newSingleThreadExecutor().execute {
            val data = db.structuredMessageDao().getAll()
            structMessages.postValue(data)
        }
    }

    fun getNewStructuredMessages(): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        checkStructMessages(data)
        return data
    }

    private fun checkStructMessages(liveData: SingleLiveData<Boolean>) {
        Executors.newSingleThreadExecutor().execute {
            val connections = db.connectionDao().getAll()
            connections.forEach { c ->
                Messages.getPendingMessages(c.serialized, MessageType.QUESTION).handle { res, throwable ->
                    res?.forEach { msg ->
                        val holder = StructuredMessages.extract(msg)
                        if (!db.structuredMessageDao().checkMessageExists(holder.id, c.id)) {
                            val sm = StructuredMessage(
                                    connectionId = c.id,
                                    messageId = msg.uid,
                                    entryId = holder.id,
                                    questionText = holder.questionText,
                                    questionDetail = holder.questionDetail,
                                    answers = holder.responses,
                                    serialized = msg.payload
                            )
                            db.structuredMessageDao().insertAll(sm)
                            loadStructuredMessages()
                        }
                    }
                    liveData.postValue(true)
                }
            }
        }
    }

    fun answerMessage(messageId: Int, nonce: String): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        answerStructMessage(messageId, nonce, data)
        return data
    }

    private fun answerStructMessage(messageId: Int, nonce: String, liveData: SingleLiveData<Boolean>) {
        Executors.newSingleThreadExecutor().execute {
            val sm = db.structuredMessageDao().getById(messageId)
            val con = db.connectionDao().getById(sm.connectionId)
            StructuredMessages.answer(con.serialized, sm.messageId, nonce).handle { res, err ->
                if (res != null) {
                    var sa = ""
                    for (r in sm.answers) {
                        if (r.nonce == nonce) {
                            sa = r.text
                        }
                        sm.selectedAnswer = sa
                        db.structuredMessageDao().update(sm)
                    }
                }
                loadStructuredMessages()
                liveData.postValue(err == null)
            }
        }
    }
}