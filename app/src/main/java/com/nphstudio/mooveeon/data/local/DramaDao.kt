package com.nphstudio.mooveeon.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DramaDao {
    // History
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("SELECT * FROM history WHERE id = :dramaId")
    suspend fun getHistoryById(dramaId: String): HistoryEntity?

    @Query("DELETE FROM history WHERE id = :dramaId")
    suspend fun deleteHistory(dramaId: String)

    // Favorites
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :dramaId")
    suspend fun deleteFavoriteById(dramaId: String)

    @Query("UPDATE history SET isFavorite = :isFav WHERE id = :dramaId")
    suspend fun updateHistoryFavorite(dramaId: String, isFav: Boolean)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :dramaId)")
    fun isFavorite(dramaId: String): Flow<Boolean>
}
