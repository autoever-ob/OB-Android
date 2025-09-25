package com.nobody.campick.activities

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.nobody.campick.R
import com.nobody.campick.databinding.ActivitySignupBinding
import com.nobody.campick.fragments.signup.*
import com.nobody.campick.viewmodels.SignupViewModel
import com.nobody.campick.views.components.CommonHeader
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: SignupViewModel by viewModels()
    private var currentProgress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupViewPager()
        observeViewModel()
    }

    private fun setupHeader() {
        binding.commonHeader.setupHeader(
            type = CommonHeader.HeaderType.Navigation(
                title = "회원가입",
                showBackButton = true,
                showRightButton = false
            ),
            onBackClick = {
                viewModel.goBack { finish() }
            }
        )
    }

    private fun setupViewPager() {
        binding.viewPager.adapter = SignupPagerAdapter(this)
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateProgressBar()
            }
        })
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.step.collect { step ->
                binding.viewPager.setCurrentItem(step.ordinal, false)
                updateTitle(step)
                animateProgress(step)
            }
        }
    }

    private fun updateTitle(step: SignupViewModel.Step) {
        val title = when (step) {
            SignupViewModel.Step.EMAIL -> "회원가입"
            SignupViewModel.Step.PASSWORD -> "비밀번호 설정"
            SignupViewModel.Step.PHONE -> "휴대폰 인증"
            SignupViewModel.Step.NICKNAME -> "닉네임 설정"
            SignupViewModel.Step.COMPLETE -> "가입 완료"
        }
        binding.commonHeader.setupHeader(
            type = CommonHeader.HeaderType.Navigation(
                title = title,
                showBackButton = true,
                showRightButton = false
            ),
            onBackClick = {
                viewModel.goBack { finish() }
            }
        )
    }

    private fun animateProgress(step: SignupViewModel.Step) {
        val targetProgress = when (step) {
            SignupViewModel.Step.EMAIL -> 25
            SignupViewModel.Step.PASSWORD -> 50
            SignupViewModel.Step.PHONE -> 75
            SignupViewModel.Step.NICKNAME -> 90
            SignupViewModel.Step.COMPLETE -> 100
        }

        val animator = ObjectAnimator.ofInt(binding.progressBar, "progress", currentProgress, targetProgress)
        animator.duration = 350
        animator.interpolator = DecelerateInterpolator()
        animator.start()

        currentProgress = targetProgress
    }

    private fun updateProgressBar() {
        val step = viewModel.step.value
        currentProgress = when (step) {
            SignupViewModel.Step.EMAIL -> 25
            SignupViewModel.Step.PASSWORD -> 50
            SignupViewModel.Step.PHONE -> 75
            SignupViewModel.Step.NICKNAME -> 90
            SignupViewModel.Step.COMPLETE -> 100
        }
        binding.progressBar.progress = currentProgress
    }

    private inner class SignupPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> EmailStepFragment()
                1 -> PasswordStepFragment()
                2 -> PhoneStepFragment()
                3 -> NicknameStepFragment()
                4 -> CompleteStepFragment()
                else -> EmailStepFragment()
            }
        }
    }
}