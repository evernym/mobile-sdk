package msdk.kotlin.sample.db.dao

import androidx.room.*
import msdk.kotlin.sample.db.entity.Backup


@Dao
interface BackupDao {
    @Query("SELECT * FROM backup WHERE id = :id")
    suspend fun getById(id: Int): Backup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg backups: Backup)

    @Update
    suspend fun update(connection: Backup)

    @Delete
    suspend fun delete(connection: Backup)
}