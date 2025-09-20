package com.nobody.campick.models.chat

data class ChatListModel(
    val id: Int,
    val nickname: String,
    val profileImage: String? = null,
    val productName: String,
    val productThumbnail: String? = null,
    val lastMessage: String,
    val lastMessageCreatedAt: String,
    val unreadMessage: Int = 0,
    val isOnline: Boolean = false
)