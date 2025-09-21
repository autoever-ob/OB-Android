package com.nobody.campick.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nobody.campick.R
import com.nobody.campick.resources.theme.AppColors
import androidx.compose.ui.text.TextStyle
import com.nobody.campick.extensions.titleFont
import com.nobody.campick.extensions.bodyFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    onAccountDeletion: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        // Header
        ProfileHeader(
            onEditClick = { showEditProfileDialog = true }
        )

        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProfileStatsSection()
            }

            item {
                ProductListSection()
            }

            item {
                SettingsSection(
                    onLogoutClick = { showLogoutDialog = true },
                    onDeleteAccountClick = { showDeleteAccountDialog = true },
                    onSettingsClick = onSettingsClick
                )
            }
        }
    }

    // Dialogs
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        AccountDeletionDialog(
            onConfirm = {
                showDeleteAccountDialog = false
                onAccountDeletion()
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }

    if (showEditProfileDialog) {
        ProfileEditDialog(
            onSave = {
                showEditProfileDialog = false
                onEditProfile()
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }
}

@Composable
private fun ProfileHeader(
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.brandWhite10
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Box {
                AsyncImage(
                    model = null, // TODO: Add profile image URL
                    contentDescription = "프로필 이미지",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AppColors.brandWhite20),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_person)
                )

                // Edit Button
                FloatingActionButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd),
                    containerColor = AppColors.brandOrange,
                    contentColor = AppColors.primaryText
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "편집",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            Text(
                text = "사용자 이름", // TODO: Get from ViewModel
                style = TextStyle.titleFont(20),
                color = AppColors.primaryText
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = "자기소개를 입력하세요", // TODO: Get from ViewModel
                style = TextStyle.bodyFont(14),
                color = AppColors.brandWhite70
            )
        }
    }
}

@Composable
private fun ProfileStatsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.brandWhite05
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                title = "등록한 매물",
                count = "0", // TODO: Get from ViewModel
                modifier = Modifier.weight(1f)
            )

            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(AppColors.brandWhite20)
            )

            StatItem(
                title = "판매한 매물",
                count = "0", // TODO: Get from ViewModel
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    count: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            style = TextStyle.titleFont(24),
            color = AppColors.brandOrange,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            style = TextStyle.bodyFont(12),
            color = AppColors.brandWhite60
        )
    }
}

@Composable
private fun ProductListSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.brandWhite05
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "등록한 매물",
                style = TextStyle.titleFont(16),
                color = AppColors.primaryText,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Empty state
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_car),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = AppColors.brandWhite40
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "아직 등록한 매물이 없습니다",
                    style = TextStyle.bodyFont(14),
                    color = AppColors.brandWhite60
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.brandWhite05
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "설정",
                style = TextStyle.titleFont(16),
                color = AppColors.primaryText,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsItem(
                icon = Icons.Default.Settings,
                title = "앱 설정",
                onClick = onSettingsClick
            )

            SettingsItem(
                icon = Icons.Default.Logout,
                title = "로그아웃",
                onClick = onLogoutClick
            )

            SettingsItem(
                icon = Icons.Default.Person,
                title = "회원 탈퇴",
                onClick = onDeleteAccountClick,
                isDestructive = true
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isDestructive) AppColors.red else AppColors.brandWhite90
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                style = TextStyle.bodyFont(16),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Dialog Composables (simplified versions)
@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "로그아웃",
                color = AppColors.primaryText
            )
        },
        text = {
            Text(
                text = "정말로 로그아웃 하시겠습니까?",
                color = AppColors.brandWhite80
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("로그아웃", color = AppColors.brandOrange)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = AppColors.brandWhite70)
            }
        },
        containerColor = AppColors.background
    )
}

@Composable
private fun AccountDeletionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "회원 탈퇴",
                color = AppColors.primaryText
            )
        },
        text = {
            Text(
                text = "정말로 회원 탈퇴를 하시겠습니까?\n\n탈퇴 후에는 모든 데이터가 삭제되며 복구할 수 없습니다.",
                color = AppColors.brandWhite80
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("탈퇴하기", color = AppColors.red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = AppColors.brandWhite70)
            }
        },
        containerColor = AppColors.background
    )
}

@Composable
private fun ProfileEditDialog(
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "프로필 수정",
                color = AppColors.primaryText
            )
        },
        text = {
            Text(
                text = "프로필 수정 기능은 준비 중입니다.",
                color = AppColors.brandWhite80
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("확인", color = AppColors.brandOrange)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = AppColors.brandWhite70)
            }
        },
        containerColor = AppColors.background
    )
}