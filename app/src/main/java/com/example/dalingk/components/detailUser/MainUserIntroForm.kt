package com.example.dalingk.components.detailUser

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dalingk.ui.theme.DalingKTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dalingk.FullScreenLoading
import com.example.dalingk.R
import com.example.dalingk.navigation.Routes
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import data.viewmodel.UserInputViewModel
import kotlinx.coroutines.delay
import util.FileUtil

import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import data.repository.AuthViewModel

class MainUserIntroForm : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DalingKTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    UserIntroFormUI()
                }
            }
        }
    }
}


//navController: NavController
@Composable
fun ArrowScreen(
    navController: NavController,
    context: Context,
    viewModel: UserInputViewModel,
    authViewModel: AuthViewModel
) {
    val database = FirebaseDatabase.getInstance()
    val currentScreen by viewModel.currentScreen
    val progress by remember { derivedStateOf { viewModel.progress } }
    val upLoading by viewModel.isLoading

    // Trạng thái để hiển thị lỗi qua Snackbar
    var saveStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        authViewModel.checkCurrentUserDataExists(onResult = { route ->
            if (route == Routes.InputDetail) {
                saveStatus = "Vì bạn chưa nhập thông tin \n" + "Nên hãy nhập thông tin để macth nhé"
            }
        }, onError = { error ->
            Log.e("AppNavigation", "Lỗi kiểm tra dữ liệu: $error")
        })
    }

    // Tự động ẩn Snackbar sau 2 giây
    LaunchedEffect(saveStatus) {
        if (saveStatus != null) {
            delay(4000L) // Chờ 4 giây
            saveStatus = null // Ẩn Snackbar
        }
    }

    var showExitDialog by remember { mutableStateOf(false) }

    // Nếu showExitDialog = true, hiển thị hộp thoại cảnh báo
    if (showExitDialog) {
        AlertDialog(onDismissRequest = { showExitDialog = false },
            properties = DialogProperties(dismissOnClickOutside = false), // Không tắt khi nhấn ra ngoài
            shape = RoundedCornerShape(16.dp), // Bo góc đẹp hơn
            containerColor = Color.White, // Nền trắng mềm mại
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cảnh báo!", style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold, color = Color(0xFFE53935)
                        )
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nếu bạn thoát, thông tin đã nhập sẽ không được lưu nhưng tài khoản và mật khẩu sẽ được lưu. \nBạn có chắc chắn muốn thoát không?",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        navController.navigate(Routes.TrailerScreen)
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Thoát", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExitDialog = false }, border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text("Hủy", color = Color.Gray)
                }
            })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF5F5))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues()),
        ) {
            LineBig(progress) {
                if (currentScreen == UserInputViewModel.Screen.INTRO_FORM) {
                    showExitDialog = true // Hiển thị hộp thoại cảnh báo
                } else {
                    viewModel.currentScreen.value = when (currentScreen) {
                        UserInputViewModel.Screen.LOCATION -> UserInputViewModel.Screen.INTRO_FORM
                        UserInputViewModel.Screen.GENDER -> UserInputViewModel.Screen.LOCATION
                        UserInputViewModel.Screen.LOOKING_FOR -> UserInputViewModel.Screen.GENDER
                        UserInputViewModel.Screen.INTEREST -> UserInputViewModel.Screen.LOOKING_FOR
                        UserInputViewModel.Screen.UPPHOTO -> UserInputViewModel.Screen.INTEREST
                        else -> currentScreen
                    }
                }
            }

            Spacer(modifier = Modifier.height(70.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (currentScreen) {
                    UserInputViewModel.Screen.INTRO_FORM -> IntroFormUI(viewModel)
                    UserInputViewModel.Screen.LOCATION -> Location(viewModel)
                    UserInputViewModel.Screen.GENDER -> GenderSelectionScreen(viewModel)
                    UserInputViewModel.Screen.LOOKING_FOR -> LookingForUi(viewModel)
                    UserInputViewModel.Screen.INTEREST -> InterestUi(viewModel)
                    UserInputViewModel.Screen.UPPHOTO -> UpPhotoUi(viewModel, context)
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(WindowInsets.systemBars.asPaddingValues()),
                ) {
                    FrameLine()
                    CircularIconButton(
                        modifier = Modifier.padding(start = 291.dp),
                        onClick = {
                            if (currentScreen == UserInputViewModel.Screen.UPPHOTO) {
                                val userId =
                                    UserPreferences.getUserId(context) ?: return@CircularIconButton
                                Log.d(
                                    "ArrowScreen",
                                    "Bắt đầu xử lý ảnh để lưu lên Firebase cho userId: $userId"
                                )

                                val localUris = viewModel.photoUrls.value
                                val uploadedUrls = MutableList(6) { "" }
                                var uploadCount = localUris.count {
                                    it.isNotEmpty() && (it.startsWith("content://") || it.startsWith(
                                        "file://"
                                    ))
                                }
                                var completedUploads = 0

                                if (uploadCount == 0) {
                                    viewModel.saveToFirebase(database, userId, onSuccess = {
                                        Log.d(
                                            "ArrowScreen",
                                            "Lưu dữ liệu lên Firebase thành công (không cần tải ảnh)"
                                        )
                                        navController.navigate(Routes.TrailerScreen)
                                    }, onError = { error ->
                                        Log.d("ArrowScreen", "Lỗi khi lưu lên Firebase: $error")
                                        saveStatus =
                                            "Lỗi khi lưu dữ liệu: $error" // Cập nhật trạng thái lỗi
                                    })
                                } else {
                                    localUris.forEachIndexed { index, uri ->
                                        if (uri.isNotEmpty() && (uri.startsWith("content://") || uri.startsWith(
                                                "file://"
                                            ))
                                        ) {
                                            val filePath = FileUtil.getPath(
                                                context, android.net.Uri.parse(uri)
                                            )
                                            if (filePath != null) {
                                                viewModel.uploadPhotoToCloudinary(filePath,
                                                    onSuccess = { url ->
                                                        uploadedUrls[index] = url
                                                        completedUploads++
                                                        if (completedUploads == uploadCount) {
                                                            viewModel.photoUrls.value = uploadedUrls
                                                            viewModel.saveToFirebase(database,
                                                                userId,
                                                                onSuccess = {
                                                                    Log.d(
                                                                        "ArrowScreen",
                                                                        "Lưu dữ liệu lên Firebase thành công"
                                                                    )
                                                                    navController.navigate(Routes.TrailerScreen)
                                                                },
                                                                onError = { error ->
                                                                    Log.d(
                                                                        "ArrowScreen",
                                                                        "Lỗi khi lưu lên Firebase: $error"
                                                                    )
                                                                    saveStatus =
                                                                        "Lỗi khi lưu dữ liệu: $error" // Cập nhật trạng thái lỗi
                                                                })
                                                        }
                                                    },
                                                    onError = { error ->
                                                        Log.d(
                                                            "ArrowScreen",
                                                            "Lỗi khi tải ảnh lên Cloudinary: $error"
                                                        )
                                                        saveStatus =
                                                            "Lỗi khi tải ảnh: $error" // Cập nhật trạng thái lỗi
                                                    })
                                            }
                                        } else {
                                            uploadedUrls[index] = uri
                                        }
                                    }
                                }
                            } else {
                                viewModel.currentScreen.value = when (currentScreen) {
                                    UserInputViewModel.Screen.INTRO_FORM -> UserInputViewModel.Screen.LOCATION
                                    UserInputViewModel.Screen.LOCATION -> UserInputViewModel.Screen.GENDER
                                    UserInputViewModel.Screen.GENDER -> UserInputViewModel.Screen.LOOKING_FOR
                                    UserInputViewModel.Screen.LOOKING_FOR -> UserInputViewModel.Screen.INTEREST
                                    UserInputViewModel.Screen.INTEREST -> UserInputViewModel.Screen.UPPHOTO
                                    UserInputViewModel.Screen.UPPHOTO -> UserInputViewModel.Screen.UPPHOTO
                                }
                            }
                        },
                        icon = Icons.Default.ArrowForward,
                        contentDescription = "Forward",
                        backgroundColor = Color(0xffff5069),
                        iconTint = Color.White
                    )
                }
            }
        }

        // Hiển thị Snackbar ở dưới cùng màn hình
        if (saveStatus != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .padding(WindowInsets.systemBars.asPaddingValues()),
            ) {
                Snackbar(action = {
//                        TextButton(onClick = { saveStatus = null }) {
//                            Text("OK", color = Color.White)
//                        }
                }) {
                    Text(saveStatus ?: "", fontSize = 16.sp)
                }
            }
        }

        // Hiển thị FullScreenLoading nếu đang tải
        if (upLoading) {
            FullScreenLoading(isLoading = upLoading)
        }
    }
}


// nut chon
@Composable
fun CircularIconButton(
    modifier: Modifier = Modifier, // Đặt giá trị mặc định
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String? = null,
    backgroundColor: Color = Color.Transparent,
    iconTint: Color = Color.White,
    buttonSize: Dp = 56.dp,
    iconSize: Dp = 24.dp,
    isFlipped: Boolean = false // Thêm tùy chọn để lật
) {
    IconButton(
        onClick = onClick, modifier = modifier // Sử dụng modifier truyền vào
            .size(buttonSize)
            .background(
                color = backgroundColor, shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer(
                    scaleX = if (isFlipped) -1f else 1f // Lật theo trục X
                )
        )
    }
}

// dương o o tren
@Composable
fun LineBig(progress: Float, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        CircularIconButton(
            onClick = onBackClick,
            icon = Icons.Default.ArrowForward,
            contentDescription = "Back",
            iconTint = Color.Black,
            isFlipped = true
        )
        Box(
            modifier = Modifier
                .requiredWidth(190.dp)
                .requiredHeight(8.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color.LightGray)
                .align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFFF5069))
            )
        }
    }
}


@Composable
fun FrameLine(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.line),
        contentDescription = "Mask group",
        modifier = modifier
            .requiredWidth(width = 400.dp)
            .requiredHeight(height = 198.dp)
    )
}


@Preview(showBackground = true)
@Composable
fun UserIntroFormUI() {
    DalingKTheme {
//        ArrowScreen(
//        )
    }
}
