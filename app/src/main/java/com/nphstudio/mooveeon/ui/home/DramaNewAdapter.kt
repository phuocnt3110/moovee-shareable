package com.nphstudio.mooveeon.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nphstudio.mooveeon.data.model.DramaSeries
import com.nphstudio.mooveeon.databinding.ItemDramaNewBinding
import com.nphstudio.mooveeon.utils.GlideUtils
import com.nphstudio.mooveeon.utils.TranslationHelper

class DramaNewAdapter(
    private var historyMap: Map<String, Int> = emptyMap(),
    private val onItemClick: (DramaSeries) -> Unit
) : ListAdapter<DramaSeries, DramaNewAdapter.ViewHolder>(DiffCallback()) {

    fun updateHistory(newHistory: Map<String, Int>) {
        this.historyMap = newHistory
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDramaNewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemDramaNewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DramaSeries) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description
            
            val episodeLabel = TranslationHelper.getString("episode", "Episode")
            val lastEpisode = historyMap[item.id]
            if (lastEpisode != null) {
                binding.tvEpisodeCount.text = "$episodeLabel $lastEpisode/${item.episodes.size}"
            } else {
                val notWatched = TranslationHelper.getString("not_watched", "Unwatched")
                binding.tvEpisodeCount.text = "$notWatched - ${item.episodes.size} $episodeLabel"
            }

            GlideUtils.loadImage(binding.ivPoster, item.posterUrl)
            binding.btnWatch.text = TranslationHelper.getString("watch_drama", "Watch Drama")
            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnWatch.setOnClickListener { onItemClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DramaSeries>() {
        override fun areItemsTheSame(oldItem: DramaSeries, newItem: DramaSeries): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DramaSeries, newItem: DramaSeries): Boolean {
            return oldItem == newItem
        }
    }
}
