package com.nphstudio.mooveeon.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nphstudio.mooveeon.R
import com.nphstudio.mooveeon.databinding.ItemSpeedOptionBinding

class SpeedAdapter(
    private val speeds: List<Float>,
    private var currentSpeed: Float,
    private val onSpeedClick: (Float) -> Unit
) : RecyclerView.Adapter<SpeedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSpeedOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(speeds[position])
    }

    override fun getItemCount(): Int = speeds.size

    inner class ViewHolder(private val binding: ItemSpeedOptionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(speed: Float) {
            binding.tvSpeedValue.text = if (speed == speed.toInt().toFloat()) "${speed.toInt()}x" else "${speed}x"
            
            val context = itemView.context
            if (speed == currentSpeed) {
                binding.tvSpeedValue.setBackgroundResource(R.drawable.bg_episode_item_selector)
                binding.tvSpeedValue.backgroundTintList = ContextCompat.getColorStateList(context, R.color.green_main)
            } else {
                binding.tvSpeedValue.setBackgroundResource(R.drawable.bg_episode_item_selector)
                binding.tvSpeedValue.backgroundTintList = null
            }
            
            binding.root.setOnClickListener { 
                currentSpeed = speed
                notifyDataSetChanged()
                onSpeedClick(speed)
            }
        }
    }
}
