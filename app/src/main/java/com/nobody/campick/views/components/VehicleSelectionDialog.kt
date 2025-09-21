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
import com.nobody.campick.databinding.ItemSelectionOptionBinding

class VehicleSelectionDialog(
    private val context: Context,
    private val title: String,
    private val options: List<String>,
    private val selectedOption: String? = null,
    private val onSelectionChanged: (String) -> Unit
) {

    private val dialog = Dialog(context)
    private lateinit var binding: DialogVehicleSelectionBinding
    private lateinit var adapter: SelectionAdapter

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
        binding.buttonClose.setOnClickListener { dialog.dismiss() }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = SelectionAdapter(options, selectedOption) { selectedItem ->
            onSelectionChanged(selectedItem)
            dialog.dismiss()
        }

        binding.recyclerViewSelections.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@VehicleSelectionDialog.adapter
        }
    }

    fun show() {
        dialog.show()
    }

    private class SelectionAdapter(
        private val options: List<String>,
        private val selectedOption: String?,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.Adapter<SelectionAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSelectionOptionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(options[position])
        }

        override fun getItemCount() = options.size

        inner class ViewHolder(private val binding: ItemSelectionOptionBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(option: String) {
                binding.textViewOption.text = option
                binding.imageViewCheck.visibility =
                    if (option == selectedOption) View.VISIBLE else View.GONE

                binding.root.setOnClickListener {
                    onItemClick(option)
                }
            }
        }
    }
}