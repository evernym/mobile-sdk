package me.connect.sdk.java.sample.backups;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import java.util.concurrent.Executors;

import me.connect.sdk.java.WalletBackups;
import me.connect.sdk.java.sample.Constants;
import me.connect.sdk.java.sample.SingleLiveData;
import me.connect.sdk.java.sample.db.Database;
import me.connect.sdk.java.sample.db.entity.Backup;

public class BackupsViewModel extends AndroidViewModel {
    private final Database db;


    public BackupsViewModel(Application application) {
        super(application);
        db = Database.getInstance(application);
    }

    public SingleLiveData<String> performBackup(String backupKey) {
        SingleLiveData<String> data = new SingleLiveData<>();
        backup(backupKey, data);
        return data;
    }

    private void backup(String backupKey, SingleLiveData<String> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            WalletBackups.create(getApplication(), Constants.WALLET_NAME, backupKey, "backup").handle((res, err) -> {
                if (res != null) {
                    Backup backup = new Backup();
                    backup.id = 1;
                    backup.path = res;
                    db.backupDao().insertAll(backup);
                    liveData.postValue("Saved to: " + res);
                } else {
                    err.printStackTrace();
                    liveData.postValue("Error: " + err.toString());
                }
                return null;
            });
        });
    }

    public SingleLiveData<String> performRestore(String backupKey) {
        SingleLiveData<String> data = new SingleLiveData<>();
        restore(backupKey, data);
        return data;
    }

    private void restore(String backupKey, SingleLiveData<String> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Backup backup = db.backupDao().getById(1);
            WalletBackups.restore(getApplication(), backupKey, backup.path).handle((res, err) -> {
                if (err == null) {
                    liveData.postValue("Success");
                } else {
                    err.printStackTrace();
                    liveData.postValue("Error: " + err.toString());
                }
                return null;
            });
        });
    }

    public SingleLiveData<String> getLastBackup() {
        SingleLiveData<String> data = new SingleLiveData<>();
        getState(data);
        return data;
    }

    private void getState(SingleLiveData<String> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Backup backup = db.backupDao().getById(1);
            if (backup != null) {
                liveData.postValue(backup.path);
            } else {
                liveData.postValue(null);
            }
        });
    }
}
