package com.nphstudio.mooveeon.ui.discover

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nphstudio.mooveeon.data.model.DramaSeries
import com.nphstudio.mooveeon.databinding.ItemDiscoverDramaBinding
import com.nphstudio.mooveeon.utils.GlideUtils
import com.nphstudio.mooveeon.utils.TranslationHelper

class DiscoverAdapter(
    private val items: List<DramaSeries>,
    private val historyMap: Map<String, Int> = emptyMap(),
    private val onWatchClick: (DramaSeries) -> Unit
) : RecyclerView.Adapter<DiscoverAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDiscoverDramaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemDiscoverDramaBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        private var isExpanded = false

        fun bind(item: DramaSeries) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description
            val episodeLabel = TranslationHelper.getString("episode", "Episode")
            val lastEpisode = historyMap[item.id]
            if (lastEpisode != null) {
                binding.tvEpisodeCount.text = "$episodeLabel $lastEpisode/${item.episodes.size}"
            } else {
                val notWatched = TranslationHelper.getString("not_watched", "Chưa xem")
                binding.tvEpisodeCount.text = "$notWatched - ${item.episodes.size} $episodeLabel"
            }
            
            // Reset state for recycled view
            isExpanded = false
            binding.tvDescription.maxLines = 1
            binding.tvSeeMore.text = TranslationHelper.getString("see_more", "See more")
            binding.tvSeeMore.visibility = android.view.View.VISIBLE

            val toggleDescription = {
                isExpanded = !isExpanded
                if (isExpanded) {
                    binding.tvDescription.maxLines = Integer.MAX_VALUE
                    binding.tvSeeMore.visibility = android.view.View.GONE
                } else {
                    binding.tvDescription.maxLines = 1
                    binding.tvSeeMore.visibility = android.view.View.VISIBLE
                }
            }

            binding.tvSeeMore.setOnClickListener { toggleDescription() }
            binding.tvDescription.setOnClickListener { if (isExpanded) toggleDescription() }
            
            GlideUtils.loadImage(binding.ivPoster, item.posterUrl)

            binding.btnWatchAll.text = TranslationHelper.getString("watch_all", "Watch All")
            binding.btnWatchAll.setOnClickListener { onWatchClick(item) }
        }
    }
}
