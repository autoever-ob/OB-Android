package com.nobody.campick.components.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nobody.campick.resources.theme.AppColors

@Composable
fun SignupProgressView(
    progressWidth: Dp? = null,   // 고정 폭 (예: 120.dp)
    progress: Float? = null      // 0.0 ~ 1.0 사이 진행도
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidth = maxWidth
        val clampedProgress = (progress ?: 0f).coerceIn(0f, 1f)

        val computedWidth = progressWidth ?: (totalWidth * clampedProgress)

        // 배경 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.1f))
        )

        // 진행 바
        Box(
            modifier = Modifier
                .width(computedWidth)
                .height(8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AppColors.brandOrange)
        )
    }
}