package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SummaryDao {
    @Query("SELECT * FROM summaries ORDER BY createdAt DESC")
    fun getAllSummaries(): Flow<List<SummaryEntity>>

    @Query("SELECT * FROM summaries WHERE id = :id LIMIT 1")
    suspend fun getSummaryById(id: Long): SummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: SummaryEntity): Long

    @Update
    suspend fun updateSummary(summary: SummaryEntity)

    @Delete
    suspend fun deleteSummary(summary: SummaryEntity)

    @Query("DELETE FROM summaries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
