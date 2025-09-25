package com.nobody.campick.fragments.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.nobody.campick.databinding.FragmentPasswordStepBinding
import com.nobody.campick.viewmodels.SignupViewModel
import kotlinx.coroutines.launch

class PasswordStepFragment : Fragment() {

    private var _binding: FragmentPasswordStepBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordStepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPasswordInput()
        setupConfirmPasswordInput()
        observeViewModel()
    }

    private fun setupPasswordInput() {
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s?.toString() ?: ""
                viewModel.setPassword(password)
            }
        })
    }

    private fun setupConfirmPasswordInput() {
        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val confirm = s?.toString() ?: ""
                viewModel.setConfirmPassword(confirm)
            }
        })

        binding.btnNext.setOnClickListener {
            viewModel.passwordNext()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.password.collect { password ->
                if (binding.etPassword.text?.toString() != password) {
                    binding.etPassword.setText(password)
                }
                binding.tvConfirmLabel.isVisible = password.isNotEmpty()
                binding.layoutConfirmPassword.isVisible = password.isNotEmpty()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.confirmPassword.collect { confirm ->
                if (binding.etConfirmPassword.text?.toString() != confirm) {
                    binding.etConfirmPassword.setText(confirm)
                }
                binding.btnNext.isVisible = confirm.isNotEmpty()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.passwordError.collect { error ->
                binding.tvError.isVisible = error != null
                binding.tvError.text = error
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}