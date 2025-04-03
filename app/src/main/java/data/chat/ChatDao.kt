package data.chat

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_list WHERE ownerId = :ownerId ORDER BY timestamp DESC")
    fun getChatList(ownerId: String): LiveData<List<CachedChatListItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatListItem(item: CachedChatListItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CachedMessage)

    @Query("SELECT * FROM messages WHERE matchId = :matchId AND ownerId = :ownerId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessage(matchId: String, ownerId: String): CachedMessage?

    @Query("SELECT * FROM messages WHERE matchId = :matchId AND ownerId = :ownerId ORDER BY timestamp ASC")
    fun getMessagesByMatchId(matchId: String, ownerId: String): LiveData<List<CachedMessage>>

    @Query("UPDATE messages SET isSynced = 1 WHERE messageId = :messageId")
    suspend fun markMessageAsSynced(messageId: String)

    @Query("UPDATE chat_list SET isSynced = 1 WHERE matchId = :matchId")
    suspend fun markChatListItemAsSynced(matchId: String)

    @Query("SELECT * FROM messages WHERE matchId = :matchId AND ownerId = :ownerId ORDER BY timestamp ASC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesByMatchIdWithPagination(matchId: String, ownerId: String, limit: Int, offset: Int): List<CachedMessage>

    // Thêm hàm xóa dữ liệu từ bảng chat_list
    @Query("DELETE FROM chat_list")
    suspend fun clearChatList()

    // Thêm hàm xóa dữ liệu từ bảng messages
    @Query("DELETE FROM messages")
    suspend fun clearMessages()
}