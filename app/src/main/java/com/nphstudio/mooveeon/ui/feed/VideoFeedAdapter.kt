package com.nphstudio.mooveeon.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import com.nphstudio.mooveeon.data.model.Episode
import com.nphstudio.mooveeon.databinding.ItemVideoFeedBinding
import com.nphstudio.mooveeon.utils.TranslationHelper
import okhttp3.OkHttpClient
import androidx.media3.datasource.okhttp.OkHttpDataSource
import com.nphstudio.mooveeon.R

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class VideoFeedAdapter(
    private val dramaTitle: String,
    private val episodes: List<Episode>,
    private val onEpisodesClick: (Episode) -> Unit,
    private val onUnlockClick: (Episode) -> Unit,
    private val onSpeedClick: (Float) -> Unit,
    private val onFavoriteClick: (Episode, (Boolean) -> Unit) -> Unit,
    private val onBackClick: () -> Unit,
    private val onFullscreenClick: (Boolean) -> Unit,
    private val onPlaybackEnded: (Int) -> Unit
) : RecyclerView.Adapter<VideoFeedAdapter.VideoViewHolder>() {

    private val activeHolders = mutableSetOf<VideoViewHolder>()

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"

        private val okHttpClient by lazy {
            OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", USER_AGENT)
                        .build()
                    chain.proceed(request)
                }
                .build()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoFeedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.root.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(episodes[position])
    }

    override fun getItemCount(): Int = episodes.size

    inner class VideoViewHolder(private val binding: ItemVideoFeedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var player: ExoPlayer? = null
        private var currentSpeed: Float = 1.0f
        private val progressRunnable = object : Runnable {
            override fun run() {
                updateProgress()
                binding.root.postDelayed(this, 1000)
            }
        }

        fun bind(episode: Episode) {
            val episodeLabel = TranslationHelper.getString("episode", "Episode")
            binding.tvTopTitle.text = dramaTitle
            binding.tvTopEpisode.text = "$episodeLabel ${episode.episodeNumber}"
            binding.tvCurrentSpeed.text = if (currentSpeed == currentSpeed.toInt().toFloat()) "${currentSpeed.toInt()}x" else "${currentSpeed}x"
            
            binding.btnSeries.setOnClickListener { onEpisodesClick(episode) }
            binding.btnBack.setOnClickListener { onBackClick() }
            
            binding.btnSpeed.setOnClickListener {
                onSpeedClick(currentSpeed)
            }

            binding.btnPlayPause.setOnClickListener {
                togglePlayPause()
            }

            binding.btnForward.setOnClickListener {
                seekRelative(5000)
            }

            binding.btnBackward.setOnClickListener {
                seekRelative(-5000)
            }

            binding.videoSeekbar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        player?.seekTo(progress.toLong())
                        updateProgressText(progress.toLong(), player?.duration ?: 0L)
                    }
                }
                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
                    stopProgressUpdates()
                }
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                    startProgressUpdates()
                }
            })
            
            // Show/Hide controls on player view click
            binding.playerView.setOnClickListener {
                toggleControlsVisibility()
            }

            binding.btnFullscreen.setOnClickListener {
                val willBeFullscreen = binding.layoutTop.visibility == android.view.View.VISIBLE
                toggleUIVisibility()
                onFullscreenClick(willBeFullscreen)
                
                // Toggle icon
                if (willBeFullscreen) {
                    binding.ivFullscreen.setImageResource(R.drawable.ic_fullscreen_exit)
                } else {
                    binding.ivFullscreen.setImageResource(R.drawable.ic_fullscreen)
                }
            }

            binding.btnFavorite.setOnClickListener {
                onFavoriteClick(episode) { isFavNow ->
                    updateFavoriteUI(isFavNow)
                }
            }
            
            // Check initial favorite status (you might want to pass this in or handle in Fragment)
            // For now, let's keep it simple and update based on actual state later
            updateFavoriteUI(false) 

            if (episode.isLocked) {
                binding.playerView.alpha = 0.5f
                binding.btnSeries.setOnClickListener { onUnlockClick(episode) }
                binding.layoutCenterControls.visibility = android.view.View.GONE
                stopProgressUpdates()
            } else {
                binding.playerView.alpha = 1.0f
                binding.layoutCenterControls.visibility = android.view.View.VISIBLE
                currentUrl = episode.videoUrl
                playWhenReadyState = false // Reset state for new episode
                if (itemView.isAttachedToWindow) {
                    setupPlayer(currentUrl ?: "")
                }
                startProgressUpdates()
            }
        }

        private var currentUrl: String? = null

        private fun togglePlayPause() {
            player?.let { p ->
                if (p.isPlaying) {
                    p.pause()
                    binding.btnPlayPause.setImageResource(R.drawable.ic_play)
                } else {
                    p.play()
                    binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
                }
            }
        }

        private fun seekRelative(millis: Long) {
            player?.let { p ->
                val newPos = (p.currentPosition + millis).coerceIn(0, p.duration)
                p.seekTo(newPos)
                updateProgress()
            }
        }

        fun showUI() {
            if (binding.layoutTop.visibility != android.view.View.VISIBLE) {
                toggleUIVisibility()
                binding.ivFullscreen.setImageResource(R.drawable.ic_fullscreen)
            }
        }

        private fun toggleUIVisibility() {
            val isCurrentlyVisible = binding.layoutTop.visibility == android.view.View.VISIBLE
            val targetAlpha = if (isCurrentlyVisible) 0f else 1f
            
            val viewsToAnimate = listOf(
                binding.layoutTop,
                binding.layoutBottomControls
            )
            
            viewsToAnimate.forEach { view ->
                if (!isCurrentlyVisible) view.visibility = android.view.View.VISIBLE
                view.animate()
                    .alpha(targetAlpha)
                    .setDuration(300)
                    .withEndAction {
                        if (isCurrentlyVisible) view.visibility = android.view.View.GONE
                    }
                    .start()
            }
        }

        private fun toggleControlsVisibility() {
            // If main UI is hidden (full screen), show it back instead of toggling playback controls
            if (binding.layoutTop.visibility == android.view.View.GONE) {
                toggleUIVisibility()
                return
            }

            if (binding.layoutCenterControls.visibility == android.view.View.VISIBLE) {
                binding.layoutCenterControls.animate().alpha(0f).withEndAction {
                    binding.layoutCenterControls.visibility = android.view.View.GONE
                }.start()
            } else {
                binding.layoutCenterControls.alpha = 0f
                binding.layoutCenterControls.visibility = android.view.View.VISIBLE
                binding.layoutCenterControls.animate().alpha(1f).start()
                
                // Auto hide after 3 seconds
                binding.layoutCenterControls.postDelayed({
                    if (binding.layoutCenterControls.visibility == android.view.View.VISIBLE) {
                        toggleControlsVisibility()
                    }
                }, 3000)
            }
        }

        private fun updateFavoriteUI(isFavorite: Boolean) {
            val color = if (isFavorite) android.graphics.Color.RED else android.graphics.Color.WHITE
            (binding.btnFavorite.getChildAt(0) as android.widget.ImageView).setColorFilter(color)
            binding.btnFavorite.tag = isFavorite
        }

        fun setPlaybackSpeed(speed: Float) {
            currentSpeed = speed
            player?.setPlaybackSpeed(speed)
            binding.tvCurrentSpeed.text = if (speed == speed.toInt().toFloat()) "${speed.toInt()}x" else "${speed}x"
        }

        private var playWhenReadyState: Boolean = true

        fun setPlayWhenReady(playWhenReady: Boolean) {
            this.playWhenReadyState = playWhenReady
            player?.playWhenReady = playWhenReady
            binding.btnPlayPause.setImageResource(if (playWhenReady) R.drawable.ic_pause else R.drawable.ic_play)
            if (playWhenReady) startProgressUpdates() else stopProgressUpdates()
        }

        fun restart() {
            player?.seekTo(0)
            player?.play()
        }

        private fun setupPlayer(url: String) {
            if (url.isBlank()) return
            
            // Optimization: If player already exists and is loading same URL, skip recreation
            if (player != null && player?.currentMediaItem?.localConfiguration?.uri?.toString() == url) {
                return
            }

            player?.release()
            
            val uri = url.toUri()
            val host = uri.host ?: ""
            val origin = if (uri.scheme != null && host.isNotEmpty()) "${uri.scheme}://$host" else ""
            
            val headers = mutableMapOf(
                "Accept" to "*/*",
                "Accept-Language" to "en-US,en;q=0.9"
            )
            if (origin.isNotEmpty()) {
                headers["Origin"] = origin
                headers["Referer"] = "$origin/"
            }

            val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
                .setUserAgent(USER_AGENT)
                .setDefaultRequestProperties(headers)

            binding.pbLoading.visibility = android.view.View.VISIBLE
            binding.btnPlayPause.visibility = android.view.View.GONE

            player = ExoPlayer.Builder(itemView.context)
                .setMediaSourceFactory(androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSourceFactory))
                .build().apply {
                playWhenReady = playWhenReadyState
                repeatMode = ExoPlayer.REPEAT_MODE_OFF
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                
                addListener(object : androidx.media3.common.Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            androidx.media3.common.Player.STATE_BUFFERING -> {
                                binding.pbLoading.visibility = android.view.View.VISIBLE
                                binding.btnPlayPause.visibility = android.view.View.GONE
                            }
                            androidx.media3.common.Player.STATE_ENDED -> {
                                onPlaybackEnded(bindingAdapterPosition)
                            }
                            else -> {
                                binding.pbLoading.visibility = android.view.View.GONE
                                binding.btnPlayPause.visibility = android.view.View.VISIBLE
                            }
                        }
                    }
                    override fun onEvents(p: androidx.media3.common.Player, events: androidx.media3.common.Player.Events) {
                        if (events.contains(androidx.media3.common.Player.EVENT_PLAYBACK_STATE_CHANGED)) {
                            if (p.duration > 0) {
                                binding.videoSeekbar.max = p.duration.toInt()
                            }
                        }
                    }
                    override fun onPositionDiscontinuity(oldPosition: androidx.media3.common.Player.PositionInfo, newPosition: androidx.media3.common.Player.PositionInfo, reason: Int) {
                        updateProgress()
                    }
                })
            }
            binding.playerView.player = player
            updateProgress()
        }

        private fun updateProgress() {
            val p = player ?: return
            if (p.duration > 0) {
                binding.videoSeekbar.max = p.duration.toInt()
                binding.videoSeekbar.progress = p.currentPosition.toInt()
                updateProgressText(p.currentPosition, p.duration)
            } else {
                updateProgressText(0L, 0L)
            }
        }

        private fun updateProgressText(currentMs: Long, totalMs: Long) {
            if (totalMs > 0) {
                val current = String.format("%d:%02d", (currentMs / 1000) / 60, (currentMs / 1000) % 60)
                val total = String.format("%d:%02d", (totalMs / 1000) / 60, (totalMs / 1000) % 60)
                binding.tvTimeProgress.text = "$current / $total"
            } else {
                binding.tvTimeProgress.text = "0:00 / 0:00"
            }
        }

        private fun startProgressUpdates() {
            stopProgressUpdates()
            binding.root.post(progressRunnable)
        }

        private fun stopProgressUpdates() {
            binding.root.removeCallbacks(progressRunnable)
        }

        fun onAttached() {
            currentUrl?.let { setupPlayer(it) }
        }

        fun releasePlayer() {
            player?.release()
            player = null
            playWhenReadyState = false
            stopProgressUpdates()
        }
    }

    override fun onViewAttachedToWindow(holder: VideoViewHolder) {
        super.onViewAttachedToWindow(holder)
        activeHolders.add(holder)
        holder.onAttached()
    }

    override fun onViewDetachedFromWindow(holder: VideoViewHolder) {
        super.onViewDetachedFromWindow(holder)
        activeHolders.remove(holder)
        holder.releasePlayer()
    }

    fun releaseAllPlayers() {
        activeHolders.forEach { it.releasePlayer() }
        activeHolders.clear()
    }

    fun pauseAllPlayers() {
        activeHolders.forEach { it.setPlayWhenReady(false) }
    }
}
