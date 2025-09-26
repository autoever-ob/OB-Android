package com.nobody.campick.views.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nobody.campick.resources.theme.AppColors
import com.nobody.campick.ui.theme.CampickBrandFontFamily

sealed class HeaderType {
    object Brand : HeaderType()
    data class Navigation(
        val title: String,
        val showBackButton: Boolean = true,
        val showRightButton: Boolean = false,
        val rightButtonIcon: ImageVector? = null,
        val rightButtonAction: (() -> Unit)? = null
    ) : HeaderType()
    data class Custom(
        val title: String,
        val leftIcon: ImageVector? = null,
        val rightIcon: ImageVector? = null,
        val leftAction: (() -> Unit)? = null,
        val rightAction: (() -> Unit)? = null
    ) : HeaderType()
}

@Composable
fun CommonHeaderCompose(
    type: HeaderType,
    onBackClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when (type) {
        is HeaderType.Brand -> {
            BrandHeader(
                onProfileClick = onProfileClick,
                modifier = modifier
            )
        }
        is HeaderType.Navigation -> {
            NavigationHeader(
                type = type,
                onBackClick = onBackClick,
                modifier = modifier
            )
        }
        is HeaderType.Custom -> {
            CustomHeader(
                type = type,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun BrandHeader(
    onProfileClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.brandBackground)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 브랜드 로고
        Text(
            text = "Campick",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CampickBrandFontFamily,
            color = AppColors.primaryText,
            modifier = Modifier.weight(1f)
        )

        // 프로필 버튼
        IconButton(
            onClick = { onProfileClick?.invoke() },
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = AppColors.brandOrange,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "프로필",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun NavigationHeader(
    type: HeaderType.Navigation,
    onBackClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.brandBackground)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽 버튼
        if (type.showBackButton) {
            IconButton(
                onClick = { onBackClick?.invoke() },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = AppColors.brandWhite10,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = AppColors.primaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }

        // 제목
        Text(
            text = type.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.primaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        // 오른쪽 버튼
        if (type.showRightButton && type.rightButtonIcon != null) {
            IconButton(
                onClick = { type.rightButtonAction?.invoke() },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = AppColors.brandWhite10,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = type.rightButtonIcon,
                    contentDescription = "액션",
                    tint = AppColors.primaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
private fun CustomHeader(
    type: HeaderType.Custom,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.brandBackground)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽 버튼
        if (type.leftIcon != null) {
            IconButton(
                onClick = { type.leftAction?.invoke() },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = AppColors.brandWhite10,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = type.leftIcon,
                    contentDescription = "왼쪽 액션",
                    tint = AppColors.primaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }

        // 제목
        Text(
            text = type.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.primaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        // 오른쪽 버튼
        if (type.rightIcon != null) {
            IconButton(
                onClick = { type.rightAction?.invoke() },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = AppColors.brandWhite10,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = type.rightIcon,
                    contentDescription = "오른쪽 액션",
                    tint = AppColors.primaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}
