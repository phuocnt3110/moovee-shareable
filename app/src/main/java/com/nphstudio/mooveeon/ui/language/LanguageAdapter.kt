package com.nphstudio.mooveeon.ui.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nphstudio.mooveeon.databinding.ItemLanguageBinding

data class LanguageItem(
    val nameRes: Int,
    val flagRes: Int,
    val code: String
)

class LanguageAdapter(
    private val items: List<LanguageItem>,
    private val onItemClick: (LanguageItem) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    fun setSelectedPosition(position: Int) {
        val previous = selectedPosition
        selectedPosition = position
        notifyItemChanged(previous)
        notifyItemChanged(selectedPosition)
    }

    override fun getItemCount() = items.size

    fun getSelectedItem(): LanguageItem? = items.getOrNull(selectedPosition)

    inner class ViewHolder(private val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LanguageItem, isSelected: Boolean) {
            binding.tvName.setText(item.nameRes)
            binding.ivFlag.setImageResource(item.flagRes)
            binding.ivRadio.setImageResource(
                if (isSelected) com.nphstudio.mooveeon.R.drawable.ic_radio_selected
                else com.nphstudio.mooveeon.R.drawable.ic_radio_unselected
            )
            binding.clItem.isSelected = isSelected
            binding.root.setOnClickListener {
                if (selectedPosition != adapterPosition) {
                    val previous = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(previous)
                    notifyItemChanged(selectedPosition)
                    onItemClick(item)
                }
            }
        }
    }
}
