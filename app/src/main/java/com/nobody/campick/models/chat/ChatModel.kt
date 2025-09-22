package com.nobody.campick.models.chat

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
enum class ChatMessageType {
    TEXT, IMAGE, SYSTEM
}

@Serializable
enum class MessageStatus {
    SENT, DELIVERED, READ
}

@Serializable
data class ChatMessage(
    val id: String,
    val text: String = "",
    val imageUrl: String? = null,
    @Contextual
    val timestamp: Date,
    val isMyMessage: Boolean,
    val type: ChatMessageType,
    val status: MessageStatus = MessageStatus.SENT
)

@Serializable
data class ChatSeller(
    val id: String,
    val name: String,
    val avatar: String,
    val isOnline: Boolean,
    @Contextual
    val lastSeen: Date? = null,
    val phoneNumber: String
)

@Serializable
data class ChatVehicle(
    val id: String,
    val title: String,
    val price: Int,
    val status: String,
    val image: String
)