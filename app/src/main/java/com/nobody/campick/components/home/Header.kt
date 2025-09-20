package com.nobody.campick.components.home

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Header(
    showSlideMenu: Boolean,
    onShowSlideMenuChange: (Boolean) -> Unit,

) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B211A)) // AppColors.brandBackground 대체
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Campick 텍스트
        Text(
            text = "Campick",
            fontSize = 30.sp,
            fontFamily = FontFamily.Cursive, // "Pacifico" 대신
            color = Color.White
        )

        // 버튼
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF6F00)) // AppColors.brandOrange 대체
                .clickable {
                    onShowSlideMenuChange(true)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menu_myplaces), // person 아이콘 대체
                contentDescription = "Profile",
                tint = Color.White
            )
        }
    }
}