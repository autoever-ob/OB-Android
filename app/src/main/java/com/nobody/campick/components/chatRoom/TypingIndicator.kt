package com.nobody.campick.components.chatRoom

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun TypingIndicator() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .padding(8.dp)
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        repeat(3) { i ->
            Dot(index = i)
        }
    }
}

@Composable
private fun Dot(index: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotAnim")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = -4f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing, delayMillis = index * 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetAnim"
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .offset(y = offsetY.dp)
            .clip(CircleShape)
            .background(Color.Gray)
    )
}