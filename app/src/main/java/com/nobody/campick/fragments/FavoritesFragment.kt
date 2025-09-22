package com.nobody.campick.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.nobody.campick.activities.VehicleDetailActivity
import com.nobody.campick.ui.theme.CampickTheme
import com.nobody.campick.views.FavoritesView

class FavoritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CampickTheme {
                    FavoritesView(
                        onVehicleClick = { vehicleId ->
                            startActivity(VehicleDetailActivity.newIntent(requireContext(), vehicleId))
                        }
                    )
                }
            }
        }
    }
}