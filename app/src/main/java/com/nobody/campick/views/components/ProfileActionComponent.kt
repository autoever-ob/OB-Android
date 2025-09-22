package com.nobody.campick.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nobody.campick.resources.theme.AppColors

@Composable
fun ProfileActionComponent(
    onDetailTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onDetailTap,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            AppColors.brandOrange,
                            AppColors.brandLightOrange
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "프로필 상세보기",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}