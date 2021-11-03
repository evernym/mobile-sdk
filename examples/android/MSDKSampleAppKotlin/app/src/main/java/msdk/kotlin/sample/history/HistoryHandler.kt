package msdk.kotlin.sample.history

import msdk.kotlin.sample.SingleLiveData
import msdk.kotlin.sample.db.ActionStatus
import msdk.kotlin.sample.db.Database
import msdk.kotlin.sample.db.entity.Action
import msdk.kotlin.sample.homepage.Results

object HistoryHandler {
    suspend fun addHistoryAction(
        db: Database,
        name: String,
        description: String,
        icon: String,
        liveData: SingleLiveData<Results>
    ) {
        try {
            val action = Action(
                invite = null,
                name = name,
                description = description,
                icon = icon,
                status = ActionStatus.HISTORIZED.toString()
            )

            db.actionDao().insertAll(action)
            liveData.postValue(Results.ACTION_SUCCESS)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            liveData.postValue(Results.ACTION_FAILURE)
        }
    }

    suspend fun addToHistory(
        actionId: Int,
        description: String,
        db: Database,
        liveData: SingleLiveData<Results>
    ) {
        try {
            val action = db.actionDao().getActionsById(actionId)
            action.status = ActionStatus.HISTORIZED.toString()
            action.description = description
            db.actionDao().update(action)
            liveData.postValue(Results.REJECT)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            liveData.postValue(Results.FAILURE)
        }
    }
}