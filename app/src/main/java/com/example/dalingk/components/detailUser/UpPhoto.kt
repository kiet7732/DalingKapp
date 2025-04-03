package com.example.dalingk.components.detailUser

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dalingk.ui.theme.DalingKTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import coil3.compose.AsyncImage
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import com.example.dalingk.R
import data.viewmodel.UserInputViewModel
import util.ImageUtils


class UpPhoto : ComponentActivity() {
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
                    GreetingPreview()
                }
            }
        }
    }
}


// Function to update the image state
fun updateImage(images: MutableState<List<Uri?>>, index: Int, uri: Uri) {
    images.value = images.value.toMutableList().apply {
        this[index] = uri
    }
}

@Composable
fun ReusableFrame(
    width: Dp,
    height: Dp,
    imageSize: Dp = 30.dp,
    borderColor: Color = Color(0xffff5069),
    imageUri: Uri? = null, // Pass the image URI
    modifier: Modifier = Modifier,
    onClick: () -> Unit, // Click listener for uploading
    onCancelClick: () -> Unit = {} // Callback for cancel action
) {
    Column(
        modifier = modifier
            .padding(5.dp)
            .clip(shape = RoundedCornerShape(16.dp)), // Padding for the entire box
    ) {
        Box(
            modifier = modifier
                .requiredWidth(width)
                .requiredHeight(height)
                .clickable { onClick() }, // Click to trigger image upload
            contentAlignment = Alignment.Center // Center the content
        ) {
            // Background box with border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape = RoundedCornerShape(16.dp))
                    .background(color = Color.White)
                    .border(
                        border = BorderStroke(1.2.dp, borderColor),
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            if (imageUri != null) {
                // Image content
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop // Ensure the image fills the box
                )

                // Cancel icon positioned at top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd) // Position at top-right corner
                        .padding(4.dp) // Small padding from edge
                        .clip(RoundedCornerShape(50)) // Circular shape for the cancel button
                        .background(Color.White.copy(alpha = 0.8f)) // Semi-transparent white background
                        .requiredSize(imageSize + 4.dp) // Slightly larger than the icon for padding
                        .clickable { onCancelClick() } // Handle cancel click
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_cancel),
                        contentDescription = "Cancel Icon",
                        colorFilter = ColorFilter.tint(borderColor),
                        modifier = Modifier
                            .align(Alignment.Center) // Center the icon in the circular background
                            .requiredSize(imageSize)
                    )
                }
            } else {
                // Plus icon when no image is present
                Image(
                    painter = painterResource(id = R.drawable.iconplus),
                    contentDescription = "Plus Icon",
                    colorFilter = ColorFilter.tint(borderColor),
                    modifier = Modifier
                        .requiredSize(imageSize) // Image size
                )
            }
        }
    }
}


// Up Photo to device user    end

@Composable
fun UpPhotoUi(viewModel: UserInputViewModel, context: Context) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = "Upload Your Photo",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "We'd love to see you. Up a photo for you dating journey ",
            style = TextStyle(
                fontSize = 16.sp,
                color = Color.Gray
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(280.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        UpPhotoUser(viewModel, context)
    }
}

@Composable
fun UpPhotoUser(viewModel: UserInputViewModel, context: Context) {
    var showSuccess by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Khởi tạo localPhotoUris với 6 phần tử, lấy từ viewModel.photoUrls.value
    var localPhotoUris by remember {
        mutableStateOf(
            List(6) { index ->
                val photoUrls = viewModel.photoUrls.value
                if (index < photoUrls.size && photoUrls[index].isNotEmpty()) Uri.parse(photoUrls[index]) else null
            }
        )
    }

    // Log trạng thái ban đầu
    Log.d("UpPhotoUser", "Khởi tạo localPhotoUris: ${localPhotoUris.map { it?.toString() ?: "null" }}")

    // Đồng bộ localPhotoUris với viewModel.photoUrls.value khi localPhotoUris thay đổi
    LaunchedEffect(localPhotoUris) {
        val updatedPhotoUrls = MutableList(6) { "" }
        localPhotoUris.forEachIndexed { index, uri ->
            updatedPhotoUrls[index] = uri?.toString() ?: ""
        }
        viewModel.photoUrls.value = updatedPhotoUrls
        Log.d("UpPhotoUser", "Đồng bộ photoUrls từ localPhotoUris: ${viewModel.photoUrls.value}")
    }

    // Đồng bộ ngược từ viewModel.photoUrls.value khi nó thay đổi từ bên ngoài
    LaunchedEffect(viewModel.photoUrls.value) {
        val photoUrls = viewModel.photoUrls.value
        val currentUris = List(6) { index ->
            if (index < photoUrls.size && photoUrls[index].isNotEmpty()) Uri.parse(photoUrls[index]) else null
        }
        if (currentUris != localPhotoUris) {
            localPhotoUris = currentUris
            Log.d("UpPhotoUser", "Đồng bộ localPhotoUris từ viewModel.photoUrls: ${localPhotoUris.map { it?.toString() ?: "null" }}")
        }
    }

    // Tạo danh sách launcher cho 6 khung ảnh
    val launchers = List(6) { index ->
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                Log.d("UpPhotoUser", "Ảnh được chọn cho ô $index: $uri")
                val compressedFile = ImageUtils.compressImageToFile(context, it, maxSizeKB = 150)
                val compressedUri = Uri.fromFile(compressedFile)
                localPhotoUris = localPhotoUris.toMutableList().apply { this[index] = compressedUri }
                Log.d("UpPhotoUser", "Cập nhật localPhotoUris sau khi thêm ảnh vào ô $index: ${localPhotoUris.map { it?.toString() ?: "null" }}")
            }
        }
    }

    // Hiển thị thông báo khi tải ảnh thành công
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            snackbarHostState.showSnackbar("Tải ảnh lên thành công!")
            showSuccess = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Khung ảnh lớn (ô 0)
                ReusableFrame(
                    width = 206.dp,
                    height = 210.dp,
                    imageUri = localPhotoUris[0],
                    onClick = {
                        Log.d("UpPhotoUser", "Bấm vào ô 0 để tải ảnh")
                        launchers[0].launch("image/*")
                    },
                    onCancelClick = {
                        Log.d("UpPhotoUser", "Bấm X để xóa ảnh tại ô 0")
                        localPhotoUris = localPhotoUris.toMutableList().apply { this[0] = null }
                        Log.d("UpPhotoUser", "Cập nhật localPhotoUris sau khi xóa ô 0: ${localPhotoUris.map { it?.toString() ?: "null" }}")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Hai khung ảnh nhỏ (ô 1 và 2)
                    for (i in 1..2) {
                        ReusableFrame(
                            width = 100.dp,
                            height = 100.dp,
                            imageUri = localPhotoUris[i],
                            onClick = {
                                Log.d("UpPhotoUser", "Bấm vào ô $i để tải ảnh")
                                launchers[i].launch("image/*")
                            },
                            onCancelClick = {
                                Log.d("UpPhotoUser", "Bấm X để xóa ảnh tại ô $i")
                                localPhotoUris = localPhotoUris.toMutableList().apply { this[i] = null }
                                Log.d("UpPhotoUser", "Cập nhật localPhotoUris sau khi xóa ô $i: ${localPhotoUris.map { it?.toString() ?: "null" }}")
                            }
                        )
                        if (i == 1) Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                // Ba khung ảnh nhỏ (ô 3, 4, 5)
                for (i in 3..5) {
                    ReusableFrame(
                        width = 100.dp,
                        height = 100.dp,
                        imageUri = localPhotoUris[i],
                        onClick = {
                            Log.d("UpPhotoUser", "Bấm vào ô $i để tải ảnh")
                            launchers[i].launch("image/*")
                        },
                        onCancelClick = {
                            Log.d("UpPhotoUser", "Bấm X để xóa ảnh tại ô $i")
                            localPhotoUris = localPhotoUris.toMutableList().apply { this[i] = null }
                            Log.d("UpPhotoUser", "Cập nhật localPhotoUris sau khi xóa ô $i: ${localPhotoUris.map { it?.toString() ?: "null" }}")
                        }
                    )
                    if (i < 5) Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


@Composable
fun ReusableFrameUser(
    width: Dp,
    height: Dp,
    imageSize: Dp = 30.dp,
    borderColor: Color = Color(0xffff5069),
    imageUri: Uri? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(5.dp)
            .clip(shape = RoundedCornerShape(16.dp)),
    ) {
        Box(
            modifier = modifier
                .requiredWidth(width)
                .requiredHeight(height),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape = RoundedCornerShape(16.dp))
                    .background(color = Color.White)
                    .border(
                        border = BorderStroke(1.2.dp, borderColor),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
            if (imageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .size(width.toPx().toInt(), height.toPx().toInt()) // Giới hạn kích thước
                        .crossfade(true)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .size(
                            width.toPx().toInt(),
                            height.toPx().toInt()
                        ) // Resize ảnh theo kích thước khung
                        .precision(Precision.INEXACT) // Giảm độ chính xác để tiết kiệm bộ nhớ
                        .build(),
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.iconplus),
                    contentDescription = "Plus Icon",
                    colorFilter = ColorFilter.tint(borderColor),
                    modifier = Modifier.requiredSize(imageSize)
                )
            }
        }
    }
}

@Composable
fun Dp.toPx(): Float = with(LocalDensity.current) { this@toPx.toPx() }



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DalingKTheme {
        val viewModelUser: UserInputViewModel = viewModel()
        val context = LocalContext.current
        UpPhotoUi(viewModelUser, context)
    }
}