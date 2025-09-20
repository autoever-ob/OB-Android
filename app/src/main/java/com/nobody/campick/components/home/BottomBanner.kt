package com.nobody.campick.components.home
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri

@Composable
fun BottomBanner(onDetailClick: () -> Unit = {}) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = "asset:///bottomBanner.mov".toUri()
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = ExoPlayer.REPEAT_MODE_ALL // 무한 루프
            prepare()
            playWhenReady = true
            volume = 0f // 음소거
        }
    }

    Box(
        modifier = Modifier
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // 비디오 플레이어
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false // 재생 컨트롤 숨김
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        )

        // 그라데이션 오버레이
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
                .clip(RoundedCornerShape(16.dp))
        )

        // 콘텐츠
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(
                            id = android.R.drawable.ic_menu_compass // 임시 아이콘
                        ),
                        contentDescription = "Flame",
                        tint = Color(0xFFFF6F00)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "첫 거래 특별 혜택",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "수수료 50% 할인",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onDetailClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00)),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "자세히 보기",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

//@Preview()
//@Composable
//fun BottomBannerPreview() {
//    BottomBanner()
//}