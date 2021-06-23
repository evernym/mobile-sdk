package me.connect.sdk.java.samplekt.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

import androidx.room.PrimaryKey


@Entity
data class CredentialOffer(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,

        @ColumnInfo(name = "pwdid")
        var pwDid: String? = null,

        @ColumnInfo(name = "threadId")
        var threadId: String,

        @ColumnInfo(name = "serialized")
        var serialized: String,

        @ColumnInfo(name = "claim_id")
        var claimId: String,

        @ColumnInfo(name = "attachConnection")
        var attachConnection: String? = null,

        @ColumnInfo(name = "attachConnectionName")
        var attachConnectionName: String? = null,

        @ColumnInfo(name = "attachConnectionLogo")
        var attachConnectionLogo: String? = null
)