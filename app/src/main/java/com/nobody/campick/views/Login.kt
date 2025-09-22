package com.nobody.campick.views
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.viewmodels.LoginViewModel

@Composable
fun Login(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val keepLoggedIn by viewModel.keepLoggedIn.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showSignupPrompt by viewModel.showSignupPrompt.collectAsState()
    val showServerAlert by viewModel.showServerAlert.collectAsState()
    var showPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.brandBackground)

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(150.dp))

            Text(
                text = "Campick",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = "프리미엄 캠핑카 플랫폼",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 이메일 입력
            Text("이메일", color = Color.White, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.onEmailChanged(it) },
                placeholder = { Text("이메일을 입력하세요") },
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email, // 기본 메일 아이콘
                        contentDescription = "이메일 아이콘",
                        tint = Color.Gray
                    )
                }

            )

            Spacer(modifier = Modifier.height(16.dp))

            // 비밀번호 입력
            Text("비밀번호", color = Color.White,modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                placeholder = { Text("비밀번호를 입력하세요") },
                textStyle = TextStyle(color = Color.White),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    val image = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (showPassword) "Hide password" else "Show password"

                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 로그인 유지 & 비밀번호 찾기
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { viewModel.toggleKeepLoggedIn() }
                ) {
                    Checkbox(
                        checked = keepLoggedIn,
                        onCheckedChange = { viewModel.toggleKeepLoggedIn() }
                    )
                    Text("로그인 유지", color = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "비밀번호 찾기",
                    color = Color(0xFFFF6F00), // brandOrange
                    modifier = Modifier.clickable {
                        navController.navigate("findPassword")
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 로그인 버튼
            Button(
                onClick = { viewModel.login() },
                enabled = !viewModel.isLoginDisabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.brandOrange,
                    contentColor = Color.White,
                    disabledContainerColor = AppColors.brandOrange80,
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                )

            ) {
                Text("로그인", fontWeight = FontWeight.Bold)
            }

            if (!errorMessage.isNullOrBlank()) {
                Text(
                    errorMessage ?: "",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 구분선
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.weight(1f))
                Text("또는", color = Color.White.copy(alpha = 0.6f))
                Divider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 회원가입
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("아직 계정이 없으신가요?", color = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "회원가입",
                    color = Color(0xFFFF6F00),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController.navigate("signupFlow")
                    }
                )
            }
        }
    }

    // Alert 예시
    if (showSignupPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSignupPrompt() },
            title = { Text("존재하지 않는 사용자입니다.") },
            text = { Text("Campick과 함께 새로운 계정을 만들어보세요.") },
            confirmButton = {
                TextButton(onClick = {
                    navController.navigate("signupFlow")
                    viewModel.dismissSignupPrompt()
                }) { Text("Campick과 함께하기") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSignupPrompt() }) { Text("닫기") }
            }
        )
    }

    if (showServerAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissServerAlert() },
            title = { Text("서버 연결이 불안정합니다.") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissServerAlert() }) { Text("확인") }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    val viewModel: LoginViewModel = viewModel()
    val navController = rememberNavController()
    // Preview에서는 실제 ViewModel 대신 Mock ViewModel을 써도 됨
    Login(navController = navController, viewModel = viewModel)
}