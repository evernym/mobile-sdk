package me.connect.sdk.java.sample.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import me.connect.sdk.java.sample.db.entity.StructuredMessage;

@Dao
public interface StructuredMessageDao {

    @Query("SELECT * FROM structuredmessage")
    LiveData<List<StructuredMessage>> getAll();

    @Query("SELECT * FROM structuredmessage WHERE id = :id")
    StructuredMessage getById(int id);

    @Insert
    void insertAll(StructuredMessage... messages);

    @Query("SELECT EXISTS(SELECT * FROM structuredmessage WHERE (entry_id = :entryId AND pwdid = :pwDid))")
    boolean checkMessageExists(String entryId, String pwDid);

    @Query("SELECT * FROM structuredmessage WHERE (entry_id = :entryId AND pwdid = :pwDid)")
    StructuredMessage getByEntryIdAndPwDid(String entryId, String pwDid);

    @Update
    void update(StructuredMessage message);

    @Delete
    void delete(StructuredMessage message);
}
