package com.nphstudio.mooveeon.data.remote

object ApiConfig {
    // For Android Emulator, localhost is 10.0.2.2
    // If using physical device, replace with your machine's local IP
    const val BASE_URL = "http://10.0.2.2:3000/"
    
    object Endpoints {
        const val MOVIE_LIST = "api/v1/movie/list"
        const val MOVIE_TRENDING = "api/v1/movie/trending"
        const val MOVIE_RECOMMENDATIONS = "api/v1/movie/recommendations"
        const val MOVIE_SEARCH = "api/v1/movie/search"
        const val MOVIE_DETAIL = "api/v1/movie/detail"
        const val MOVIE_VIDEO = "api/v1/movie/video"
    }
}
