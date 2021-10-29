package msdk.java.samplekt.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

import androidx.room.PrimaryKey


@Entity
data class ProofRequest(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,

        @ColumnInfo(name = "thread_id")
        var threadId: String,

        @ColumnInfo(name = "pwdid")
        var pwDid: String? = null,

        @ColumnInfo(name = "serialized")
        var serialized: String,

        @ColumnInfo(name = "attachConnection")
        var attachConnection: String? = null,

        @ColumnInfo(name = "attachConnectionName")
        var attachConnectionName: String? = null,

        @ColumnInfo(name = "attachConnectionLogo")
        var attachConnectionLogo: String? = null
)