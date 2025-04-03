package com.example.dalingk.components

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.OutlinedButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.example.dalingk.components.ui.theme.DalingKTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

class MatchNotif : ComponentActivity() {
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
                    GreetingPreview14()
                }
            }
        }
    }
}
@Composable
fun MatchConfetti(
    particleCount: Int = 50 // Giảm số lượng hạt mặc định để tối ưu hiệu suất
) {
    // Định nghĩa hạt confetti
    data class ConfettiParticle(
        var x: Float,
        var y: Float,
        val size: Float,
        val color: Color,
        val shapeIsCircle: Boolean,
        val speed: Float,
        val alpha: Float // Thêm alpha cố định để tránh tạo mới liên tục
    )

    // Khởi tạo danh sách hạt confetti một lần duy nhất
    val confettiParticles = remember {
        List(particleCount) {
            ConfettiParticle(
                x = Random.nextFloat() * 1000f, // Giả sử chiều rộng tối đa ban đầu
                y = Random.nextFloat() * -500f, // Bắt đầu từ trên màn hình
                size = Random.nextFloat() * 15f + 5f,
                color = listOf(
                    Color(0xFFFF4081), // Hồng
                    Color(0xFF536DFE), // Xanh dương
                    Color(0xFFFFD54F), // Vàng
                    Color(0xFF4CAF50), // Xanh lá
                    Color(0xFFE040FB)  // Tím
                ).random(),
                shapeIsCircle = Random.nextBoolean(),
                speed = Random.nextFloat() * 3f + 1f,
                alpha = Random.nextFloat() * 0.7f + 0.3f // Giá trị alpha cố định
            )
        }
    }

    // Tạo hiệu ứng vô hạn với tốc độ hợp lý
    val infiniteTransition = rememberInfiniteTransition()
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing), // Giảm thời gian để mượt hơn
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Cập nhật và vẽ các hạt confetti
        confettiParticles.forEach { particle ->
            // Cập nhật vị trí Y
            particle.y += particle.speed * 5f // Giảm tốc độ nhân để mượt hơn
            if (particle.y > canvasHeight) {
                particle.y = Random.nextFloat() * -500f // Reset về trên cùng
                particle.x = Random.nextFloat() * canvasWidth
            }

            // Vẽ hạt confetti với alpha cố định
            if (particle.shapeIsCircle) {
                drawCircle(
                    color = particle.color,
                    radius = particle.size / 2,
                    center = Offset(particle.x, particle.y),
                    alpha = particle.alpha
                )
            } else {
                drawRect(
                    color = particle.color,
                    topLeft = Offset(particle.x, particle.y),
                    size = Size(particle.size, particle.size),
                    alpha = particle.alpha
                )
            }
        }
    }
}

@Composable
fun MatchNotification(
    showMatchNotification: Boolean,
    matchedUserName: String,
    onDismiss: () -> Unit,
    matchedUserImage: String? = null
) {
    if (showMatchNotification) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 8.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        matchedUserImage?.let {
                            AsyncImage(
                                model = it,
                                contentDescription = "Matched User Image",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFFF4081),
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFFFCDD2), CircleShape)
                                .padding(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "It's a Match!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Bạn và $matchedUserName đã tìm thấy nhau",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Gọi MatchConfetti với số lượng tối ưu
                MatchConfetti(particleCount = 50)
            }
        }

        LaunchedEffect(Unit) {
            delay(1500) // Giữ nguyên thời gian đóng
            onDismiss()
        }
    }
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview14() {
    DalingKTheme {
        MatchNotification(showMatchNotification = true, matchedUserName = "Nguyễn Tuấn kiệt", onDismiss = {})
    }
}