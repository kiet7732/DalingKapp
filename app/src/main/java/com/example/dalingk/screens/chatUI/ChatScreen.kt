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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.dalingk.R
import com.example.dalingk.navigation.Routes
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import data.chat.AppChatDatabase
import data.chat.viewmodel.ChatListViewModelFactory
import kotlinx.coroutines.launch
import data.chat.viewmodel.getUserData
import data.viewmodel.UserInputViewModel
import util.ImageUtils

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
    val userInputViewModel = UserInputViewModel()

    // Đánh dấu tất cả tin nhắn trong matchId là đã thông báo khi mở ChatScreen
    LaunchedEffect(matchId) {
        coroutineScope.launch {
            AppChatDatabase.getDatabase(context).chatDao().markAllMessagesAsNotified(matchId)
            viewModel.loadMessages(matchId)
            util.AppState.setCurrentChatScreen(matchId)
        }
    }

    // Trạng thái danh sách tin nhắn
    val messages by viewModel.messages.collectAsState()

    // Lấy thông tin người dùng đối phương
    var userData by remember { mutableStateOf<UserData?>(null) }
    LaunchedEffect(matchId) {
        coroutineScope.launch {
            val otherUserId = viewModel.getOtherUserId(matchId)
            userData = otherUserId?.let { getUserData(it, context) }
        }
    }

    // Trạng thái cuộn
    val lazyListState = rememberLazyListState()

    // Tự động cuộn đến tin nhắn mới nhất
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(matchId) {
        viewModel.startMessagesListener(matchId)
    }

    // Trình chọn ảnh
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    // Nén ảnh
                    val compressedFile =
                        ImageUtils.compressImageToFile(context, it, maxSizeKB = 150)
                    // Tải ảnh lên Cloudinary
                    userInputViewModel.uploadPhotoToCloudinary(
                        filePath = compressedFile.absolutePath,
                        onSuccess = { imageUrl ->
                            // Gửi URL ảnh dưới dạng tin nhắn
                            viewModel.sendImageMessage(matchId, currentUserId, imageUrl)
                        },
                        onError = { error ->
                            // Hiển thị lỗi nếu có
                            coroutineScope.launch {
                                // Có thể thêm Snackbar để thông báo lỗi
                            }
                        }
                    )
                } catch (e: Exception) {
                    // Xử lý lỗi
                }
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
                reverseLayout = false
            ) {
                itemsIndexed(messages) { index, message ->
                    val previousMessage = messages.getOrNull(index - 1)
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
                // Nút chọn ảnh
                IconButton(
                    onClick = {
                        pickImageLauncher.launch("image/*")
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AttachFile,
                        contentDescription = "Chọn ảnh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
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

    // Đặt lại trạng thái khi rời khỏi màn hình chat
    DisposableEffect(matchId) {
        onDispose {
            util.AppState.setCurrentChatScreen(null)
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
    var showFullImage by remember { mutableStateOf(false) } // Trạng thái phóng to ảnh

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
                ) {
                    if (message.messageType == "image") {
                        // Hiển thị hình ảnh mà không có viền như bong bóng chat
                        Box(
                            modifier = Modifier
                                .widthIn(max = 340.dp)
                                .heightIn(max = 370.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showFullImage = true }
                        ) {
                            AsyncImage(
                                model = message.text,
                                contentDescription = "Hình ảnh",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.ic_error),
                                error = painterResource(R.drawable.ic_error)
                            )
                        }
                    } else {
                        // Bong bóng chat cho văn bản
                        Column(
                            modifier = Modifier
                                .background(
                                    color = if (isSentByCurrentUser)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                    else Color(0xFFF1F1F1),
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
                        }
                    }
                }
                if (isSentByCurrentUser) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
        // Hiển thị ảnh toàn màn hình khi nhấp

        if (showFullImage) {
            Dialog(
                onDismissRequest = { showFullImage = false },
                properties = DialogProperties(usePlatformDefaultWidth = false) // Cho phép full màn hình
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x3EC5C5C5)) // Nền tối toàn màn hình
                        .clickable { showFullImage = false }, // Bấm nền ngoài để tắt
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = message.text,
                        contentDescription = "Hình ảnh toàn màn hình",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable { showFullImage = false },
                        contentScale = ContentScale.Fit
                    )
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