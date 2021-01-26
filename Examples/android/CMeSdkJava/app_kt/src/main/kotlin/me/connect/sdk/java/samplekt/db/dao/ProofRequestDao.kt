package me.connect.sdk.java.samplekt.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import me.connect.sdk.java.samplekt.db.entity.ProofRequest


@Dao
interface ProofRequestDao {
    @Query("SELECT * FROM proofrequest")
    fun getAll(): LiveData<List<ProofRequest>>

    @Query("SELECT * FROM proofrequest where id = :id")
    suspend fun getById(id: Int): ProofRequest

    @Query("SELECT EXISTS(SELECT * FROM proofrequest WHERE thread_id = :threadId)")
    suspend fun checkExists(threadId: String): Boolean

    @Insert
    suspend fun insertAll(vararg proofRequests: ProofRequest)

    @Update
    suspend fun update(proofRequest: ProofRequest)

    @Delete
    suspend fun delete(proofRequest: ProofRequest)
}