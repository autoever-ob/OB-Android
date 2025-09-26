package com.nobody.campick.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nobody.campick.managers.UserState
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.ui.theme.CampickBrandFontFamily

@Composable
fun Header(
    showSlideMenu: Boolean,
    onShowSlideMenuChange: (Boolean) -> Unit,
) {
    val profileImageUrl by UserState.profileImageUrl.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.brandBackground)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Campick 텍스트 (Swift와 동일)
        Text(
            text = "Campick",
            fontSize = 30.sp,
            fontFamily = CampickBrandFontFamily,
            color = Color.White
        )

        // 프로필 이미지 버튼 (Swift와 동일)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable {
                    onShowSlideMenuChange(true)
                },
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUrl.isNotEmpty() && profileImageUrl.startsWith("http")) {
                // 실제 프로필 이미지
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 플레이스홀더 (Swift와 동일)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.brandOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
