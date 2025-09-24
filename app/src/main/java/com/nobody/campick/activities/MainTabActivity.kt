package com.nobody.campick.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nobody.campick.R
import com.nobody.campick.databinding.ActivityMainTabBinding
import com.nobody.campick.fragments.FavoritesFragment
import com.nobody.campick.fragments.HomeFragment
import com.nobody.campick.fragments.ProfileFragment
import com.nobody.campick.fragments.VehicleRegistrationFragment
import com.nobody.campick.fragments.VehiclesFragment
import com.nobody.campick.managers.UserState
import com.nobody.campick.services.network.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainTabActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainTabBinding

    companion object {
        const val EXTRA_EDITING_PRODUCT_ID = "editing_product_id"
        const val EXTRA_INITIAL_TAB = "initial_tab"
        const val TAB_REGISTRATION = "tab_registration"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 토큰 체크 및 인증 상태 확인 (iOS RootView와 동일한 로직)
        checkAuthenticationState()

        binding = ActivityMainTabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUserStateObserver()

        // Intent에서 수정할 상품 ID 확인
        val editingProductId = intent.getStringExtra(EXTRA_EDITING_PRODUCT_ID)
        val initialTab = intent.getStringExtra(EXTRA_INITIAL_TAB)

        // 초기 화면 설정
        if (savedInstanceState == null) {
            when {
                editingProductId != null -> {
                    // 수정 모드로 VehicleRegistrationFragment 열기
                    replaceFragment(VehicleRegistrationFragment.newInstance(editingProductId))
                    // selectedItemId를 먼저 설정 (listener 설정 전)
                    binding.bottomNavigation.selectedItemId = R.id.nav_register
                }
                initialTab == TAB_REGISTRATION -> {
                    replaceFragment(VehicleRegistrationFragment())
                    binding.bottomNavigation.selectedItemId = R.id.nav_register
                }
                else -> {
                    replaceFragment(HomeFragment())
                    binding.bottomNavigation.selectedItemId = R.id.nav_home
                }
            }
        }

        // Fragment 설정 후 listener 설정 (selectedItemId 이후)
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

            when (item.itemId) {
                R.id.nav_home -> {
                    if (currentFragment !is HomeFragment) {
                        replaceFragment(HomeFragment())
                    }
                    true
                }
                R.id.nav_vehicles -> {
                    if (currentFragment !is VehiclesFragment) {
                        replaceFragment(VehiclesFragment())
                    }
                    true
                }
                R.id.nav_register -> {
                    if (currentFragment !is VehicleRegistrationFragment) {
                        replaceFragment(VehicleRegistrationFragment())
                    }
                    true
                }
                R.id.nav_favorites -> {
                    if (currentFragment !is FavoritesFragment) {
                        replaceFragment(FavoritesFragment())
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (currentFragment !is ProfileFragment) {
                        replaceFragment(ProfileFragment())
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun navigateToHome() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    fun navigateToFindVehicle() {
        binding.bottomNavigation.selectedItemId = R.id.nav_vehicles
    }

    /**
     * 앱 시작 시 인증 상태 체크 (iOS RootView의 로직과 동일)
     */
    private fun checkAuthenticationState() {
        lifecycleScope.launch {
            val hasValidToken = TokenManager.isLoggedIn()
            val isUserLoggedIn = UserState.isLoggedIn.first()

            println("🔐 토큰 체크 결과: hasValidToken=$hasValidToken, isUserLoggedIn=$isUserLoggedIn")

            if (!hasValidToken || !isUserLoggedIn) {
                println("❌ 인증되지 않은 상태 - LoginActivity로 이동")
                navigateToLogin()
            } else {
                println("✅ 인증된 상태 - 메인 화면 진행")
            }
        }
    }

    /**
     * UserState 변화 관찰 - 런타임 중 로그아웃 처리
     */
    private fun setupUserStateObserver() {
        lifecycleScope.launch {
            UserState.isLoggedIn.collect { isLoggedIn ->
                if (!isLoggedIn) {
                    println("🚪 사용자가 로그아웃됨 - LoginActivity로 이동")
                    navigateToLogin()
                }
            }
        }
    }

    /**
     * LoginActivity로 이동 (현재 액티비티 종료)
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}