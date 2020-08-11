package me.connect.sdk.java.sample.db;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import me.connect.sdk.java.sample.db.dao.ConnectionDao;
import me.connect.sdk.java.sample.db.dao.CredentialOfferDao;
import me.connect.sdk.java.sample.db.dao.ProofRequestDao;
import me.connect.sdk.java.sample.db.dao.StructuredMessageDao;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.CredentialOffer;
import me.connect.sdk.java.sample.db.entity.ProofRequest;
import me.connect.sdk.java.sample.db.entity.StructuredMessage;

@androidx.room.Database(entities = {Connection.class, CredentialOffer.class, ProofRequest.class, StructuredMessage.class},
        version = 1)
@TypeConverters({ResponseConverter.class})
public abstract class Database extends RoomDatabase {
    private static final String DB_NAME = "db";
    private static Database instance = null;

    public abstract ConnectionDao connectionDao();

    public abstract CredentialOfferDao credentialOffersDao();

    public abstract ProofRequestDao proofRequestDao();

    public abstract StructuredMessageDao structuredMessageDao();

    public static Database getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), Database.class, DB_NAME).build();
        }
        return instance;
    }
}
