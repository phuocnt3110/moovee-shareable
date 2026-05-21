package com.nphstudio.mooveeon.utils

import android.content.Context
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.nphstudio.mooveeon.R

object GlideUtils {
    fun loadImage(view: ImageView, url: String?) {
        val context = view.context
        val circularProgressDrawable = CircularProgressDrawable(context).apply {
            strokeWidth = 5f
            centerRadius = 30f
            // Matching the gray dots in the user's request
            setColorSchemeColors(android.graphics.Color.GRAY)
            start()
        }

        Glide.with(view)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(circularProgressDrawable)
            .error(R.drawable.img_placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }

    fun preload(context: Context, urls: List<String>) {
        val appContext = context.applicationContext
        urls.forEach { url ->
            Glide.with(appContext)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .preload()
        }
    }
}
