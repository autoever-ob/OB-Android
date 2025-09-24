package com.nobody.campick.components.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nobody.campick.resources.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun CompleteStep(
    onAutoForward: () -> Unit
) {
    // 1초 후 자동 실행
    LaunchedEffect(Unit) {
        delay(1000)
        onAutoForward()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.brandBackground),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            // 바깥 원 (투명도 적용된 주황색)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(AppColors.brandOrange.copy(alpha = 0.22f))
            )

            // 안쪽 원 + 체크 아이콘
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent) // stroke 효과 대신 background + border 써도 됨
            ) {
                // 테두리
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(width = 4.dp, color = AppColors.brandOrange, shape = CircleShape)
                )

                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Checkmark",
                    tint = AppColors.brandOrange,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "회원가입이 완료되었습니다!!",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}