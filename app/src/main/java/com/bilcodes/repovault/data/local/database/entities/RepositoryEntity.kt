package com.bilcodes.repovault.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repositories")
data class Repository(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val repoId: Long,
    val name: String,
    val description: String,
    val stars: Int
)