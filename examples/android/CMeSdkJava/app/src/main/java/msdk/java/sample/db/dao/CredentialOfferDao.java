package msdk.java.sample.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import msdk.java.sample.db.entity.CredentialOffer;

@Dao
public interface CredentialOfferDao {
    @Query("SELECT * FROM credentialoffer")
    LiveData<List<CredentialOffer>> getAll();

    @Query("SELECT * FROM credentialoffer WHERE id = :id")
    CredentialOffer getById(int id);

    @Insert
    void insertAll(CredentialOffer... connections);

    @Query("SELECT EXISTS(SELECT * FROM credentialoffer WHERE (claim_id = :claimId))")
    boolean checkOfferExists(String claimId);

    @Query("SELECT * FROM credentialoffer WHERE (claim_id = :claimId AND pwDid = :pwDid)")
    CredentialOffer getByPwDidAndClaimId(String claimId, String pwDid);

    @Update
    void update(CredentialOffer connection);

    @Delete
    void delete(CredentialOffer connection);
}
