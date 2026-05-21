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
        private const val SAMPLE_VIDEO = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_5MB.mp4"
        private const val SAMPLE_VIDEO_2 = "https://test-videos.co.uk/vids/sintel/mp4/h264/720/Sintel_720_10s_5MB.mp4"
        private const val SAMPLE_VIDEO_3 = "https://test-videos.co.uk/vids/jellyfish/mp4/h264/720/Jellyfish_720_10s_5MB.mp4"
        private const val SAMPLE_VIDEO_4 = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_1MB.mp4"
        private const val SAMPLE_VIDEO_5 = "https://test-videos.co.uk/vids/sintel/mp4/h264/720/Sintel_720_10s_1MB.mp4"

        private val embeddedMocks = listOf(
            DramaMock("d1", "Unconditional Dad", "After ten years in prison for avenging his eldest daughter's death, he returns to find his family in ruins and his second daughter struggling to survive against corrupt forces.", "https://images.unsplash.com/photo-1536440136628-849c177e76a1", 8, listOf(SAMPLE_VIDEO, SAMPLE_VIDEO_2)),
            DramaMock("d2", "The Wrong Daughter", "A heart-wrenching story of a young woman who discovers her true identity as the heiress to a vast billionaire empire, only to face betrayal from the family she once called her own.", "https://images.unsplash.com/photo-1485846234645-a62644f84728", 12, listOf(SAMPLE_VIDEO_3, SAMPLE_VIDEO_4)),
            DramaMock("d3", "Stepmom's Redemption", "Award-winning actress Chloe Evans finds herself transported into her own screenplay, forced to redeem the villainous stepmom character she was destined to play.", "https://images.unsplash.com/photo-1478720568477-152d9b164e26", 10, listOf(SAMPLE_VIDEO_5, SAMPLE_VIDEO)),
            DramaMock("d4", "Sister Brides", "Two sisters, one wedding, and a secret that could destroy their bond forever. A tale of love, rivalry, and the lengths we go for family.", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23", 16, listOf(SAMPLE_VIDEO_2, SAMPLE_VIDEO_3)),
            DramaMock("d5", "The Lost Kingdom", "An epic journey of a young warrior seeking to reclaim his throne from a dark sorcerer who took over the kingdom centuries ago.", "https://images.unsplash.com/photo-1440404653325-ab127d49abc1", 6, listOf(SAMPLE_VIDEO_4, SAMPLE_VIDEO_5)),
            DramaMock("d6", "Cyber City 2077", "In a neon-drenched future, a rogue hacker discovers a conspiracy that could change humanity forever.", "https://images.unsplash.com/photo-1550745165-9bc0b252726f", 8, listOf(SAMPLE_VIDEO, SAMPLE_VIDEO_3)),
            DramaMock("d7", "The Quiet Whisper", "A psychological thriller about a woman who begins to hear voices in her new home, leading her to uncover a dark secret.", "https://images.unsplash.com/photo-1509248961158-e54f6934749c", 10, listOf(SAMPLE_VIDEO_2, SAMPLE_VIDEO_4)),
            DramaMock("d8", "Shadow Ninja", "A former assassin comes out of retirement to protect his village from an ancient evil force.", "https://images.unsplash.com/photo-1514539079130-25950c84af65", 14, listOf(SAMPLE_VIDEO_5, SAMPLE_VIDEO)),
            DramaMock("d9", "Ocean's Melody", "A marine biologist and a musician team up to save a dying coral reef, discovering harmony in unexpected places.", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e", 20, listOf(SAMPLE_VIDEO_3, SAMPLE_VIDEO_2)),
            DramaMock("d10", "Winter Sonata", "A love story unfolds across seasons as two people separated by circumstance find their way back to each other.", "https://images.unsplash.com/photo-1491002052546-bf38f186af56", 16, listOf(SAMPLE_VIDEO_4, SAMPLE_VIDEO_5))
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
