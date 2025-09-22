package com.nobody.campick.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nobody.campick.models.vehicle.Seller
import com.nobody.campick.resources.theme.AppColors

@Composable
fun SellerModalView(
    seller: Seller,
    isPresented: Boolean,
    onDismiss: () -> Unit,
    onProfileDetailClick: (String, Boolean) -> Unit // (sellerId, isOwnProfile)
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
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .heightIn(max = screenHeight * 0.7f)
                        .clickable { /* Prevent dismiss on content click */ },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.background
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Header with close button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "판매자 정보",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "닫기",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Profile Header Component
                        ProfileHeaderComponent(seller = seller)

                        // Profile Stats Component
                        ProfileStatsComponent(
                            totalListings = seller.totalListings,
                            totalSales = seller.totalSales
                        )

                        // Profile Action Component
                        ProfileActionComponent(
                            onDetailTap = {
                                // TODO: Check if this is own profile
                                val isOwnProfile = false // For now, assume it's another person's profile
                                onProfileDetailClick(seller.id, isOwnProfile)
                                onDismiss()
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}