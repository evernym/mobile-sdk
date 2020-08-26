package me.connect.sdk.java.samplekt.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Backup(
        @PrimaryKey
        var id: Int,
        @ColumnInfo
        var path: String
)