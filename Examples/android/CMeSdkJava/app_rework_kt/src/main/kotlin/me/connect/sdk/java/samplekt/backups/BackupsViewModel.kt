package me.connect.sdk.java.samplekt.backups

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.connect.sdk.java.WalletBackups
import me.connect.sdk.java.samplekt.Constants
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.Backup


class BackupsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Database.getInstance(application)

    fun performBackup(): SingleLiveData<String> {
        val data = SingleLiveData<String>()
        backup(data)
        return data
    }

    private fun backup(liveData: SingleLiveData<String>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val res = WalletBackups.create(getApplication(), Constants.WALLET_NAME, "secret_key", "backup").get()
            val backup = Backup(
                    id = 1,
                    path = res
            )
            db.backupDao().insertAll(backup)
            liveData.postValue("Saved to: $res")
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue("Error: $e")
        }
    }

    fun performRestore(): SingleLiveData<String> {
        val data = SingleLiveData<String>()
        restore(data)
        return data
    }

    private fun restore(liveData: SingleLiveData<String>) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val backup = db.backupDao().getById(1)!!
            WalletBackups.restore(getApplication(), "secret_key", backup.path).get()
            liveData.postValue("Success")
        } catch (e: Exception) {
            e.printStackTrace()
            liveData.postValue("Error: $e")
        }
    }

    fun getLastBackup(): SingleLiveData<String?> {
        val data = SingleLiveData<String?>()
        getState(data)
        return data
    }

    private fun getState(liveData: SingleLiveData<String?>) = viewModelScope.launch(Dispatchers.IO) {
        val backup = db.backupDao().getById(1)
        liveData.postValue(backup?.path)
    }

}
