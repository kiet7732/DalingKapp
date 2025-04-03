package com.example.dalingk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import com.example.dalingk.components.ui.theme.DalingKTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

class Loading : ComponentActivity() {
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
                    FullScreenLoading(true)
                }
            }
        }
    }
}

@Composable
fun FullScreenLoading(isLoading: Boolean) {

    var showText by remember { mutableStateOf(false) }

    // Khi `isLoading` trở thành true, bắt đầu đếm 3.5s để hiển thị văn bản
    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(3500) // Chờ 3.5 giây
            showText = true
        } else {
            showText = false // Reset khi `isLoading` trở về false
        }
    }

    if (isLoading) {
        Dialog(
            onDismissRequest = {}, // Không cho phép tắt
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "loading_rotation")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_loading),
                    contentDescription = "Loading",
                    tint = Color.White,
                    modifier = Modifier
                        .size(60.dp)
                        .rotate(rotation)
                )
                // Chỉ hiển thị text sau 3.5s
                if (showText) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Vui lòng chờ trong giây lát...",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SuccessMessage(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    message: String = "Thành công"
) {
    // Tự động ẩn sau 1 giây
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(1000L) // Chờ 1 giây
            onDismiss() // Gọi hàm để ẩn thông báo
        }
    }

    if (isVisible) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.medium)
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview10() {
    DalingKTheme {
        Text("Mở Mối quan hệ")
        Text("Mở Mối quan hệ")
        FullScreenLoading(true)
    }
}