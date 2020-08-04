package me.connect.sdk.java.samplekt.db.dao

import androidx.room.*
import me.connect.sdk.java.samplekt.db.entity.ProofRequest


@Dao
interface ProofRequestDao {
    @Query("SELECT * FROM proofrequest")
    fun getAll(): List<ProofRequest>

    @Query("SELECT * FROM proofrequest where id = :id")
    fun getById(id: Int): ProofRequest

    @Query("SELECT EXISTS(SELECT * FROM proofrequest WHERE thread_id = :threadId)")
    fun checkExists(threadId: String): Boolean

    @Insert
    fun insertAll(vararg proofRequests: ProofRequest)

    @Update
    fun update(proofRequest: ProofRequest)

    @Delete
    fun delete(proofRequest: ProofRequest)
}