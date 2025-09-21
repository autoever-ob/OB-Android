package com.nobody.campick.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nobody.campick.databinding.ItemVehicleOptionBinding
import com.nobody.campick.models.vehicle.VehicleOption

class VehicleOptionAdapter(
    private val onOptionChecked: (VehicleOption, Boolean) -> Unit
) : ListAdapter<VehicleOption, VehicleOptionAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVehicleOptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemVehicleOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VehicleOption) {
            binding.apply {
                textViewOptionName.text = item.optionName
                checkBox.isChecked = item.isInclude

                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    onOptionChecked(item, isChecked)
                }

                root.setOnClickListener {
                    checkBox.isChecked = !checkBox.isChecked
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<VehicleOption>() {
        override fun areItemsTheSame(oldItem: VehicleOption, newItem: VehicleOption): Boolean {
            return oldItem.optionName == newItem.optionName
        }

        override fun areContentsTheSame(oldItem: VehicleOption, newItem: VehicleOption): Boolean {
            return oldItem == newItem
        }
    }
}