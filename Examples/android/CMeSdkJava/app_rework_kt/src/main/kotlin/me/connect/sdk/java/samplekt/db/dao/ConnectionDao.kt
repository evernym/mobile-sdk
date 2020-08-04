package me.connect.sdk.java.samplekt.db.dao


import androidx.room.*

import me.connect.sdk.java.samplekt.db.entity.Connection


@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connection")
    fun getAll(): List<Connection>

    @Query("SELECT * FROM connection WHERE id = :id")
    fun getById(id: Int): Connection

    @Insert
    fun insertAll(vararg connections: Connection)

    @Update
    fun update(connection: Connection)

    @Delete
    fun delete(connection: Connection)
}
