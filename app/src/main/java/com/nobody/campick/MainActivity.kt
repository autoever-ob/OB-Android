package com.nobody.campick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import com.nobody.campick.models.chat.ChatSeller
import com.nobody.campick.models.chat.ChatVehicle
import com.nobody.campick.ui.theme.CampickTheme
import com.nobody.campick.views.ChatRoom

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampickTheme {
                Scaffold { innerPadding ->
//                    val mockChats = listOf(
//                        ChatListModel(
//                            id = 1,
//                            nickname = "티파니 갱",
//                            profileImage = null,
//                            productName = "현대 포레스트 프리미엄",
//                            productThumbnail = null,
//                            lastMessage = "안녕하세요! 관심 있으신가요?",
//                            lastMessageCreatedAt = "오후 3:30",
//                            unreadMessage = 2,
//                            isOnline = true
//                        ),
//                        ChatListModel(
//                            id = 2,
//                            nickname = "박우진",
//                            profileImage = null,
//                            productName = "기아 봉고 캠퍼",
//                            productThumbnail = null,
//                            lastMessage = "가격 조율 가능할까요?",
//                            lastMessageCreatedAt = "오후 1:10",
//                            unreadMessage = 0,
//                            isOnline = false
//                        ),
//                        ChatListModel(
//                            id = 3,
//                            nickname = "崔东进",
//                            profileImage = null,
//                            productName = "벤츠 스프린터",
//                            productThumbnail = null,
//                            lastMessage = "이 차량 얼마인가요?",
//                            lastMessageCreatedAt = "어제",
//                            unreadMessage = 3,
//                            isOnline = true
//                        )
//                    )
//
//                    ChatRoomListView(
//                        chats = mockChats,
//                        onChatClick = { chat ->
//                            println("채팅방 클릭됨: ${chat.nickname}")
//                        },
//                        onFindVehicleClick = {
//                            println("매물 찾기 버튼 클릭됨")
//                        },
//                    )

                    ChatRoom(
                        seller = ChatSeller(
                            id = "1",
                            name = "티파니 갱",
                            avatar = "https://example.com/avatar1.png",
                            isOnline = true,
                            lastSeen = null,
                            phoneNumber = "010-1234-5678"
                        ),
                        vehicle = ChatVehicle(
                            id = "101",
                            title = "현대 포레스트 프리미엄",
                            price = 50000000,
                            status = "판매중",
                            image = "https://example.com/vehicle1.png"
                        ),
                        onBack = { finish() } // MainActivity에서라면 액티비티 종료
                    )
                }
            }
        }
    }
}

