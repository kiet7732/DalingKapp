package data.chat.viewmodel

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.dalingk.R
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import data.chat.AppChatDatabase
import data.chat.CachedChatListItem
import data.chat.CachedMessage
import data.chat.services.MessageSyncService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await



class ChatListViewModel(
    val context: Context,
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : ViewModel() {

    private val chatDao = AppChatDatabase.getDatabase(context).chatDao()
    private val _chatList = MutableStateFlow<List<CachedChatListItem>>(emptyList())
    val chatList: StateFlow<List<CachedChatListItem>> = _chatList.asStateFlow()

    private val _messages = MutableStateFlow<List<CachedMessage>>(emptyList())
    val messages: StateFlow<List<CachedMessage>> = _messages.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    private var currentUserId: String? = null
    private var isNetworkAvailable = true

    init {
        currentUserId = UserPreferences.getUserId(context)
        checkNetworkAvailability()
        Log.d("ChatListViewModel", "Current User ID: $currentUserId")
        startChatSync()
    }

    fun startChatSync() {
        viewModelScope.launch {
            if (isNetworkAvailable) {
                syncWithFirebase() // Đồng bộ từ Firebase khi khởi động
            }
            loadChatListFromRoom()
        }
    }

    private var currentPage = 0
    private val pageSize = 50

    // ChatListViewModel.kt
    fun loadMessages(matchId: String) {
        viewModelScope.launch {
            currentUserId?.let { ownerId ->
                val allMessages = chatDao.getMessagesByMatchId(matchId, ownerId).asFlow()
                allMessages.collect { messages ->
                    _messages.value = messages.sortedBy { it.timestamp }
                    Log.d("ChatListViewModel", "Loaded ${messages.size} messages for matchId: $matchId")
                }
            }
        }
    }

    //load tin nhắn
    fun sendMessage(matchId: String, senderId: String, text: String) {
        val messageId = database.getReference("matches/$matchId/chat").push().key ?: return
        currentUserId?.let { ownerId ->
            val message = CachedMessage(
                messageId = messageId,
                matchId = matchId,
                senderId = senderId,
                text = text,
                timestamp = System.currentTimeMillis(),
                isSynced = false,
                ownerId = ownerId
            )

            viewModelScope.launch {
                chatDao.insertMessage(message)
                updateChatListItem(matchId, text, message.timestamp)
                if (isNetworkAvailable) {
                    syncMessageToFirebaseWithRetry(message)
                }
                // Khởi động Service để đồng bộ
                val intent = Intent(context, MessageSyncService::class.java)
                context.startService(intent)
            }
        }
    }

    //Thêm cơ chế thử lại nếu đồng bộ với Firebase thất bại:
    private suspend fun syncMessageToFirebaseWithRetry(message: CachedMessage, maxRetries: Int = 3) {
        var retryCount = 0
        while (retryCount < maxRetries) {
            try {
                database.getReference("matches/${message.matchId}/chat/${message.messageId}")
                    .setValue(
                        mapOf(
                            "senderId" to message.senderId,
                            "text" to message.text,
                            "timestamp" to message.timestamp,
                            "messageType" to message.messageType // Thêm messageType
                        )
                    ).await()
                chatDao.markMessageAsSynced(message.messageId)
                chatDao.markChatListItemAsSynced(message.matchId)
                return
            } catch (e: Exception) {
                retryCount++
                if (retryCount == maxRetries) {
                    Log.e("ChatListViewModel", "Failed to sync message after $maxRetries retries: ${e.message}")
                }
                delay(1000L * retryCount) // Delay tăng dần
            }
        }
    }

    // Xóa toàn bộ dữ liệu Room khi đăng xuất
    fun clearDataOnLogout() {
        viewModelScope.launch {
//            AppChatDatabase.getDatabase(context).clearAllTables()
            _chatList.value = emptyList()
            _messages.value = emptyList()
            Log.d("ChatListViewModel", "Cleared all Room data on logout")
        }
    }

    private fun checkNetworkAvailability() {
        isNetworkAvailable = context.isNetworkAvailable()
    }

    fun sendImageMessage(matchId: String, senderId: String, imageUrl: String) {
        val messageId = database.getReference("matches/$matchId/chat").push().key ?: return
        currentUserId?.let { ownerId ->
            val message = CachedMessage(
                messageId = messageId,
                matchId = matchId,
                senderId = senderId,
                text = imageUrl, // Lưu URL ảnh vào text
                timestamp = System.currentTimeMillis(),
                isSynced = false,
                ownerId = ownerId,
                messageType = "image" // Đặt messageType là "image"
            )

            viewModelScope.launch {
                chatDao.insertMessage(message)
                updateChatListItem(matchId, "Đã gửi một hình ảnh", message.timestamp)
                if (isNetworkAvailable) {
                    syncMessageToFirebaseWithRetry(message)
                }
                // Khởi động Service để đồng bộ
                val intent = Intent(context, MessageSyncService::class.java)
                context.startService(intent)
            }
        }
    }

    private fun loadChatListFromRoom() {
        viewModelScope.launch {
            currentUserId?.let { ownerId ->
                chatDao.getChatList(ownerId).asFlow().collect { items ->
                    Log.d("ChatListViewModel", "Room items: ${items.size}")
                    _chatList.value = items
                }
            }
        }
    }

    private var messagesListener: ValueEventListener? = null

    //Thêm listener cho cập nhật thời gian thực:
    fun startMessagesListener(matchId: String): Pair<ChildEventListener, DatabaseReference> {
        val messagesRef = database.getReference("matches/$matchId/chat")
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                viewModelScope.launch {
                    val messageId = snapshot.key ?: return@launch
                    val senderId = snapshot.child("senderId").value as? String ?: return@launch
                    val text = snapshot.child("text").value as? String ?: return@launch
                    val timestamp = snapshot.child("timestamp").value as? Long ?: return@launch
                    val messageType = snapshot.child("messageType").value as? String ?: "text"

                    // Bỏ qua tin nhắn cũ
                    val currentTime = System.currentTimeMillis()
                    if (timestamp < currentTime - 10_000) {
                        chatDao.markMessageAsNotified(messageId)
                        return@launch
                    }

                    val message = CachedMessage(
                        messageId = messageId,
                        matchId = matchId,
                        senderId = senderId,
                        text = text,
                        timestamp = timestamp,
                        isSynced = true,
                        ownerId = currentUserId ?: "",
                        isNotified = false,
                        messageType = messageType
                    )

                    // Lưu tin nhắn vào Room
                    chatDao.insertMessage(message)

                    // Cập nhật danh sách tin nhắn
                    val currentMessages = _messages.value.toMutableList()
                    currentMessages.add(message)
                    _messages.value = currentMessages.distinctBy { it.messageId }.sortedBy { it.timestamp }

                    // Kiểm tra điều kiện gửi thông báo
                    val shouldNotify = senderId != currentUserId &&
                            !message.isNotified &&
                            (!util.AppState.isAppInForeground() || !util.AppState.isChatScreenOpen(matchId))

                    val isAppInForeground = util.AppState.isAppInForeground()

                    if (shouldNotify) {
                        if (isAppInForeground) {
                            val userData = getUserData(senderId, context)
                            _notificationMessage.value = "${userData.name}: ${if (messageType == "image") "Đã gửi một hình ảnh" else text}"
                            Log.d("ChatListViewModel", "Snackbar notification sent for message: $messageId")
                        } else {
                            val intent = Intent(context, MessageSyncService::class.java).apply {
                                putExtra("NOTIFICATION_MESSAGE", message)
                            }
                            context.startService(intent)
                            chatDao.markMessageAsNotified(messageId)
                            Log.d("ChatListViewModel", "System notification intent sent for message: $messageId")
                        }
                    } else if (util.AppState.isChatScreenOpen(matchId)) {
                        chatDao.markMessageAsNotified(messageId)
                        Log.d("ChatListViewModel", "Message $messageId marked as notified (chat screen open)")
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatListViewModel", "Messages listener cancelled: ${error.message}")
            }
        }

        messagesRef.addChildEventListener(listener)
        return Pair(listener, messagesRef)
    }


    override fun onCleared() {
        messagesListener?.let { database.getReference().removeEventListener(it) }
        super.onCleared()
    }

    private suspend fun updateChatListItem(
        matchId: String,
        latestMessage: String,
        timestamp: Long
    ) {
        val otherUserId = getOtherUserId(matchId) ?: return
        currentUserId?.let { ownerId ->
            Log.d("ChatListViewModel", "Other User ID (updateChatListItem): $otherUserId")
            val userData = getUserData(otherUserId, context)
            val chatListItem = CachedChatListItem(
                matchId = matchId,
                userId = otherUserId,
                name = userData.name,
                avatarUrl = userData.avatarUrl ?: "",
                latestMessage = latestMessage,
                timestamp = timestamp,
                isSynced = false,
                ownerId = ownerId // Gán ownerId
            )
            chatDao.insertChatListItem(chatListItem)
        }
    }


    private fun syncWithFirebase() {
        database.getReference("matches").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val tempList = mutableListOf<CachedChatListItem>()
                    snapshot.children.forEach { matchSnapshot ->
                        val matchId = matchSnapshot.key ?: return@forEach
                        val user1Id = matchSnapshot.child("user1Id").value as? String
                        val user2Id = matchSnapshot.child("user2Id").value as? String

                        if (user1Id == null || user2Id == null) return@forEach

                        if (user1Id != currentUserId && user2Id != currentUserId) return@forEach

                        val otherUserId = if (user1Id == currentUserId) user2Id else user1Id
                        val chatSnapshot = matchSnapshot.child("chat")
                        if (chatSnapshot.exists()) {
                            currentUserId?.let { ownerId ->
                                chatSnapshot.children.forEach { messageSnapshot ->
                                    val messageId = messageSnapshot.key ?: return@forEach
                                    val senderId = messageSnapshot.child("senderId").value as? String ?: return@forEach
                                    val text = messageSnapshot.child("text").value as? String ?: return@forEach
                                    val timestamp = messageSnapshot.child("timestamp").value as? Long ?: return@forEach
                                    val messageType = messageSnapshot.child("messageType").value as? String ?: "text"

                                    val message = CachedMessage(
                                        messageId = messageId,
                                        matchId = matchId,
                                        senderId = senderId,
                                        text = text,
                                        timestamp = timestamp,
                                        isSynced = true,
                                        ownerId = ownerId,
                                        messageType = messageType
                                    )
                                    chatDao.insertMessage(message)
                                }

                                val latestMessage = chatDao.getLatestMessage(matchId, ownerId)
                                if (latestMessage != null) {
                                    val userData = getUserData(otherUserId, context)
                                    val latestMessageText = when (latestMessage.messageType) {
                                        "image" -> "Đã gửi một hình ảnh"
                                        "audio" -> "Đã gửi một tin nhắn âm thanh"
                                        else -> latestMessage.text
                                    }
                                    val chatListItem = CachedChatListItem(
                                        matchId = matchId,
                                        userId = otherUserId,
                                        name = userData.name,
                                        avatarUrl = userData.avatarUrl ?: "",
                                        latestMessage = latestMessageText,
                                        timestamp = latestMessage.timestamp,
                                        isSynced = true,
                                        ownerId = ownerId
                                    )
                                    chatDao.insertChatListItem(chatListItem)
                                    tempList.add(chatListItem)
                                }
                            }
                        }
                    }
                    Log.d("ChatListViewModel", "Synced ${tempList.size} items from Firebase")
                    _chatList.value = tempList
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatListViewModel", "Lỗi khi đồng bộ: ${error.message}")
            }
        })
    }

    fun sendAudioMessage(matchId: String, senderId: String, audioUrl: String) {
        val messageId = database.getReference("matches/$matchId/chat").push().key ?: return
        currentUserId?.let { ownerId ->
            val message = CachedMessage(
                messageId = messageId,
                matchId = matchId,
                senderId = senderId,
                text = audioUrl,
                timestamp = System.currentTimeMillis(),
                isSynced = false,
                ownerId = ownerId,
                messageType = "audio"
            )

            viewModelScope.launch {
                chatDao.insertMessage(message)
                updateChatListItem(matchId, "Đã gửi một tin nhắn âm thanh", message.timestamp)
                if (isNetworkAvailable) {
                    syncMessageToFirebaseWithRetry(message)
                }
                val intent = Intent(context, MessageSyncService::class.java)
                context.startService(intent)
            }
        }
    }

    public suspend fun getOtherUserId(matchId: String): String? {
        if (currentUserId == null) {
            Log.w("ChatListViewModel", "Current User ID is null")
            return null
        }

        val matchRef = database.getReference("matches/$matchId")
        val snapshot = matchRef.get().await()
        val user1Id = snapshot.child("user1Id").value as? String
        val user2Id = snapshot.child("user2Id").value as? String

        if (user1Id == null || user2Id == null) {
            Log.w("ChatListViewModel", "Missing user1Id or user2Id for matchId: $matchId")
            return null
        }

        // So sánh currentUserId với user1Id và user2Id để lấy userId của người dùng đối phương
        return if (user1Id == currentUserId) user2Id else user1Id
    }

    private fun Context.isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

suspend fun getUserData(userId: String, context: Context): UserData {
    val userRef = FirebaseDatabase.getInstance().getReference("users/$userId")
    val snapshot = userRef.get().await()
    val name = snapshot.child("fullName").value as? String ?: "Unknown"
    val imageUrls = snapshot.child("imageUrls").value as? List<String>
    val firstImageUrl = imageUrls?.firstOrNull()

    Log.d("ChatListViewModel", "User ID: $userId, Name: $name, Avatar URL: $firstImageUrl")
    return UserData(
        userId = userId,
        name = name,
        avatarUrl = firstImageUrl
    )
}

class ChatListViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatListViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class UserData(
    val userId: String,
    val name: String,
    val avatarUrl: String?
)


