package com.nobody.campick.components.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.nobody.campick.resources.theme.AppColors

@Composable
fun PasswordStep(
    password: String,
    confirm: String,
    errorMessage: String?,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onNext: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "비밀번호",
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = password,
            onValueChange = { onPasswordChange(it) },
            placeholder = { Text("비밀번호 (8자 이상)") },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (showPassword) "비밀번호 숨기기" else "비밀번호 보기",
                        tint = Color.Gray
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = AppColors.brandOrange,
                focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                focusedBorderColor = AppColors.brandOrange,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (password.isNotEmpty()) {
            Text(
                text = "비밀번호 확인",
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = confirm,
                onValueChange = { onConfirmChange(it) },
                placeholder = { Text("비밀번호 확인") },
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (showConfirm) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = if (showConfirm) "비밀번호 숨기기" else "비밀번호 보기",
                            tint = Color.Gray
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = AppColors.brandOrange,
                    focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                    focusedBorderColor = AppColors.brandOrange,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (confirm.isNotEmpty()) {
            Button(
                onClick = {
                    if (password.length >= 8 && confirm == password) {
                        onNext()
                    } else {
                        // 에러 메시지는 ViewModel에서 관리하는 게 깔끔함
                    }
                },
                enabled = password.length >= 8 && confirm.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.brandOrange,
                    disabledContainerColor = AppColors.brandOrange50,
                    contentColor = Color.White,
                    disabledContentColor = Color.White),

            ) {
                Text("다음", fontWeight = FontWeight.Bold)
            }
        }
    }
}