package com.nobody.campick.components.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nobody.campick.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
// 추천 매물 전체 컴포넌트
@Composable
fun RecommendVehicle(
    vehicles: List<VehicleModel>,
    onAllClick: () -> Unit,
    onLikeClick: (VehicleModel) -> Unit


) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 상단 헤더
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "추천",
                    tint = Color(0xFFFF6F00)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "추천 매물",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onAllClick) {
                Text(
                    text = "전체보기",
                    color = Color(0xFFFFB74D),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "더보기",
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // 매물 리스트
        if (vehicles.isEmpty()) {
            Text(
                text = "추천 매물이 존재하지 않습니다",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                vehicles.forEachIndexed { index, vehicle ->
                    VehicleCard(
                        vehicle = vehicle,
                        badge = if (index == 0) "NEW" else if (index == 1) "HOT" else null,
                        badgeColor = if (index == 0) Color(0xFF4CAF50)
                        else if (index == 1) Color(0xFFFF6F00)
                        else Color.Transparent,
                        onLikeClick = { onLikeClick(vehicle) }
                    )
                }
            }
        }
    }
}

@Composable
fun VehicleCard(
    vehicle: VehicleModel,
    badge: String?,
    badgeColor: Color,
    onLikeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 썸네일
        Box(modifier = Modifier.size(90.dp)) {
            AsyncImage(
                model = vehicle.thumbNail.ifEmpty { R.drawable.test_image1 },
                contentDescription = vehicle.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            )
            if (!badge.isNullOrEmpty()) {
                Text(
                    text = badge,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(badgeColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = vehicle.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (vehicle.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "좋아요",
                        tint = if (vehicle.isLiked) Color.Red else Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(text = vehicle.year)
                InfoChip(text = vehicle.mileage)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = vehicle.price,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.Yellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${vehicle.likeCount}",
                        color = Color.Yellow,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = Color.White.copy(alpha = 0.8f),
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

// SwiftUI의 Vehicle 모델 대응
data class VehicleModel(
    val id: Int,
    val title: String,
    val year: String,
    val mileage: String,
    val price: String,
    val thumbNail: String,
    val isLiked: Boolean,
    val likeCount: Int
)