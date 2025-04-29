package data.chat.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import com.example.dalingk.R
import com.example.dalingk.screens.chatUI.ChatScreen
import com.google.firebase.database.FirebaseDatabase
import data.chat.AppChatDatabase
import data.chat.CachedMessage
import data.chat.viewmodel.UserData
import data.chat.viewmodel.getUserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MessageSyncService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var chatDao: data.chat.ChatDao
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate() {
        super.onCreate()
        chatDao = AppChatDatabase.getDatabase(this).chatDao()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getParcelableExtra<CachedMessage>("NOTIFICATION_MESSAGE")?.let { message ->
            if (!message.isNotified) {
                scope.launch {
                    val senderId = message.senderId
                    val userData = getUserData(senderId, this@MessageSyncService)
                    // Kiểm tra xem tin nhắn có phải là mới nhất (gần với thời gian hiện tại)
                    val currentTime = System.currentTimeMillis()
                    if (message.timestamp >= currentTime - 10_000) {
                        sendNotification(message, userData)
                        chatDao.markMessageAsNotified(message.messageId)
                        android.util.Log.d("MessageSyncService", "Direct notification sent for message: ${message.messageId}")
                    } else {
                        android.util.Log.d("MessageSyncService", "Ignoring old message in notification: ${message.messageId}")
                        chatDao.markMessageAsNotified(message.messageId)
                    }
                }
            }
        } ?: run {
            scope.launch {
                syncMessages()
            }
        }

        return START_STICKY
    }

    private suspend fun syncMessages() {
        val ownerId = UserPreferences.getUserId(this@MessageSyncService)
        if (ownerId == null) {
            android.util.Log.e("MessageSyncService", "Owner ID is null, cannot sync messages")
            stopSelf()
            return
        }

        android.util.Log.d("MessageSyncService", "Starting syncMessages for ownerId: $ownerId")
        val unsyncedMessages = chatDao.getMessagesByMatchIdWithPagination(ownerId, ownerId, 100, 0)
            .filter { !it.isSynced && !it.isNotified && it.senderId != ownerId }
        android.util.Log.d("MessageSyncService", "Found ${unsyncedMessages.size} unsynced messages")

        unsyncedMessages.forEach { message ->
            try {
                android.util.Log.d("MessageSyncService", "Syncing message: ${message.messageId}")
                database.getReference("matches/${message.matchId}/chat/${message.messageId}")
                    .setValue(
                        mapOf(
                            "senderId" to message.senderId,
                            "text" to message.text,
                            "timestamp" to message.timestamp
                        )
                    ).await()

                chatDao.markMessageAsSynced(message.messageId)
                chatDao.markChatListItemAsSynced(message.matchId)

                val shouldNotify = !message.isNotified &&
                        message.senderId != ownerId &&
                        (!util.AppState.isAppInForeground() || !util.AppState.isChatScreenOpen(message.matchId))
                android.util.Log.d(
                    "MessageSyncService",
                    "Message ${message.messageId}: shouldNotify=$shouldNotify, " +
                            "isNotified=${message.isNotified}, " +
                            "isFromOwner=${message.senderId == ownerId}, " +
                            "isAppInForeground=${util.AppState.isAppInForeground()}, " +
                            "isChatScreenOpen=${util.AppState.isChatScreenOpen(message.matchId)}"
                )

                if (shouldNotify) {
                    // Kiểm tra xem tin nhắn có phải là mới nhất
                    val currentTime = System.currentTimeMillis()
                    if (message.timestamp >= currentTime - 10_000) {
                        val userData = getUserData(message.senderId, this@MessageSyncService)
                        sendNotification(message, userData)
                        chatDao.markMessageAsNotified(message.messageId)
                        android.util.Log.d("MessageSyncService", "Notification sent for message: ${message.messageId}")
                    } else {
                        chatDao.markMessageAsNotified(message.messageId)
                        android.util.Log.d("MessageSyncService", "Ignoring old message in sync: ${message.messageId}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MessageSyncService", "Error syncing message ${message.messageId}: ${e.message}")
            }
        }

        android.util.Log.d("MessageSyncService", "Finished syncMessages, stopping service")
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Kênh đồng bộ tin nhắn",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private suspend fun sendNotification(message: CachedMessage, userData: UserData) {
        val intent = Intent(this, ChatScreen::class.java).apply {
            putExtra("matchId", message.matchId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            message.messageId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val bitmap = if (userData.avatarUrl?.isNotEmpty() == true) {
            val request = ImageRequest.Builder(this)
                .data(userData.avatarUrl)
                .allowHardware(false)
                .build()
            imageLoader.execute(request).drawable?.toBitmap()
        } else {
            null
        }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(userData.name)
            .setContentText(message.text)
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (bitmap != null) {
            notificationBuilder.setLargeIcon(bitmap)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(message.messageId.hashCode(), notificationBuilder.build())
    }

    companion object {
        private const val CHANNEL_ID = "MessageSyncChannel"
    }
}