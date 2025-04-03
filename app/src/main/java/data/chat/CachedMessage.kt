package data.chat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class CachedMessage(
    @PrimaryKey
    val messageId: String,
    val matchId: String,
    val senderId: String,
    val text: String,
    val timestamp: Long,
    val isSynced: Boolean = false,
    val ownerId: String
)
