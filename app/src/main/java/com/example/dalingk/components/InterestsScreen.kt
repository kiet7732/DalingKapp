package com.example.dalingk.components

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.dalingk.R
import com.example.dalingk.components.ui.theme.DalingKTheme
import data.chat.viewmodel.QuickChatState
import data.chat.viewmodel.QuickChatViewModel
import data.repository.AuthViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import data.chat.viewmodel.QuickChatViewModelFactory

class InterestsScreen : ComponentActivity() {
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
                    GreetingPreview21()
                }
            }
        }
    }
}

@Composable
fun CardInterests(
    backgroundColor: Color,
    jsonFileName: Int,
    text: String,
    badgeText: String,
    height: Dp = 280.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(15.dp)
    ) {
        // Badge in the top-right corner
        if (badgeText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Icon centered in the Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            val animationModifier = if (height == 280.dp) {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .scale(1.3f)
            } else {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.9f)
            }

            LottieAnimationCus(
                jsonFileName = jsonFileName,
                modifier = animationModifier,
                loop = false
            )
        }

        // Text at the bottom-left corner
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Start,
            modifier = Modifier
                .align(Alignment.BottomStart)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterestsScreenUI(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val quickChatViewModel: QuickChatViewModel = viewModel(
        factory = QuickChatViewModelFactory(context)
    )
    val userData by authViewModel.userData.collectAsState()
    val quickChatState by quickChatViewModel.quickChatState.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    // Thêm trạng thái cho Dialog
    var showDialog by remember { mutableStateOf(false) }

    // Hiển thị Dialog khi ghép đôi thành công
    LaunchedEffect(quickChatState) {
        Log.d("QuickChat", "quickChatState updated: $quickChatState")
        if (quickChatState.isMatched && quickChatState.matchId != null) {
            Log.d("QuickChat", "Chúc mừng! Bạn đã được ghép đôi!-----------------------------")
            showDialog = true // Hiển thị Dialog
            // Không chuyển hướng ngay, đợi người dùng nhấn OK
            showBottomSheet = false
        }
        quickChatState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Dialog thông báo
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.EmojiEmotions,
                    contentDescription = null,
                    tint = Color(0xFFFF5069),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(id = R.string.textfind_9),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.textfind_10),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        navController.navigate("chat/${quickChatState.matchId}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5069))
                ) {
                    Text(stringResource(id = R.string.textfind_11))
                }
            },
//            dismissButton = {
//                OutlinedButton(
//                    onClick = {
//                        showDialog = false
//                        quickChatViewModel.leaveQueue(userData?.userId ?: "")
//                    }
//                ) {
//                    Text("Hủy")
//                }
//            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 8.dp
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 5.dp, end = 5.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.textfind_1),
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .padding(5.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showBottomSheet = true
                        },
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CardInterests(
                        backgroundColor = Color(0xFFFF8A5A),
                        jsonFileName = R.raw.heart_a,
                        text = stringResource(id = R.string.textfind_2),
                        badgeText = "",
                        modifier = Modifier.weight(1f),
                        height = 250.dp
                    )
                }
            }
            // Giữ nguyên các item khác
            item {
                Text(
                    text = stringResource(id = R.string.textfind_3),
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 5.dp)
                )

                Text(
                    text = stringResource(id = R.string.textfind_4),
                    color = Color.Gray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(bottom = 5.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CardInterests(
                        backgroundColor = Color(0xFFFF5A5F),
                        jsonFileName = R.raw.moon_a,
                        text = stringResource(id = R.string.textfind_5),
                        badgeText = "",
                        modifier = Modifier.weight(1f)
                    )
                    CardInterests(
                        backgroundColor = Color(0xFF5A9CFF),
                        jsonFileName = R.raw.passport_a,
                        text = stringResource(id = R.string.textfind_6),
                        badgeText = "",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CardInterests(
                        backgroundColor = Color(0xFF8A5AFF),
                        jsonFileName = R.raw.movie_a,
                        text = stringResource(id = R.string.textfind_7),
                        badgeText = "",
                        modifier = Modifier.weight(1f)
                    )
                    CardInterests(
                        backgroundColor = Color(0xFFFF8A5A),
                        jsonFileName = R.raw.sport_a,
                        text = stringResource(id = R.string.textfind_8),
                        badgeText = "",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // ModalBottomSheet cho Trò chuyện nhanh
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    userData?.userId?.let { quickChatViewModel.leaveQueue(it) }
                    showBottomSheet = false
                }
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            QuickChatUI(
                quickChatState = quickChatState,
                onJoinQueue = {
                    userData?.let { user ->
                        quickChatViewModel.joinQuickChatQueue(
                            userId = user.userId,
                            gender = user.gender,
                            preferredGender = if (user.gender.lowercase() == "male") "female" else "male"
                        )
                    }
                },
                onCancel = {
                    scope.launch {
                        userData?.userId?.let { quickChatViewModel.leaveQueue(it) }
                        sheetState.hide()
                        showBottomSheet = false
                    }
                }
            )
        }
    }
}

@Composable
fun QuickChatUI(
    quickChatState: QuickChatState,
    onJoinQueue: () -> Unit,
    onCancel: () -> Unit
) {
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.find_a))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.textfind_12),
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFFFF5069),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        if (quickChatState.isInQueue) {
            LottieAnimation(
                composition = lottieComposition,
                modifier = Modifier.size(260.dp),
                iterations = LottieConstants.IterateForever
            )
            Text(
                text = stringResource(id = R.string.textfind_13),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFADAEBB))
            ) {
                Text(
                    stringResource(id = R.string.textfind_14),
                    fontWeight = FontWeight.Bold,
                )
            }
        } else {
            Text(
                text = stringResource(id = R.string.textfind_15),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onJoinQueue,
                enabled = !quickChatState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5069))
            ) {
                Text(
                    stringResource(id = R.string.textfind_16),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview21() {
    DalingKTheme {

    }
}