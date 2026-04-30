package com.nphstudio.mooveeon.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DramaSeries(
    val id: String,
    val title: String,
    val description: String,
    val posterUrl: String,
    val episodes: List<Episode>
) : Parcelable

@Parcelize
data class Episode(
    val id: String,
    val episodeNumber: Int,
    val title: String,
    val videoUrl: String,
    val isLocked: Boolean,
    val price: Int = 10
) : Parcelable
