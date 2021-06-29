package me.connect.sdk.java.sample.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Connection {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "serialized")
    public String serialized;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "icon")
    public String icon;

    @ColumnInfo(name = "pwdid")
    public String pwDid;

    @ColumnInfo(name = "invitation")
    public String invitation;
}
