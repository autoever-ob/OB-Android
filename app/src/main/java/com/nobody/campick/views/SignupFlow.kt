package com.nobody.campick.views

import EmailStep
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.nobody.campick.components.signup.CompleteStep
import com.nobody.campick.components.signup.NicknameStep
import com.nobody.campick.components.signup.PasswordStep
import com.nobody.campick.components.signup.PhoneStep
import com.nobody.campick.components.signup.SignupProgress
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.viewmodels.SignupFlowViewModel
import com.nobody.campick.viewmodels.SignupStep
import com.nobody.campick.views.components.TopBarView
import kotlinx.coroutines.launch

@Composable
fun SignupFlow(
    navController: NavController,
    vm: SignupFlowViewModel = viewModel(),
    onComplete: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppColors.brandBackground,
        topBar = {
            TopBarView(
                title = vm.title(),
                onBackClick = { vm.goBack { navController.popBackStack() } }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 진행바
            SignupProgress(
                progress = vm.progress,
                startFrom = vm.prevProgress,
            )


            // 단계별 화면
            when (vm.step) {
                SignupStep.Email -> {
                    EmailStep(
                        userType = vm.userType,
                        email = vm.email,
                        showCodeField = vm.showEmailCodeField,
                        code = vm.emailCode,
                        showMismatchModal = vm.showEmailMismatchModal,
                        showDuplicateModal = vm.showEmailDuplicateModal,
                        termsAgreed = vm.termsAgreed,
                        privacyAgreed = vm.privacyAgreed,
                        onUserTypeChange = { vm.onUserTypeChange(it) },
                        onEmailChange = { vm.onEmailChange(it) },
                        onCodeChange = { vm.onEmailCodeChange(it) },
                        onTermsChange = { vm.onTermsChange(it) },
                        onPrivacyChange = { vm.onPrivacyChange(it) },
                        onNext = { vm.emailNext() },
                        onSend = { coroutineScope.launch { vm.sendEmailCode() } },
                        onDuplicateLogin = { navController.popBackStack() },
                        onDuplicateFindPassword = {
                            // navController.navigate("findPassword")
                        },
                        onDismissMismatch = { vm.showEmailMismatchModal = false }
                    )
                }

                SignupStep.Password -> {
                    PasswordStep(
                        password = vm.password,
                        confirm = vm.confirm,
                        errorMessage = vm.passwordError,
                        onPasswordChange = { vm.password = it },
                        onConfirmChange = { vm.confirm = it },
                        onNext = { vm.passwordNext() }
                    )
                }

                SignupStep.Phone -> {
                    PhoneStep(
                        userType = vm.userType,
                        phone = vm.phone,
                        showCodeField = vm.showPhoneCodeField,
                        code = vm.phoneCode,
                        codeVerified = vm.codeVerified,
                        showDealerField = vm.showDealerField,
                        dealerNumber = vm.dealerNumber,
                        errorMessage = vm.phoneError,
                        onPhoneChange = { vm.phone = it },
                        onCodeChange = { vm.phoneCode = it },
                        onDealerChange = { vm.dealerNumber = it },
                        onCodeVerified = { vm.codeVerified = true },
                        onDealerVerified = { vm.onDealerVerified()},
                        onNext = { vm.phoneNext() },
                        onSend = { vm.startPhoneCodeFlow() },
                        onShowCodeFieldChange = { vm.showPhoneCodeField = it }

                    )
                }

                SignupStep.Nickname -> {
                    NicknameStep(
                        nickname = vm.nickname,
                        selectedImage = vm.selectedImage,
                        onCameraClick = { vm.showCamera },
                        onGalleryClick = { vm.showGallery },
                        isSubmitting = vm.isSubmitting,
                        submitError = vm.submitError,
                        onNicknameChange = { vm.nickname = it },
                        onNext = { vm.nicknameNext() }
                    )
                }

                SignupStep.Complete -> {
                    CompleteStep(
                        onAutoForward = { coroutineScope.launch { vm.autoLoginAfterSignup() } }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    // 서버 연결 불안정 Alert
    if (vm.showServerAlert) {
        AlertDialog(
            onDismissRequest = { vm.showServerAlert = false },
            confirmButton = {
                TextButton(onClick = { vm.showServerAlert = false }) {
                    Text("확인")
                }
            },
            text = { Text("서버 연결이 불안정합니다. 잠시후 다시 시도해 주세요") }
        )
    }

    // Home 화면으로 이동
    LaunchedEffect(vm.shouldNavigateHome) {
        if (vm.shouldNavigateHome) {
            vm.shouldNavigateHome = false
            onComplete()
        }
    }
}