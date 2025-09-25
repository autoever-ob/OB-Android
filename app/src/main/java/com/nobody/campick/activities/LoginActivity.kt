package com.nobody.campick.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nobody.campick.managers.UserState
import com.nobody.campick.services.network.TokenManager
import com.nobody.campick.ui.theme.CampickTheme
import com.nobody.campick.viewmodels.LoginViewModel
import com.nobody.campick.views.Login
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 로그인 액티비티
 * iOS의 LoginView와 동일한 역할
 */
class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 앱 시작 시 자동 로그인 체크 (iOS RootView와 동일한 로직)
        checkAuthenticationStateOnStart()

        setContent {
            CampickTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val loginViewModel: LoginViewModel = viewModel()

                    // 로그인 상태 관찰 - 로그인 성공 시 메인 액티비티로 이동
                    val isLoggedIn by UserState.isLoggedIn.collectAsState()

                    LaunchedEffect(isLoggedIn) {
                        if (isLoggedIn) {
                            println("🎉 로그인 완료 - MainTabActivity로 이동")
                            val intent = Intent(this@LoginActivity, MainTabActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        composable("login") {
                            Login(
                                navController = navController,
                                viewModel = loginViewModel,
                                onSignupClick = {
                                    val intent = Intent(this@LoginActivity, SignupActivity::class.java)
                                    startActivity(intent)
                                }
                            )
                        }

                        composable("findPassword") {
                            // 비밀번호 찾기 화면 (향후 구현)
                        }
                    }
                }
            }
        }
    }

    /**
     * 앱 시작 시 인증 상태 체크 (iOS RootView의 로직과 동일)
     * 이미 로그인된 사용자라면 즉시 MainTabActivity로 이동
     */
    private fun checkAuthenticationStateOnStart() {
        lifecycleScope.launch {
            val hasValidToken = TokenManager.isLoggedIn()
            val isUserLoggedIn = UserState.isLoggedIn.first()

            println("🔐 앱 시작 시 토큰 체크 결과: hasValidToken=$hasValidToken, isUserLoggedIn=$isUserLoggedIn")

            if (hasValidToken && isUserLoggedIn) {
                println("✅ 이미 인증된 상태 - MainTabActivity로 즉시 이동")
                navigateToMainTab()
            } else {
                println("❌ 인증되지 않은 상태 - 로그인 화면 표시")
            }
        }
    }

    /**
     * MainTabActivity로 이동 (현재 액티비티 종료)
     */
    private fun navigateToMainTab() {
        val intent = Intent(this, MainTabActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}