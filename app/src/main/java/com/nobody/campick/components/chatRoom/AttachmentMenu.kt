package com.nobody.campick.components.chatRoom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AttachmentMenu(
    showAttachmentMenu: Boolean,
    onDismiss: () -> Unit,
    onSelectGallery: () -> Unit,
    onSelectCamera: () -> Unit
) {
    if (showAttachmentMenu) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomStart
        ) {
            AnimatedVisibility(
                visible = showAttachmentMenu,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.9f)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(dampingRatio = 0.9f)
                ) + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .padding(start = 16.dp, bottom = 80.dp)
                        .background(Color(0xFF0B211A), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                        .width(160.dp)
                ) {
                    TextButton(
                        onClick = {
                            onSelectGallery()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("사진", color = Color(0xFFFF6F00))
                    }

                    TextButton(
                        onClick = {
                            onSelectCamera()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("카메라", color = Color(0xFFFF6F00))
                    }
                }
            }
        }
    }
}