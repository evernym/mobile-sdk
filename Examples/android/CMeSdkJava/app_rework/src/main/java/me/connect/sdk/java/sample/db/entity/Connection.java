package me.connect.sdk.java.sample.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Connection {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "serialized_connection")
    public String serializedConnection;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "icon")
    public String icon;
}
