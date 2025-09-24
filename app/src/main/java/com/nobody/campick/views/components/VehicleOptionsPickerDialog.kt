package com.nobody.campick.views.components

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nobody.campick.R
import com.nobody.campick.models.vehicle.VehicleOption

class VehicleOptionsPickerDialog(
    context: Context,
    private val options: List<VehicleOption>,
    private val onOptionsSelected: (List<VehicleOption>) -> Unit
) : BottomSheetDialog(context, R.style.BottomSheetDialogTheme) {

    private val currentOptions = options.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_vehicle_options_picker, null)
        setContentView(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewOptions)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = OptionsAdapter(currentOptions) { position ->
            currentOptions[position] = currentOptions[position].copy(
                isInclude = !currentOptions[position].isInclude
            )
            recyclerView.adapter?.notifyItemChanged(position)
        }

        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnConfirm.setOnClickListener {
            onOptionsSelected(currentOptions)
            dismiss()
        }
    }

    private class OptionsAdapter(
        private val options: List<VehicleOption>,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<OptionsAdapter.OptionViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_vehicle_option, parent, false)
            return OptionViewHolder(view)
        }

        override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
            holder.bind(options[position], position, onItemClick)
        }

        override fun getItemCount() = options.size

        class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val ivCheckbox: ImageView = itemView.findViewById(R.id.ivCheckbox)
            private val tvOptionName: TextView = itemView.findViewById(R.id.tvOptionName)

            fun bind(option: VehicleOption, position: Int, onItemClick: (Int) -> Unit) {
                tvOptionName.text = option.optionName

                ivCheckbox.setImageResource(
                    if (option.isInclude) R.drawable.ic_checkbox_checked
                    else R.drawable.ic_checkbox_unchecked
                )

                itemView.setOnClickListener {
                    onItemClick(position)
                }
            }
        }
    }
}