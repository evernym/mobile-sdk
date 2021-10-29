package msdk.java.sample.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

import msdk.java.messages.StructuredMessage.Response;

@Entity
public class StructuredMessage {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "pwdid")
    public String pwDid;

    @ColumnInfo(name = "serialized")
    public String serialized;

    @ColumnInfo(name = "entry_id")
    public String entryId;

    @ColumnInfo(name = "answers")
    public List<Response> answers;

    @ColumnInfo(name = "selected_answer")
    public String selectedAnswer;

    @ColumnInfo(name = "type")
    public String type;
}
