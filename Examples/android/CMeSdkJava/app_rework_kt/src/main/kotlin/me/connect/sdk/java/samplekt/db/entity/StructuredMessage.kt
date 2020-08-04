package me.connect.sdk.java.samplekt.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

import androidx.room.PrimaryKey
import me.connect.sdk.java.message.StructuredMessageHolder.Response


@Entity
data class StructuredMessage(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,

        @ColumnInfo(name = "connection_id")
        var connectionId: Int,

        @ColumnInfo(name = "serialized")
        var serialized: String,

        @ColumnInfo(name = "entry_id")
        var entryId: String,

        @ColumnInfo(name = "message_id")
        var messageId: String,

        @ColumnInfo(name = "question_text")
        var questionText: String,

        @ColumnInfo(name = "question_detail")
        var questionDetail: String,

        @ColumnInfo(name = "answers")
        var answers: List<Response>,

        @ColumnInfo(name = "selected_answer")
        var selectedAnswer: String? = null
)