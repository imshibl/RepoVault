package com.bilcodes.repovault.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bilcodes.repovault.data.local.database.entities.Repository

@Dao
interface RepositoryDao {
    // Insert a repository
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(repository: Repository)

    @Query("DELETE FROM repositories WHERE repoId = :repoId")
    suspend fun deleteRepositoryById(repoId: Long)

    // Get all repositories
    @Query("SELECT * FROM repositories")
    suspend fun getAllRepositories(): List<Repository>

    @Query("SELECT EXISTS(SELECT 1 FROM repositories WHERE repoId = :repoId)")
    suspend fun isRepositoryExists(repoId: Int): Boolean
}
