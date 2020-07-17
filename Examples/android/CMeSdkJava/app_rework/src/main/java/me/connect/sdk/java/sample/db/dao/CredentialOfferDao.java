package me.connect.sdk.java.sample.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import me.connect.sdk.java.sample.db.entity.CredentialOffer;

@Dao
public interface CredentialOfferDao {
    @Query("SELECT * FROM credentialoffer")
    List<CredentialOffer> getAll();

    @Query("SELECT * FROM credentialoffer WHERE id = :id")
    CredentialOffer getById(int id);

    @Insert
    void insertAll(CredentialOffer... connections);

    @Query("SELECT EXISTS(SELECT * FROM credentialoffer WHERE claim_id = :claimId)")
    boolean checkOfferExists(int claimId);

    @Update
    void update(CredentialOffer connection);

    @Delete
    void delete(CredentialOffer connection);
}
