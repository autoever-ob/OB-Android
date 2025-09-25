package com.nobody.campick.views.components

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nobody.campick.R
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.services.CategoryService
import com.nobody.campick.services.network.ApiResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VehicleTypeModelPickerDialog(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val selectedType: String,
    private val selectedModel: String,
    private val availableTypes: List<String>,
    private val onTypeModelSelected: (String, String) -> Unit
) : BottomSheetDialog(context, R.style.BottomSheetDialogTheme) {

    private var tempSelectedType: String = selectedType
    private var tempSelectedModel: String = selectedModel
    private var availableModels: List<String> = emptyList()

    private lateinit var typeRecyclerView: RecyclerView
    private lateinit var modelRecyclerView: RecyclerView
    private lateinit var loadingContainer: View
    private lateinit var btnCancel: TextView
    private lateinit var btnConfirm: TextView

    private val typeAdapter = TypeAdapter(availableTypes) { type ->
        tempSelectedType = type
        tempSelectedModel = ""
        loadModelsForType(type)
    }

    private val modelAdapter = ModelAdapter(emptyList()) { model ->
        tempSelectedModel = model
        updateConfirmButton()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(
            R.layout.dialog_vehicle_type_model_picker,
            null
        )
        setContentView(view)

        typeRecyclerView = view.findViewById(R.id.recyclerViewTypes)
        modelRecyclerView = view.findViewById(R.id.recyclerViewModels)
        loadingContainer = view.findViewById(R.id.loadingContainer)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnConfirm = view.findViewById(R.id.btnConfirm)

        typeRecyclerView.layoutManager = LinearLayoutManager(context)
        typeRecyclerView.adapter = typeAdapter

        modelRecyclerView.layoutManager = LinearLayoutManager(context)
        modelRecyclerView.adapter = modelAdapter

        btnCancel.setOnClickListener { dismiss() }
        btnConfirm.setOnClickListener {
            if (tempSelectedType.isNotEmpty() && tempSelectedModel.isNotEmpty()) {
                onTypeModelSelected(tempSelectedType, tempSelectedModel)
                dismiss()
            }
        }

        // 초기 선택값이 있으면 모델 로드
        if (tempSelectedType.isNotEmpty()) {
            loadModelsForType(tempSelectedType)
        }

        updateConfirmButton()
    }

    private fun loadModelsForType(type: String) {
        loadingContainer.isVisible = true
        modelRecyclerView.isVisible = false

        lifecycleOwner.lifecycleScope.launch {
            try {
                val result = CategoryService.getModelsForType(type)
                when (result) {
                    is ApiResult.Success -> {
                        availableModels = result.data
                        modelAdapter.updateModels(availableModels, tempSelectedModel)
                        modelRecyclerView.isVisible = true
                    }
                    is ApiResult.Error -> {
                        Toast.makeText(context, "모델 로드 실패: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "모델 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                loadingContainer.isVisible = false
            }
        }
    }

    private fun updateConfirmButton() {
        btnConfirm.isEnabled = tempSelectedType.isNotEmpty() && tempSelectedModel.isNotEmpty()
        btnConfirm.alpha = if (btnConfirm.isEnabled) 1.0f else 0.5f
    }

    inner class TypeAdapter(
        private val types: List<String>,
        private val onTypeClick: (String) -> Unit
    ) : RecyclerView.Adapter<TypeAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.textViewItem)
            val checkIcon: ImageView = view.findViewById(R.id.imageViewCheck)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_picker_option, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val type = types[position]
            holder.textView.text = type

            val isSelected = type == tempSelectedType
            holder.checkIcon.isVisible = isSelected
            holder.itemView.setBackgroundColor(
                if (isSelected) AppColors.brandOrange.copy(alpha = 0.1f).toArgb()
                else Color.TRANSPARENT
            )

            holder.itemView.setOnClickListener {
                onTypeClick(type)
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = types.size
    }

    inner class ModelAdapter(
        private var models: List<String>,
        private val onModelClick: (String) -> Unit
    ) : RecyclerView.Adapter<ModelAdapter.ViewHolder>() {

        private var currentSelectedModel: String? = null

        fun updateModels(newModels: List<String>, selected: String?) {
            models = newModels
            currentSelectedModel = selected
            notifyDataSetChanged()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView: TextView = view.findViewById(R.id.textViewItem)
            val checkIcon: ImageView = view.findViewById(R.id.imageViewCheck)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_picker_option, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val model = models[position]
            holder.textView.text = model

            val isSelected = model == currentSelectedModel
            holder.checkIcon.isVisible = isSelected
            holder.itemView.setBackgroundColor(
                if (isSelected) AppColors.brandOrange.copy(alpha = 0.1f).toArgb()
                else Color.TRANSPARENT
            )

            holder.itemView.setOnClickListener {
                currentSelectedModel = model
                onModelClick(model)
                notifyDataSetChanged()
            }
        }

        override fun getItemCount() = models.size
    }
}