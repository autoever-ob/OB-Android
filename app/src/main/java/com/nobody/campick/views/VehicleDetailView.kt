package com.nobody.campick.views

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.BasicTextField
import com.nobody.campick.R
import com.nobody.campick.managers.UserState
import com.nobody.campick.models.vehicle.Seller
import com.nobody.campick.models.vehicle.VehicleDetailViewData
import com.nobody.campick.models.vehicle.VehicleStatus
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.viewmodels.VehicleDetailViewModel
import com.nobody.campick.views.components.SellerModalView

@Composable
fun VehicleDetailView(
    vehicleId: String,
    isOwnerHint: Boolean = false,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onChatClick: (Int) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: VehicleDetailViewModel = viewModel()
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentUser by UserState.memberId.collectAsState()

    var chatMessage by remember { mutableStateOf("") }
    var showSellerModal by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val isOwner = remember(detail, currentUser) {
        if (isOwnerHint) return@remember true
        val mine = currentUser.trim()
        val seller = detail?.seller?.id?.trim() ?: ""
        mine.isNotEmpty() && mine == seller
    }

    LaunchedEffect(detail?.isLiked) {
        detail?.isLiked?.let { isFavorite = it }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        when {
            detail != null -> {
                Box {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val scrollState = rememberScrollState()

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(scrollState)
                        ) {
                            VehicleImageGallery(
                                images = if (detail!!.images.isEmpty()) listOf("bannerImage") else detail!!.images,
                                onBackClick = onBackClick,
                                showEditButton = isOwner,
                                onEditClick = onEditClick
                            )

                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Spacer(modifier = Modifier.height(0.dp))

                                VehicleInfoCard(
                                    title = detail!!.title,
                                    priceText = detail!!.priceText,
                                    yearText = detail!!.yearText,
                                    mileageText = detail!!.mileageText,
                                    typeText = detail!!.typeText,
                                    location = detail!!.location,
                                    status = detail!!.status,
                                    isOwner = isOwner,
                                    onStatusChange = { newStatus ->
                                        viewModel.changeStatus(vehicleId, newStatus)
                                    }
                                )

                                VehicleSellerCard(
                                    seller = detail!!.seller,
                                    onTap = { showSellerModal = true }
                                )

                                if (detail!!.features.isNotEmpty()) {
                                    VehicleFeaturesCard(features = detail!!.features)
                                }

                                VehicleDescriptionCard(description = detail!!.description)

                                Spacer(modifier = Modifier.height(120.dp))
                            }
                        }
                    }

                    BottomChatBar(
                        chatMessage = chatMessage,
                        onChatMessageChange = { chatMessage = it },
                        isFavorite = isFavorite,
                        onFavoriteClick = {
                            isFavorite = !isFavorite
                            viewModel.toggleLike()
                        },
                        onSendClick = {
                            keyboardController?.hide()
                            chatMessage = ""
                        }
                    )
                }
            }
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
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
                            textAlign = TextAlign.Center
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

        detail?.let { vehicleDetail ->
            SellerModalView(
                seller = vehicleDetail.seller,
                isPresented = showSellerModal,
                onDismiss = { showSellerModal = false },
                onProfileDetailClick = { sellerId, _ -> onProfileClick(sellerId) }
            )
        }
    }

    LaunchedEffect(vehicleId) {
        viewModel.load(vehicleId)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VehicleImageGallery(
    images: List<String>,
    onBackClick: () -> Unit,
    showEditButton: Boolean,
    onEditClick: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { images.size })
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        Box(
            modifier = Modifier.height(250.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = images[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.banner_image),
                    error = painterResource(R.drawable.banner_image)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = "${pagerState.currentPage + 1}/${images.size}",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (showEditButton) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "수정",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            if (isExpanded) {
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    images.forEachIndexed { index, image ->
                        ThumbnailImage(
                            image = image,
                            isSelected = index == pagerState.currentPage,
                            onClick = {
                                /* Scroll to image */
                            }
                        )
                    }
                }

                Button(
                    onClick = { isExpanded = false },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.3f)
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "접기",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState())
                            .padding(start = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        images.take(5).forEachIndexed { index, image ->
                            ThumbnailImage(
                                image = image,
                                isSelected = index == pagerState.currentPage,
                                onClick = {
                                    /* Scroll to image */
                                }
                            )
                        }
                    }

                    if (images.size > 5) {
                        Button(
                            onClick = { isExpanded = true },
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(width = 64.dp, height = 48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black.copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, AppColors.brandOrange),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
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
private fun ThumbnailImage(
    image: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 2.dp,
                color = if (isSelected) AppColors.brandOrange else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = image,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.banner_image),
            error = painterResource(R.drawable.banner_image)
        )
    }
}

@Composable
private fun VehicleInfoCard(
    title: String,
    priceText: String,
    yearText: String,
    mileageText: String,
    typeText: String,
    location: String,
    status: VehicleStatus,
    isOwner: Boolean,
    onStatusChange: (VehicleStatus) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                if (isOwner) {
                    StatusMenu(
                        currentStatus = status,
                        onStatusChange = onStatusChange
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = priceText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.brandOrange
                )

                Text(
                    text = location,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(17.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VehicleDetailItem(
                iconName = "calendar",
                title = "연식",
                value = yearText,
                modifier = Modifier.weight(1f)
            )

            VehicleDetailItem(
                iconName = "speedometer",
                title = "주행거리",
                value = mileageText,
                modifier = Modifier.weight(1f)
            )

            VehicleDetailItem(
                iconName = "directions_car",
                title = "차종",
                value = typeText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VehicleDetailItem(
    iconName: String,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = AppColors.brandOrange.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (iconName) {
                    "calendar" -> Icons.Default.DateRange
                    "speedometer" -> Icons.Default.Speed
                    else -> Icons.Default.DirectionsCar
                },
                contentDescription = null,
                tint = AppColors.brandOrange,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusMenu(
    currentStatus: VehicleStatus,
    onStatusChange: (VehicleStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Row(
            modifier = Modifier
                .menuAnchor()
                .background(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = when (currentStatus) {
                            VehicleStatus.ACTIVE -> Color.Green
                            VehicleStatus.RESERVED -> AppColors.brandOrange
                            VehicleStatus.SOLD -> Color.Gray
                        },
                        shape = CircleShape
                    )
            )

            Text(
                text = currentStatus.displayText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(10.dp)
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("판매중")
                        if (currentStatus == VehicleStatus.ACTIVE) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                onClick = {
                    onStatusChange(VehicleStatus.ACTIVE)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("예약중")
                        if (currentStatus == VehicleStatus.RESERVED) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                onClick = {
                    onStatusChange(VehicleStatus.RESERVED)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("판매완료")
                        if (currentStatus == VehicleStatus.SOLD) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                onClick = {
                    onStatusChange(VehicleStatus.SOLD)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun VehicleSellerCard(
    seller: Seller,
    onTap: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onTap)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = seller.avatar,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_person),
            error = painterResource(R.drawable.ic_person)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = seller.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                if (seller.isDealer) {
                    Text(
                        text = "딜러",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                color = AppColors.brandOrange.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "판매 ${seller.totalListings}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = "완료 ${seller.totalSales}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = "평점 ${seller.rating}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.4f)
        )
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = AppColors.brandOrange,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "주요 옵션",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            if (features.size > 10) {
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .background(
                            color = AppColors.brandOrange.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(999.dp)
                        )
                        .height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isExpanded) "접기" else "더보기",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.brandOrange
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AppColors.brandOrange,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in displayedFeatures.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeatureItem(
                        feature = displayedFeatures[i],
                        modifier = Modifier.weight(1f)
                    )

                    if (i + 1 < displayedFeatures.size) {
                        FeatureItem(
                            feature = displayedFeatures[i + 1],
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(
    feature: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = feature,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun VehicleDescriptionCard(description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "상세 설명",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Text(
            text = description,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f),
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun BoxScope.BottomChatBar(
    chatMessage: String,
    onChatMessageChange: (String) -> Unit,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onSendClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.1f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.background.copy(alpha = 0.95f))
                .padding(horizontal = 16.dp, vertical = 17.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "찜하기",
                    tint = if (isFavorite) Color.Red else Color.White
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (chatMessage.isEmpty()) {
                    Text(
                        text = "안녕하세요. 문의하고싶습니다.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }

                BasicTextField(
                    value = chatMessage,
                    onValueChange = onChatMessageChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White,
                        fontSize = 14.sp
                    ),
                    singleLine = true,
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(AppColors.brandOrange)
                )
            }

            IconButton(
                onClick = onSendClick,
                enabled = chatMessage.trim().isNotEmpty(),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                AppColors.brandOrange,
                                AppColors.brandLightOrange
                            )
                        ),
                        shape = CircleShape
                    ),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "전송"
                )
            }
        }
    }
}