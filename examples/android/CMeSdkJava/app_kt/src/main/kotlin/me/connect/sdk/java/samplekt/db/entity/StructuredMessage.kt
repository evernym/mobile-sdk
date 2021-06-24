package me.connect.sdk.java.samplekt.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

import androidx.room.PrimaryKey
import me.connect.sdk.java.message.StructuredMessageHolder.Response


@Entity
data class StructuredMessage(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,

        @ColumnInfo(name = "pwdid")
        var pwDid: String,

        @ColumnInfo(name = "serialized")
        var serialized: String,

        @ColumnInfo(name = "entry_id")
        var entryId: String,

        @ColumnInfo(name = "answers")
        var answers: List<Response>,

        @ColumnInfo(name = "selected_answer")
        var selectedAnswer: String? = null,

        @ColumnInfo(name = "type")
        var type: String
)