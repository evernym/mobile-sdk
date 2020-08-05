package me.connect.sdk.java.samplekt.structmessages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.StructuredMessage
import me.connect.sdk.java.Messages
import me.connect.sdk.java.StructuredMessages
import me.connect.sdk.java.message.MessageType
import me.connect.sdk.java.samplekt.wrap
import java.lang.Exception
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

    private fun loadStructuredMessages() = viewModelScope.launch(Dispatchers.IO) {
        val data = db.structuredMessageDao().getAll()
        structMessages.postValue(data)
    }

    fun getNewStructuredMessages(): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        checkStructMessages(data)
        return data
    }

    private fun checkStructMessages(liveData: SingleLiveData<Boolean>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val connections = db.connectionDao().getAll()
            connections.forEach { c ->
                val res = Messages.getPendingMessages(c.serialized, MessageType.QUESTION).wrap().await()
                res.forEach { msg ->
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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            liveData.postValue(true)
        }
    }


    fun answerMessage(messageId: Int, nonce: String): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        answerStructMessage(messageId, nonce, data)
        return data
    }

    private fun answerStructMessage(messageId: Int, nonce: String, liveData: SingleLiveData<Boolean>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val sm = db.structuredMessageDao().getById(messageId)
            val con = db.connectionDao().getById(sm.connectionId)
            val res = StructuredMessages.answer(con.serialized, sm.messageId, nonce).wrap().await()
            val sa = sm.answers.first { it.nonce == nonce }.text
            sm.selectedAnswer = sa
            db.structuredMessageDao().update(sm)
            loadStructuredMessages()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            liveData.postValue(true)
        }
    }


}