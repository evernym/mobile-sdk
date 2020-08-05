package me.connect.sdk.java.samplekt.db.dao


import androidx.room.*

import me.connect.sdk.java.samplekt.db.entity.Connection


@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connection")
    suspend fun getAll(): List<Connection>

    @Query("SELECT * FROM connection WHERE id = :id")
    suspend fun getById(id: Int): Connection

    @Insert
    suspend fun insertAll(vararg connections: Connection)

    @Update
    suspend fun update(connection: Connection)

    @Delete
    suspend fun delete(connection: Connection)
}
