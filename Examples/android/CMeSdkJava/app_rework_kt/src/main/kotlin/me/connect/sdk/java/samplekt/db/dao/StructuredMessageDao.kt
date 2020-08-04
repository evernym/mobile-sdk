package me.connect.sdk.java.samplekt.db.dao

import androidx.room.*
import me.connect.sdk.java.samplekt.db.entity.StructuredMessage


@Dao
interface StructuredMessageDao {
    @Query("SELECT * FROM structuredmessage")
    fun getAll(): List<StructuredMessage>

    @Query("SELECT * FROM structuredmessage WHERE id = :id")
    fun getById(id: Int): StructuredMessage

    @Insert
    fun insertAll(vararg messages: StructuredMessage)

    @Query("SELECT EXISTS(SELECT * FROM structuredmessage WHERE (entry_id = :entryId AND connection_id = :connectionId))")
    fun checkMessageExists(entryId: String, connectionId: Int): Boolean

    @Update
    fun update(message: StructuredMessage)

    @Delete
    fun delete(message: StructuredMessage)
}
