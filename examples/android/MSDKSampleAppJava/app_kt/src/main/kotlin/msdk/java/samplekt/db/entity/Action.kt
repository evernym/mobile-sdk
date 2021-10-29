package msdk.java.samplekt.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import msdk.java.messages.StructuredMessage

@Entity
data class Action(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "name")
    var name: String? = null,

    @ColumnInfo(name = "type")
    var type: String? = null,

    @ColumnInfo(name = "description")
    var description: String? = null,

    @ColumnInfo(name = "details")
    var details: String? = null,

    @ColumnInfo(name = "icon")
    var icon: String? = null,

    @ColumnInfo(name = "invite")
    var invite: String? = null,

    @ColumnInfo(name = "status")
    var status: String? = null,

    @ColumnInfo(name = "pwDid")
    var pwDid: String? = null,

    @ColumnInfo(name = "claimId")
    var claimId: String? = null,

    @ColumnInfo(name = "threadId")
    var threadId: String? = null,

    @ColumnInfo(name = "entryId")
    var entryId: String? = null,

    @ColumnInfo(name = "selectedAnswer")
    var selectedAnswer: String? = null,

    @ColumnInfo(name = "messageAnswers")
    var messageAnswers: List<StructuredMessage.Response>? = null
)