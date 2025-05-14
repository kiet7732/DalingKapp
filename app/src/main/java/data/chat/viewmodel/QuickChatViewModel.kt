package data.chat.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import data.chat.AppChatDatabase
import data.chat.CachedChatListItem
import data.chat.CachedMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class QuickChatState(
    val isInQueue: Boolean = false,
    val isMatched: Boolean = false,
    val matchId: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class QuickChatViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: FirebaseDatabase
) : ViewModel() {

    private val chatDao = AppChatDatabase.getDatabase(context).chatDao()
    private val _quickChatState = MutableStateFlow(QuickChatState())
    val quickChatState: StateFlow<QuickChatState> = _quickChatState.asStateFlow()
    private var queueListener: ValueEventListener? = null

    fun joinQuickChatQueue(userId: String, gender: String, preferredGender: String) {
        viewModelScope.launch {
            _quickChatState.value = QuickChatState(isInQueue = true, isLoading = true)
            val queueRef = database.getReference("quick_chat_queue")

            // Kiểm tra xem người dùng đã trong hàng đợi chưa
            val snapshot = queueRef.child(userId).get().await()
            if (snapshot.exists()) {
                _quickChatState.value = QuickChatState(
                    isInQueue = true,
                    errorMessage = "Bạn đang trong hàng đợi, vui lòng chờ!"
                )
                return@launch
            }

            // Thêm người dùng vào hàng đợi
            val userData = mapOf(
                "userId" to userId,
                "gender" to gender,
                "preferredGender" to preferredGender,
                "status" to "waiting",
                "timestamp" to System.currentTimeMillis()
            )
            queueRef.child(userId).setValue(userData).await()

            // Lắng nghe hàng đợi để ghép đôi
            startQueueListener(userId, gender, preferredGender)
        }
    }

    fun leaveQueue(userId: String) {
        viewModelScope.launch {
            val queueRef = database.getReference("quick_chat_queue/$userId")
            queueRef.removeValue().await()
            queueListener?.let { database.getReference("quick_chat_queue").removeEventListener(it) }
            _quickChatState.value = QuickChatState()
        }
    }

    private fun startQueueListener(userId: String, gender: String, preferredGender: String) {
        val queueRef = database.getReference("quick_chat_queue")
        queueListener = queueRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val currentUserSnapshot = snapshot.child(userId)
                    if (!currentUserSnapshot.exists()) {
                        return@launch
                    }

                    // Lấy danh sách userId đã nhắn tin
                    val chattedUserIds = getChattedUserIds(userId)
                    Log.d("QuickChat", "Chatted user IDs: $chattedUserIds")

                    // Tìm người phù hợp, loại bỏ những người đã nhắn tin
                    val matchedUser = snapshot.children.find { child ->
                        val otherUserId = child.child("userId").getValue(String::class.java) ?: return@find false // Thêm khai báo otherUserId
                        val otherGender = child.child("gender").getValue(String::class.java)?.lowercase() ?: ""
                        val otherPreferredGender = child.child("preferredGender").getValue(String::class.java)?.lowercase() ?: ""
                        val currentGender = gender.lowercase()
                        val currentPreferredGender = preferredGender.lowercase()
                        Log.d("QuickChat", "Checking match: otherUserId=$otherUserId, otherGender=$otherGender, otherPreferredGender=$otherPreferredGender, currentGender=$currentGender, currentPreferredGender=$currentPreferredGender")
                        otherUserId != userId &&
                                otherGender == currentPreferredGender &&
                                otherPreferredGender == currentGender &&
                                child.child("status").getValue(String::class.java) == "waiting"
                                && otherUserId !in chattedUserIds // phan loc user da nhan
                    }

                    if (matchedUser != null) {
                        val matchedUserId = matchedUser.child("userId").getValue(String::class.java) ?: return@launch
                        Log.d("QuickChat", "Found match: $matchedUserId")
                        createMatch(userId, matchedUserId)
                    } else {
                        Log.d("QuickChat", "No match found")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("QuickChat", "Queue listener cancelled: ${error.message}")
                _quickChatState.value = QuickChatState(errorMessage = "Lỗi: ${error.message}")
            }
        })
    }

    private suspend fun createMatch(userId: String, matchedUserId: String) {
        val queueRef = database.getReference("quick_chat_queue")
        val matchRef = database.getReference("matches")
        val matchId = matchRef.push().key ?: return

        // Xóa cả hai người dùng khỏi hàng đợi
        queueRef.child(userId).removeValue().await()
        queueRef.child(matchedUserId).removeValue().await()

        // Tạo match mới
        val matchData = mapOf(
            "user1Id" to userId,
            "user2Id" to matchedUserId,
            "timestamp" to System.currentTimeMillis()
        )
        matchRef.child(matchId).setValue(matchData).await()

        // Tạo tin nhắn chào mừng
        val chatRef = matchRef.child(matchId).child("chat")
        val messageId = chatRef.push().key ?: return
        val welcomeMessage = mapOf(
            "senderId" to "system",
            "text" to "Bạn đã được ghép đôi! Bắt đầu trò chuyện nào!",
            "timestamp" to System.currentTimeMillis()
        )
        chatRef.child(messageId).setValue(welcomeMessage).await()

        // Lưu vào Room
        withContext(Dispatchers.IO) {
            val userData = getUserData(matchedUserId, context)
            val chatListItem = CachedChatListItem(
                matchId = matchId,
                userId = matchedUserId,
                name = userData.name,
                avatarUrl = userData.avatarUrl ?: "",
                latestMessage = "Bạn đã tìm thấy đươc người ấy! Bắt đầu trò chuyện nào!",
                timestamp = System.currentTimeMillis(),
                isSynced = true,
                ownerId = userId
            )
            chatDao.insertChatListItem(chatListItem)

            val message = CachedMessage(
                messageId = messageId,
                matchId = matchId,
                senderId = "system",
                text = "Bạn đã tìm thấy đươc người ấy! Bắt đầu trò chuyện nào!",
                timestamp = System.currentTimeMillis(),
                isSynced = true,
                ownerId = userId
            )
            chatDao.insertMessage(message)
        }

        // Cập nhật trạng thái
        _quickChatState.value = QuickChatState(isMatched = true, matchId = matchId)
    }

    private suspend fun getChattedUserIds(userId: String): List<String> {
        return withContext(Dispatchers.IO) {
            chatDao.getChatListItems(userId).map { it.userId }
        }
    }

    override fun onCleared() {
        queueListener?.let { database.getReference("quick_chat_queue").removeEventListener(it) }
        super.onCleared()
    }
}