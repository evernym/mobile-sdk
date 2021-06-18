package me.connect.sdk.java.samplekt.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import me.connect.sdk.java.samplekt.db.entity.Action

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
    fun insertAll(vararg actions: Action)

    @Update
    fun update(action: Action)

    @Delete
    fun delete(action: Action)
}