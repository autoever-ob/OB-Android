package com.nobody.campick.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 프로필 헤더 스켈레톤
 * Swift의 ProfileHeaderSkeleton과 동일한 레이아웃
 */
@Composable
fun ProfileHeaderSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 프로필 정보 섹션
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 아바타와 기본 정보
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 프로필 이미지
                SkeletonCircle(size = 70.dp)

                // 사용자 정보
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 닉네임과 편집 버튼
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SkeletonBox(
                            height = 24.dp,
                            width = 120.dp,
                            cornerRadius = 6.dp
                        )
                        SkeletonBox(
                            height = 32.dp,
                            width = 50.dp,
                            cornerRadius = 8.dp
                        )
                    }

                    // 평점과 가입일
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SkeletonBox(
                            height = 16.dp,
                            width = 60.dp,
                            cornerRadius = 4.dp
                        )
                        SkeletonBox(
                            height = 14.dp,
                            width = 100.dp,
                            cornerRadius = 4.dp
                        )
                    }
                }
            }

            // 자기소개 섹션
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.1f))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonBox(
                    height = 16.dp,
                    width = 60.dp,
                    cornerRadius = 4.dp
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SkeletonBox(
                        height = 14.dp,
                        cornerRadius = 4.dp
                    )
                    SkeletonBox(
                        height = 14.dp,
                        width = 200.dp,
                        cornerRadius = 4.dp
                    )
                }
            }
        }

        // 통계 섹션
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Gray.copy(alpha = 0.1f))
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(3) {
                StatItemSkeleton()
                if (it < 2) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(Color.Gray.copy(alpha = 0.2f))
                    )
                }
            }
        }
    }
}

/**
 * 통계 항목 스켈레톤
 */
@Composable
private fun StatItemSkeleton() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SkeletonBox(
            height = 12.dp,
            width = 40.dp,
            cornerRadius = 3.dp
        )
        SkeletonBox(
            height = 20.dp,
            width = 30.dp,
            cornerRadius = 4.dp
        )
    }
}

/**
 * 탭 네비게이션 스켈레톤
 */
@Composable
fun TabNavigationSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SkeletonBox(
            height = 40.dp,
            width = 80.dp,
            cornerRadius = 8.dp
        )
        SkeletonBox(
            height = 40.dp,
            width = 90.dp,
            cornerRadius = 8.dp
        )
    }
}

/**
 * 매물 아이템 스켈레톤 (프로필 페이지용)
 * Swift의 ProductItemSkeleton과 동일한 레이아웃
 */
@Composable
fun ProductItemSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 이미지 (100x80)
        SkeletonBox(
            modifier = Modifier.size(width = 100.dp, height = 80.dp),
            height = 80.dp,
            width = 100.dp,
            cornerRadius = 12.dp
        )

        // 매물 정보
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 제목
            SkeletonBox(
                height = 18.dp,
                cornerRadius = 4.dp
            )

            // 가격
            SkeletonBox(
                height = 20.dp,
                width = 100.dp,
                cornerRadius = 4.dp
            )

            // 상태와 날짜
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonBox(
                    height = 16.dp,
                    width = 60.dp,
                    cornerRadius = 8.dp
                )
                SkeletonBox(
                    height = 14.dp,
                    width = 80.dp,
                    cornerRadius = 4.dp
                )
            }
        }
    }
}

/**
 * 설정 섹션 스켈레톤
 */
@Composable
fun SettingsSectionSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SkeletonBox(
                    height = 18.dp,
                    width = 120.dp,
                    cornerRadius = 4.dp
                )
                SkeletonBox(
                    height = 16.dp,
                    width = 20.dp,
                    cornerRadius = 4.dp
                )
            }
        }
    }
}

/**
 * 완전한 프로필 스켈레톤 뷰
 * Swift의 ProfileSkeletonView와 동일한 구조
 */
@Composable
fun ProfileSkeletonView(
    isOwnProfile: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 프로필 헤더
        ProfileHeaderSkeleton(
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // 탭 네비게이션
        TabNavigationSkeleton()

        // 매물 리스트 (4개)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) {
                ProductItemSkeleton()
            }
        }

        // 설정 섹션 (본인 프로필인 경우에만)
        if (isOwnProfile) {
            SettingsSectionSkeleton(
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}