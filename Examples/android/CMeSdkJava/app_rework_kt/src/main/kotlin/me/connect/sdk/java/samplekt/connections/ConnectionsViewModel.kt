package me.connect.sdk.java.samplekt.connections

import android.app.Application
import android.net.Uri
import android.util.Base64
import android.webkit.URLUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.connect.sdk.java.samplekt.SingleLiveData
import me.connect.sdk.java.samplekt.db.Database
import me.connect.sdk.java.samplekt.db.entity.Connection
import me.connect.sdk.java.Connections
import me.connect.sdk.java.connection.QRConnection
import me.connect.sdk.java.message.MessageState
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.Executors


class ConnectionsViewModel(application: Application) : AndroidViewModel(application) {
    private val db: Database = Database.getInstance(application)
    private val connections: MutableLiveData<List<Connection>> by lazy {
        MutableLiveData<List<Connection>>()
    }

    fun getConnections(): LiveData<List<Connection>> {
        loadConnections()
        return connections
    }

    fun newConnection(invite: String): SingleLiveData<Boolean> {
        val data = SingleLiveData<Boolean>()
        createConnection(invite, data)
        return data
    }

    private fun loadConnections() {
        Executors.newSingleThreadExecutor().execute {
            val data = db.connectionDao().getAll()
            connections.postValue(data)
        }
    }

    private fun createConnection(invite: String, liveData: SingleLiveData<Boolean>) {
        Executors.newSingleThreadExecutor().execute {
            val parsedInvite = parseInvite(invite)
            val data = extractDataFromInvite(parsedInvite)
            Connections.create(parsedInvite, QRConnection())
                    .handle { res, throwable ->
                        if (res != null) {
                            val serializedCon = Connections.awaitStatusChange(res, MessageState.ACCEPTED)
                            val c = Connection(
                                    name = data!!.name,
                                    icon = data.logo,
                                    serialized = serializedCon
                            )
                            db.connectionDao().insertAll(c)
                            loadConnections()
                        }
                        liveData.postValue(throwable == null)
                    }
        }
    }

    private fun parseInvite(invite: String): String = if (URLUtil.isValidUrl(invite)) {
        val uri = Uri.parse(invite)
        val param = uri.getQueryParameter("c_i")
        if (param != null) {
            String(Base64.decode(param, Base64.NO_WRAP))
        } else {
            ""
        }
    } else {
        invite
    }

    private fun extractDataFromInvite(invite: String): ConnDataHolder? {
        return try {
            val json = JSONObject(invite)
            if (json.has("label")) {
                val label = json.getString("label")
                return ConnDataHolder(label, null)
            }
            val data = json.optJSONObject("s")
            if (data != null) {
                ConnDataHolder(data.getString("n"), data.getString("l"))
            } else {
                // workaround in case details missing
                val sourceId: String = json.getString("id")
                ConnDataHolder(sourceId, null)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    internal class ConnDataHolder(var name: String, var logo: String?)
}
