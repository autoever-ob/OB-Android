package com.nobody.campick.views.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
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
import coil.compose.AsyncImage
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nobody.campick.R
import com.nobody.campick.models.vehicle.Vehicle
import com.nobody.campick.resources.theme.AppColors

@Composable
fun VehicleCardView(
    vehicle: Vehicle,
    onFavoriteClick: (String) -> Unit = {},
    onCardClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isFavorite by remember { mutableStateOf(vehicle.isFavorite) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clickable { onCardClick(vehicle.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Box {

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header Section (Image)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(
                            Color.Transparent,
                            RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                        )
                ) {
                    // Vehicle Image
                    val imageUrl = vehicle.thumbnailURL

                    if (imageUrl != null && imageUrl.startsWith("http")) {
                        // 실제 URL인 경우 Coil로 로드
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = vehicle.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // 테스트 이미지인 경우 기존 방식 사용
                        val imageRes = when (vehicle.imageName) {
                            "testImage1" -> R.drawable.test_image1
                            "testImage2" -> R.drawable.test_image2
                            "testImage3" -> R.drawable.test_image3
                            else -> R.drawable.test_image1 // Default image
                        }

                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = vehicle.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Overlay chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sales Status Chip
                        SalesStatusChip(isOnSale = vehicle.isOnSale)

                        // Location Chip
                        LocationChip(location = vehicle.location)

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                // Info Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(top = 10.dp, bottom = 10.dp)
                ) {
                    // Title and Favorite
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = vehicle.title,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = vehicle.price,
                                color = AppColors.brandOrange,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        // iOS와 동일: Favorite Button (더 작은 크기)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Color.White.copy(alpha = 0.15f),
                                    CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    isFavorite = !isFavorite
                                    onFavoriteClick(vehicle.id)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "즐겨찾기",
                                tint = if (isFavorite) Color.Red else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // iOS와 동일: Vehicle Specs Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            SpecItem(label = "연식", value = vehicle.year)
                            SpecItem(label = "주행거리", value = vehicle.mileage)
                            SpecItem(label = "연료", value = vehicle.fuelType)
                            SpecItem(label = "변속기", value = vehicle.transmission)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            color = AppColors.brandWhite70,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SalesStatusChip(isOnSale: Boolean) {
    val backgroundColor = if (isOnSale) Color(0xFF34C759) else Color(0xFFFF3B30) // iOS 스타일 색상
    val text = if (isOnSale) "판매중" else "판매완료"

    Surface(
        color = backgroundColor.copy(alpha = 0.9f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(2.dp),
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun LocationChip(location: String) {
    Surface(
        color = Color(0xFF007AFF).copy(alpha = 0.9f), // iOS 시스템 파란색
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(2.dp),
        shadowElevation = 2.dp
    ) {
        Text(
            text = location,
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}