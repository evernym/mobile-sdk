package me.connect.sdk.java.samplekt.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

import androidx.room.PrimaryKey


@Entity
data class CredentialOffer(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,

        @ColumnInfo(name = "connection_id")
        var connectionId: Int,

        @ColumnInfo(name = "serialized")
        var serialized: String,

        @ColumnInfo(name = "claim_id")
        var claimId: String,

        @ColumnInfo(name = "name")
        var name: String,

        // todo temporary
        @ColumnInfo(name = "attributes")
        var attributes: String,

        @ColumnInfo(name = "accepted")
        var accepted: Boolean? = null,

        @ColumnInfo(name = "message_id")
        var messageId: String
)