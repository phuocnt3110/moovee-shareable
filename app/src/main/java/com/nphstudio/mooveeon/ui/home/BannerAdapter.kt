package com.nphstudio.mooveeon.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nphstudio.mooveeon.data.model.DramaSeries
import com.nphstudio.mooveeon.databinding.ItemBannerBinding
import com.nphstudio.mooveeon.utils.GlideUtils

class BannerAdapter(
    private var banners: List<DramaSeries>,
    private val onItemClick: (DramaSeries) -> Unit
) : RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBannerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        binding.root.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(banners[position])
    }

    override fun getItemCount(): Int = banners.size

    inner class ViewHolder(private val binding: ItemBannerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DramaSeries) {
            binding.tvTitle.text = item.title
            GlideUtils.loadImage(binding.ivPoster, item.posterUrl)
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
