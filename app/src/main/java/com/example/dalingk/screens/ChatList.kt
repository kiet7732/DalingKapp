package com.example.dalingk.screens

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dalingk.ui.theme.DalingKTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import data.chat.CachedChatListItem
import data.chat.viewmodel.ChatListViewModel
import data.chat.viewmodel.ChatListViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.rememberAsyncImagePainter
import com.example.dalingk.R

class ChatList : ComponentActivity() {
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
                    GreetingPreview15()
                }
            }
        }
    }
}

@Composable
fun ChatListUI(
    viewModel: ChatListViewModel = viewModel(factory = ChatListViewModelFactory(LocalContext.current)),
    navController: NavController
) {
    val chatList by viewModel.chatList.collectAsState()
    val isLoading by remember { mutableStateOf(true) } // Thay bằng trạng thái thực tế từ ViewModel nếu có

    LaunchedEffect(Unit) {
        viewModel.startChatSync()
        // Giả định có trạng thái loading từ ViewModel
        // isLoading = viewModel.isLoading.value
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading && chatList.isEmpty()) {
//            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Danh sách avatar ngang
                HorizontalAvatarList(chatList = chatList, navController = navController)

                // Danh sách chat dọc
                val listState = rememberLazyListState()
                Box(  modifier = Modifier
                    .weight(1f) // Chiếm phần còn lại của không gian
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)) // Bo tròn góc trên
                    .background(Color(0xFFFFE6E9)), // Màu nền để viền trông rõ hơn (tùy chọn)
                     ){
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        state = listState
                    ) {
                        items(chatList, key = { it.matchId }) { item ->
                            ChatListItem(item = item, navController = navController)
                        }
                    }
                }

                LaunchedEffect(chatList.size) {
                    if (chatList.isNotEmpty()) {
                        listState.animateScrollToItem(chatList.size - 1)
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalAvatarList(
    chatList: List<CachedChatListItem>,
    navController: NavController
) {
    LazyRow(
        modifier = Modifier
            .padding(top = 10.dp)
            .height(100.dp)
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(chatList, key = { it.matchId }) { item ->
            Column(
                modifier = Modifier
                    .width(100.dp)
                    .padding(horizontal = 4.dp) // Thêm padding ngang cho khoảng cách giữa các item
                    .clickable {
//                        navController.navigate("chat/${item.matchId}")
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = if (item.avatarUrl.isNotEmpty()) {
                        rememberAsyncImagePainter(
                            model = item.avatarUrl,
                            placeholder = painterResource(R.drawable.ic_error),
                            error = painterResource(R.drawable.ic_error)
                        )
                    } else {
                        painterResource(R.drawable.ic_error)
                    },
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = item.name,
//                    fontSize = 12.sp,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    textAlign = TextAlign.Center
//                )
            }
        }
    }

}
@Composable
fun ChatListItem(
    item: CachedChatListItem,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth() // Lấp đầy toàn bộ chiều ngang
            .height(95.dp)
            .clickable {
                navController.navigate("chat/${item.matchId}")
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFE6E9) // Đặt màu nền rõ ràng
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize() // Đảm bảo nội dung không bị co lại
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = if (item.avatarUrl.isNotEmpty()) {
                    rememberAsyncImagePainter(
                        model = item.avatarUrl,
                        placeholder = painterResource(R.drawable.ic_error),
                        error = painterResource(R.drawable.ic_error)
                    )
                } else {
                    painterResource(R.drawable.ic_error)
                },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.latestMessage,
                    color = Color(0xFF383838),
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = formatTime(item.timestamp),
                color = Color.Black,
                fontSize = 13.sp,
                textAlign = TextAlign.End
            )
        }
    }
}

fun formatTime(timestamp: Long): String {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return timeFormat.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview15() {
    DalingKTheme {

    }
}