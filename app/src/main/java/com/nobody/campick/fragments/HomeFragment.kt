package com.nobody.campick.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.compose.rememberNavController
import com.nobody.campick.activities.VehicleDetailActivity
import com.nobody.campick.databinding.FragmentHomeBinding
import com.nobody.campick.ui.theme.CampickTheme
import com.nobody.campick.viewmodels.LoginViewModel
import com.nobody.campick.viewmodels.SignupFlowViewModel
import com.nobody.campick.views.FavoritesView
import com.nobody.campick.views.Home
import com.nobody.campick.views.Login
import com.nobody.campick.views.SignupFlow

class HomeFragment : Fragment() {
    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = rememberNavController()
//                val viewModel = LoginViewModel()
                val viewModel = SignupFlowViewModel()

                CampickTheme {
//                    Home()
                    SignupFlow(navController, viewModel)
                }

            }
        }
    }
}