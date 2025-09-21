package com.nobody.campick.views.components

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nobody.campick.databinding.DialogVehicleSelectionBinding
import com.nobody.campick.databinding.ItemMultiSelectionOptionBinding
import com.nobody.campick.models.vehicle.VehicleOption

class VehicleMultiSelectionDialog(
    private val context: Context,
    private val title: String,
    private val options: List<VehicleOption>,
    private val onSelectionChanged: (List<VehicleOption>) -> Unit
) {

    private val dialog = Dialog(context)
    private lateinit var binding: DialogVehicleSelectionBinding
    private lateinit var adapter: MultiSelectionAdapter
    private val selectedOptions = options.toMutableList()

    init {
        setupDialog()
    }

    private fun setupDialog() {
        binding = DialogVehicleSelectionBinding.inflate(LayoutInflater.from(context))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.textViewDialogTitle.text = title
        binding.buttonClose.setOnClickListener {
            onSelectionChanged(selectedOptions)
            dialog.dismiss()
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = MultiSelectionAdapter(selectedOptions) { position, isSelected ->
            selectedOptions[position] = selectedOptions[position].copy(isInclude = isSelected)
        }

        binding.recyclerViewSelections.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@VehicleMultiSelectionDialog.adapter
        }
    }

    fun show() {
        dialog.show()
    }

    private class MultiSelectionAdapter(
        private val options: MutableList<VehicleOption>,
        private val onItemToggle: (Int, Boolean) -> Unit
    ) : RecyclerView.Adapter<MultiSelectionAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemMultiSelectionOptionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(options[position], position)
        }

        override fun getItemCount() = options.size

        inner class ViewHolder(private val binding: ItemMultiSelectionOptionBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(option: VehicleOption, position: Int) {
                binding.textViewOption.text = option.optionName

                // 체크박스 상태 설정
                val isSelected = option.isInclude
                binding.checkboxBackground.isSelected = isSelected
                binding.imageViewCheck.visibility = if (isSelected) View.VISIBLE else View.GONE

                binding.root.setOnClickListener {
                    val newState = !option.isInclude
                    options[position] = option.copy(isInclude = newState)

                    // UI 업데이트
                    binding.checkboxBackground.isSelected = newState
                    binding.imageViewCheck.visibility = if (newState) View.VISIBLE else View.GONE

                    onItemToggle(position, newState)
                }
            }
        }
    }
}