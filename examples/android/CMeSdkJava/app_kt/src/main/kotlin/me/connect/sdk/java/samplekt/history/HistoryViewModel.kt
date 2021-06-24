package me.connect.sdk.java.samplekt.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.Action

import me.connect.sdk.java.samplekt.db.ActionStatus.HISTORIZED

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private var db = Database.getInstance(application)
    private val actionsLiveData: LiveData<List<Action>> by lazy {
        db.actionDao().getActionsByStatus(HISTORIZED.toString())
    }

    fun getHistory(): LiveData<List<Action>> = actionsLiveData
}