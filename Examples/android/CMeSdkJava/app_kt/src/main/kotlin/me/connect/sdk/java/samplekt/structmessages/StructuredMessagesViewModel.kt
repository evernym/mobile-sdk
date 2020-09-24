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
                                serialized = msg.payload,
                                type = holder.type
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


    fun answerMessage(messageId: Int, answer: String): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        answerStructMessage(messageId, answer, data)
        return data
    }

    private fun answerStructMessage(messageId: Int, answer: String, liveData: SingleLiveData<Boolean>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val sm = db.structuredMessageDao().getById(messageId)
            val con = db.connectionDao().getById(sm.connectionId)
            StructuredMessages.answer(con.serialized, sm.messageId, sm.type, sm.serialized, answer).wrap().await()
            sm.selectedAnswer = answer;
            db.structuredMessageDao().update(sm)
            loadStructuredMessages()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            liveData.postValue(true)
        }
    }


}