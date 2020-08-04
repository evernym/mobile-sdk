package me.connect.sdk.java.samplekt.db.dao

import androidx.room.*
import me.connect.sdk.java.samplekt.db.entity.CredentialOffer


@Dao
interface CredentialOfferDao {
    @Query("SELECT * FROM credentialoffer")
    fun getAll(): List<CredentialOffer>

    @Query("SELECT * FROM credentialoffer WHERE id = :id")
    fun getById(id: Int): CredentialOffer

    @Insert
    fun insertAll(vararg connections: CredentialOffer)

    @Query("SELECT EXISTS(SELECT * FROM credentialoffer WHERE (claim_id = :claimId AND connection_id = :connectionId))")
    fun checkOfferExists(claimId: String, connectionId: Int): Boolean

    @Update
    fun update(connection: CredentialOffer)

    @Delete
    fun delete(connection: CredentialOffer)
}