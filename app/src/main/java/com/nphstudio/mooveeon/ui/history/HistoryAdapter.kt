package com.nphstudio.mooveeon.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nphstudio.mooveeon.R
import com.nphstudio.mooveeon.databinding.ItemHistoryFavoriteBinding
import com.nphstudio.mooveeon.utils.GlideUtils
import com.nphstudio.mooveeon.utils.TranslationHelper
import java.text.SimpleDateFormat
import java.util.*

sealed class HistoryFavoriteItem {
    data class History(
        val id: String,
        val title: String,
        val posterUrl: String,
        val episode: Int,
        val totalEpisodes: Int,
        val timestamp: Long,
        val isFavorite: Boolean
    ) : HistoryFavoriteItem()

    data class Favorite(
        val id: String,
        val title: String,
        val posterUrl: String,
        val totalEpisodes: Int,
        val timestamp: Long,
        val lastEpisode: Int
    ) : HistoryFavoriteItem()
}

class HistoryAdapter(
    private val onItemClick: (String) -> Unit,
    private val onFavoriteClick: (HistoryFavoriteItem) -> Unit
) : ListAdapter<HistoryFavoriteItem, HistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryFavoriteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHistoryFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

        fun bind(item: HistoryFavoriteItem) {
            val episodeLabel = TranslationHelper.getString("episode", "Episode")
            when (item) {
                is HistoryFavoriteItem.History -> {
                    binding.tvTitle.text = item.title
                    binding.tvEpisode.text = "$episodeLabel ${item.episode}/${item.totalEpisodes}"
                    binding.tvDate.text = dateFormat.format(Date(item.timestamp))
                    binding.ivFavorite.setImageResource(
                        if (item.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
                    )
                    binding.ivFavorite.tag = item.isFavorite
                }
                is HistoryFavoriteItem.Favorite -> {
                    binding.tvTitle.text = item.title
                    binding.tvEpisode.text = "$episodeLabel ${item.lastEpisode}/${item.totalEpisodes}"
                    binding.tvDate.text = dateFormat.format(Date(item.timestamp))
                    binding.ivFavorite.setImageResource(R.drawable.ic_favorite_filled)
                    binding.ivFavorite.tag = true
                }
            }

            val posterUrl = when (item) {
                is HistoryFavoriteItem.History -> item.posterUrl
                is HistoryFavoriteItem.Favorite -> item.posterUrl
            }

            GlideUtils.loadImage(binding.ivPoster, posterUrl)

            binding.root.setOnClickListener {
                val id = when (item) {
                    is HistoryFavoriteItem.History -> item.id
                    is HistoryFavoriteItem.Favorite -> item.id
                }
                onItemClick(id)
            }

            binding.ivFavorite.setOnClickListener {
                onFavoriteClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryFavoriteItem>() {
        override fun areItemsTheSame(oldItem: HistoryFavoriteItem, newItem: HistoryFavoriteItem): Boolean {
            return when {
                oldItem is HistoryFavoriteItem.History && newItem is HistoryFavoriteItem.History -> oldItem.id == newItem.id
                oldItem is HistoryFavoriteItem.Favorite && newItem is HistoryFavoriteItem.Favorite -> oldItem.id == newItem.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: HistoryFavoriteItem, newItem: HistoryFavoriteItem): Boolean {
            return oldItem == newItem
        }
    }
}
