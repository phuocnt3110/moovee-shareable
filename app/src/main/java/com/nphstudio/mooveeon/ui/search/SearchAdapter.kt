package com.nphstudio.mooveeon.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nphstudio.mooveeon.data.model.DramaSeries
import com.nphstudio.mooveeon.databinding.ItemSearchResultBinding
import com.nphstudio.mooveeon.utils.GlideUtils
import com.nphstudio.mooveeon.utils.TranslationHelper

class SearchAdapter(
    private var historyMap: Map<String, Int> = emptyMap(),
    private val onItemClick: (DramaSeries) -> Unit
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private var items: List<DramaSeries> = emptyList()

    fun submitList(newItems: List<DramaSeries>, newHistoryMap: Map<String, Int>? = null) {
        items = newItems
        newHistoryMap?.let { this.historyMap = it }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
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

            GlideUtils.loadImage(binding.ivPoster, item.posterUrl)
            binding.btnWatch.text = TranslationHelper.getString("watch_drama", "Watch Drama")
            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnWatch.setOnClickListener { onItemClick(item) }
        }
    }
}
