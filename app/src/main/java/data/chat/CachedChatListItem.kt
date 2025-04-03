package data.chat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_list")
data class CachedChatListItem(
    @PrimaryKey
    val matchId: String, // MatchId là duy nhất cho mỗi cặp match
    val userId: String, // ID của người dùng đã match (khác với người dùng hiện tại)
    val name: String, // Tên người dùng
    val avatarUrl: String, // ID tài nguyên avatar (có thể thay bằng URL)
    val latestMessage: String, // Tin nhắn mới nhất
    val timestamp: Long, // Thời gian của tin nhắn mới nhất
    val isSynced: Boolean = false, // Đánh dấu đã đồng bộ với Firebase chưa
    val ownerId: String
)
