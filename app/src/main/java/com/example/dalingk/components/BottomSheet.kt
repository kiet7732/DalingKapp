package com.example.dalingk.components

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
import com.example.dalingk.components.ui.theme.DalingKTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dalingk.components.detailUser.GenderSelectionScreen
import com.example.dalingk.components.detailUser.InterestUi
import com.example.dalingk.components.detailUser.IntroFormUI
import com.example.dalingk.components.detailUser.LookingForUi
import data.viewmodel.UserInputViewModel
import kotlinx.coroutines.launch

class BottomSheet : ComponentActivity() {
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

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = sheetState,
            modifier =Modifier.navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content() // Hiển thị nội dung động

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismiss()
                            }
                        }
                    }
                ) {
                    Text("Xác nhận")
                }
            }
        }
    }

    // Đóng BottomSheet ngay khi `isVisible` thay đổi
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    onDismiss()
                }
            }
        }
    }
}






@Preview(showBackground = true)
@Composable
fun GreetingPreview13() {
    DalingKTheme {
        Text("Mở Sở thích")
        Text("Mở Sở thích")
        var showBottomSheet by remember { mutableStateOf(false) }
        var editSection by remember { mutableStateOf<String?>(null) }
        val viewModelUser: UserInputViewModel = viewModel()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                editSection = "interests"
                showBottomSheet = true
            }) {
                Text("Mở Sở thích")
            }
            Button(onClick = {
                editSection = "lookingFor"
                showBottomSheet = true
            }) {
                Text("Mở Mối quan hệ")
            }
            Button(onClick = {
                editSection = "img"
                showBottomSheet = true
            }) {
                Text("Mở img")
            }
            Button(onClick = {
                editSection = "in"
                showBottomSheet = true
            }) {
                Text("Mở nhap")
            }
        }

        EditBottomSheet(
            isVisible = showBottomSheet, // Đặt thành showBottomSheet thay vì true để kiểm soát
            onDismiss = {
                showBottomSheet = false
                editSection = null
            },
            content = {
                when (editSection) {
                    "in" -> IntroFormUI(viewModelUser)
                    "img" -> GenderSelectionScreen(viewModelUser)
                    "interests" -> InterestUi(viewModelUser)
                    "lookingFor" -> LookingForUi(viewModelUser)
                    else -> Text("Không có nội dung để chỉnh sửa", color = Color.White)
                }
            }
        )
    }
}