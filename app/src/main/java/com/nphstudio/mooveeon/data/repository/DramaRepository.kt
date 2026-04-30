package com.nphstudio.mooveeon.data.repository

import android.content.Context
import android.util.Log
import com.nphstudio.mooveeon.data.model.DramaSeries
import com.nphstudio.mooveeon.data.model.Episode

class DramaRepository(private val context: Context) {
    
    data class DramaMock(
        val id: String,
        val title: String,
        val description: String,
        val posterUrl: String,
        val episodeCount: Int,
        val videoUrls: List<String>
    )

    suspend fun getDramas(page: Int = 1, pageSize: Int = 10): List<DramaSeries> {
        return try {
            val mocks = com.nphstudio.mooveeon.data.remote.RetrofitClient.apiService.getMovies(page, pageSize)
            mocks.map { mapToDramaSeries(it) }
        } catch (e: Exception) {
            Log.e("DramaRepository", "Error getting dramas", e)
            emptyList()
        }
    }

    suspend fun getTrendingRemote(): List<DramaSeries> {
        return try {
            val mocks = com.nphstudio.mooveeon.data.remote.RetrofitClient.apiService.getTrendingMovies()
            mocks.map { mapToDramaSeries(it) }
        } catch (e: Exception) {
            Log.e("DramaRepository", "Error getting trending", e)
            emptyList()
        }
    }

    suspend fun getRecommendationsRemote(): List<DramaSeries> {
        return try {
            val mocks = com.nphstudio.mooveeon.data.remote.RetrofitClient.apiService.getRecommendations()
            mocks.map { mapToDramaSeries(it) }
        } catch (e: Exception) {
            Log.e("DramaRepository", "Error getting recommendations", e)
            emptyList()
        }
    }

    suspend fun searchRemote(keyword: String, page: Int = 1, pageSize: Int = 10): List<DramaSeries> {
        return try {
            val mocks = com.nphstudio.mooveeon.data.remote.RetrofitClient.apiService.searchMovies(keyword, page, pageSize)
            mocks.map { mapToDramaSeries(it) }
        } catch (e: Exception) {
            Log.e("DramaRepository", "Error searching movies", e)
            emptyList()
        }
    }

    suspend fun getMovieDetailRemote(id: String): DramaSeries? {
        return try {
            val mocks = com.nphstudio.mooveeon.data.remote.RetrofitClient.apiService.getMovieDetail(id)
            mocks.firstOrNull()?.let { mapToDramaSeries(it) }
        } catch (e: Exception) {
            Log.e("DramaRepository", "Error getting movie detail", e)
            null
        }
    }

    private fun mapToDramaSeries(mock: DramaMock): DramaSeries {
        val episodeLabel = com.nphstudio.mooveeon.utils.TranslationHelper.getString("episode", "Episode")
        return DramaSeries(
            id = mock.id,
            title = mock.title,
            description = mock.description,
            posterUrl = mock.posterUrl,
            episodes = (1..mock.episodeCount).map { i ->
                Episode(
                    id = "${mock.id}_$i",
                    episodeNumber = i,
                    title = "$episodeLabel $i",
                    videoUrl = if (mock.videoUrls.isNotEmpty()) {
                        mock.videoUrls[(i - 1) % mock.videoUrls.size]
                    } else {
                        ""
                    },
                    isLocked = i >= 5,
                    price = 10
                )
            }
        )
    }
}
