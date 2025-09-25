package com.nobody.campick.components.home
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuItem(
    icon: String,               // 이모지 아이콘 (Swift와 동일)
    title: String,              // 제목
    subtitle: String,           // 부제목
    badge: String? = null,      // 뱃지 텍스트
    onShowSlideMenuChange: (Boolean) -> Unit = {},
    onClick: () -> Unit = {}    // 클릭 시 동작
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onShowSlideMenuChange(false)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아이콘 + 뱃지
        Box(
            modifier = Modifier.size(35.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            // 배경 원
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 18.sp
                )
            }

            // 뱃지
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .offset(x = 2.dp, y = (-3).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 텍스트
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}