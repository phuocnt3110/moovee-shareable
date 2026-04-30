package com.nphstudio.mooveeon.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val posterUrl: String,
    val lastEpisode: Int,
    val totalEpisodes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val posterUrl: String,
    val totalEpisodes: Int,
    val timestamp: Long = System.currentTimeMillis()
)
