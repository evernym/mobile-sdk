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

    public SingleLiveData<String> performBackup() {
        SingleLiveData<String> data = new SingleLiveData<>();
        backup(data);
        return data;
    }

    private void backup(SingleLiveData<String> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            WalletBackups.create(getApplication(), Constants.WALLET_NAME, "secret_key", "backup").handle((res, err) -> {
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

    public SingleLiveData<String> performRestore() {
        SingleLiveData<String> data = new SingleLiveData<>();
        restore(data);
        return data;
    }

    private void restore(SingleLiveData<String> liveData) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Backup backup = db.backupDao().getById(1);
            WalletBackups.restore(getApplication(), "secret_key", backup.path).handle((res, err) -> {
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
