package com.nobody.campick.views.components

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nobody.campick.R
import com.nobody.campick.adapters.LocationAdapter
import com.nobody.campick.data.KoreanDistricts

class LocationPickerDialog(
    context: Context,
    private val initialLocation: String?,
    private val onLocationSelected: (String) -> Unit
) : BottomSheetDialog(context, R.style.BottomSheetDialogTheme) {

    private var selectedCity: String = ""
    private var selectedDistrict: String = ""

    private lateinit var cityAdapter: LocationAdapter
    private lateinit var districtAdapter: LocationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_location_picker)

        val recyclerViewCities = findViewById<RecyclerView>(R.id.recyclerViewCities)!!
        val recyclerViewDistricts = findViewById<RecyclerView>(R.id.recyclerViewDistricts)!!
        val btnCancel = findViewById<TextView>(R.id.btnCancel)!!
        val btnDone = findViewById<TextView>(R.id.btnDone)!!

        cityAdapter = LocationAdapter(KoreanDistricts.cities) { city ->
            selectedCity = city
            selectedDistrict = ""
            cityAdapter.selectedItem = city

            val districts = KoreanDistricts.getDistricts(city)
            districtAdapter = LocationAdapter(districts) { district ->
                selectedDistrict = district
                districtAdapter.selectedItem = district
            }
            recyclerViewDistricts.adapter = districtAdapter
        }

        districtAdapter = LocationAdapter(emptyList()) { district ->
            selectedDistrict = district
            districtAdapter.selectedItem = district
        }

        recyclerViewCities.layoutManager = LinearLayoutManager(context)
        recyclerViewCities.adapter = cityAdapter

        recyclerViewDistricts.layoutManager = LinearLayoutManager(context)
        recyclerViewDistricts.adapter = districtAdapter

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnDone.setOnClickListener {
            if (selectedCity.isNotEmpty() && selectedDistrict.isNotEmpty()) {
                onLocationSelected("$selectedCity $selectedDistrict")
            }
            dismiss()
        }
    }
}
