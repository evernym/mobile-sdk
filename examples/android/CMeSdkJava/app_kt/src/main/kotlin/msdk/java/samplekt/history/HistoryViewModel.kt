package msdk.java.samplekt.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import msdk.java.samplekt.db.Database
import msdk.java.samplekt.db.entity.Action

import msdk.java.samplekt.db.ActionStatus.HISTORIZED

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private var db = Database.getInstance(application)
    private val actionsLiveData: LiveData<List<Action>> by lazy {
        db.actionDao().getActionsByStatus(HISTORIZED.toString())
    }

    fun getHistory(): LiveData<List<Action>> = actionsLiveData
}