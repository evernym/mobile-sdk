package me.connect.sdk.java.samplekt.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

import androidx.room.PrimaryKey


@Entity
data class Connection(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,

        @ColumnInfo(name = "serialized")
        var serialized: String,

        @ColumnInfo(name = "name")
        var name: String,

        @ColumnInfo(name = "icon")
        var icon: String?
)
