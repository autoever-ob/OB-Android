import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nobody.campick.resources.theme.AppColors
import kotlinx.coroutines.delay
import com.nobody.campick.viewmodels.UserType
@Composable
fun EmailStep(
    userType: UserType?,
    email: String,
    showCodeField: Boolean,
    code: String,
    showMismatchModal: Boolean,
    showDuplicateModal: Boolean,
    termsAgreed: Boolean,
    privacyAgreed: Boolean,
    onUserTypeChange: (UserType) -> Unit,
    onEmailChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onTermsChange: (Boolean) -> Unit,
    onPrivacyChange: (Boolean) -> Unit,
    onNext: () -> Unit,
    onSend: () -> Unit,
    onDuplicateLogin: () -> Unit,
    onDuplicateFindPassword: () -> Unit,
    onDismissMismatch: () -> Unit,
) {
    var remainingSeconds by remember { mutableStateOf(0) }
    var timerActive by remember { mutableStateOf(false) }
    var showExpiredNotice by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val isExpired = showCodeField && remainingSeconds == 0

    // 타이머 동작
    LaunchedEffect(timerActive) {
        if (timerActive) {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
            timerActive = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("사용자 유형", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { onUserTypeChange(UserType.Normal) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userType == UserType.Normal) AppColors.brandOrange else AppColors.brandOrange50,
                ),
                modifier= Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)

            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("일반 사용자")
                }
            }
            Button(
                onClick = { onUserTypeChange(UserType.Dealer) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userType == UserType.Dealer) AppColors.brandOrange else AppColors.brandOrange50,
                ),
                modifier= Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("딜러")
                }
            }
        }

        if (userType != null) {
            // --- 약관 동의 ---
            AgreementRow(
                isOn = termsAgreed,
                title = "서비스 이용 약관에 동의합니다.",
                onToggle = { onTermsChange(!termsAgreed) },
                onDetail = { /* TODO: 약관 보기 */ }
            )
            AgreementRow(
                isOn = privacyAgreed,
                title = "개인정보 수집 및 이용에 동의합니다.",
                onToggle = { onPrivacyChange(!privacyAgreed) },
                onDetail = { /* TODO: 개인정보 처리방침 */ }
            )

            // --- 이메일 입력 + 인증하기 ---
            Text("이메일", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)

            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("이메일을 입력하세요") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AppColors.brandOrange,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                        focusedBorderColor = AppColors.brandOrange,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        onSend()
                        showExpiredNotice = false
                        onCodeChange("")
                        remainingSeconds = 180
                        timerActive = true
                    },
                    enabled = email.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.brandOrange,
                    disabledContainerColor = AppColors.brandOrange50,
                    contentColor = Color.White,
                    disabledContentColor = Color.White
                ),
                    modifier = Modifier
                        .width(100.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isExpired) "재전송" else "인증하기")
                }
            }

            // --- 인증 코드 입력 ---
            if (showCodeField) {
                Text("이메일 인증번호", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = code,
                    onValueChange = { onCodeChange(it.filter { ch -> ch.isDigit() }) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("인증번호") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AppColors.brandOrange,
                        focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                        focusedBorderColor = AppColors.brandOrange,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                    )

                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text("⏱ ")
                    Text(
                        timeString(remainingSeconds),
                        color = if (remainingSeconds > 0) Color.White.copy(alpha = 0.7f) else Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showExpiredNotice && isExpired) {
                    Text(
                        "인증 시간이 만료되었습니다. 재전송 후 다시 입력해주세요.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (code.isNotEmpty()) {
                    Button(
                        onClick = {
                            if (isExpired) {
                                showExpiredNotice = true
                                onCodeChange("")
                            } else {
                                onNext()
                            }
                        },
                        enabled = termsAgreed && privacyAgreed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.brandOrange,
                            disabledContainerColor = AppColors.brandOrange50,
                            contentColor = Color.White,
                            disabledContentColor = Color.White)

                    ) {
                        Text("다음", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 중복/불일치 Alert
    if (showMismatchModal) {
        AlertDialog(
            onDismissRequest = {
                onCodeChange("")
                onDismissMismatch()
            },
            confirmButton = {
                TextButton(onClick = {
                    onCodeChange("")
                    onDismissMismatch()
                    remainingSeconds = 180
                    timerActive = true
                }) {
                    Text("다시 입력하기")
                }
            },
            title = { Text("인증번호가 일치하지 않습니다.") },
            text = { Text("받으신 인증번호를 다시 확인한 뒤 입력해주세요.") }
        )
    }

    if (showDuplicateModal) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = { onDuplicateLogin() }) { Text("다시 로그인하기") }
            },
            dismissButton = {
                TextButton(onClick = { onDuplicateFindPassword() }) { Text("비밀번호 찾기") }
            },
            title = { Text("이미 가입된 이메일입니다.") },
            text = { Text("기존 계정으로 로그인하거나 비밀번호를 찾을 수 있습니다.") }
        )
    }
}

@Composable
private fun AgreementRow(
    isOn: Boolean,
    title: String,
    onToggle: () -> Unit,
    onDetail: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (isOn) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
            contentDescription = null,
            tint = if (isOn) AppColors.brandOrange else Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .size(24.dp)
                .clickable { onToggle() }
        )
        Spacer(Modifier.width(4.dp))
        Text(title, color = Color.White, modifier = Modifier.weight(1f), fontSize = 14.sp)
        TextButton(onClick = onDetail) {
            Text("보기", color = AppColors.brandOrange80, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun timeString(seconds: Int): String =
    String.format("%02d:%02d", seconds / 60, seconds % 60)