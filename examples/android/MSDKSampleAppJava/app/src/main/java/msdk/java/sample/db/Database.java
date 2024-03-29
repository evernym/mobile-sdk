package msdk.java.sample.db;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import msdk.java.sample.db.dao.ActionDao;
import msdk.java.sample.db.dao.BackupDao;
import msdk.java.sample.db.dao.ConnectionDao;
import msdk.java.sample.db.dao.CredentialOfferDao;
import msdk.java.sample.db.dao.ProofRequestDao;
import msdk.java.sample.db.dao.StructuredMessageDao;
import msdk.java.sample.db.entity.Action;
import msdk.java.sample.db.entity.Backup;
import msdk.java.sample.db.entity.Connection;
import msdk.java.sample.db.entity.CredentialOffer;
import msdk.java.sample.db.entity.ProofRequest;
import msdk.java.sample.db.entity.StructuredMessage;

@androidx.room.Database(entities = {
        Backup.class,
        Connection.class,
        CredentialOffer.class,
        ProofRequest.class,
        StructuredMessage.class,
        Action.class
},
        version = 1)

@TypeConverters({ResponseConverter.class})
public abstract class Database extends RoomDatabase {
    private static final String DB_NAME = "db";
    private static Database instance = null;

    public abstract BackupDao backupDao();

    public abstract ConnectionDao connectionDao();

    public abstract CredentialOfferDao credentialOffersDao();

    public abstract ProofRequestDao proofRequestDao();

    public abstract ActionDao actionDao();

    public abstract StructuredMessageDao structuredMessageDao();

    public static Database getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), Database.class, DB_NAME).build();
        }
        return instance;
    }
}
