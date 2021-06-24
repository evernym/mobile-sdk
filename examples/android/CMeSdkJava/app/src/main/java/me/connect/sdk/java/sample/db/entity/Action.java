package me.connect.sdk.java.sample.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

import me.connect.sdk.java.message.StructuredMessageHolder;

@Entity
public class Action {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "type")
    public String type = null;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "details")
    public String details;

    @ColumnInfo(name = "icon")
    public String icon;

    @ColumnInfo(name = "invite")
    public String invite;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "pwDid")
    public String pwDid;

    @ColumnInfo(name = "claimId")
    public String claimId;

    @ColumnInfo(name="threadId")
    public String threadId;

    @ColumnInfo(name = "entryId")
    public String entryId;

    @ColumnInfo(name = "selectedAnswer")
    public String selectedAnswer;

    @ColumnInfo(name = "messageAnswers")
    public List<StructuredMessageHolder.Response> messageAnswers;
}
