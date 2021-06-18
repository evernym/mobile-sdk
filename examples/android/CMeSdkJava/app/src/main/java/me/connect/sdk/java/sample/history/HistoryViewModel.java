package me.connect.sdk.java.sample.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Action;

import static me.connect.sdk.java.sample.db.ActionStatus.HISTORIZED;

public class HistoryViewModel extends AndroidViewModel {
    private final Database db;
    private LiveData<List<Action>> actions;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        db = Database.getInstance(application);
    }

    public LiveData<List<Action>> getHistory() {
        if (actions == null) {
            actions = db.actionDao().getActionsByStatus(HISTORIZED.toString());
        }
        return actions;
    }
}
