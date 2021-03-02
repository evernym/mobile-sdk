package me.connect.sdk.java.samplekt.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import me.connect.sdk.java.samplekt.db.entity.CredentialOffer


@Dao
interface CredentialOfferDao {
    @Query("SELECT * FROM credentialoffer")
    fun getAll(): LiveData<List<CredentialOffer>>

    @Query("SELECT * FROM credentialoffer WHERE id = :id")
    suspend fun getById(id: Int): CredentialOffer

    @Insert
    suspend fun insertAll(vararg connections: CredentialOffer)

    @Query("SELECT EXISTS(SELECT * FROM credentialoffer WHERE (claim_id = :claimId AND connection_id = :connectionId))")
    suspend fun checkOfferExists(claimId: String, connectionId: Int): Boolean

    @Update
    suspend fun update(connection: CredentialOffer)

    @Delete
    suspend fun delete(connection: CredentialOffer)
}