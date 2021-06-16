package me.connect.sdk.java.sample.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ProofRequest {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name="thread_id")
    public String threadId;

    @ColumnInfo(name = "pwdid")
    public String pwDid;

    @ColumnInfo(name = "serialized")
    public String serialized;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "attributes")
    public String attributes;

    @ColumnInfo(name = "accepted")
    public Boolean accepted;

    @ColumnInfo(name = "message_id")
    public String messageId;

    @ColumnInfo(name = "attachConnection")
    public String attachConnection;

    @ColumnInfo(name = "attachConnectionName")
    public String attachConnectionName;

    @ColumnInfo(name = "attachConnectionLogo")
    public String attachConnectionLogo;
}