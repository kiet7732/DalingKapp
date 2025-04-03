package com.example.dalingk.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import android.util.Log
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.dalingk.R
import com.example.dalingk.navigation.Routes
import data.chat.viewmodel.ChatListViewModelFactory
import kotlinx.coroutines.launch
import data.chat.viewmodel.getUserData
import kotlinx.coroutines.delay

class ChatScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DalingKTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                )
                {

                }
            }
        }
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
        viewModel.loadMessages(matchId) // Tải tin nhắn ban đầu
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
    var visibleMessageCount by remember { mutableStateOf(20) } // Số tin nhắn hiển thị
    val totalMessages = messages.size
    val pageSize = 20 // Kích thước mỗi trang

    // Chỉ lấy tin nhắn cần hiển thị dựa trên phân trang
    val displayedMessages = remember(messages, visibleMessageCount) {
        messages
            .sortedBy { it.timestamp }
            .takeLast(visibleMessageCount.coerceAtMost(totalMessages))
    }

    // Trạng thái cuộn của LazyColumn
    val lazyListState = rememberLazyListState()

    // Tải thêm tin nhắn cũ khi cuộn gần đầu danh sách
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { firstVisibleIndex ->
                if (firstVisibleIndex < 10 && visibleMessageCount < totalMessages) { // Tăng ngưỡng lên 10
                    val oldFirstVisibleItem = lazyListState.firstVisibleItemIndex
                    visibleMessageCount += pageSize
                    delay(50) // Giảm delay để phản hồi nhanh hơn
                    val newIndex = (oldFirstVisibleItem + pageSize).coerceAtMost(displayedMessages.size - 1)
                    lazyListState.animateScrollToItem(newIndex)
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                        navController.currentBackStackEntry?.savedStateHandle?.set(Routes.DetailU, 2)
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
                            .size(40.dp)
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
        val reversedMessages = remember(displayedMessages) { displayedMessages.reversed() } //Lưu danh sách đã đảo ngược một lần và tái sử dụng:
        // Danh sách tin nhắn
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            state = lazyListState,
            contentPadding = PaddingValues(vertical = 8.dp),
            reverseLayout = true // Tin nhắn mới nhất ở dưới cùng
        ) {
            itemsIndexed(displayedMessages.reversed()) { index, message ->
                // Lấy tin nhắn trước đó (nếu có)
                val previousMessage = reversedMessages.getOrNull(reversedMessages.indexOf(message) + 1)
                val shouldShowDate = previousMessage == null || !isSameDay(message.timestamp, previousMessage.timestamp)
                if (shouldShowDate) {
                    Text(
                        text = formatDate(message.timestamp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                ChatItem(
                    message = message,
                    currentUserId = currentUserId,
                    context = context
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        var messageText by remember { mutableStateOf("") }
        val keyboardController = LocalSoftwareKeyboardController.current // Quản lý bàn phím
        val focusManager = LocalFocusManager.current // Quản lý focus

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp)
                .background(Color.Transparent)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus() // Ẩn bàn phím khi bấm ra ngoài
                    keyboardController?.hide()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hộp nhập tin nhắn
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f) // Để TextField mở rộng tối đa
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
                                focusManager.clearFocus() // Bỏ focus khỏi TextField
                            }
                        }
                    ),
                    shape = RoundedCornerShape(20.dp), // Bo góc mềm mại
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Nút gửi tin nhắn
                FloatingActionButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(matchId, currentUserId, messageText)
                            messageText = ""
                            focusManager.clearFocus() // Bỏ focus khỏi TextField
                        }
                    },
                    modifier = Modifier
                        .size(55.dp) // Nhỏ gọn hơn
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
    context: Context
) {
    // Nếu là tin nhắn hệ thống (senderId = "system"), hiển thị ở giữa
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
        // Tin nhắn thông thường (không phải hệ thống)
        var userData by remember { mutableStateOf<UserData?>(null) }
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(message.senderId) {
            coroutineScope.launch {
                userData = getUserData(message.senderId, context)
            }
        }

        // Chỉ hiển thị khi userData đã được tải
        userData?.let { data ->
            val isSentByCurrentUser = message.senderId == currentUserId

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = if (isSentByCurrentUser) Arrangement.End else Arrangement.Start
            ) {
//                if (!isSentByCurrentUser) {
//                    Image(
//                        painter = painterResource(id = data.avatarResId),
//                        contentDescription = "Avatar",
//                        modifier = Modifier
//                            .size(40.dp)
//                            .clip(CircleShape)
//                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                }
                Column(
                    modifier = Modifier
                        .wrapContentWidth() // Co giãn theo nội dung
                        .widthIn(max = 300.dp) // Giới hạn tối đa 250.dp
                        .background(
                            color = if (isSentByCurrentUser) Color(0xFFDCF8C6) else Color(0xFF464545),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = message.text,
                        color = if (isSentByCurrentUser) Color.Black else Color.White,
                        fontSize = 15.sp,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.wrapContentWidth() // Đảm bảo Text co giãn theo nội dung
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatMessageTime(message.timestamp),
                        color = if (isSentByCurrentUser) Color.DarkGray else Color(0xFFB3B9C9),
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .wrapContentWidth() // Co giãn theo nội dung
                            .align(if (isSentByCurrentUser) Alignment.Start else Alignment.End)
                    )
                }
//                if (isSentByCurrentUser) {
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Image(
//                        painter = painterResource(id = data.avatarResId),
//                        contentDescription = "Avatar",
//                        modifier = Modifier
//                            .size(40.dp)
//                            .clip(CircleShape)
//                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
//                    )
//                }
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

fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

fun formatMessageTime(timestamp: Long): String {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(Date(timestamp))
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