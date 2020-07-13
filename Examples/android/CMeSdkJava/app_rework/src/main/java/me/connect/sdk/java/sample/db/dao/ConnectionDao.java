package me.connect.sdk.java.sample.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import me.connect.sdk.java.sample.db.entity.Connection;

@Dao
public interface ConnectionDao {
    @Query("SELECT * FROM connection")
    List<Connection> getAll();

    @Query("SELECT * FROM connection WHERE id = :id")
    Connection getById(int id);

    @Insert
    void insertAll(Connection... connections);

    @Delete
    void delete(Connection connection);
}
