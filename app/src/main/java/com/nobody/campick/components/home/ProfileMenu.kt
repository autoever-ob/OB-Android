package com.nobody.campick.components.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.nobody.campick.managers.UserState
import com.nobody.campick.resources.theme.AppColors

@Composable
fun ProfileMenu(
    showSlideMenu: Boolean,
    onShowSlideMenuChange: (Boolean) -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val profileImageUrl by UserState.profileImageUrl.collectAsState()
    val nickName by UserState.nickName.collectAsState()
    val email by UserState.email.collectAsState()

    val overlayAlpha by animateFloatAsState(
        targetValue = if (showSlideMenu) 0.5f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "overlay"
    )

    val slideOffset by animateFloatAsState(
        targetValue = if (showSlideMenu) 0f else 300f,
        animationSpec = tween(durationMillis = 300),
        label = "slide"
    )

    if (showSlideMenu || slideOffset > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(300f) // Swift와 동일한 zIndex
        ) {
            // 반투명 오버레이 (Swift와 동일)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(overlayAlpha)
                    .background(Color.Black)
                    .clickable(enabled = showSlideMenu) {
                        onShowSlideMenuChange(false)
                    }
            )

            // 오른쪽 슬라이드 메뉴 (Swift와 동일한 위치 및 너비)
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.End
            ) {
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .offset(x = slideOffset.dp)
                        .background(Color(red = 0.043f, green = 0.129f, blue = 0.102f)) // Swift와 동일한 배경색
                        .padding(top = 50.dp) // Swift와 동일한 상단 패딩
                ) {
                    // 상단 헤더 (Swift와 동일)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
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
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onShowSlideMenuChange(false) }
                        )
                    }

                    // 사용자 프로필 카드 (Swift와 동일)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 프로필 이미지 (44dp - Swift와 동일)
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (profileImageUrl.isNotEmpty() && profileImageUrl.startsWith("http")) {
                                    AsyncImage(
                                        model = profileImageUrl,
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Profile",
                                            tint = Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }

                            // 사용자 정보 (Swift와 동일)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = nickName.ifEmpty { "사용자" },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                if (email.isNotEmpty()) {
                                    Text(
                                        text = email,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        // 프로필 보기 버튼 (Swift와 동일)
                        Button(
                            onClick = {
                                onShowSlideMenuChange(false)
                                onNavigateToProfile()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.brandOrange),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "프로필 보기",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 메뉴 아이템들 (Swift와 동일)
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        MenuItem(
                            icon = "🚗",
                            title = "내 매물",
                            subtitle = "등록한 매물 관리",
                            onShowSlideMenuChange = onShowSlideMenuChange,
                            onClick = { /* TODO: Nav */ }
                        )
                        MenuItem(
                            icon = "💬",
                            title = "채팅",
                            subtitle = "진행중인 대화",
                            badge = "3",
                            onShowSlideMenuChange = onShowSlideMenuChange,
                            onClick = { /* TODO: Nav */ }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 로그아웃 버튼 (Swift와 동일)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLogout() }
                            .padding(16.dp)
                            .padding(bottom = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⬅️",
                            fontSize = 13.sp
                        )
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
}