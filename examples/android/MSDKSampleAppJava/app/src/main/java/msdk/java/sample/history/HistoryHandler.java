package msdk.java.sample.history;

import java.util.concurrent.Executors;

import msdk.java.sample.SingleLiveData;
import msdk.java.sample.db.Database;
import msdk.java.sample.db.entity.Action;
import msdk.java.sample.homepage.Results;

import static msdk.java.sample.db.ActionStatus.HISTORIZED;
import static msdk.java.sample.homepage.Results.ACTION_FAILURE;
import static msdk.java.sample.homepage.Results.ACTION_SUCCESS;
import static msdk.java.sample.homepage.Results.FAILURE;
import static msdk.java.sample.homepage.Results.REJECT;
import static msdk.java.sample.homepage.Results.SUCCESS;

public class HistoryHandler {

    public static void addHistoryAction(
            Database db,
            String name,
            String description,
            String icon,
            SingleLiveData<Results> liveData
    ) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Action action = new Action();
                action.invite = null;
                action.name = name;
                action.description = description;
                action.icon = icon;
                action.status = HISTORIZED.toString();
                db.actionDao().insertAll(action);
                liveData.postValue(ACTION_SUCCESS);
            } catch (Exception e) {
                e.printStackTrace();
                liveData.postValue(ACTION_FAILURE);
            }
        });
    }

    public static void addToHistory(int actionId, String description, Database db, SingleLiveData<Results> liveData) {
        try {
            Action action = db.actionDao().getActionsById(actionId);
            action.status = HISTORIZED.toString();
            action.description = description;
            db.actionDao().update(action);
            liveData.postValue(SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            liveData.postValue(FAILURE);
        }
    }
}
