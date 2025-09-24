package com.nobody.campick.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.nobody.campick.models.vehicle.Seller
import com.nobody.campick.resources.theme.AppColors

@Composable
fun SellerModalView(
    seller: Seller,
    isPresented: Boolean,
    onDismiss: () -> Unit,
    onProfileDetailClick: (String, Boolean) -> Unit
) {
    if (isPresented) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) { }
                        .background(
                            color = AppColors.background,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(48.dp))

                        Text(
                            text = "판매자 정보",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = onDismiss,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "닫기",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }

                    ProfileHeaderComponent(seller = seller)

                    ProfileStatsComponent(
                        totalListings = seller.totalListings,
                        totalSales = seller.totalSales
                    )

                    ProfileActionComponent(
                        onDetailTap = {
                            onProfileDetailClick(seller.id, false)
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(0.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderComponent(seller: Seller) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = seller.avatar,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = seller.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                if (seller.isDealer) {
                    Text(
                        text = "딜러",
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                color = AppColors.brandOrange,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = String.format("%.1f", seller.rating),
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "(평점)",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ProfileStatsComponent(
    totalListings: Int,
    totalSales: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileStatCard(
            title = "등록 매물",
            value = totalListings.toString(),
            modifier = Modifier.weight(1f)
        )

        ProfileStatCard(
            title = "판매 완료",
            value = totalSales.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ProfileActionComponent(
    onDetailTap: () -> Unit
) {
    Button(
        onClick = onDetailTap,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            AppColors.brandOrange,
                            AppColors.brandLightOrange
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "프로필 상세보기",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}