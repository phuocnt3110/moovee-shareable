package com.nphstudio.mooveeon.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.nphlab.sdk.ads.NphAds
import com.nphstudio.mooveeon.R
import com.nphstudio.mooveeon.data.model.DramaSeries
import com.nphstudio.mooveeon.databinding.FragmentHomeBinding
import com.nphstudio.mooveeon.utils.TranslationHelper
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.nphstudio.mooveeon.data.repository.DramaRepository

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var bannerAdapter: BannerAdapter
    private lateinit var trendingAdapter: DramaTrendingAdapter
    private lateinit var newReleasesAdapter: DramaNewAdapter
    private val db by lazy { com.nphstudio.mooveeon.data.local.AppDatabase.getDatabase(requireContext()) }
    private val repository by lazy { DramaRepository(requireContext()) }
    
    private var trendingDramas: List<DramaSeries> = emptyList()
    private var newReleaseDramas: MutableList<DramaSeries> = mutableListOf()
    
    private var currentPage = 1
    private val pageSize = 5
    private var isLoading = false
    private var isLastPage = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupAdapters()
        setupScrollListener()
        
        // Initial load
        loadInitialData()

        loadAds()
        
        // Preload interstitial ad
        NphAds.preload(requireActivity(), "nsp-interstitial-home-fullscreen-clickSettings")

        binding.ivSearch.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }

        binding.ivSettings.setOnClickListener {
            showSettingsInterstitial()
        }

        binding.tvMore.setOnClickListener {
            // "See more" for trending can navigate to Discover tab
            findNavController().navigate(R.id.discoverFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshHistory()
    }

    private fun refreshHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            val historyList = db.dramaDao().getAllHistory().first()
            val historyMap = historyList.associate { it.id to it.lastEpisode }
            trendingAdapter.updateHistory(historyMap)
            newReleasesAdapter.updateHistory(historyMap)
        }
    }

    private fun loadInitialData() {
        // Reset state to avoid issues when returning from search screen
        currentPage = 1
        isLastPage = false
        isLoading = false
        newReleaseDramas.clear()
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Load Trending
            trendingDramas = repository.getTrendingRemote()
            updateTrendingUI()
            
            // Load First Page of New Releases
            loadMoreDramas()
        }
    }

    private fun loadMoreDramas() {
        if (isLoading || isLastPage) return
        
        isLoading = true
        binding.pbLoadMore.visibility = View.VISIBLE
        
        viewLifecycleOwner.lifecycleScope.launch {
            val newData = repository.getDramas(currentPage, pageSize)
            
            if (newData.isEmpty()) {
                isLastPage = true
            } else {
                newReleaseDramas.addAll(newData)
                newReleasesAdapter.submitList(newReleaseDramas.toList())
                currentPage++
            }
            
            isLoading = false
            binding.pbLoadMore.visibility = View.GONE
        }
    }

    private fun setupScrollListener() {
        binding.nsvHome.setOnScrollChangeListener(androidx.core.widget.NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                loadMoreDramas()
            }
        })
    }

    private fun updateTrendingUI() {
        if (trendingDramas.isEmpty()) return

        bannerAdapter = BannerAdapter(trendingDramas.take(3)) { drama -> onDramaClick(drama) }
        binding.viewPagerBanners.adapter = bannerAdapter
        
        trendingAdapter.submitList(trendingDramas)
    }

    private fun showSettingsInterstitial() {
        NphAds.showInterstitial(
            activity = requireActivity(),
            nameSpace = "nsp-interstitial-home-fullscreen-clickSettings",
            listener = object : com.nphlab.sdk.ads.listener.NphAdListener() {
                override fun onAdDismissed() {
                    if (isAdded) findNavController().navigate(R.id.settingsFragment)
                }
                override fun onAdFailed(error: com.nphlab.sdk.ads.AdError) {
                    if (isAdded) findNavController().navigate(R.id.settingsFragment)
                }
            }
        )
    }

    private fun setupUI() {
        binding.tvTrendingTitle.text = TranslationHelper.getString("trending", "Trending")
        binding.tvMore.text = TranslationHelper.getString("more", "More >")
        binding.tvNewReleasesTitle.text = TranslationHelper.getString("new_releases", "New Releases")
    }

    private fun loadAds() {
        NphAds.loadBannerInto(binding.adBannerContainer, "nsp-banner-home-bottom-auto")
        NphAds.loadNativeInto(binding.adNativeContainer, "nsp-native-home-top-auto")
    }

    private fun setupAdapters() {
        bannerAdapter = BannerAdapter(emptyList()) { drama -> onDramaClick(drama) }
        binding.viewPagerBanners.adapter = bannerAdapter
        TabLayoutMediator(binding.tabLayoutDots, binding.viewPagerBanners) { _, _ -> }.attach()

        trendingAdapter = DramaTrendingAdapter { drama -> onDramaClick(drama) }
        binding.rvTrending.adapter = trendingAdapter

        newReleasesAdapter = DramaNewAdapter { drama -> onDramaClick(drama) }
        binding.rvNewReleases.adapter = newReleasesAdapter
    }

    private fun onDramaClick(drama: DramaSeries) {
        val bundle = Bundle().apply {
            putParcelable("drama", drama)
        }
        findNavController().navigate(R.id.action_homeFragment_to_feedFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
