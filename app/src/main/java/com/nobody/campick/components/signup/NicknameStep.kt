package com.nobody.campick.components.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nobody.campick.resources.theme.AppColors

@Composable
fun NicknameStep(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    selectedImage: String?,
    isSubmitting: Boolean,
    submitError: String?,
    onNext: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 아바타
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImage != null) {
                AsyncImage(
                    model = selectedImage,
                    contentDescription = "Selected Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Default Avatar",
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 버튼들 (사진 찍기, 갤러리)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onCameraClick,
                modifier = Modifier.width(120.dp).height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.brandOrange,
                    disabledContainerColor = AppColors.brandOrange50,
                    contentColor = Color.White,
                    disabledContentColor = Color.White),
            ) {
                Text("사진 찍기", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
            }
            Button(
                onClick = onGalleryClick,
                modifier = Modifier.width(120.dp).height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.brandOrange,
                    disabledContainerColor = AppColors.brandOrange50,
                    contentColor = Color.White,
                    disabledContentColor = Color.White),
            ) {
                Text("갤러리", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 닉네임 입력
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("닉네임", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom=10.dp))
            OutlinedTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                placeholder = { Text("닉네임을 입력하세요 (2자 이상)") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = AppColors.brandOrange,
                    focusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.6f),
                    focusedBorderColor = AppColors.brandOrange,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                ),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 가입 완료 버튼
        if (nickname.trim().length >= 2) {
            Button(
                onClick = onNext,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.brandOrange,
                    disabledContainerColor = AppColors.brandOrange50,
                    contentColor = Color.White,
                    disabledContentColor = Color.White),
            ) {
                Text(
                    if (isSubmitting) "처리 중..." else "가입 완료",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 에러 메시지
        submitError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }
    }
}