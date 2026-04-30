package com.nphstudio.mooveeon.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nphstudio.mooveeon.R
import com.nphstudio.mooveeon.data.model.DramaSeries
import com.nphstudio.mooveeon.data.repository.DramaRepository
import com.nphstudio.mooveeon.databinding.FragmentSearchBinding
import com.nphstudio.mooveeon.utils.TranslationHelper

import androidx.lifecycle.lifecycleScope
import com.nphstudio.mooveeon.data.local.AppDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchAdapter: SearchAdapter
    private val db by lazy { AppDatabase.getDatabase(requireContext()) }
    private val repository by lazy { DramaRepository(requireContext()) }
    private var allSearchResults: MutableList<DramaSeries> = mutableListOf()
    private var historyMap: Map<String, Int> = emptyMap()
    
    private var currentPage = 1
    private val pageSize = 5
    private var isLoading = false
    private var isLastPage = false
    private var currentQuery = ""
    private var searchJob: Job? = null
    private var loadJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()

        viewLifecycleOwner.lifecycleScope.launch {
            val history = db.dramaDao().getAllHistory().first()
            historyMap = history.associate { it.id to it.lastEpisode }
            loadInitialData()
        }
    }

    private fun loadInitialData() {
        currentPage = 1
        isLastPage = false
        allSearchResults.clear()
        searchAdapter.submitList(emptyList(), historyMap)
        loadMoreResults()
    }

    private fun loadMoreResults() {
        if (isLoading || isLastPage) return
        
        isLoading = true
        binding.pbSearchLoading.visibility = View.VISIBLE
        
        loadJob?.cancel()
        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            val newData = if (currentQuery.isEmpty()) {
                repository.getDramas(currentPage, pageSize)
            } else {
                repository.searchRemote(currentQuery, currentPage, pageSize)
            }
            
            if (newData.isEmpty()) {
                isLastPage = true
            } else {
                allSearchResults.addAll(newData)
                searchAdapter.submitList(allSearchResults.toList(), historyMap)
                currentPage++
            }
            
            isLoading = false
            binding.pbSearchLoading.visibility = View.GONE
            binding.tvNoResults.visibility = if (allSearchResults.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupUI() {
        binding.etSearch.hint = TranslationHelper.getString("search_hint", "Search here")
        binding.tvNoResults.text = TranslationHelper.getString("no_results", "No results found")

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        searchAdapter = SearchAdapter(historyMap) { drama ->
            val bundle = Bundle().apply {
                putParcelable("drama", drama)
            }
            findNavController().navigate(R.id.action_searchFragment_to_feedFragment, bundle)
        }
        
        binding.rvSearchResults.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = searchAdapter
            setHasFixedSize(true)
            
            addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= pageSize) {
                            loadMoreResults()
                        }
                    }
                }
            })
        }

        binding.etSearch.addTextChangedListener { text ->
            binding.ivClear.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
            val query = text.toString()
            if (query != currentQuery) {
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(500)
                    currentQuery = query
                    loadInitialData()
                }
            }
        }

        binding.ivClear.setOnClickListener {
            binding.etSearch.text.clear()
            currentQuery = ""
            loadInitialData()
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentQuery = binding.etSearch.text.toString()
                loadInitialData()
                true
            } else false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
