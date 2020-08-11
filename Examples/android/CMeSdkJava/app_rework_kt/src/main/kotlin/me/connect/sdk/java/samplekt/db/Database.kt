package me.connect.sdk.java.samplekt.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.connect.sdk.java.samplekt.db.dao.CredentialOfferDao
import me.connect.sdk.java.samplekt.db.dao.ProofRequestDao
import me.connect.sdk.java.samplekt.db.dao.StructuredMessageDao
import me.connect.sdk.java.samplekt.db.entity.Connection
import me.connect.sdk.java.samplekt.db.entity.CredentialOffer
import me.connect.sdk.java.samplekt.db.entity.ProofRequest
import me.connect.sdk.java.samplekt.db.entity.StructuredMessage
import me.connect.sdk.java.samplekt.db.dao.ConnectionDao


@androidx.room.Database(entities = [
    Connection::class,
    CredentialOffer::class,
    ProofRequest::class,
    StructuredMessage::class
], version = 1)
@TypeConverters(ResponseConverter::class)
abstract class Database : RoomDatabase() {
    abstract fun connectionDao(): ConnectionDao
    abstract fun credentialOffersDao(): CredentialOfferDao
    abstract fun proofRequestDao(): ProofRequestDao
    abstract fun structuredMessageDao(): StructuredMessageDao

    companion object {
        private const val DB_NAME = "db"
        private var instance: Database? = null
        fun getInstance(context: Context): Database {
            return instance ?: Room
                    .databaseBuilder(context.applicationContext, Database::class.java, DB_NAME)
                    .build()
                    .also { instance = it }
        }
    }
}