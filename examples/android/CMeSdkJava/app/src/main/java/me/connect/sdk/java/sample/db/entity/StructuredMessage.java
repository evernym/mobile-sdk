package me.connect.sdk.java.sample.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

import me.connect.sdk.java.message.StructuredMessageHolder.Response;

@Entity
public class StructuredMessage {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "connection_id")
    public int connectionId;

    @ColumnInfo(name = "serialized")
    public String serialized;

    @ColumnInfo(name = "entry_id")
    public String entryId;

    @ColumnInfo(name = "message_id")
    public String messageId;

    @ColumnInfo(name = "question_text")
    public String questionText;

    @ColumnInfo(name = "question_detail")
    public String questionDetail;

    @ColumnInfo(name = "answers")
    public List<Response> answers;

    @ColumnInfo(name = "selected_answer")
    public String selectedAnswer;

    @ColumnInfo(name = "type")
    public String type;
}
