package com.nobody.campick.components.signup


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nobody.campick.resources.theme.AppColors

@Composable
fun SignupProgress(
    progressWidth: Dp? = null,        // 고정 폭 (예: 120.dp)
    progress: Float? = null,          // 0.0 ~ 1.0 사이 진행도
    startFrom: Float? = null,         // 등장 시 시작 위치
    animateOnAppear: Boolean = true   // 애니메이션 여부
) {
    // 내부 애니메이션 상태
    var animatedProgress by remember { mutableStateOf(startFrom ?: 0f) }

    // progress 값이 변경될 때마다 애니메이션으로 업데이트
    val targetProgress = progress?.coerceIn(0f, 1f) ?: 0f
    val animated by animateFloatAsState(
        targetValue = if (animateOnAppear) targetProgress else targetProgress,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 350),
        label = "progress"
    )

    // progressWidth가 있으면 고정 폭, 없으면 전체 폭 * progress
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // 배경 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.1f))
        )

        // 진행 바
        val barWidthModifier = if (progressWidth != null) {
            Modifier.width(progressWidth)
        } else {
            Modifier.fillMaxWidth(animated)
        }

        Box(
            modifier = barWidthModifier
                .height(8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppColors.brandOrange)
        )
    }
}