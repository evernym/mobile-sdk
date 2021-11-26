package msdk.java.sample.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import msdk.java.sample.db.entity.ProofRequest;

@Dao
public interface ProofRequestDao {
    @Query("SELECT * FROM proofrequest")
    LiveData<List<ProofRequest>> getAll();

    @Query("SELECT * FROM proofrequest where id = :id")
    ProofRequest getById(int id);

    @Query("SELECT * FROM proofrequest WHERE thread_id = :threadId")
    ProofRequest getByPwDidAndThreadId(String threadId);

    @Insert
    void insertAll(ProofRequest... proofRequests);

    @Update
    void update(ProofRequest proofRequest);

    @Delete
    void delete(ProofRequest proofRequest);
}
