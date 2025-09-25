package com.nobody.campick.components.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.nobody.campick.R
import com.nobody.campick.resources.theme.AppColors
import kotlinx.coroutines.delay

data class BannerItem(
    val imageRes: Int,
    val title: String,
    val subtitle: String
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AutoSlidingBanner(
    modifier: Modifier = Modifier
) {
    // Swift와 동일한 5개 배너 데이터
    val banners = remember {
        listOf(
            BannerItem(
                imageRes = R.drawable.banner_image_1,
                title = "완벽한 캠핑카를\n찾아보세요",
                subtitle = "전국 최다 프리미엄 캠핑카 매물"
            ),
            BannerItem(
                imageRes = R.drawable.banner_image_2,
                title = "당신의 차를\n등록해보세요",
                subtitle = "간편하고 빠른 매물 등록으로 판매 시작"
            ),
            BannerItem(
                imageRes = R.drawable.banner_image_3,
                title = "멋진 캠핑카와 함께\n여행을 떠나보세요",
                subtitle = "꿈꿔왔던 자유로운 여행이 시작됩니다"
            ),
            BannerItem(
                imageRes = R.drawable.banner_image_4,
                title = "새로운 모험이\n기다립니다",
                subtitle = "광활한 자연 속에서 펼쳐지는 특별한 경험"
            ),
            BannerItem(
                imageRes = R.drawable.banner_image_5,
                title = "최고의 선택,\n최고의 가치",
                subtitle = "엄선된 고품질 캠핑카로 완벽한 여행을"
            )
        )
    }

    val pagerState = rememberPagerState()

    // Swift와 동일한 3초 자동 슬라이드
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // 3초 대기
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .shadow(5.dp, RoundedCornerShape(20.dp))
    ) {
        HorizontalPager(
            count = banners.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            BannerCard(banner = banners[page])
        }
    }
}

@Composable
private fun BannerCard(
    banner: BannerItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
    ) {
        // 배경 이미지 (Swift와 동일)
        Image(
            painter = painterResource(id = banner.imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 그라데이션 오버레이 (Swift와 동일)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Transparent
                        ),
                        startY = Float.POSITIVE_INFINITY,
                        endY = 0f
                    )
                )
        )

        // 텍스트 컨텐츠 (Swift와 동일)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = banner.title,
                fontSize = 28.sp, // Swift의 .title과 동일
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 34.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(bottom = 7.dp)
            )

            Text(
                text = banner.subtitle,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}