package com.nobody.campick.fragments.signup

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.nobody.campick.databinding.FragmentPhoneStepBinding
import com.nobody.campick.models.auth.UserType
import com.nobody.campick.viewmodels.SignupViewModel
import kotlinx.coroutines.launch

class PhoneStepFragment : Fragment() {

    private var _binding: FragmentPhoneStepBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignupViewModel by activityViewModels()

    private var countDownTimer: CountDownTimer? = null
    private var remainingSeconds = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneStepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPhoneInput()
        setupVerificationCodeInput()
        setupDealerInput()
        observeViewModel()
    }

    private fun setupPhoneInput() {
        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val formatted = formatPhone(s?.toString() ?: "")
                if (formatted != s?.toString()) {
                    binding.etPhone.setText(formatted)
                    binding.etPhone.setSelection(formatted.length)
                }
                viewModel.setPhone(formatted)
            }
        })

        binding.btnSendCode.setOnClickListener {
            viewModel.showPhoneCodeField()
            binding.layoutVerification.isVisible = true
            startTimer()
            binding.etVerificationCode.requestFocus()
        }
    }

    private fun setupVerificationCodeInput() {
        binding.etVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val digits = s?.toString()?.filter { it.isDigit() } ?: ""
                if (digits != s?.toString()) {
                    binding.etVerificationCode.setText(digits)
                    binding.etVerificationCode.setSelection(digits.length)
                }
                viewModel.setPhoneCode(digits)
                binding.tvExpiredNotice.isVisible = false
            }
        })

        binding.btnNext.setOnClickListener {
            if (remainingSeconds == 0) {
                binding.tvExpiredNotice.isVisible = true
                binding.etVerificationCode.setText("")
            } else {
                viewModel.phoneNext()
            }
        }
    }

    private fun setupDealerInput() {
        binding.etDealerNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val digits = s?.toString()?.filter { it.isDigit() } ?: ""
                if (digits != s?.toString()) {
                    binding.etDealerNumber.setText(digits)
                    binding.etDealerNumber.setSelection(digits.length)
                }
                viewModel.setDealerNumber(digits)
            }
        })

        binding.btnDealerNext.setOnClickListener {
            viewModel.dealerNext()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.phone.collect { phone ->
                if (binding.etPhone.text?.toString() != phone) {
                    binding.etPhone.setText(phone)
                }
                updateSendButtonState()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showPhoneCodeField.collect { show ->
                binding.layoutVerification.isVisible = show
                if (show) {
                    startTimer()
                    binding.etVerificationCode.requestFocus()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.phoneCode.collect { code ->
                binding.btnNext.isVisible = code.isNotEmpty() && !viewModel.phoneCodeVerified.value
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showDealerField.collect { show ->
                binding.layoutDealer.isVisible = show
                if (show) {
                    binding.etDealerNumber.requestFocus()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dealerNumber.collect { number ->
                binding.btnDealerNext.isVisible = number.isNotEmpty()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.phoneError.collect { error ->
                binding.tvError.isVisible = error != null
                binding.tvError.text = error
            }
        }
    }

    private fun formatPhone(input: String): String {
        val digits = input.filter { it.isDigit() }
        return when {
            digits.length <= 3 -> digits
            digits.length <= 7 -> {
                val a = digits.substring(0, 3)
                val b = digits.substring(3)
                "$a-$b"
            }
            else -> {
                val a = digits.substring(0, 3)
                val midLen = if (digits.length == 11) 4 else 3
                val b = digits.substring(3, 3 + midLen.coerceAtMost(digits.length - 3))
                val c = if (digits.length > 3 + midLen) digits.substring(3 + midLen) else ""
                if (c.isEmpty()) "$a-$b" else "$a-$b-$c"
            }
        }
    }

    private fun isValidPhone(phone: String): Boolean {
        val digits = phone.filter { it.isDigit() }
        return digits.length == 10 || digits.length == 11
    }

    private fun updateSendButtonState() {
        val valid = isValidPhone(binding.etPhone.text?.toString() ?: "")
        binding.btnSendCode.isEnabled = valid
        binding.btnSendCode.alpha = if (valid) 1.0f else 0.5f

        val expired = binding.layoutVerification.isVisible && remainingSeconds == 0
        binding.btnSendCode.text = if (expired) "재전송하기" else "인증하기"
    }

    private fun startTimer() {
        countDownTimer?.cancel()
        remainingSeconds = 180

        countDownTimer = object : CountDownTimer(180000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = (millisUntilFinished / 1000).toInt()
                updateTimerUI()
            }

            override fun onFinish() {
                remainingSeconds = 0
                updateTimerUI()
                updateSendButtonState()
            }
        }.start()
    }

    private fun updateTimerUI() {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)

        val isExpired = remainingSeconds == 0
        val color = if (isExpired) {
            ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
        } else {
            ContextCompat.getColor(requireContext(), android.R.color.white)
        }

        binding.tvTimer.setTextColor(color)
        binding.ivTimer.setColorFilter(color)
        binding.tvTimer.alpha = if (isExpired) 1.0f else 0.7f
        binding.ivTimer.alpha = if (isExpired) 1.0f else 0.7f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
        _binding = null
    }
}