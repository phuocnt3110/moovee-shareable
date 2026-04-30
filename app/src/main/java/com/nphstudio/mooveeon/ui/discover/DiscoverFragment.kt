package com.nphstudio.mooveeon.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nphstudio.mooveeon.R
import com.nphstudio.mooveeon.data.repository.DramaRepository
import com.nphstudio.mooveeon.databinding.FragmentDiscoverBinding

import androidx.lifecycle.lifecycleScope
import com.nphstudio.mooveeon.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!
    private val repository by lazy { DramaRepository(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val db = AppDatabase.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            val dramas = repository.getDramas()
            val history = db.dramaDao().getAllHistory().first()
            val historyMap = history.associate { it.id to it.lastEpisode }

            val adapter = DiscoverAdapter(dramas, historyMap) { drama ->
                val bundle = Bundle().apply {
                    putParcelable("drama", drama)
                }
                findNavController().navigate(R.id.action_discoverFragment_to_feedFragment, bundle)
            }
            
            binding.viewPagerDiscover.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
