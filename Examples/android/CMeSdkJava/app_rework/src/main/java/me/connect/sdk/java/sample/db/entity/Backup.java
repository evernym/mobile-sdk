package me.connect.sdk.java.sample.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Backup {
    @PrimaryKey
    public int id;

    @ColumnInfo
    public String path;
}
