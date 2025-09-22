package com.nobody.campick.components.signup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nobody.campick.viewmodels.UserType
import kotlinx.coroutines.delay

@Composable
fun PhoneStep(
    userType: UserType?,
    phone: String,
    code: String,
    dealerNumber: String,
    codeVerified: Boolean,
    showCodeField: Boolean,
    showDealerField: Boolean,
    errorMessage: String?,
    onPhoneChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onDealerChange: (String) -> Unit,
    onCodeVerified: () -> Unit,
    onDealerVerified: () -> Unit,

    // ✅ VM에 위임할 콜백들
    onNext: () -> Unit,                 // vm.phoneNext()
    onSend: () -> Unit,                 // 코드 발송 트리거(네트워크 등)
    onShowCodeFieldChange: (Boolean) -> Unit // showPhoneCodeField 토글
) {
    var remainingSeconds by remember { mutableStateOf(0) }
    var timerActive by remember { mutableStateOf(false) }
    var showExpiredNotice by remember { mutableStateOf(false) }

    val isExpired = showCodeField && remainingSeconds == 0

    // 타이머
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
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("휴대폰 번호", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = phone,
                onValueChange = { onPhoneChange(formatPhone(it)) },
                placeholder = { Text("휴대폰 번호 입력") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    // ✅ VM 상태 변경은 콜백으로
                    onSend()
                    onShowCodeFieldChange(true)
                    showExpiredNotice = false
                    onCodeChange("")
                    remainingSeconds = 180
                    timerActive = true
                },
                enabled = isValidPhone(phone),
                modifier = Modifier.width(100.dp)
            ) {
                Text(if (isExpired) "재전송" else "인증하기")
            }
        }

        if (showCodeField) {
            Text("인증번호", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = code,
                onValueChange = { onCodeChange(it.filter { ch -> ch.isDigit() }) },
                placeholder = { Text("인증번호") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⏱", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = timeString(remainingSeconds),
                    color = if (remainingSeconds > 0) Color.White.copy(alpha = 0.7f) else Color.Red,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        if (!errorMessage.isNullOrEmpty()) {
            Text(errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        if (showCodeField && code.isNotEmpty() && !codeVerified) {
            Button(
                onClick = {
                    if (isExpired) {
                        showExpiredNotice = true
                        onCodeChange("")
                    } else {
                        // ✅ 검증은 VM에서: 0000, 휴대폰 형식, 딜러 분기 등
                        onNext()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("다음", fontWeight = FontWeight.Bold)
            }
        }

        if (showExpiredNotice && isExpired) {
            Text(
                "인증 시간이 만료되었습니다. 재전송 후 다시 입력해주세요.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (userType == UserType.Dealer && showDealerField) {
            Text("딜러 번호", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = dealerNumber,
                onValueChange = { onDealerChange(it.filter { ch -> ch.isDigit() }) },
                placeholder = { Text("딜러 번호 입력") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (dealerNumber.isNotEmpty()) {
                Button(
                    onClick = {
                        // ✅ 딜러 번호 검증/분기도 VM에서
                        onDealerVerified()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("다음", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- Helpers ---
private fun isValidPhone(phone: String): Boolean {
    val digits = phone.filter { it.isDigit() }
    return digits.length == 10 || digits.length == 11
}

private fun formatPhone(input: String): String {
    val d = input.filter { it.isDigit() }
    if (d.length <= 3) return d
    if (d.length <= 7) {
        val a = d.take(3)
        val b = d.drop(3)
        return "$a-$b"
    }
    val a = d.take(3)
    val midLen = if (d.length == 11) 4 else 3
    val b = d.drop(3).take(midLen)
    val c = d.drop(3 + midLen)
    return if (c.isEmpty()) "$a-$b" else "$a-$b-$c"
}

private fun timeString(seconds: Int): String =
    String.format("%02d:%02d", seconds / 60, seconds % 60)