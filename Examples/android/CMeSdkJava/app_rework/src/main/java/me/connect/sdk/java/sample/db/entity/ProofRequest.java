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

    @ColumnInfo(name = "connection_id")
    public int connectionId;

    @ColumnInfo(name = "serialized")
    public String serialized;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "attributes")
    public String attributes;

    @ColumnInfo(name = "accepted")
    public Boolean accepted;
}