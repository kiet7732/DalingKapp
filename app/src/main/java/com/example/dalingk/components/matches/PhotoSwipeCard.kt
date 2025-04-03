package com.example.dalingk.components.matches

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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import data.repository.AuthViewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.example.dalingk.components.MatchNotification
import kotlin.math.min

class PhotoSwipeCard : ComponentActivity() {
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
                    GreetingPreview3()
                }
            }
        }
    }
}


@Composable
fun SwipeScreen(
    profiles: List<AuthViewModel.UserData>,
    viewModel: AuthViewModel
) {
    var currentProfileIndex by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    val offsetAnimation = remember { Animatable(0f) }
    val rotationAnimation = remember { Animatable(0f) }
    val maxSwipeDistance = 300f

//    var showMatchNotification by remember { mutableStateOf(false) }
//    var matchedUserName by remember { mutableStateOf("") }

    // Khi danh sách profiles thay đổi, reset lại index
    LaunchedEffect(profiles) {
        Log.d("DEBUG", "Cập nhật danh sách profiles: ${profiles.size}")
        if (profiles.isNotEmpty()) {
            currentProfileIndex = 0
        }
    }

    fun resetSwipeWithAnimation() {
        coroutineScope.launch {
            offsetAnimation.animateTo(0f, tween(150))
            rotationAnimation.animateTo(0f, tween(150))
        }
    }

    val showMatchNotification by viewModel.showMatchNotification.collectAsState()
    val matchedUserName by viewModel.matchedUserName.collectAsState()

    fun performSwipe(isLike: Boolean) {
        coroutineScope.launch {
            offsetAnimation.snapTo(if (isLike) 50f else -50f)
            rotationAnimation.snapTo(if (isLike) 10f else -10f)
            delay(100)

            val targetOffset = if (isLike) 1000f else -1000f
            offsetAnimation.animateTo(targetOffset, tween(200))
            delay(200)

            val currentProfile = profiles.getOrNull(currentProfileIndex)
            currentProfile?.let {
                val targetUserId = it.userId
                if (isLike) {
                    Log.d("DEBUG", "Liking user: $targetUserId")

                    val isMatched = viewModel.like(targetUserId,it.fullName) //xy ly like macth va shownotif

                    Log.d("DEBUG", "Is matched: $isMatched")
                } else {
                    viewModel.dislike(targetUserId)
                }
            }

            if (profiles.isNotEmpty() && currentProfileIndex < profiles.size - 1) {
                currentProfileIndex++
            } else {
                currentProfileIndex = 0
                viewModel.loadNewProfiles()
            }

            resetSwipeWithAnimation()
        }
    }


    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .padding(5.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetAnimation.value > maxSwipeDistance -> performSwipe(isLike = true)
                                offsetAnimation.value < -maxSwipeDistance -> performSwipe(isLike = false)
                                else -> resetSwipeWithAnimation()
                            }
                        }
                    ) { _, dragAmount ->
                        coroutineScope.launch {
                            offsetAnimation.snapTo(offsetAnimation.value + dragAmount)
                            rotationAnimation.snapTo(15f * (offsetAnimation.value / maxSwipeDistance))
                        }
                    }
                }
        ) {
            if (offsetAnimation.value > maxSwipeDistance / 2) {
                Text(
                    text = "LIKE",
                    color = Color.Green,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .graphicsLayer {
                            alpha = min(
                                1f,
                                (offsetAnimation.value - maxSwipeDistance / 2) / (maxSwipeDistance / 2)
                            )
                            rotationZ = -10f
                        }
                )
            } else if (offsetAnimation.value < -maxSwipeDistance / 2) {
                Text(
                    text = "DISLIKE",
                    color = Color.Red,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .graphicsLayer {
                            alpha = min(
                                1f,
                                (-offsetAnimation.value - maxSwipeDistance / 2) / (maxSwipeDistance / 2)
                            )
                            rotationZ = 10f
                        }
                )
            }

            if (profiles.isNotEmpty() && currentProfileIndex in profiles.indices) {
                ProfileCard(
                    userData = profiles[currentProfileIndex],
                    modifier = Modifier
                        .graphicsLayer(
                            rotationZ = rotationAnimation.value,
                            translationX = offsetAnimation.value
                        )
                        .fillMaxWidth()
                )

            } else {
                Text("Không còn hồ sơ nào!", fontSize = 20.sp, color = Color.Gray)
            }
        }

        Log.d("DEBUG", "showMatchNotification==========: $showMatchNotification")
        // Hiển thị thông báo match
        if (showMatchNotification) {
            MatchNotification(
                showMatchNotification = true,
                matchedUserName = matchedUserName,
                onDismiss = { viewModel.dismissMatchNotification() }
            )
        }

        ActionButtonsGroup(
            onDislikeClick = { performSwipe(isLike = false) },
            onLikeClick = { performSwipe(isLike = true) },
            onSuperLikeClick = { }
        )
    }
}


//val profiles = listOf(
//
//)

@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    DalingKTheme {
//        SwipeScreen(profiles = profiles)
    }
}