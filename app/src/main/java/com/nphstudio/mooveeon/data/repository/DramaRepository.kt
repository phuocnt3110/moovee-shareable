package com.nphstudio.mooveeon.data.repository

import android.content.Context
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

    companion object {
        private const val SAMPLE_VIDEO = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        private const val SAMPLE_VIDEO_2 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        private const val SAMPLE_VIDEO_3 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        private const val SAMPLE_VIDEO_4 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
        private const val SAMPLE_VIDEO_5 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"

        private val embeddedMocks = listOf(
            DramaMock("d1", "Whispers of the Heart", "A young girl discovers a magical world hidden inside an antique shop, leading her on a journey of self-discovery and first love.", "https://image.tmdb.org/t/p/w500/kMKyx1k8hWWscYFnPbnxxYIEEV.jpg", 8, listOf(SAMPLE_VIDEO, SAMPLE_VIDEO_2)),
            DramaMock("d2", "Eternal Sunshine", "Two strangers from different worlds meet on a train journey and find their lives intertwined by fate.", "https://image.tmdb.org/t/p/w500/5MwkWH9tYHv3mV9OdYTMR5qreIz.jpg", 12, listOf(SAMPLE_VIDEO_3, SAMPLE_VIDEO_4)),
            DramaMock("d3", "The Last Promise", "A retired detective receives a letter from a case he never solved, pulling him back into a web of mystery.", "https://image.tmdb.org/t/p/w500/4m1Au3YkjqsxF8iwQy0fPYSxE0h.jpg", 10, listOf(SAMPLE_VIDEO_5, SAMPLE_VIDEO)),
            DramaMock("d4", "City of Dreams", "In a bustling metropolis, four families compete for control of the most powerful corporation in the country.", "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911BTUgMe1nFGDb.jpg", 16, listOf(SAMPLE_VIDEO_2, SAMPLE_VIDEO_3)),
            DramaMock("d5", "Ocean's Melody", "A marine biologist and a musician team up to save a dying coral reef, discovering harmony in unexpected places.", "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QI4S2t0PODi.jpg", 6, listOf(SAMPLE_VIDEO_4, SAMPLE_VIDEO_5)),
            DramaMock("d6", "Midnight Express", "A thriller following a journalist who uncovers a conspiracy during a cross-country train ride.", "https://image.tmdb.org/t/p/w500/jBJWaqoSCiARWtfV0GlqHrcdiJq.jpg", 8, listOf(SAMPLE_VIDEO, SAMPLE_VIDEO_3)),
            DramaMock("d7", "Summer Romance", "Two childhood friends reunite during a summer festival and rediscover feelings they thought were lost.", "https://image.tmdb.org/t/p/w500/xRWht48C2V8XNfzvPehyClOvDni.jpg", 10, listOf(SAMPLE_VIDEO_2, SAMPLE_VIDEO_4)),
            DramaMock("d8", "The Crown's Secret", "A period drama set in a fictional kingdom where a young prince must navigate palace intrigue to protect his family.", "https://image.tmdb.org/t/p/w500/jRXYjXNq0Cs2TcJjLkki24MLp7u.jpg", 14, listOf(SAMPLE_VIDEO_5, SAMPLE_VIDEO)),
            DramaMock("d9", "Starlight Academy", "A group of aspiring idols at a prestigious academy compete, clash, and form bonds on the road to stardom.", "https://image.tmdb.org/t/p/w500/hO7KbdvGOtDdeg0W4Y5nKEHeDDh.jpg", 20, listOf(SAMPLE_VIDEO_3, SAMPLE_VIDEO_2)),
            DramaMock("d10", "Winter Sonata", "A love story unfolds across seasons as two people separated by circumstance find their way back to each other.", "https://image.tmdb.org/t/p/w500/wuMc08IPKEatf9rnMNXvIDxqP4W.jpg", 16, listOf(SAMPLE_VIDEO_4, SAMPLE_VIDEO_5))
        )
    }

    suspend fun getDramas(page: Int = 1, pageSize: Int = 10): List<DramaSeries> {
        val start = (page - 1) * pageSize
        if (start >= embeddedMocks.size) return emptyList()
        return embeddedMocks.subList(start, minOf(start + pageSize, embeddedMocks.size)).map { mapToDramaSeries(it) }
    }

    suspend fun getTrendingRemote(): List<DramaSeries> {
        return embeddedMocks.shuffled().take(5).map { mapToDramaSeries(it) }
    }

    suspend fun getRecommendationsRemote(): List<DramaSeries> {
        return embeddedMocks.shuffled().take(4).map { mapToDramaSeries(it) }
    }

    suspend fun searchRemote(keyword: String, page: Int = 1, pageSize: Int = 10): List<DramaSeries> {
        val filtered = embeddedMocks.filter {
            it.title.contains(keyword, ignoreCase = true) || it.description.contains(keyword, ignoreCase = true)
        }
        val start = (page - 1) * pageSize
        if (start >= filtered.size) return emptyList()
        return filtered.subList(start, minOf(start + pageSize, filtered.size)).map { mapToDramaSeries(it) }
    }

    suspend fun getMovieDetailRemote(id: String): DramaSeries? {
        return embeddedMocks.find { it.id == id }?.let { mapToDramaSeries(it) }
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
