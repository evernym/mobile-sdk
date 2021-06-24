package me.connect.sdk.java.samplekt.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import me.connect.sdk.java.samplekt.db.entity.Action

@Dao
interface ActionDao {
    @Query("SELECT * FROM `action`")
    fun getAllAsync(): List<Action>

    @Query("SELECT * FROM `action`")
    fun getAll(): LiveData<List<Action>>

    @Query("SELECT * FROM `action` WHERE id = :id")
    fun getActionsById(id: Int): Action

    @Query("SELECT * FROM `action` WHERE status = :status")
    fun getActionsByStatus(status: String?): LiveData<List<Action>>

    @Insert
    suspend fun insertAll(vararg actions: Action)

    @Update
    suspend fun update(action: Action)

    @Delete
    suspend fun delete(action: Action)
}