package msdk.java.sample.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import msdk.java.sample.db.entity.Action;

@Dao
public interface ActionDao {
    @Query("SELECT * FROM `action`")
    List<Action> getAllAsync();

    @Query("SELECT * FROM `action`")
    LiveData<List<Action>> getAll();

    @Query("SELECT * FROM `action` WHERE id = :id")
    Action getActionsById(int id);

    @Query("SELECT * FROM `action` WHERE status = :status")
    LiveData<List<Action>> getActionsByStatus(String status);

    @Insert
    void insertAll(Action... actions);

    @Update
    void update(Action action);

    @Delete
    void delete(Action action);
}
