package msdk.kotlin.sample.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import msdk.kotlin.sample.db.Database
import msdk.kotlin.sample.db.entity.Action

import msdk.kotlin.sample.db.ActionStatus.HISTORIZED

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private var db = Database.getInstance(application)
    private val actionsLiveData: LiveData<List<Action>> by lazy {
        db.actionDao().getActionsByStatus(HISTORIZED.toString())
    }

    fun getHistory(): LiveData<List<Action>> = actionsLiveData
}