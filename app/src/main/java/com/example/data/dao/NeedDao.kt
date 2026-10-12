package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.NeedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NeedDao {
    @Query("SELECT * FROM needs ORDER BY isDone ASC, createdAtEpochMs DESC")
    fun getAllNeeds(): Flow<List<NeedEntity>>

    @Query("SELECT * FROM needs WHERE isDone = 0 ORDER BY createdAtEpochMs DESC")
    fun getActiveNeeds(): Flow<List<NeedEntity>>

    @Query("SELECT * FROM needs WHERE isDone = 1 ORDER BY doneAtEpochMs DESC")
    fun getDoneNeeds(): Flow<List<NeedEntity>>

    @Query("SELECT * FROM needs WHERE id = :id LIMIT 1")
    suspend fun getNeedById(id: Long): NeedEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNeed(need: NeedEntity): Long

    @Update
    suspend fun updateNeed(need: NeedEntity)

    @Query("UPDATE needs SET isDone = :isDone, doneAtEpochMs = :doneAtMs WHERE id = :id")
    suspend fun setNeedDone(id: Long, isDone: Boolean, doneAtMs: Long?)

    @Query("DELETE FROM needs WHERE id = :id")
    suspend fun deleteNeed(id: Long)

    @Query("DELETE FROM needs WHERE isDone = 1")
    suspend fun clearAllDone()
}
