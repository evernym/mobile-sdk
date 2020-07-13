package me.connect.sdk.java.sample.db;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import me.connect.sdk.java.sample.db.dao.ConnectionDao;
import me.connect.sdk.java.sample.db.entity.Connection;

@androidx.room.Database(entities = {Connection.class}, version = 1)
public abstract class Database extends RoomDatabase {
    private static final String DB_NAME = "db";

    public abstract ConnectionDao connectionDao();

    public static Database newInstance(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(), Database.class, DB_NAME).build();
    }
}
