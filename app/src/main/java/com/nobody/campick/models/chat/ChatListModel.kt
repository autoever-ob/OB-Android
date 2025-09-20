import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ChatListModel(
    @SerialName("chatRoomId")
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