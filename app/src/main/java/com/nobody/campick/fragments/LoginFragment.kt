package com.nobody.campick.fragments

import androidx.fragment.app.Fragment
import com.nobody.campick.viewmodels.LoginViewModel
import com.nobody.campick.views.Login
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.compose.rememberNavController
import com.nobody.campick.ui.theme.CampickTheme

class LoginFragment : Fragment() {
    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val navController = rememberNavController()
                val viewModel = LoginViewModel()
                CampickTheme {
                    Login(navController, viewModel)
                }

            }
        }
    }
}