package com.example.dalingk.screens.chatUI

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dalingk.screens.ui.theme.DalingKTheme
import data.chat.viewmodel.ChatListViewModel

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.database.FirebaseDatabase
import data.chat.CachedMessage
import data.chat.viewmodel.UserData
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.dalingk.R
import com.example.dalingk.navigation.Routes
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import data.chat.viewmodel.ChatListViewModelFactory
import kotlinx.coroutines.launch
import data.chat.viewmodel.getUserData

class ChatScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DalingKTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Truyền NavController và matchId từ Intent hoặc savedInstanceState
                    val matchId = intent.getStringExtra("matchId") ?: return@Surface
                    ChatScreen(navController = rememberNavController(), matchId = matchId)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        util.AppState.setAppForeground(true)
    }

    override fun onPause() {
        super.onPause()
        util.AppState.setAppForeground(false)
    }
}


@Composable
fun ChatScreen(
    navController: NavController,
    matchId: String,
    viewModel: ChatListViewModel = viewModel(factory = ChatListViewModelFactory(LocalContext.current))
) {
    val context = LocalContext.current
    val currentUserId = UserPreferences.getUserId(context) ?: return
    val coroutineScope = rememberCoroutineScope()

    // Trạng thái danh sách tin nhắn
    LaunchedEffect(matchId) {
        viewModel.loadMessages(matchId)
    }
    val messages by viewModel.messages.collectAsState()


    // Lấy thông tin người dùng đối phương
    var userData by remember { mutableStateOf<UserData?>(null) }
    LaunchedEffect(matchId) {
        coroutineScope.launch {
            val otherUserId = viewModel.getOtherUserId(matchId)
            userData = otherUserId?.let { getUserData(it, context) }
        }
    }

    // Quản lý phân trang
    var visibleMessageCount by remember { mutableStateOf(50) }
    val totalMessages = messages.size
    val pageSize = 50
    var isLoading by remember { mutableStateOf(false) }

    // Sắp xếp tin nhắn theo thời gian tăng dần
    val displayedMessages = remember(messages, visibleMessageCount) {
        messages
            .sortedBy { it.timestamp } // Sắp xếp tăng dần để tin nhắn cũ ở trên, mới ở dưới
            .takeLast(visibleMessageCount.coerceAtMost(totalMessages))
    }

    // Trạng thái cuộn
    val lazyListState = rememberLazyListState()

    // Tự động cuộn đến tin nhắn mới nhất
    LaunchedEffect(matchId, displayedMessages) {
        if (displayedMessages.isNotEmpty()) {
            lazyListState.animateScrollToItem(displayedMessages.size - 1)
        }
    }

    LaunchedEffect(matchId) {
        viewModel.loadMessages(matchId)
        viewModel.startMessagesListener(matchId)
    }

    // Tải thêm tin nhắn khi cuộn gần đầu
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { firstVisibleIndex ->
                if (firstVisibleIndex < 10 && visibleMessageCount < totalMessages && !isLoading) {
                    isLoading = true
                    visibleMessageCount += pageSize
                    isLoading = false
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        // Header
        userData?.let { data ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            Routes.DetailU,
                            2
                        )
                        navController.navigate(Routes.MainMatch)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Image(
                        painter = if (data.avatarUrl?.isNotEmpty() == true) {
                            rememberAsyncImagePainter(
                                model = data.avatarUrl,
                                placeholder = painterResource(R.drawable.ic_error),
                                error = painterResource(R.drawable.ic_error)
                            )
                        } else {
                            painterResource(R.drawable.ic_error)
                        },
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = data.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Divider(modifier = Modifier.padding(vertical = 1.dp))
            }
        }

        // Danh sách tin nhắn
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                state = lazyListState,
                contentPadding = PaddingValues(vertical = 8.dp),
                reverseLayout = false // Bỏ reverseLayout để hiển thị từ trên xuống
            ) {
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                itemsIndexed(displayedMessages) { index, message ->
                    val previousMessage = displayedMessages.getOrNull(index - 1)
                    val isSameSenderAsPrevious = previousMessage?.senderId == message.senderId
                    val shouldShowDate = previousMessage == null || !isSameDay(
                        message.timestamp,
                        previousMessage.timestamp
                    )
                    if (shouldShowDate) {
                        Text(
                            text = formatFriendlyDate(message.timestamp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Gray.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    ChatItem(
                        message = message,
                        currentUserId = currentUserId,
                        userData = userData,
                        showAvatar = !isSameSenderAsPrevious && message.senderId != currentUserId
                    )
                    Spacer(modifier = Modifier.height(if (isSameSenderAsPrevious) 2.dp else 8.dp))
                }
            }
        }

        // Hộp nhập tin nhắn
        var messageText by remember { mutableStateOf("") }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp)
                .background(Color.Transparent)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    placeholder = {
                        Text("Nhập tin nhắn...", color = Color.Gray.copy(alpha = 0.5f))
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(matchId, currentUserId, messageText)
                                messageText = ""
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(matchId, currentUserId, messageText)
                            messageText = ""
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier
                        .size(55.dp)
                        .padding(4.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Gửi",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatItem(
    message: CachedMessage,
    currentUserId: String,
    userData: UserData?,
    showAvatar: Boolean
) {
    var showContextMenu by remember { mutableStateOf(false) }

    if (message.senderId == "system") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = message.text,
                color = Color.Gray,
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    } else {
        userData?.let { data ->
            val isSentByCurrentUser = message.senderId == currentUserId
            val shape = if (isSentByCurrentUser) {
                RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 4.dp,
                    bottomStart = 12.dp,
                    bottomEnd = 4.dp
                )
            } else {
                RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 12.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 12.dp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .clickable(
                        onClick = { showContextMenu = true },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                horizontalArrangement = if (isSentByCurrentUser) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.Bottom
            ) {
                if (!isSentByCurrentUser && showAvatar) {
                    Image(
                        painter = if (data.avatarUrl?.isNotEmpty() == true) {
                            rememberAsyncImagePainter(
                                model = data.avatarUrl,
                                placeholder = painterResource(R.drawable.ic_error),
                                error = painterResource(R.drawable.ic_error)
                            )
                        } else {
                            painterResource(R.drawable.ic_error)
                        },
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else if (!isSentByCurrentUser) {
                    Spacer(modifier = Modifier.width(40.dp))
                }
                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .widthIn(max = 300.dp)
                        .background(
                            color = if (isSentByCurrentUser) MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.9f
                            ) else Color(0xFFF1F1F1),
                            shape = shape
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = message.text,
                        color = if (isSentByCurrentUser) Color.White else Color.Black,
                        fontSize = 15.sp,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.wrapContentWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatMessageTime(message.timestamp),
                        color = if (isSentByCurrentUser) Color.White.copy(alpha = 0.7f) else Color.Gray.copy(
                            alpha = 0.7f
                        ),
                        fontSize = 11.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.End)
                    )
                }
                if (isSentByCurrentUser) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}


// Hàm hỗ trợ
suspend fun getOtherUserId(matchId: String, currentUserId: String, context: Context): String? {
    val database = FirebaseDatabase.getInstance()
    val snapshot = database.getReference("matches/$matchId").get().await()
    val user1Id = snapshot.child("user1Id").value as? String
    val user2Id = snapshot.child("user2Id").value as? String
    return if (user1Id == currentUserId) user2Id else user1Id
}

fun formatFriendlyDate(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    val today = calendar.timeInMillis
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis
    val startOfYesterday = startOfToday - 24 * 60 * 60 * 1000

    return when {
        timestamp >= startOfToday -> "Hôm nay"
        timestamp >= startOfYesterday -> "Hôm qua"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

fun formatMessageTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}

fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
    val calendar1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val calendar2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview16() {
    DalingKTheme {

    }
}