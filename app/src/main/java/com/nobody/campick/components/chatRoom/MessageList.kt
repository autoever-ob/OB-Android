package com.nobody.campick.components.chatRoom


import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nobody.campick.R
import com.nobody.campick.models.chat.ChatMessage
import com.nobody.campick.models.chat.MessageStatus
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll

@Composable
fun MessageList(
    messages: List<ChatMessage>,
    isTyping: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val isAtBottom by remember {
        derivedStateOf {
            !listState.canScrollForward
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B211A)),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(
                    message = msg,
                    isLastMyMessage = msg.id == messages.lastOrNull { it.isMyMessage }?.id
                )
                Spacer(Modifier.height(8.dp))
            }
            if (isTyping) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TypingIndicator()
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !isAtBottom,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(messages.lastIndex)
                    }
                },
                containerColor = Color(0xFFFF6F00)
            ) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Scroll to bottom", tint = Color.White)
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isLastMyMessage: Boolean) {
    Row(
        horizontalArrangement = if (message.isMyMessage) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!message.isMyMessage) {

            Image(
                imageVector = Icons.Default.Person,
                contentDescription = "기본 프로필",
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (message.isMyMessage) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (message.isMyMessage) Color(0xFFFF6F00) else Color.White.copy(alpha = 0.1f),
                tonalElevation = 2.dp
            ) {
                Text(
                    text = message.text,
                    color = if (message.isMyMessage) Color.White else Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "12:30", // TODO: timestamp 포맷팅 필요
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                if (isLastMyMessage) {
                    MessageStat(status = message.status ?: MessageStatus.SENT)
                }
            }
        }
    }
}

@Composable
fun MessageStat(status: MessageStatus) {
    when (status) {
        MessageStatus.SENT -> Icon(
            imageVector = Icons.Filled.Done,
            contentDescription = "Sent",
            tint = Color.Gray,
            modifier = Modifier.size(14.dp)
        )
        MessageStatus.DELIVERED -> Icon(
            imageVector = Icons.Filled.Done,
            contentDescription = "Delivered",
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp)
        )
        MessageStatus.READ -> Icon(
            imageVector = Icons.Filled.DoneAll,
            contentDescription = "Read",
            tint = Color.Blue,
            modifier = Modifier.size(14.dp)
        )
    }
}