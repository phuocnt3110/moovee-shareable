package com.nphstudio.mooveeon.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.activity.addCallback
import com.nphlab.sdk.ads.AdError
import com.nphlab.sdk.ads.NphAds
import com.nphlab.sdk.ads.listener.NphAdListener
import com.nphstudio.mooveeon.R
import com.nphstudio.mooveeon.databinding.FragmentFeedBinding
import com.nphstudio.mooveeon.utils.TranslationHelper

import androidx.lifecycle.lifecycleScope
import com.nphstudio.mooveeon.data.local.AppDatabase
import com.nphstudio.mooveeon.data.local.HistoryEntity
import com.nphstudio.mooveeon.data.model.DramaSeries
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.nphstudio.mooveeon.data.local.FavoriteEntity
import com.nphstudio.mooveeon.data.model.Episode
import com.google.android.material.tabs.TabLayout
import com.nphstudio.mooveeon.data.repository.DramaRepository

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private val db by lazy { AppDatabase.getDatabase(requireContext()) }
    private val repository by lazy { DramaRepository(requireContext()) }
    private var currentDrama: DramaSeries? = null
    private var currentSpeed: Float = 1.0f
    private val watchedEpisodes = mutableSetOf<String>() // Simple memory cache for session
    private var videoAdapter: VideoFeedAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        currentDrama = arguments?.getParcelable("drama")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupVideoFeed()
        
        // Preload interstitial ad for back home
        NphAds.preload(requireActivity(), "nsp-interstitial-feed-fullscreen-back")

        setupBackNavigation()
    }

    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (requireActivity().requestedOrientation == android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                // Exit landscape mode
                requireActivity().requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                
                // We should also tell the current view holder to show the UI
                val currentPos = binding.viewPagerVideos.currentItem
                val recyclerView = binding.viewPagerVideos.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView
                val viewHolder = recyclerView?.findViewHolderForAdapterPosition(currentPos) as? VideoFeedAdapter.VideoViewHolder
                viewHolder?.showUI()
            } else {
                // Normal back behavior
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }
    }

    private fun updateHistory(episodeIndex: Int) {
        val drama = currentDrama ?: return
        lifecycleScope.launch {
            val episodes = if (drama.episodes.isNotEmpty()) drama.episodes else emptyList()
            val currentId = episodes.getOrNull(episodeIndex)?.id ?: (episodeIndex + 1).toString()
            watchedEpisodes.add(currentId)
            
            val isFav = db.dramaDao().isFavorite(drama.id).first()
            
            // MO.md: Ensure history is saved even if fragment is destroyed during back navigation
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.NonCancellable) {
                db.dramaDao().insertHistory(
                    HistoryEntity(
                        id = drama.id,
                        title = drama.title,
                        posterUrl = drama.posterUrl,
                        lastEpisode = episodeIndex + 1,
                        totalEpisodes = if (drama.episodes.isNotEmpty()) drama.episodes.size else 20,
                        isFavorite = isFav
                    )
                )
            }
        }
    }

    private fun setupVideoFeed() {
        lifecycleScope.launch {
            // Fetch drama from repository if not provided or empty
            var drama = currentDrama
            
            if (drama != null && drama.episodes.isEmpty()) {
                drama = repository.getMovieDetailRemote(drama.id)
            }
            
            if (drama == null || drama.episodes.isEmpty()) {
                drama = repository.getTrendingRemote().firstOrNull()
            }
            
            if (drama == null) {
                // Handle case where no dramas are available
                return@launch
            }
            
            currentDrama = drama

            val history = db.dramaDao().getAllHistory().first().find { it.id == drama.id }
            val startEpisode = history?.lastEpisode?.minus(1)?.coerceAtLeast(0) ?: 0

            val episodes = drama.episodes
            
            // Note: Manual population from history removed as per user request to only gray out explicitly clicked episodes.
            // But we add the current start episode to the list.
            episodes.getOrNull(startEpisode)?.id?.let { watchedEpisodes.add(it) }

            val adapter = VideoFeedAdapter(
                dramaTitle = drama.title,
                episodes = episodes,
                onEpisodesClick = { showEpisodesBottomSheet(episodes) },
                onUnlockClick = { unlockEpisode(it) },
                onSpeedClick = { speed -> showSpeedBottomSheet(speed) },
                onFavoriteClick = { ep, callback -> toggleFavorite(callback) },
                onBackClick = { showBackInterstitial() },
                onFullscreenClick = { isFullscreen ->
                    if (isFullscreen) {
                        requireActivity().requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        requireActivity().requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                },
                onPlaybackEnded = { position ->
                    val nextPos = position + 1
                    if (nextPos < episodes.size) {
                        val nextEp = episodes[nextPos]
                        if (nextEp.isLocked) {
                            restartPlayer(position)
                        } else {
                            binding.viewPagerVideos.setCurrentItem(nextPos, true)
                        }
                    } else {
                        restartPlayer(position)
                    }
                }
            )
            videoAdapter = adapter
            binding.viewPagerVideos.adapter = videoAdapter
            
            // Resume from last watched episode
            if (startEpisode > 0 && startEpisode < episodes.size) {
                binding.viewPagerVideos.setCurrentItem(startEpisode, false)
            }
            
            // Update history and player state when page changes
            binding.viewPagerVideos.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateHistory(position)
                    updatePlayerStates(position)
                }
            })
            
            // Initial history save for the first episode
            updateHistory(startEpisode)
            // Wait for adapter to be ready then update initial player state
            binding.viewPagerVideos.post { updatePlayerStates(startEpisode) }
        }
    }

    private fun restartPlayer(position: Int) {
        val recyclerView = binding.viewPagerVideos.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView ?: return
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val holder = recyclerView.getChildViewHolder(child) as? VideoFeedAdapter.VideoViewHolder
            if (holder != null && holder.bindingAdapterPosition == position) {
                holder.restart()
                return
            }
        }
    }

    private fun updatePlayerStates(currentPosition: Int) {
        val recyclerView = binding.viewPagerVideos.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView ?: return
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val holder = recyclerView.getChildViewHolder(child) as? VideoFeedAdapter.VideoViewHolder
            if (holder != null) {
                val pos = holder.bindingAdapterPosition
                holder.setPlayWhenReady(pos == currentPosition)
            }
        }
    }

    private fun toggleFavorite(callback: (Boolean) -> Unit) {
        val drama = currentDrama ?: return
        lifecycleScope.launch {
            val isCurrentlyFav = db.dramaDao().isFavorite(drama.id).first()
            if (isCurrentlyFav) {
                db.dramaDao().deleteFavoriteById(drama.id)
                db.dramaDao().updateHistoryFavorite(drama.id, false)
                callback(false)
            } else {
                db.dramaDao().insertFavorite(
                    FavoriteEntity(
                        id = drama.id,
                        title = drama.title,
                        posterUrl = drama.posterUrl,
                        totalEpisodes = drama.episodes.size
                    )
                )
                db.dramaDao().updateHistoryFavorite(drama.id, true)
                callback(true)
            }
        }
    }

    private fun showEpisodesBottomSheet(allEpisodes: List<Episode>) {
        val drama = currentDrama ?: return
        val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext(), R.style.TransparentBottomSheetDialog)
        
        bottomSheet.setOnShowListener {
            val dialog = it as com.google.android.material.bottomsheet.BottomSheetDialog
            val internalSheet = dialog.findViewById<android.view.View>(com.google.android.material.R.id.design_bottom_sheet)
            internalSheet?.let { sheet ->
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(sheet)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        
        val view = layoutInflater.inflate(R.layout.bottom_sheet_episodes, null)
        
        val tvTitle = view.findViewById<android.widget.TextView>(R.id.tv_drama_title)
        val tvCurrentEp = view.findViewById<android.widget.TextView>(R.id.tv_current_episode)
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout_ranges)
        val rv = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_episodes)
        val adContainer = view.findViewById<android.view.ViewGroup>(R.id.ad_container)

        // MO.md: Load Banner Ad for Episodes Sheet
        NphAds.loadBannerInto(adContainer, "nsp-banner-feed-dialog-auto")
        
        tvTitle.text = drama.title
        val currentPos = binding.viewPagerVideos.currentItem
        val currentEpisode = allEpisodes.getOrNull(currentPos)
        val episodeLabel = TranslationHelper.getString("episode", "Episode")
        tvCurrentEp.text = "$episodeLabel ${currentPos + 1}"

        // Setup Tabs (1-24, 25-48, etc.)
        tabLayout.removeAllTabs()
        val pageSize = 24
        val pageCount = (allEpisodes.size + pageSize - 1) / pageSize
        
        for (i in 0 until pageCount) {
            val start = i * pageSize + 1
            val end = minOf((i + 1) * pageSize, allEpisodes.size)
            tabLayout.addTab(tabLayout.newTab().setText("$start-$end"))
        }

        val episodeAdapter = EpisodeAdapter(
            episodes = allEpisodes.take(pageSize),
            currentEpisodeId = currentEpisode?.id ?: (currentPos + 1).toString(),
            watchedEpisodeIds = watchedEpisodes,
            onEpisodeClick = { episode ->
                if (episode.episodeNumber >= 5) {
                    bottomSheet.dismiss()
                    val upgradePro = TranslationHelper.getString("upgrade_pro", "Upgrade Pro to continue watching")
                    android.widget.Toast.makeText(requireContext(), upgradePro, android.widget.Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.settingsFragment)
                } else {
                    watchedEpisodes.add(episode.id)
                    binding.viewPagerVideos.setCurrentItem(episode.episodeNumber - 1, false)
                    bottomSheet.dismiss()
                }
            }
        )
        rv.adapter = episodeAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                val start = position * pageSize
                val end = minOf((position + 1) * pageSize, allEpisodes.size)
                val currentId = allEpisodes.getOrNull(binding.viewPagerVideos.currentItem)?.id ?: (binding.viewPagerVideos.currentItem + 1).toString()
                episodeAdapter.updateEpisodes(allEpisodes.subList(start, end), currentId)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Select correct tab based on current episode
        val initialTab = currentPos / pageSize
        tabLayout.getTabAt(initialTab)?.select()

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun showSpeedBottomSheet(speed: Float) {
        val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext(), R.style.TransparentBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_speed, null)
        
        val rvSpeeds = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_speeds)
        val adContainer = view.findViewById<android.view.ViewGroup>(R.id.ad_container)
        
        // MO.md: Load Ad for Speed Sheet
        NphAds.loadBannerInto(adContainer, "nsp-banner-feed-dialog-auto")

        val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 2.5f, 3.0f)
        
        val speedAdapter = SpeedAdapter(speedOptions, currentSpeed) { selectedSpeed ->
            currentSpeed = selectedSpeed
            // Update current visible view holder's speed
            val currentPos = binding.viewPagerVideos.currentItem
            val viewHolder = (binding.viewPagerVideos.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView)
                ?.findViewHolderForAdapterPosition(currentPos) as? VideoFeedAdapter.VideoViewHolder
            viewHolder?.setPlaybackSpeed(selectedSpeed)
            bottomSheet.dismiss()
        }
        
        rvSpeeds.adapter = speedAdapter

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun unlockEpisode(episode: com.nphstudio.mooveeon.data.model.Episode) {
        NphAds.showRewarded(
            activity = requireActivity(),
            nameSpace = "nsp-rewarded-feed-fullscreen-unlockEpisode",
            listener = object : com.nphlab.sdk.ads.listener.NphRewardListener() {
                override fun onRewardEarned(rewardType: String, rewardAmount: Int) {
                    val unlockedLabel = TranslationHelper.getString("unlocked", "Unlocked")
                    android.widget.Toast.makeText(requireContext(), "$unlockedLabel: ${episode.title}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun showBackInterstitial() {
        NphAds.showInterstitial(
            activity = requireActivity(),
            nameSpace = "nsp-interstitial-feed-fullscreen-back",
            listener = object : NphAdListener() {
                override fun onAdDismissed() {
                    if (isAdded) findNavController().navigateUp()
                }
                override fun onAdFailed(error: AdError) {
                    if (isAdded) findNavController().navigateUp()
                }
            }
        )
    }

    override fun onPause() {
        super.onPause()
        videoAdapter?.pauseAllPlayers()
    }

    override fun onDestroyView() {
        binding.viewPagerVideos.adapter = null
        videoAdapter?.releaseAllPlayers()
        videoAdapter = null
        super.onDestroyView()
        requireActivity().requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        _binding = null
    }
}
