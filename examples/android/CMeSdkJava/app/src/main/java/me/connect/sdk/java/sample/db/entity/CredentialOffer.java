package me.connect.sdk.java.sample.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class CredentialOffer implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "pwdid")
    public String pwDid;

    @ColumnInfo(name = "serialized")
    public String serialized;

    @ColumnInfo(name = "claim_id")
    public String claimId;

    @ColumnInfo(name = "name")
    public String name;

    // todo temporary
    @ColumnInfo(name = "attributes")
    public String attributes;

    @ColumnInfo(name = "accepted")
    public Boolean accepted;

    @ColumnInfo(name = "message_id")
    public String messageId;
}
