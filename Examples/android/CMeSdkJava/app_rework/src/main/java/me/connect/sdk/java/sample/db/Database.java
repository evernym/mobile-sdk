package me.connect.sdk.java.sample.db;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import me.connect.sdk.java.sample.db.dao.ConnectionDao;
import me.connect.sdk.java.sample.db.dao.CredentialOfferDao;
import me.connect.sdk.java.sample.db.entity.Connection;
import me.connect.sdk.java.sample.db.entity.CredentialOffer;

@androidx.room.Database(entities = {Connection.class, CredentialOffer.class}, version = 1)
public abstract class Database extends RoomDatabase {
    private static final String DB_NAME = "db";
    private static Database instance = null;

    public abstract ConnectionDao connectionDao();

    public abstract CredentialOfferDao credentialOffersDao();

    public static Database getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), Database.class, DB_NAME).build();
        }
        return instance;
    }
}
