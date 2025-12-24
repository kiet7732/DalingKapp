package com.example.dalingk.screens.chatUI

import android.Manifest
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
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.util.Log
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.dalingk.R
import com.example.dalingk.data.chat.video.VideoMessageHandler
import com.example.dalingk.navigation.Routes
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import data.chat.AppChatDatabase
import data.chat.video.VideoPlayer
import data.chat.viewmodel.ChatListViewModelFactory
import kotlinx.coroutines.launch
import data.chat.viewmodel.getUserData
import data.viewmodel.UserInputViewModel
import kotlinx.coroutines.delay
import util.AppState
import util.ImageUtils
import java.io.File

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
    val snackbarHostState = remember { SnackbarHostState() }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Cần quyền ghi âm để sử dụng tính năng này")
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.notificationMessage.collect { message ->
            if (message != null) {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    var isRecording by remember { mutableStateOf(false) }
    var isRecordingStopped by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var recordingProgress by remember { mutableStateOf(0f) }
    var showAttachmentMenu by remember { mutableStateOf(false) }

    val videoLaunchers = VideoMessageHandler.setupVideoLaunchers(
        matchId = matchId,
        currentUserId = currentUserId,
        userInputViewModel = userInputViewModel,
        coroutineScope = coroutineScope,
        snackbarHostState = snackbarHostState,
        onVideoSent = { videoUrl ->
            Log.d("ChatScreen", "Video message sent: $videoUrl")
            viewModel.sendVideoMessage(matchId, currentUserId, videoUrl)
        }
    )

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingProgress = 0f
            while (isRecording) {
                recordingProgress = (recordingProgress + 0.1f) % 1f
                delay(100)
            }
        } else {
            recordingProgress = 0f
        }
    }

    LaunchedEffect(matchId) {
        coroutineScope.launch {
            AppChatDatabase.getDatabase(context).chatDao().markAllMessagesAsNotified(matchId)
            viewModel.loadMessages(matchId)
            AppState.setCurrentChatScreen(matchId)
        }
    }

    val messages by viewModel.messages.collectAsState()
    var userData by remember { mutableStateOf<UserData?>(null) }
    LaunchedEffect(matchId) {
        coroutineScope.launch {
            val otherUserId = viewModel.getOtherUserId(matchId)
            userData = otherUserId?.let { getUserData(it, context) }
        }
    }

    val lazyListState = rememberLazyListState()
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(matchId) {
        viewModel.startMessagesListener(matchId)
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val compressedFile = ImageUtils.compressImageToFile(context, it, maxSizeKB = 150)
                    userInputViewModel.uploadFileToCloudinary(
                        filePath = compressedFile.absolutePath,
                        fileType = UserInputViewModel.FileType.IMAGE,
                        onSuccess = { imageUrl ->
                            Log.d("ChatScreen", "Image URL uploaded: $imageUrl")
                            viewModel.sendImageMessage(matchId, currentUserId, imageUrl)
                            compressedFile.delete()
                        },
                        onError = { error ->
                            Log.e("ChatScreen", "Error uploading image: $error")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Lỗi tải ảnh: $error")
                            }
                            compressedFile.delete()
                        }
                    )
                } catch (e: Exception) {
                    Log.e("ChatScreen", "Exception during image processing: ${e.message}")
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Lỗi xử lý ảnh: ${e.message}")
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
            ) {
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
                                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Quay lại")
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

                Box(modifier = Modifier.weight(1f)) {
                    // Thanh loading nằm trên khung chat
                    val isLoading = remember { mutableStateOf(false) }
                    LaunchedEffect(messages) {
                        isLoading.value = messages.any { !it.isSynced && (it.messageType == "video" || it.messageType == "image" || it.messageType == "audio") }
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isLoading.value) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .align(Alignment.TopCenter),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        }
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
                                    showAvatar = !isSameSenderAsPrevious && message.senderId != currentUserId,
                                    userInputViewModel = userInputViewModel
                                )
                                Spacer(modifier = Modifier.height(if (isSameSenderAsPrevious) 2.dp else 8.dp))
                            }
                        }
                    }
                }

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
                        Box {
                            IconButton(
                                onClick = { showAttachmentMenu = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(end = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Đính kèm",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            DropdownMenu(
                                expanded = showAttachmentMenu,
                                onDismissRequest = { showAttachmentMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Chọn ảnh") },
                                    onClick = {
                                        pickImageLauncher.launch("image/*")
                                        showAttachmentMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Ghi âm") },
                                    onClick = {
                                        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                            try {
                                                val fileName = "${context.cacheDir}/audio_${System.currentTimeMillis()}.m4a"
                                                audioFile = File(fileName)
                                                mediaRecorder = MediaRecorder().apply {
                                                    setAudioSource(MediaRecorder.AudioSource.MIC)
                                                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                                    setOutputFile(fileName)
                                                    prepare()
                                                    start()
                                                }
                                                isRecording = true
                                                Log.d("ChatScreen", "Started recording: $fileName")
                                            } catch (e: Exception) {
                                                Log.e("ChatScreen", "Failed to start recording: ${e.message}")
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Lỗi khởi động ghi âm: ${e.message}")
                                                }
                                                audioFile?.delete()
                                                audioFile = null
                                                isRecording = false
                                            }
                                        } else {
                                            recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                        }
                                        showAttachmentMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Quay video") },
                                    onClick = {
                                        coroutineScope.launch {
                                            Log.d("ChatScreen", "Launching video capture")
                                            VideoMessageHandler.launchCaptureVideo(
                                                context = context,
                                                cameraPermissionLauncher = videoLaunchers.cameraPermissionLauncher,
                                                captureVideoLauncher = videoLaunchers.captureVideoLauncher,
                                                setVideoUri = videoLaunchers.setVideoUri
                                            )
                                        }
                                        showAttachmentMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Videocam, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Chọn video") },
                                    onClick = {
                                        Log.d("ChatScreen", "Launching video picker")
                                        VideoMessageHandler.launchPickVideo(
                                            context = context,
                                            storagePermissionLauncher = videoLaunchers.storagePermissionLauncher,
                                            pickVideoLauncher = videoLaunchers.pickVideoLauncher
                                        )
                                        showAttachmentMenu = false
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                )
                            }
                        }

                        if (isRecording || isRecordingStopped) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .background(Color.White, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isRecording) {
                                    IconButton(
                                        onClick = {
                                            try {
                                                mediaRecorder?.apply {
                                                    stop()
                                                    release()
                                                }
                                                mediaRecorder = null
                                                isRecording = false
                                                isRecordingStopped = true
                                                Log.d("ChatScreen", "Recording stopped: ${audioFile?.absolutePath}")
                                            } catch (e: Exception) {
                                                Log.e("ChatScreen", "Failed to stop recording: ${e.message}")
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Lỗi dừng ghi âm: ${e.message}")
                                                }
                                                audioFile?.delete()
                                                audioFile = null
                                                isRecording = false
                                                isRecordingStopped = false
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = "Dừng ghi âm",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            audioFile?.let { file ->
                                                if (file.exists() && file.length() > 0) {
                                                    coroutineScope.launch {
                                                        userInputViewModel.uploadFileToCloudinary(
                                                            filePath = file.absolutePath,
                                                            fileType = UserInputViewModel.FileType.AUDIO,
                                                            onSuccess = { audioUrl ->
                                                                Log.d("ChatScreen", "Audio URL uploaded: $audioUrl")
                                                                viewModel.sendAudioMessage(matchId, currentUserId, audioUrl)
                                                                file.delete()
                                                                audioFile = null
                                                                isRecordingStopped = false
                                                            },
                                                            onError = { error ->
                                                                Log.e("ChatScreen", "Error uploading audio: $error")
                                                                coroutineScope.launch {
                                                                    snackbarHostState.showSnackbar("Lỗi tải âm thanh: $error")
                                                                }
                                                                file.delete()
                                                                audioFile = null
                                                                isRecordingStopped = false
                                                            }
                                                        )
                                                    }
                                                } else {
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar("File âm thanh không hợp lệ")
                                                    }
                                                    file.delete()
                                                    audioFile = null
                                                    isRecordingStopped = false
                                                }
                                            } ?: run {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Không có file âm thanh để gửi")
                                                }
                                                isRecordingStopped = false
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Gửi ghi âm",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            try {
                                                mediaRecorder?.release()
                                                mediaRecorder = null
                                                audioFile?.let { file ->
                                                    if (file.exists()) {
                                                        file.delete()
                                                        Log.d("ChatScreen", "Audio file deleted: ${file.absolutePath}")
                                                    }
                                                }
                                                audioFile = null
                                                isRecordingStopped = false
                                                isRecording = false
                                            } catch (e: Exception) {
                                                Log.e("ChatScreen", "Error during cancel: ${e.message}")
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Lỗi khi hủy ghi âm: ${e.message}")
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hủy ghi âm",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        } else {
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
        }
    )

    DisposableEffect(matchId) {
        onDispose {
            mediaRecorder?.release()
            mediaRecorder = null
            audioFile?.delete()
            audioFile = null
            VideoMessageHandler.cleanup(context, videoLaunchers.videoUri())
            AppState.setCurrentChatScreen(null)
            Log.d("ChatScreen", "Cleaned up resources for matchId: $matchId")
        }
    }
}

@Composable
fun ChatItem(
    message: CachedMessage,
    currentUserId: String,
    userData: UserData?,
    showAvatar: Boolean,
    userInputViewModel: UserInputViewModel
) {
    var showContextMenu by remember { mutableStateOf(false) }
    var showFullImage by remember { mutableStateOf(false) }
    var showFullVideo by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var playbackProgress by remember { mutableStateOf(0f) }
    var currentPosition by remember { mutableStateOf(0) }
    var duration by remember { mutableStateOf(0) }
    var uploadProgress by remember { mutableStateOf(0f) }
    var isUploading by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        Log.d(
            "ChatItemDebug",
            "Message ID: ${message.messageId}, Type: ${message.messageType}, Text/URL: ${message.text}"
        )
        if (!message.isSynced && (message.messageType == "video" || message.messageType == "image" || message.messageType == "audio")) {
            isUploading = true
            uploadProgress = 0f
            while (isUploading && uploadProgress < 1f) {
                uploadProgress = minOf(uploadProgress + 0.02f, 1f)
                delay(100)
            }
            if (message.isSynced) {
                isUploading = false
                uploadProgress = 0f
            }
        }
    }


    LaunchedEffect(isPlaying) {
        if (isPlaying && mediaPlayer != null) {
            while (isPlaying && mediaPlayer != null) {
                try {
                    currentPosition = mediaPlayer!!.currentPosition
                    duration = mediaPlayer!!.duration
                    playbackProgress = if (duration > 0) currentPosition.toFloat() / duration else 0f
                } catch (e: Exception) {
                    Log.e("ChatItemDebug", "Error updating playback progress: ${e.message}")
                }
                delay(100)
            }
        }
    }

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
                    when (message.messageType) {
                        "video" -> {
                            if (message.text.isNotEmpty() && message.text.startsWith("http")) {
                                Box(
                                    modifier = Modifier
                                        .clickable { showFullVideo = true }
                                ) {
                                    VideoPlayer(
                                        videoUrl = message.text,
                                        isSentByCurrentUser = isSentByCurrentUser,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                }
                            } else {
                                Text(
                                    text = "Lỗi: URL video không hợp lệ",
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        "image" -> {
                            if (message.text.isNotEmpty() && message.text.startsWith("http")) {
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
                                Text(
                                    text = "Lỗi: URL ảnh không hợp lệ",
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        "audio" -> {
                            if (message.text.isNotEmpty() && message.text.startsWith("http")) {
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
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.widthIn(max = 250.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (isPlaying) {
                                                    mediaPlayer?.pause()
                                                    isPlaying = false
                                                } else {
                                                    if (mediaPlayer == null) {
                                                        mediaPlayer = MediaPlayer().apply {
                                                            setDataSource(message.text)
                                                            setOnPreparedListener {
                                                                duration = it.duration
                                                                start()
                                                                isPlaying = true
                                                            }
                                                            setOnCompletionListener {
                                                                release()
                                                                mediaPlayer = null
                                                                isPlaying = false
                                                                playbackProgress = 0f
                                                                currentPosition = 0
                                                            }
                                                            setOnErrorListener { _, what, extra ->
                                                                Log.e("ChatItemDebug", "MediaPlayer error: what=$what, extra=$extra")
                                                                release()
                                                                mediaPlayer = null
                                                                isPlaying = false
                                                                playbackProgress = 0f
                                                                currentPosition = 0
                                                                true
                                                            }
                                                            prepareAsync()
                                                        }
                                                    } else {
                                                        mediaPlayer?.start()
                                                        isPlaying = true
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = if (isPlaying) "Tạm dừng" else "Phát",
                                                tint = if (isSentByCurrentUser) Color.White else Color.Black
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        LinearProgressIndicator(
                                            progress = playbackProgress,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(4.dp),
                                            color = if (isSentByCurrentUser) Color.White else MaterialTheme.colorScheme.primary,
                                            trackColor = Color.Gray.copy(alpha = 0.3f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${formatTime(currentPosition)} / ${formatTime(duration)}",
                                            color = if (isSentByCurrentUser) Color.White else Color.Black,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Lỗi: URL âm thanh không hợp lệ",
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        else -> {
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
                    Text(
                        text = formatMessageTime(message.timestamp),
                        color = if (isSentByCurrentUser) Color.White.copy(alpha = 0.7f) else Color.Gray.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.End)
                    )

                    if (!message.isSynced && message.messageType in listOf("video", "image", "audio")) {
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = playbackProgress,
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp),
                            color = if (isSentByCurrentUser) Color.White else MaterialTheme.colorScheme.primary,
                            trackColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    }

                }
                if (isSentByCurrentUser) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            if (showFullImage) {
                Dialog(
                    onDismissRequest = { showFullImage = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x3EC5C5C5))
                            .clickable { showFullImage = false },
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

            if (showFullVideo) {
                Dialog(
                    onDismissRequest = { showFullVideo = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .clickable { showFullVideo = false },
                        contentAlignment = Alignment.Center
                    ) {
                        VideoPlayer(
                            videoUrl = message.text,
                            isSentByCurrentUser = isSentByCurrentUser,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                        )
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
            playbackProgress = 0f
            currentPosition = 0
        }
    }
}

// Hàm định dạng thời gian từ mili giây sang định dạng mm:ss
fun formatTime(millis: Int): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
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