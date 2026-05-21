package com.nphstudio.mooveeon

import android.app.Application
import com.nphstudio.mooveeon.data.repository.DramaRepository
import com.nphstudio.mooveeon.utils.GlideUtils

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        preloadPosterImages()
    }

    private fun preloadPosterImages() {
        val posterUrls = DramaRepository.getAllPosterUrls()
        GlideUtils.preload(this, posterUrls)
    }
}
