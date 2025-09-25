package com.nobody.campick.views.components

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nobody.campick.R
import java.util.Calendar

class YearPickerDialog(
    context: Context,
    private val currentYear: Int?,
    private val onYearSelected: (Int) -> Unit
) : BottomSheetDialog(context, R.style.BottomSheetDialogTheme) {

    private lateinit var yearPicker: NumberPicker
    private lateinit var btnDone: TextView
    private lateinit var btnCancel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_year_picker)

        yearPicker = findViewById(R.id.yearPicker)!!
        btnDone = findViewById(R.id.btnDone)!!
        btnCancel = findViewById(R.id.btnCancel)!!

        setupYearPicker()
        setupButtons()
    }

    @SuppressLint("SoonBlockedPrivateApi")
    private fun setupYearPicker() {
        val currentYearValue = Calendar.getInstance().get(Calendar.YEAR)
        val startYear = 1990

        yearPicker.apply {
            minValue = startYear
            maxValue = currentYearValue
            value = currentYear ?: currentYearValue
            wrapSelectorWheel = false

            setFormatter { value -> "${value}년" }

            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        }

        try {
            val selectorWheelPaintField = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
            selectorWheelPaintField.isAccessible = true
            (selectorWheelPaintField.get(yearPicker) as? android.graphics.Paint)?.apply {
                color = android.graphics.Color.WHITE
                textSize = 60f
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupButtons() {
        btnDone.setOnClickListener {
            onYearSelected(yearPicker.value)
            dismiss()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }
}