package msdk.kotlin.sample.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import msdk.kotlin.sample.db.entity.ProofRequest


@Dao
interface ProofRequestDao {
    @Query("SELECT * FROM proofrequest")
    fun getAll(): LiveData<List<ProofRequest>>

    @Query("SELECT * FROM proofrequest where id = :id")
    suspend fun getById(id: Int): ProofRequest

    @Query("SELECT * FROM proofrequest WHERE thread_id = :threadId")
    fun getByPwDidAndThreadId(threadId: String?): ProofRequest?

    @Insert
    suspend fun insertAll(vararg proofRequests: ProofRequest)

    @Update
    suspend fun update(proofRequest: ProofRequest)

    @Delete
    suspend fun delete(proofRequest: ProofRequest)
}