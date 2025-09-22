package com.nobody.campick.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Surface
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
import com.nobody.campick.models.vehicle.FilterOptions
import com.nobody.campick.resources.theme.AppColors
import java.text.NumberFormat
import java.util.*

@Composable
fun FilterView(
    filters: FilterOptions,
    isPresented: Boolean,
    onDismiss: () -> Unit,
    onApply: (FilterOptions) -> Unit
) {
    var tempFilters by remember { mutableStateOf(filters) }

    LaunchedEffect(filters) {
        tempFilters = filters
    }

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
                contentAlignment = Alignment.Center
            ) {
                val configuration = LocalConfiguration.current
                val screenWidth = configuration.screenWidthDp.dp

                Card(
                    modifier = Modifier
                        .width(screenWidth - 32.dp)
                        .clickable { /* Prevent dismiss on content click */ },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.background
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Header
                        FilterHeader(
                            onReset = { tempFilters = FilterOptions() },
                            onClose = onDismiss
                        )

                        // Content
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = (configuration.screenHeightDp * 0.5).dp)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Price Range Section
                            PriceRangeSection(
                                priceRange = tempFilters.priceRange,
                                onRangeChange = { newRange ->
                                    tempFilters = tempFilters.copy(priceRange = newRange)
                                }
                            )

                            // Mileage Range Section
                            MileageRangeSection(
                                mileageRange = tempFilters.mileageRange,
                                onRangeChange = { newRange ->
                                    tempFilters = tempFilters.copy(mileageRange = newRange)
                                }
                            )

                            // Year Range Section
                            YearRangeSection(
                                yearRange = tempFilters.yearRange,
                                onRangeChange = { newRange ->
                                    tempFilters = tempFilters.copy(yearRange = newRange)
                                }
                            )

                            // Vehicle Types Section
                            VehicleTypesSection(
                                selectedTypes = tempFilters.selectedVehicleTypes,
                                onTypesChange = { newTypes ->
                                    tempFilters = tempFilters.copy(selectedVehicleTypes = newTypes)
                                }
                            )

                            // Options Section
                            if (tempFilters.availableOptions.isNotEmpty()) {
                                OptionsSelectionButton(
                                    availableOptions = tempFilters.availableOptions,
                                    selectedOptions = tempFilters.selectedOptions,
                                    onOptionsChange = { newOptions ->
                                        tempFilters = tempFilters.copy(selectedOptions = newOptions)
                                    }
                                )
                            }
                        }

                        // Footer
                        FilterFooter(
                            onApply = {
                                onApply(tempFilters)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterHeader(
    onReset: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset Button
        TextButton(onClick = onReset) {
            Text(
                text = "초기화",
                color = AppColors.brandOrange,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Title
        Text(
            text = "필터",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Close Button
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                tint = AppColors.brandWhite70,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    HorizontalDivider(
        thickness = 1.dp,
        color = AppColors.brandWhite20
    )
}

@Composable
private fun PriceRangeSection(
    priceRange: ClosedFloatingPointRange<Double>,
    onRangeChange: (ClosedFloatingPointRange<Double>) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "가격 (만원)",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        RangeSlider(
            range = priceRange,
            bounds = 0.0..10000.0,
            step = 100.0,
            onRangeChange = onRangeChange
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${NumberFormat.getNumberInstance(Locale.getDefault()).format(priceRange.start.toInt())}만원",
                color = AppColors.brandWhite60,
                fontSize = 14.sp
            )
            Text(
                text = "${NumberFormat.getNumberInstance(Locale.getDefault()).format(priceRange.endInclusive.toInt())}만원",
                color = AppColors.brandWhite60,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun MileageRangeSection(
    mileageRange: ClosedFloatingPointRange<Double>,
    onRangeChange: (ClosedFloatingPointRange<Double>) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "주행거리 (km)",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        RangeSlider(
            range = mileageRange,
            bounds = 0.0..100000.0,
            step = 5000.0,
            onRangeChange = onRangeChange
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${NumberFormat.getNumberInstance(Locale.getDefault()).format(mileageRange.start.toInt())}km",
                color = AppColors.brandWhite60,
                fontSize = 14.sp
            )
            Text(
                text = "${NumberFormat.getNumberInstance(Locale.getDefault()).format(mileageRange.endInclusive.toInt())}km",
                color = AppColors.brandWhite60,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun YearRangeSection(
    yearRange: ClosedFloatingPointRange<Double>,
    onRangeChange: (ClosedFloatingPointRange<Double>) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "연식",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        RangeSlider(
            range = yearRange,
            bounds = 2010.0..2024.0,
            step = 1.0,
            onRangeChange = onRangeChange
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${yearRange.start.toInt()}년",
                color = AppColors.brandWhite60,
                fontSize = 14.sp
            )
            Text(
                text = "${yearRange.endInclusive.toInt()}년",
                color = AppColors.brandWhite60,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun VehicleTypesSection(
    selectedTypes: Set<String>,
    onTypesChange: (Set<String>) -> Unit
) {
    val vehicleTypes = listOf("모터홈", "트레일러", "픽업캠퍼", "캠핑밴")

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "차량 종류",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            vehicleTypes.chunked(2).forEach { rowTypes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowTypes.forEach { vehicleType ->
                        FilterChip(
                            title = vehicleType,
                            isSelected = selectedTypes.contains(vehicleType),
                            onToggle = {
                                val newTypes = if (selectedTypes.contains(vehicleType)) {
                                    selectedTypes - vehicleType
                                } else {
                                    selectedTypes + vehicleType
                                }
                                onTypesChange(newTypes)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // 빈 공간 채우기 (홀수 개수인 경우)
                    if (rowTypes.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionsSelectionButton(
    availableOptions: List<String>,
    selectedOptions: Set<String>,
    onOptionsChange: (Set<String>) -> Unit
) {
    var showOptionsDialog by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "옵션",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Options Selection Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showOptionsDialog = true },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppColors.brandWhite10.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedOptions.isEmpty()) {
                        "옵션 선택"
                    } else {
                        "${selectedOptions.size}개 선택됨"
                    },
                    color = if (selectedOptions.isEmpty()) AppColors.brandWhite60 else Color.White,
                    fontSize = 14.sp
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "옵션 선택",
                    tint = AppColors.brandWhite60,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Show selected options if any
        if (selectedOptions.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedOptions.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowOptions.forEach { option ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                color = AppColors.brandOrange.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = option,
                                    color = AppColors.brandOrange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        // 빈 공간 채우기 (홀수 개수인 경우)
                        if (rowOptions.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    // Options Selection Dialog
    if (showOptionsDialog) {
        OptionsSelectionDialog(
            availableOptions = availableOptions,
            selectedOptions = selectedOptions,
            onDismiss = { showOptionsDialog = false },
            onConfirm = { newOptions ->
                onOptionsChange(newOptions)
                showOptionsDialog = false
            }
        )
    }
}

@Composable
private fun FilterFooter(
    onApply: () -> Unit
) {
    HorizontalDivider(
        thickness = 1.dp,
        color = AppColors.brandWhite20
    )

    Button(
        onClick = onApply,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.brandOrange
        )
    ) {
        Text(
            text = "적용하기",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun OptionsSelectionDialog(
    availableOptions: List<String>,
    selectedOptions: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    var tempSelectedOptions by remember { mutableStateOf(selectedOptions) }

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
            contentAlignment = Alignment.Center
        ) {
            val configuration = LocalConfiguration.current
            val screenWidth = configuration.screenWidthDp.dp

            Card(
                modifier = Modifier
                    .width(screenWidth - 32.dp)
                    .clickable { /* Prevent dismiss on content click */ },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.background
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Reset Button
                        TextButton(onClick = { tempSelectedOptions = emptySet() }) {
                            Text(
                                text = "초기화",
                                color = AppColors.brandOrange,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Title
                        Text(
                            text = "옵션 선택",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Close Button (cancel)
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "닫기",
                                tint = AppColors.brandWhite70,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = AppColors.brandWhite20
                    )

                    // Options List
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = (configuration.screenHeightDp * 0.4).dp)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableOptions.forEach { option ->
                            val isSelected = tempSelectedOptions.contains(option)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempSelectedOptions = if (isSelected) {
                                            tempSelectedOptions - option
                                        } else {
                                            tempSelectedOptions + option
                                        }
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        AppColors.brandOrange.copy(alpha = 0.2f)
                                    } else {
                                        AppColors.brandWhite10.copy(alpha = 0.3f)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option,
                                        color = if (isSelected) AppColors.brandOrange else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )

                                    // Checkmark
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .background(
                                                    AppColors.brandOrange,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "✓",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = AppColors.brandWhite20
                    )

                    // Confirm Button
                    Button(
                        onClick = { onConfirm(tempSelectedOptions) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.brandOrange
                        )
                    ) {
                        Text(
                            text = "확인",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}