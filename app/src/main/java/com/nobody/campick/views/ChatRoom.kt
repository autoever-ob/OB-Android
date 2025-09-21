package com.nobody.campick.views
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nobody.campick.components.chatRoom.AttachmentMenu
import com.nobody.campick.components.chatRoom.ChatBottomBar
import com.nobody.campick.components.chatRoom.ChatHeader
import com.nobody.campick.components.chatRoom.MessageBubble
import com.nobody.campick.components.chatRoom.MessageList
import com.nobody.campick.components.chatRoom.TypingIndicator
import com.nobody.campick.models.chat.ChatMessage
import com.nobody.campick.models.chat.ChatMessageType
import com.nobody.campick.models.chat.ChatSeller
import com.nobody.campick.models.chat.ChatVehicle
import com.nobody.campick.models.chat.MessageStatus
import java.util.*

@Composable
fun ChatRoom(
    seller: ChatSeller,
    vehicle: ChatVehicle,
    onBack: () -> Unit = {}
) {

    var newMessage by remember { mutableStateOf("") }
    var pendingImage by remember { mutableStateOf<Any?>(null) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(mockMessages()) }
    var isTyping by remember { mutableStateOf(false) }
Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        ChatHeader(
            seller = seller,
            vehicle = vehicle,
            onBack = {},
            onCall = { /* 전화 기능 */ }
        )

        // Messages
        MessageList(
            messages = messages,
            isTyping = isTyping,
            modifier = Modifier.weight(1f)
        )

        ChatBottomBar(
            newMessage = newMessage,
            onMessageChange = { newMessage = it },
            pendingImage = pendingImage,
            onToggleAttachment = { showAttachmentMenu = !showAttachmentMenu },
            onSend = {
                if (newMessage.isNotBlank()) {
                    val msg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = newMessage,
                        timestamp = Date(),
                        isMyMessage = true,
                        type = ChatMessageType.TEXT,
                        status = MessageStatus.SENT
                    )
                    messages = messages + msg
                    newMessage = ""
                }
            }
        )
}
        if(showAttachmentMenu) {
            AttachmentMenu(
                onDismiss = {showAttachmentMenu = false},
                showAttachmentMenu = showAttachmentMenu,
                onSelectGallery = { /* 갤러리 선택 */ },
                onSelectCamera = { /* 카메라 선택 */ }
            )
        }
        // BottomBar
    }
}

private fun mockMessages(): List<ChatMessage> = listOf(
    ChatMessage("1", "안녕하세요! 현대 포레스트 프리미엄 매물에 관심을 가져주셔서 감사합니다.", null, Date(), false, ChatMessageType.TEXT, MessageStatus.SENT),
    ChatMessage("2", "궁금한 점이 있으시면 언제든 문의해주세요!", null, Date(), false, ChatMessageType.TEXT, MessageStatus.SENT),
    ChatMessage("3", "안녕하세요! 실제로 차량을 보고 싶은데 언제 가능한가요?", null, Date(), true, ChatMessageType.TEXT, MessageStatus.READ),
    ChatMessage("4", "네, 언제든 가능합니다! 평일 오전 10시부터 오후 6시까지 가능해요.", null, Date(), false, ChatMessageType.TEXT, MessageStatus.SENT)
)