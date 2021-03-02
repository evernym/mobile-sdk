package me.connect.sdk.java.sample.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import me.connect.sdk.java.sample.db.entity.Connection;

@Dao
public interface ConnectionDao {
    @Query("SELECT * FROM connection")
    List<Connection> getAllAsync();

    @Query("SELECT * FROM connection")
    LiveData<List<Connection>> getAll();

    @Query("SELECT serialized FROM connection")
    List<String> getAllSerializedConnections();

    @Query("SELECT * FROM connection WHERE id = :id")
    Connection getById(int id);

    @Query("SELECT * FROM connection WHERE pwdid = :pwDid")
    Connection getByPwDid(String pwDid);

    @Insert
    void insertAll(Connection... connections);

    @Update
    void update(Connection connection);

    @Delete
    void delete(Connection connection);
}
