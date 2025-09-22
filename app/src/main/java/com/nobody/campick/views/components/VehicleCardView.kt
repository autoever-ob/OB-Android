package com.nobody.campick.views.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
            .padding(horizontal = 8.dp)
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .clickable { onCardClick(vehicle.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box {
            // Background with material
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        AppColors.brandWhite10.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
            )

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header Section (Image)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                ) {
                    // Vehicle Image
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = vehicle.price,
                                color = AppColors.brandOrange,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        // Favorite Button
                        IconButton(
                            onClick = {
                                isFavorite = !isFavorite
                                onFavoriteClick(vehicle.id)
                            },
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    AppColors.brandWhite10.copy(alpha = if (isFavorite) 0.6f else 0.2f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "즐겨찾기",
                                tint = if (isFavorite) Color.Red else Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    // Vehicle Specs
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = AppColors.brandWhite10.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 28.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = AppColors.brandWhite70,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SalesStatusChip(isOnSale: Boolean) {
    val backgroundColor = if (isOnSale) Color(0xFF4CAF50) else Color(0xFFFF5722) // 초록색 / 빨간색
    val text = if (isOnSale) "판매중" else "판매완료"

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp), // 더 둥글게
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = text,
            color = Color.White, // 하얀색 글자
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun LocationChip(location: String) {
    Surface(
        color = Color(0xFF2196F3), // 파란색
        shape = RoundedCornerShape(12.dp), // 더 둥글게
        modifier = Modifier.padding(2.dp)
    ) {
        Text(
            text = location,
            color = Color.White, // 하얀색 글자
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}