package com.nphstudio.mooveeon.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nphstudio.mooveeon.databinding.FragmentHistoryBinding
import com.nphstudio.mooveeon.utils.TranslationHelper

import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.nphstudio.mooveeon.R
import com.nphstudio.mooveeon.data.local.AppDatabase
import com.nphstudio.mooveeon.data.local.FavoriteEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: HistoryAdapter
    private val db by lazy { AppDatabase.getDatabase(requireContext()) }
    private var currentTab = 0 // 0: History, 1: Favorites
    private var observeJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Check for starting tab from arguments
        currentTab = arguments?.getInt("start_tab", 0) ?: 0
        
        setupUI()
        setupRecyclerView()
        setupTabs()
        
        // Select the initial tab in the UI
        binding.tabHistory.getTabAt(currentTab)?.select()

        observeData()
    }

    private fun setupUI() {
        binding.tabHistory.getTabAt(0)?.text = TranslationHelper.getString("history", "History")
        binding.tabHistory.getTabAt(1)?.text = TranslationHelper.getString("favorites", "Favorites")
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            onItemClick = { dramaId ->
                lifecycleScope.launch {
                    val historyList = db.dramaDao().getAllHistory().first()
                    val favoriteList = db.dramaDao().getAllFavorites().first()
                    
                    val historyEntity = historyList.find { it.id == dramaId }
                    val favoriteEntity = favoriteList.find { it.id == dramaId }

                    val drama = when {
                        historyEntity != null -> {
                            com.nphstudio.mooveeon.data.model.DramaSeries(
                                id = historyEntity.id,
                                title = historyEntity.title,
                                description = "",
                                posterUrl = historyEntity.posterUrl,
                                episodes = emptyList()
                            )
                        }
                        favoriteEntity != null -> {
                            com.nphstudio.mooveeon.data.model.DramaSeries(
                                id = favoriteEntity.id,
                                title = favoriteEntity.title,
                                description = "",
                                posterUrl = favoriteEntity.posterUrl,
                                episodes = emptyList()
                            )
                        }
                        else -> null
                    }

                    drama?.let {
                        val bundle = Bundle().apply {
                            putParcelable("drama", it)
                        }
                        findNavController().navigate(R.id.action_historyFragment_to_feedFragment, bundle)
                    }
                }
            },
            onFavoriteClick = { item ->
                toggleFavorite(item)
            }
        )
        binding.rvHistory.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabHistory.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                observeData()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeData() {
        observeJob?.cancel()
        observeJob = lifecycleScope.launch {
            if (currentTab == 0) {
                db.dramaDao().getAllHistory().collect { historyList ->
                    val items = historyList.map {
                        HistoryFavoriteItem.History(
                            it.id, it.title, it.posterUrl, it.lastEpisode,
                            it.totalEpisodes, it.timestamp, it.isFavorite
                        )
                    }
                    adapter.submitList(items)
                }
            } else {
                db.dramaDao().getAllFavorites().collect { favoriteList ->
                    val historyList = db.dramaDao().getAllHistory().first()
                    val historyMap = historyList.associate { it.id to it.lastEpisode }
                    
                    val items = favoriteList.map {
                        HistoryFavoriteItem.Favorite(
                            it.id, it.title, it.posterUrl, it.totalEpisodes, it.timestamp,
                            historyMap[it.id] ?: 1
                        )
                    }
                    adapter.submitList(items)
                }
            }
        }
    }

    private fun toggleFavorite(item: HistoryFavoriteItem) {
        lifecycleScope.launch {
            when (item) {
                is HistoryFavoriteItem.History -> {
                    if (item.isFavorite) {
                        db.dramaDao().deleteFavoriteById(item.id)
                        db.dramaDao().updateHistoryFavorite(item.id, false)
                    } else {
                        db.dramaDao().insertFavorite(
                            FavoriteEntity(item.id, item.title, item.posterUrl, item.totalEpisodes)
                        )
                        db.dramaDao().updateHistoryFavorite(item.id, true)
                    }
                }
                is HistoryFavoriteItem.Favorite -> {
                    db.dramaDao().deleteFavoriteById(item.id)
                    db.dramaDao().updateHistoryFavorite(item.id, false)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
