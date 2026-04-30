package com.nphstudio.mooveeon.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nphstudio.mooveeon.R
import com.nphstudio.mooveeon.data.model.Episode
import com.nphstudio.mooveeon.databinding.ItemEpisodeNumberBinding

class EpisodeAdapter(
    private var episodes: List<Episode>,
    private var currentEpisodeId: String,
    private val watchedEpisodeIds: Set<String>,
    private val onEpisodeClick: (Episode) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.ViewHolder>() {

    fun updateEpisodes(newEpisodes: List<Episode>, newCurrentId: String) {
        this.episodes = newEpisodes
        this.currentEpisodeId = newCurrentId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEpisodeNumberBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(episodes[position])
    }

    override fun getItemCount(): Int = episodes.size

    inner class ViewHolder(private val binding: ItemEpisodeNumberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(episode: Episode) {
            binding.tvEpisodeNumber.text = episode.episodeNumber.toString()
            
            val context = itemView.context
            val isLocked = episode.isLocked
            binding.ivLock.visibility = if (isLocked) android.view.View.VISIBLE else android.view.View.GONE
            
            when {
                episode.id == currentEpisodeId -> {
                    // Current episode: Green background
                    binding.tvEpisodeNumber.setBackgroundResource(R.drawable.bg_episode_item_selector)
                    binding.tvEpisodeNumber.backgroundTintList = ContextCompat.getColorStateList(context, R.color.green_main)
                    binding.tvEpisodeNumber.setTextColor(ContextCompat.getColor(context, R.color.white))
                    binding.tvEpisodeNumber.alpha = 1.0f
                }
                watchedEpisodeIds.contains(episode.id) -> {
                    // Watched: Gray text/background
                    binding.tvEpisodeNumber.setBackgroundResource(R.drawable.bg_episode_item_selector)
                    binding.tvEpisodeNumber.backgroundTintList = null
                    binding.tvEpisodeNumber.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                    binding.tvEpisodeNumber.alpha = 0.5f
                }
                else -> {
                    // Unwatched: Normal background
                    binding.tvEpisodeNumber.setBackgroundResource(R.drawable.bg_episode_item_selector)
                    binding.tvEpisodeNumber.backgroundTintList = null
                    binding.tvEpisodeNumber.setTextColor(ContextCompat.getColor(context, R.color.white))
                    binding.tvEpisodeNumber.alpha = if (isLocked) 0.6f else 1.0f
                }
            }

            binding.root.setOnClickListener { onEpisodeClick(episode) }
        }
    }
}
