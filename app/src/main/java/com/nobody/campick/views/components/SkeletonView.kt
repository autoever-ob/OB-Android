package com.nobody.campick.views.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 기본 스켈레톤 박스 with shimmer animation
 * Swift의 SkeletonView와 동일한 애니메이션
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    width: Dp? = null,
    cornerRadius: Dp = 4.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")

    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerColors = listOf(
        Color.Gray.copy(alpha = 0.25f),
        Color.White.copy(alpha = 0.4f),
        Color.Gray.copy(alpha = 0.25f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 120f, translateAnim + 120f)
    )

    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

/**
 * 원형 스켈레톤 with shimmer animation
 * Swift의 SkeletonCircle과 동일
 */
@Composable
fun SkeletonCircle(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")

    val translateAnim by transition.animateFloat(
        initialValue = -size.value,
        targetValue = size.value * 2,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerColors = listOf(
        Color.Gray.copy(alpha = 0.25f),
        Color.White.copy(alpha = 0.4f),
        Color.Gray.copy(alpha = 0.25f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + size.value * 0.8f, translateAnim + size.value * 0.8f)
    )

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(brush)
    )
}

/**
 * 매물 찾기 카드 전용 스켈레톤
 * Swift의 FindVehicleCardSkeleton과 동일한 레이아웃
 */
@Composable
fun VehicleCardSkeleton(
    modifier: Modifier = Modifier
) {
    val cornerRadius = 12.dp
    val imageHeight = 180.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White.copy(alpha = 0.05f))
    ) {
        // 이미지 섹션 with 상태 칩 오버레이
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight)
        ) {
            // 메인 이미지 스켈레톤
            SkeletonBox(
                modifier = Modifier.fillMaxSize(),
                height = imageHeight,
                cornerRadius = 0.dp
            )

            // 상태 칩 오버레이 (판매중, 위치)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonBox(
                    height = 24.dp,
                    width = 48.dp,
                    cornerRadius = 12.dp
                )
                SkeletonBox(
                    height = 24.dp,
                    width = 65.dp,
                    cornerRadius = 12.dp
                )
            }
        }

        // 정보 섹션
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(top = 10.dp, bottom = 10.dp)
        ) {
            // 제목과 하트 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SkeletonBox(
                        height = 20.dp,
                        width = 140.dp,
                        cornerRadius = 4.dp
                    )
                    SkeletonBox(
                        height = 24.dp,
                        width = 80.dp,
                        cornerRadius = 4.dp
                    )
                }

                SkeletonCircle(size = 32.dp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 스펙 박스
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(4) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            SkeletonBox(
                                height = 13.dp,
                                width = 30.dp,
                                cornerRadius = 3.dp
                            )
                            SkeletonBox(
                                height = 15.dp,
                                width = 35.dp,
                                cornerRadius = 3.dp
                            )
                        }
                    }
                }
            }
        }
    }
}