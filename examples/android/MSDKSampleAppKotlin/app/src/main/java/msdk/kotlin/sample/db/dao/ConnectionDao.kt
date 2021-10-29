package msdk.kotlin.sample.db.dao


import androidx.lifecycle.LiveData
import androidx.room.*

import msdk.kotlin.sample.db.entity.Connection


@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connection")
    suspend fun getAllAsync(): List<Connection>

    @Query("SELECT * FROM connection")
    fun getAll(): LiveData<List<Connection>>

    @Query("SELECT serialized FROM connection")
    suspend fun getAllSerializedConnections(): List<String>

    @Query("SELECT * FROM connection WHERE id = :id")
    suspend fun getById(id: Int): Connection

    @Query("SELECT * FROM connection WHERE pwdid = :pwDid")
    suspend fun getByPwDid(pwDid: String): Connection

    @Insert
    suspend fun insertAll(vararg connections: Connection)

    @Update
    suspend fun update(connection: Connection)

    @Delete
    suspend fun delete(connection: Connection)
}
