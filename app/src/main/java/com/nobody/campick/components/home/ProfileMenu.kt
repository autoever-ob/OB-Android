package com.nobody.campick.components.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileMenu(
    showSlideMenu: Boolean,
    onShowSlideMenuChange: (Boolean) -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 반투명 오버레이
        if (showSlideMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable {
                        onShowSlideMenuChange(false)
                    }
            )
        }

        // 오른쪽 슬라이드 메뉴
        AnimatedVisibility(
            visible = showSlideMenu,
            enter = androidx.compose.animation.slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ),
            exit = androidx.compose.animation.slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            )
        ) {
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0B211A)) // AppColors.brandBackground 대체
                    .padding(16.dp)
            ) {
                // 상단 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "메뉴",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = "닫기",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onShowSlideMenuChange(false) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 사용자 프로필 카드
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            // TODO: 프로필 이미지 로드 (Coil 등 사용)
                            Text(text = "U", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "사용자님", // userState.name 바인딩
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            onShowSlideMenuChange(false)
                            onNavigateToProfile()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "프로필 보기",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 메뉴 아이템들
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    MenuItem(
                        iconRes = android.R.drawable.ic_menu_compass,
                        title = "내 매물",
                        subtitle = "등록한 매물 관리",
                        onClick = { /* TODO: Nav */ }
                    )
                    MenuItem(
                        iconRes = android.R.drawable.ic_dialog_email,
                        title = "채팅",
                        subtitle = "진행중인 대화",
                        badge = "3",
                        onClick = { /* TODO: Nav */ }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 로그아웃 버튼
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogout() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_lock_power_off),
                        contentDescription = "로그아웃",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "로그아웃",
                        fontSize = 13.sp,
                        color = Color.Red
                    )
                }
            }
        }
    }
}