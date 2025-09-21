package com.nobody.campick

import ChatListModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.nobody.campick.ui.theme.CampickTheme
import com.nobody.campick.views.ChatRoomListView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampickTheme {
                Scaffold { innerPadding ->
                    val mockChats = listOf(
                        ChatListModel(
                            id = 1,
                            nickname = "티파니 갱",
                            profileImage = null,
                            productName = "현대 포레스트 프리미엄",
                            productThumbnail = null,
                            lastMessage = "안녕하세요! 관심 있으신가요?",
                            lastMessageCreatedAt = "오후 3:30",
                            unreadMessage = 2,
                            isOnline = true
                        ),
                        ChatListModel(
                            id = 2,
                            nickname = "박우진",
                            profileImage = null,
                            productName = "기아 봉고 캠퍼",
                            productThumbnail = null,
                            lastMessage = "가격 조율 가능할까요?",
                            lastMessageCreatedAt = "오후 1:10",
                            unreadMessage = 0,
                            isOnline = false
                        ),
                        ChatListModel(
                            id = 3,
                            nickname = "崔东进",
                            profileImage = null,
                            productName = "벤츠 스프린터",
                            productThumbnail = null,
                            lastMessage = "이 차량 얼마인가요?",
                            lastMessageCreatedAt = "어제",
                            unreadMessage = 3,
                            isOnline = true
                        )
                    )

                    Box(modifier = Modifier.padding(innerPadding)) {
                        ChatRoomListView(
                            chats = mockChats,
                            onChatClick = { chat ->
                                println("채팅방 클릭됨: ${chat.nickname}")
                            },
                            onFindVehicleClick = {
                                println("매물 찾기 버튼 클릭됨")
                            },
                        )
                    }
                }
            }
        }
    }
}

