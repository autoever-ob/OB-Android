package com.nobody.campick.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.nobody.campick.R

class LocationAdapter(
    private val items: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    var selectedItem: String = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vehicle_type_model, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvText: TextView = itemView.findViewById(R.id.tvText)
        private val ivCheckmark: ImageView = itemView.findViewById(R.id.ivCheckmark)

        fun bind(item: String) {
            tvText.text = item
            ivCheckmark.isVisible = (item == selectedItem)

            itemView.setBackgroundColor(
                if (item == selectedItem)
                    itemView.context.getColor(R.color.brand_orange_10)
                else
                    android.graphics.Color.TRANSPARENT
            )

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}