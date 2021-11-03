package msdk.java.sample.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
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
