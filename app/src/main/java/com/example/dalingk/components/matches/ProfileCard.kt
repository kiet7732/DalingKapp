package com.example.dalingk.components.matches

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dalingk.ui.theme.DalingKTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.dalingk.R
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import data.repository.AuthViewModel.UserData

class ProfileCard : ComponentActivity() {
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
                    ProfileCardPreview()
                }
            }
        }
    }
}

@Composable
fun ProfileCard(userData: UserData, modifier: Modifier = Modifier) {
    var currentImageIndex by remember { mutableStateOf(0) }
    val hasImages = userData.imageUrls.isNotEmpty()
    val context = LocalContext.current
    Card(
        modifier = modifier
            .width(420.dp)
            .height(580.dp)
            .clickable {
                if (hasImages) {
                    currentImageIndex = (currentImageIndex + 1) % userData.imageUrls.size
                }
            },
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasImages) {
                val imageUrl = userData.imageUrls.getOrNull(currentImageIndex) ?: ""
                val imageRequest = remember(imageUrl) {
                    ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .size(420, 580) // Resize ảnh theo kích thước khung
                        .precision(Precision.INEXACT) // Giảm độ chính xác để tiết kiệm bộ nhớ
                        .build()
                }

                AsyncImage(
                    model = imageRequest,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_error),
                    error = painterResource(id = R.drawable.ic_error)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_error),
                    contentDescription = "No image available",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 650f,
                            endY = 1240f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = userData.fullName.ifEmpty { "Unknown" },
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (userData.age > 0) userData.age.toString() else "N/A",
                        color = Color.White,
                        fontSize = 24.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = userData.location.ifEmpty { "Unknown" }.uppercase(),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // Xóa bộ nhớ đệm khi composable bị hủy
    DisposableEffect(Unit) {
        onDispose {
            val imageLoader = ImageLoader(context)
            imageLoader.memoryCache?.clear() // Xóa bộ nhớ đệm ảnh
        }
    }
}


@Preview
@Composable
fun ProfileCardPreview() {
    ProfileCard(

    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview5() {
    DalingKTheme {

    }
}