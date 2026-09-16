package com.tokar.frez.cnc.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tokar.frez.cnc.data.entity.ToolFixtureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolFixtureDao {
    @Query("SELECT * FROM tool_fixtures ORDER BY category ASC, name ASC")
    fun getAllToolFixtures(): Flow<List<ToolFixtureEntity>>

    @Query("SELECT * FROM tool_fixtures WHERE category = :category")
    suspend fun getByCategory(category: String): List<ToolFixtureEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tools: List<ToolFixtureEntity>)
}
