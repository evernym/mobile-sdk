package msdk.java.sample.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import msdk.java.sample.db.entity.Backup;

@Dao
public interface BackupDao {

    @Query("SELECT * FROM backup WHERE id = :id")
    Backup getById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Backup... backups);

    @Update
    void update(Backup backup);

    @Delete
    void delete(Backup backup);
}
