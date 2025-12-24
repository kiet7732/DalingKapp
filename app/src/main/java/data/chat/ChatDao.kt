package data.chat

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_list WHERE ownerId = :ownerId ORDER BY timestamp DESC")
    fun getChatList(ownerId: String): LiveData<List<CachedChatListItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatListItem(item: CachedChatListItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CachedMessage)

    @Update
    suspend fun updateMessage(message: CachedMessage)

    @Query("SELECT * FROM messages WHERE matchId = :matchId AND ownerId = :ownerId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessage(matchId: String, ownerId: String): CachedMessage?

    @Query("SELECT * FROM messages WHERE matchId = :matchId AND ownerId = :ownerId ORDER BY timestamp ASC")
    fun getMessagesByMatchId(matchId: String, ownerId: String): LiveData<List<CachedMessage>>

    @Query("SELECT * FROM messages WHERE matchId = :matchId ORDER BY timestamp ASC")
    suspend fun getMessagesByMatchIdVi(matchId: String): List<CachedMessage>

    @Query("UPDATE messages SET isSynced = 1 WHERE messageId = :messageId")
    suspend fun markMessageAsSynced(messageId: String)

    @Query("UPDATE chat_list SET isSynced = 1 WHERE matchId = :matchId")
    suspend fun markChatListItemAsSynced(matchId: String)

    @Query("SELECT * FROM messages WHERE matchId = :matchId AND ownerId = :ownerId ORDER BY timestamp ASC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesByMatchIdWithPagination(matchId: String, ownerId: String, limit: Int, offset: Int): List<CachedMessage>

    @Query("UPDATE messages SET isNotified = 1 WHERE messageId = :messageId")
    suspend fun markMessageAsNotified(messageId: String)

    @Query("UPDATE messages SET isNotified = 1 WHERE matchId = :matchId AND isNotified = 0")
    suspend fun markAllMessagesAsNotified(matchId: String)

    @Query("SELECT * FROM chat_list WHERE ownerId = :ownerId")
    fun getChatListItems(ownerId: String): List<CachedChatListItem>

    @Query("DELETE FROM chat_list")
    suspend fun clearChatList()

    @Query("DELETE FROM messages")
    suspend fun clearMessages()

    @Query("SELECT * FROM messages WHERE matchId = :matchId ORDER BY timestamp ASC")
    suspend fun getMessagesForChat(matchId: String): List<CachedMessage>

    @Query("SELECT * FROM messages WHERE messageType = 'video'")
    suspend fun getAllVideoMessages(): List<CachedMessage>

    @Query("UPDATE messages SET duration = :duration WHERE messageId = :messageId")
    suspend fun updateMessageDuration(messageId: String, duration: Long)
}