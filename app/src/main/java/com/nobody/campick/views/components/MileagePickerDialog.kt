package com.nobody.campick.views.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import com.nobody.campick.R

class MileagePickerDialog(
    context: Context,
    private val initialMileage: Int?,
    private val onMileageSelected: (Int) -> Unit
) : Dialog(context) {

    private lateinit var mileagePicker: NumberPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_mileage_picker)

        mileagePicker = findViewById(R.id.mileagePicker)
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)

        mileagePicker.minValue = 0
        mileagePicker.maxValue = 500
        mileagePicker.wrapSelectorWheel = false

        val displayValues = (0..500).map { "${it * 1000}km" }.toTypedArray()
        mileagePicker.displayedValues = displayValues

        val initialValue = (initialMileage ?: 0) / 1000
        mileagePicker.value = initialValue.coerceIn(0, 500)

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnConfirm.setOnClickListener {
            val selectedMileage = mileagePicker.value * 1000
            onMileageSelected(selectedMileage)
            dismiss()
        }
    }
}