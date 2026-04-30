package com.nphstudio.mooveeon.data.remote

import com.nphstudio.mooveeon.data.repository.DramaRepository.DramaMock
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApiService {
    @GET(ApiConfig.Endpoints.MOVIE_TRENDING)
    suspend fun getTrendingMovies(): List<DramaMock>

    @GET(ApiConfig.Endpoints.MOVIE_LIST)
    suspend fun getMovies(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): List<DramaMock>

    @GET(ApiConfig.Endpoints.MOVIE_RECOMMENDATIONS)
    suspend fun getRecommendations(): List<DramaMock>

    @GET(ApiConfig.Endpoints.MOVIE_SEARCH)
    suspend fun searchMovies(
        @Query("keyword") keyword: String?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): List<DramaMock>

    @GET(ApiConfig.Endpoints.MOVIE_DETAIL)
    suspend fun getMovieDetail(
        @Query("id") id: String
    ): List<DramaMock>

    @GET(ApiConfig.Endpoints.MOVIE_VIDEO)
    suspend fun getMovieVideo(
        @Query("id") id: String,
        @Query("episode") episode: Int
    ): List<DramaMock>
}
