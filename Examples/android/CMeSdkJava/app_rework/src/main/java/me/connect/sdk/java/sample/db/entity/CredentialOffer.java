package me.connect.sdk.java.sample.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class CredentialOffer implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "connection_id")
    public int connectionId;


    @ColumnInfo(name = "serialized")
    public String serialized;


    @ColumnInfo(name = "claim_id")
    public Integer claimId;

    @ColumnInfo(name = "name")
    public String name;

    // todo temporary
    @ColumnInfo(name = "attributes")
    public String attributes;

    @ColumnInfo(name = "accepted")
    public Boolean accepted;
}
