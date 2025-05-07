package com.example.dalingk.screens

import android.content.Context
import android.content.res.Configuration
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
import androidx.compose.ui.res.stringResource
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
import data.model.LanguagePreferences
import data.repository.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import util.ImageLoaderFactory
import java.util.Locale

class AvatarDetailScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DalingKTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
    var editSection by remember { mutableStateOf<String?>(null) } // Thêm biến để xác định nội dung BottomSheet

    LaunchedEffect(currentUserId) {
        currentUserId.let { id ->
            viewModel.fetchUserData(id)
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
                    .size(200.dp)
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
                            .size(180, 180)
                            .precision(Precision.INEXACT)
                            .build()
                    }

                    AsyncImage(
                        model = imageRequest,
                        contentDescription = "Avatar",
                        imageLoader = imageLoader,
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
        }
        Spacer(modifier = Modifier.height(55.dp))

        // Thêm nút chuyển đổi ngôn ngữ
        TextCusADetail(stringResource(id = R.string.textdetail_1), getCurrentLanguage(context), {
            showBottomSheet = true
            editSection = "language"
        })
        TextCusADetail(stringResource(id = R.string.textdetail_2), stringResource(id = R.string.textdetail_5), {
            showBottomSheet = true
            editSection = "other"
        })

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
            Text(text = stringResource(id = R.string.textdetail_3))
        }
    }

    if (showBottomSheet) {
        EditBottomSheet(
            isVisible = showBottomSheet,
            onDismiss = {
                showBottomSheet = false
                editSection = null
            },
            content = {
                when (editSection) {
                    "language" -> LanguageSelectionScreen(context) // Giao diện chọn ngôn ngữ
                    else -> Text("soon coming", color = Color.Black)
                }
            }
        )
    }
}

@Composable
fun LanguageSelectionScreen(context: Context) {
    // Khởi tạo selectedLanguage với giá trị mặc định
    var selectedLanguage by remember { mutableStateOf("vi") } // Giá trị mặc định là "vi"

    // Lấy ngôn ngữ hiện tại từ LanguagePreferences
    val languageFlow = LanguagePreferences.getLanguage(context)
    LaunchedEffect(Unit) {
        languageFlow.collect { lang ->
            selectedLanguage = lang // Cập nhật selectedLanguage khi ngôn ngữ thay đổi
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Chọn ngôn ngữ",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Nút chọn Tiếng Việt
        Button(
            onClick = {
                if (selectedLanguage != "vi") {
                    selectedLanguage = "vi"
                    updateLanguage(context, "vi")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedLanguage == "vi") Color(0xffff5069) else Color.Gray
            )
        ) {
            Text("Tiếng Việt")
        }

        // Nút chọn Tiếng Anh
        Button(
            onClick = {
                if (selectedLanguage != "en") {
                    selectedLanguage = "en"
                    updateLanguage(context, "en")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedLanguage == "en") Color(0xffff5069) else Color.Gray
            )
        ) {
            Text("English")
        }
    }
}

@Composable
fun getCurrentLanguage(context: Context): String {
    val languageFlow = LanguagePreferences.getLanguage(context)
    var language by remember { mutableStateOf("vi") } // Giá trị mặc định

    LaunchedEffect(Unit) {
        languageFlow.collect { lang ->
            language = lang
        }
    }

    return when (language) {
        "vi" -> "Tiếng Việt"
        "en" -> "English"
        else -> "Tiếng Việt"
    }
}

fun updateLanguage(context: Context, languageCode: String) {
    CoroutineScope(Dispatchers.Main).launch {
        LanguagePreferences.saveLanguage(context, languageCode)
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        // Gọi lại activity để áp dụng ngôn ngữ mới
        (context as? ComponentActivity)?.recreate()
    }
}

@Composable
fun TextCusADetail(
    label: String,
    labelControl: String,
    onEditClick: () -> Unit
) {
    Divider(
        color = Color.Gray,
        thickness = 0.6.dp
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onEditClick
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.Black
        )

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = labelControl,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp
            )
        )
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