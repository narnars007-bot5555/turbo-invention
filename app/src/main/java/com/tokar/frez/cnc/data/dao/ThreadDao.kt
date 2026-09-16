package com.tokar.frez.cnc.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tokar.frez.cnc.data.entity.ThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreadDao {
    @Query("SELECT * FROM threads WHERE designation LIKE '%' || :query || '%'")
    suspend fun searchThreads(query: String): List<ThreadEntity>

    @Query("SELECT * FROM threads ORDER BY nominalDiameter ASC")
    fun getAllThreads(): Flow<List<ThreadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(threads: List<ThreadEntity>)
}
