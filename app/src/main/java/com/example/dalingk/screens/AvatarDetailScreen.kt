package com.example.dalingk.screens

import android.content.Context
import android.os.Bundle
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
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.transformations
import coil3.size.Precision
import coil3.transform.CircleCropTransformation
import com.example.dalingk.R
import com.example.dalingk.components.EditBottomSheet
import com.example.dalingk.components.LottieAnimationCus
import com.example.dalingk.components.detailUser.cities
import com.example.dalingk.navigation.Routes
import data.repository.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import util.ImageLoaderFactory


class AvatarDetailScreen : ComponentActivity() {
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
                    GreetingPreview12()
                }
            }
        }
    }
}


@Composable
fun AvatarDetail(navController: NavController, context: Context) {
    val viewModel: AuthViewModel = viewModel()
    val userData by viewModel.userData.collectAsState()
    val currentUserId = viewModel.auth.currentUser?.uid ?: ""

    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        currentUserId.let { id ->
            viewModel.fetchUserData(id) // Gọi fetchUserData bất kể userData có null hay không
        }
    }

    Log.d("DEBUG", "ID người dùng hiện tại: $currentUserId")

    val imageLoader = remember { ImageLoaderFactory.create(context) }

    val imageUrls: List<String> = userData?.imageUrls ?: emptyList()
    val firstImageUrl = imageUrls.firstOrNull() ?: ""

    LaunchedEffect(userData) {
        Log.d("AvatarDetail", "UserData: $userData")
        Log.d("AvatarDetail", "Image URLs: ${userData?.imageUrls}")
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.TopCenter)
                    .clip(CircleShape)
                    .clickable {
                        navController.navigate(Routes.Profile) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                    .border(
                        width = 5.dp,
                        color = Color(0xffff5069),
                        shape = CircleShape
                    )
            ) {
                if (firstImageUrl.isNotEmpty()) {
                    val imageRequest = remember(firstImageUrl) {
                        ImageRequest.Builder(context)
                            .data(firstImageUrl)
                            .crossfade(true)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .transformations(CircleCropTransformation())
                            .size(220, 220)
                            .precision(Precision.INEXACT)
                            .build()
                    }

                    AsyncImage(
                        model = imageRequest,
                        contentDescription = "Avatar",
                        imageLoader = imageLoader, // Sử dụng ImageLoader tùy chỉnh
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_error),
                        contentDescription = "Default Avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

//            Image(
//                imageVector = Icons.Filled.Edit,
//                contentDescription = "Edit Avatar",
//                modifier = Modifier
//                    .size(42.dp)
//                    .padding(4.dp)
//                    .align(Alignment.TopCenter)
//                    .offset(x = 45.dp, y = -5.dp)
//            )

            LottieAnimationCus(
                jsonFileName = R.raw.i_anim,
                modifier = Modifier
                    .size(72.dp)
                    .padding(4.dp)
                    .align(Alignment.TopCenter)
                    .offset(x = 70.dp, y = -5.dp)
            )

        }
        Spacer(modifier = Modifier.height(25.dp))

        TextCusADetail("Ngôn ngữ", "Tiếng Việt", {
            showBottomSheet = true
        })
        TextCusADetail("Chế độ", "Sáng", {})

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    UserPreferences.clearUserId(context)
                    UserPreferences.clearAuthToken(context)

                }
                viewModel.logout()
                navController.navigate(Routes.TrailerScreen) {
                    popUpTo(0) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            Text(text = "Đăng Xuất")
        }
    }

//    DisposableEffect(Unit) {
//        onDispose {
//            imageLoader.memoryCache?.clear() // Xóa bộ nhớ đệm khi rời màn hình
//        }
//    }

    if (showBottomSheet) {
        EditBottomSheet(
            isVisible = true,
            onDismiss = { showBottomSheet = false }, // Đóng BottomSheet
            content = {

            }
        )
    }

}


@Composable
fun TextCusADetail(
    label: String,
    labelControl: String,
    onEditClick: () -> Unit
) {
    Divider(
        color = Color.Gray, // Màu của đường kẻ
        thickness = 0.6.dp   // Độ dày của đường kẻ
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Tắt hiệu ứng nhấn
                onClick = onEditClick
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Hiển thị nhãn (Label)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.weight(1f)) // Tạo khoảng cách linh hoạt giữa "Ngôn ngữ" và "Tiếng Việt"
        Text(
            text = labelControl,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp
            )
        )
        // Biểu tượng chỉnh sửa
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            Modifier.size(35.dp),
            tint = Color.Red
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview12() {
    DalingKTheme {
        val context = LocalContext.current
        val navController = rememberNavController()
        AvatarDetail(navController, context)
    }
}