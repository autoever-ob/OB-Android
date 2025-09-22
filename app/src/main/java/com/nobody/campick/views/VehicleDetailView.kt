package com.nobody.campick.views

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nobody.campick.R
import com.nobody.campick.models.vehicle.VehicleDetailViewData
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.viewmodels.VehicleDetailViewModel
import com.nobody.campick.views.components.SellerModalView
import com.nobody.campick.views.components.CommonHeaderCompose
import com.nobody.campick.views.components.HeaderType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailView(
    vehicleId: String,
    onBackClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onSellerClick: () -> Unit = {},
    onProfileDetailClick: (String, Boolean) -> Unit = { _, _ -> }, // (sellerId, isOwnProfile)
    modifier: Modifier = Modifier,
    viewModel: VehicleDetailViewModel = viewModel()
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var chatMessage by remember { mutableStateOf("") }
    var showSellerModal by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        when {
            detail != null -> {
                VehicleDetailContent(
                    detail = detail!!,
                    chatMessage = chatMessage,
                    onChatMessageChange = { chatMessage = it },
                    onBackClick = onBackClick,
                    onShareClick = onShareClick,
                    onSellerClick = { showSellerModal = true },
                    onFavoriteClick = { viewModel.toggleLike() },
                    onSendMessage = {
                        // TODO: Implement send message
                        keyboardController?.hide()
                        chatMessage = ""
                    }
                )
            }
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AppColors.brandOrange
                    )
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )
                        Button(
                            onClick = { viewModel.load(vehicleId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.brandOrange
                            )
                        ) {
                            Text("다시 시도")
                        }
                    }
                }
            }
        }

        // Seller Modal
        detail?.let { vehicleDetail ->
            SellerModalView(
                seller = vehicleDetail.seller,
                isPresented = showSellerModal,
                onDismiss = { showSellerModal = false },
                onProfileDetailClick = onProfileDetailClick
            )
        }
    }

    LaunchedEffect(vehicleId) {
        viewModel.load(vehicleId)
    }
}

@Composable
private fun VehicleDetailContent(
    detail: VehicleDetailViewData,
    chatMessage: String,
    onChatMessageChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onSellerClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onSendMessage: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        CommonHeaderCompose(
            type = HeaderType.Navigation(
                title = "매물 상세",
                showBackButton = true,
                showRightButton = false
            ),
            onBackClick = onBackClick
        )

        // Content with Bottom Chat Input
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 120.dp) // Space for bottom chat input
            ) {
                // Image Gallery (without header)
                VehicleImageGalleryContent(
                    images = detail.images
                )

                // Content
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    VehicleInfoCard(detail)
                    VehicleSellerCard(detail.seller, onSellerClick)
                    if (detail.features.isNotEmpty()) {
                        VehicleFeaturesCard(detail.features)
                    }
                    VehicleDescriptionCard(detail.description)
                }
            }

            // Bottom Chat Input
            BottomChatInput(
                chatMessage = chatMessage,
                onChatMessageChange = onChatMessageChange,
                onSendMessage = onSendMessage,
                onFavoriteClick = onFavoriteClick,
                isFavorite = detail.isLiked,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun VehicleImageGalleryContent(
    images: List<String>
) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    var isExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Main Image Gallery
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val imageRes = when (images.getOrNull(page)) {
                    "testImage1" -> R.drawable.test_image1
                    "testImage2" -> R.drawable.test_image2
                    "testImage3" -> R.drawable.test_image3
                    else -> R.drawable.test_image1
                }

                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }


            // Image Counter (bottom right)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${images.size}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Thumbnail Gallery
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isExpanded) {
                // Expanded state: Grid layout for all images
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    items(images.size) { index ->
                        ThumbnailImageView(
                            index = index,
                            currentIndex = pagerState.currentPage,
                            onTap = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        )
                    }
                }

                // Collapse Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            isExpanded = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "접기",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "접기",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                // Collapsed state: Horizontal scroll for first 5 images
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(start = 16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(minOf(images.size, 5)) { index ->
                            ThumbnailImageView(
                                index = index,
                                currentIndex = pagerState.currentPage,
                                onTap = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                            )
                        }
                    }

                    // Plus button (show if more than 5 images)
                    if (images.size > 5) {
                        Button(
                            onClick = {
                                isExpanded = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, AppColors.brandOrange),
                            modifier = Modifier
                                .width(64.dp)
                                .height(48.dp)
                                .padding(end = 16.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "더보기",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${images.size - 5}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleInfoCard(detail: VehicleDetailViewData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title and price section
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = detail.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = detail.priceText,
                    color = AppColors.brandOrange,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = detail.location,
                        color = AppColors.brandWhite60,
                        fontSize = 16.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Yellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "4.8",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Details section in card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.brandWhite10.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AppColors.brandWhite10.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(17.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                VehicleDetailItem(
                    icon = Icons.Default.DateRange,
                    title = "연식",
                    value = detail.yearText,
                    modifier = Modifier.weight(1f)
                )

                VehicleDetailItem(
                    icon = Icons.Default.Build, // Speedometer equivalent
                    title = "주행거리",
                    value = detail.mileageText,
                    modifier = Modifier.weight(1f)
                )

                VehicleDetailItem(
                    icon = Icons.Default.DirectionsCar,
                    title = "차종",
                    value = detail.typeText,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun VehicleDetailItem(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    AppColors.brandOrange.copy(alpha = 0.2f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.brandOrange,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = title,
            color = AppColors.brandWhite60,
            fontSize = 12.sp
        )

        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun VehicleSellerCard(
    seller: com.nobody.campick.models.vehicle.Seller,
    onTap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() },
        colors = CardDefaults.cardColors(
            containerColor = AppColors.brandWhite10.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppColors.brandWhite10.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AppColors.brandOrange,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "판매자 정보",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.test_image1),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = seller.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        if (seller.isDealer) {
                            Surface(
                                color = AppColors.brandOrange,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "딜러",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "판매중 ${seller.totalListings}개 • 판매완료 ${seller.totalSales}개",
                        color = AppColors.brandWhite70,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AppColors.brandWhite70
                )
            }
        }
    }
}

@Composable
private fun VehicleFeaturesCard(features: List<String>) {
    var isExpanded by remember { mutableStateOf(false) }

    val displayedFeatures = if (isExpanded || features.size <= 10) {
        features
    } else {
        features.take(10)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.brandWhite10.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppColors.brandWhite10.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with title and expand/collapse button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = AppColors.brandOrange,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "주요 옵션",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.weight(1f))

                // Expand/collapse button (only show if more than 10 features)
                if (features.size > 10) {
                    Button(
                        onClick = {
                            isExpanded = !isExpanded
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.brandOrange.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isExpanded) "접기" else "더보기",
                                color = AppColors.brandOrange,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AppColors.brandOrange,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }

            // Features grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(
                    // Calculate height based on number of displayed features
                    ((displayedFeatures.size + 1) / 2 * 32).dp // 24dp for text + 8dp spacing
                )
            ) {
                items(displayedFeatures) { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Green,
                            modifier = Modifier.size(14.dp)
                        )

                        Text(
                            text = feature,
                            color = AppColors.brandWhite80,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleDescriptionCard(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.brandWhite10.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppColors.brandWhite10.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = AppColors.brandOrange,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "상세설명",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = description,
                color = AppColors.brandWhite80,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun BottomChatInput(
    chatMessage: String,
    onChatMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onFavoriteClick: () -> Unit,
    isFavorite: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppColors.background.copy(alpha = 0.95f)
    ) {
        Column {
            Divider(
                thickness = 1.dp,
                color = AppColors.brandWhite10
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Favorite Button
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            AppColors.brandWhite10,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "즐겨찾기",
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }

                // Chat Input
                OutlinedTextField(
                    value = chatMessage,
                    onValueChange = onChatMessageChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "안녕하세요. 문의하고싶습니다.",
                            color = AppColors.brandWhite50
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AppColors.brandOrange,
                        focusedBorderColor = AppColors.brandWhite20,
                        unfocusedBorderColor = AppColors.brandWhite20
                    ),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { onSendMessage() }
                    ),
                    singleLine = true
                )

                // Send Button
                IconButton(
                    onClick = onSendMessage,
                    enabled = chatMessage.trim().isNotEmpty(),
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (chatMessage.trim().isNotEmpty()) {
                                Brush.linearGradient(
                                    listOf(AppColors.brandOrange, AppColors.brandLightOrange)
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(AppColors.brandWhite20, AppColors.brandWhite20)
                                )
                            },
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "전송",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ThumbnailImageView(
    index: Int,
    currentIndex: Int,
    onTap: () -> Unit
) {
    val imageRes = when (index) {
        0 -> R.drawable.test_image1
        1 -> R.drawable.test_image2
        2 -> R.drawable.test_image3
        else -> R.drawable.test_image1
    }

    val isSelected = index == currentIndex
    val borderColor = if (isSelected) AppColors.brandOrange else Color.White.copy(alpha = 0.2f)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Button(
        onClick = onTap,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(borderWidth, borderColor),
        modifier = Modifier
            .width(64.dp)
            .height(48.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    }
}