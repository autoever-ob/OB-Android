package com.nobody.campick.components.chatRoom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun ChatBottomBar(
    newMessage: String,
    onMessageChange: (String) -> Unit,
    pendingImage: Any?,
    onToggleAttachment: () -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B211A))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        IconButton(
            onClick = onToggleAttachment,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "첨부",
                tint = Color.White
            )
        }


        TextField(
            value = newMessage,
            onValueChange = onMessageChange,
            placeholder = { Text("메시지를 입력하세요...", color = Color.White.copy(alpha = 0.6f)) },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
        )


        val isSendEnabled = newMessage.trim().isNotEmpty() || pendingImage != null
        IconButton(
            onClick = onSend,
            enabled = isSendEnabled,
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isSendEnabled) Color(0xFFFF6F00) else Color.White.copy(alpha = 0.2f),
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