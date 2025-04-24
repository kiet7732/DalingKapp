package com.example.dalingk.components.detailUser

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.example.dalingk.FullScreenLoading
import com.example.dalingk.navigation.Routes
import com.example.dalingk.components.EditBottomSheet
import com.example.dalingk.ui.theme.DalingKTheme
import com.google.firebase.database.FirebaseDatabase
import data.repository.AuthViewModel
import data.viewmodel.UserInputViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import util.FileUtil

enum class EditSection {
    INTRO_FORM, GENDER, LOOKING_FOR, INTEREST, UPPHOTO, LOCATION
}

@Composable
fun ProfileScreenU(navController: NavController, context: Context) {
    val authViewModel: AuthViewModel = viewModel()
    val userInputViewModel: UserInputViewModel = viewModel()
    val userData by authViewModel.userData.collectAsState()
    val currentUserId = authViewModel.auth.currentUser?.uid ?: ""
    var showBottomSheet by remember { mutableStateOf(false) }
    var editSection by remember { mutableStateOf<EditSection?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var saveStatus by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var lastClickTime by remember { mutableStateOf(0L) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty() && userData == null) {
            authViewModel.fetchUserData(currentUserId)
        }
    }

    // Hàm đồng bộ dữ liệu bất đồng bộ
    suspend fun syncDataForEdit(section: EditSection) {
        userData?.let { data ->
            when (section) {
                EditSection.INTRO_FORM -> {
                    userInputViewModel.fullName.value = data.fullName ?: ""
                    val birthdayParts = data.birthday?.split("-") ?: emptyList()
                    if (birthdayParts.size == 3) {
                        userInputViewModel.birthDay.value = birthdayParts[0]
                        userInputViewModel.birthMonth.value = birthdayParts[1]
                        userInputViewModel.birthYear.value = birthdayParts[2]
                    } else {
                        userInputViewModel.birthDay.value = ""
                        userInputViewModel.birthMonth.value = ""
                        userInputViewModel.birthYear.value = ""
                    }
                }

                EditSection.LOCATION -> userInputViewModel.location.value = data.location
                EditSection.GENDER -> userInputViewModel.gender.value = data.gender
                EditSection.LOOKING_FOR -> userInputViewModel.lookingFor.value = data.lookingFor
                EditSection.INTEREST -> userInputViewModel.interests.value =
                    data.interests ?: emptyList()

                EditSection.UPPHOTO -> {
                    val imageUrls = data.imageUrls ?: List(6) { "" }
                    userInputViewModel.photoUrls.value = imageUrls
                }
            }
        }
    }

    // Hàm kiểm tra xem dữ liệu có thay đổi hay không
    fun hasDataChanged(section: EditSection): Boolean {
        userData?.let { data ->
            return when (section) {
                EditSection.INTRO_FORM -> {
                    val currentFullName = userInputViewModel.fullName.value
                    val currentBirthday = userInputViewModel.getFormattedBirthday()
                    currentFullName != (data.fullName ?: "") || currentBirthday != (data.birthday
                        ?: "")
                }

                EditSection.LOCATION -> userInputViewModel.location.value != data.location
                EditSection.GENDER -> userInputViewModel.gender.value != data.gender
                EditSection.LOOKING_FOR -> userInputViewModel.lookingFor.value != data.lookingFor
                EditSection.INTEREST -> {
                    val currentInterests = userInputViewModel.interests.value
                    val originalInterests = data.interests ?: emptyList<String>()
                    !currentInterests.equals(originalInterests)
                }

                EditSection.UPPHOTO -> userInputViewModel.photoUrls.value != (data.imageUrls
                    ?: List(6) { "" })
            }
        }
        return false
    }

    val imageUrls = remember(userData) { userData?.imageUrls ?: emptyList() }
    val scrollState = rememberScrollState()

    // Giao diện
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF5F5F5), Color(0xFFE0E0E0))
                )
            )
            .padding(WindowInsets.systemBars.asPaddingValues())
            .verticalScroll(scrollState)
    ) {
        // Thanh tiêu đề
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.currentBackStackEntry?.savedStateHandle?.set(Routes.DetailU, 3)
                    navController.navigate(Routes.MainMatch)
                },
                modifier = Modifier
                    .size(48.dp)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Hồ Sơ Của Bạn",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        }

        // Danh sách ảnh (bao gồm ảnh chính và 5 ảnh phụ)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Ảnh chính (ô đầu tiên)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                if (imageUrls.isNotEmpty() && imageUrls[0].isNotEmpty()) {
                    AsyncImage(
                        model = imageUrls[0],
                        contentDescription = "Ảnh hồ sơ chính",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Chưa có ảnh", color = Color.Gray, fontSize = 16.sp)
                    }
                }
            }

            // Hàng 1: 3 ô ảnh phụ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(start = 4.dp, end = 4.dp)
                    ) {
                        val url = imageUrls.getOrNull(index + 1) ?: ""
                        if (url.isNotEmpty()) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Ảnh hồ sơ phụ ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Hàng 2: 2 ô ảnh phụ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(2) { index ->
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(start = 4.dp, end = 4.dp)
                    ) {
                        val url = imageUrls.getOrNull(index + 4) ?: "" // Bắt đầu từ ảnh thứ 4 (index 3 + 1)
                        if (url.isNotEmpty()) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Ảnh hồ sơ phụ ${index + 4}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Nút chỉnh sửa ảnh
        ProfileCard(
            title = "Ảnh Hồ Sơ",
            onEditClick = {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > 500 && !isLoading && !isSaving) {
                    lastClickTime = currentTime
                    coroutineScope.launch {
                        isLoading = true
                        syncDataForEdit(EditSection.UPPHOTO)
                        editSection = EditSection.UPPHOTO
                        showBottomSheet = true
                        isLoading = false
                    }
                }
            }
        )

        // Thông tin cá nhân
        ProfileCard(
            title = "Họ và Tên",
            content = userData?.fullName ?: "Chưa cập nhật",
            onEditClick = {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > 500 && !isLoading && !isSaving) {
                    lastClickTime = currentTime
                    coroutineScope.launch {
                        isLoading = true
                        syncDataForEdit(EditSection.INTRO_FORM)
                        editSection = EditSection.INTRO_FORM
                        showBottomSheet = true
                        isLoading = false
                    }
                }
            }
        )

        ProfileCard(
            title = "Địa Chỉ",
            content = userData?.location ?: "Chưa cập nhật",
            onEditClick = {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > 500 && !isLoading && !isSaving) {
                    lastClickTime = currentTime
                    coroutineScope.launch {
                        isLoading = true
                        syncDataForEdit(EditSection.LOCATION)
                        editSection = EditSection.LOCATION
                        showBottomSheet = true
                        isLoading = false
                    }
                }
            }
        )

        ProfileCard(
            title = "Giới Tính",
            content = userData?.gender ?: "Chưa cập nhật",
            onEditClick = {
                saveStatus = "Không thể đổi giới tính"
            }
        )

        ProfileCard(
            title = "Sở Thích",
            content = null,
            onEditClick = {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > 500 && !isLoading && !isSaving) {
                    lastClickTime = currentTime
                    coroutineScope.launch {
                        isLoading = true
                        syncDataForEdit(EditSection.INTEREST)
                        editSection = EditSection.INTEREST
                        showBottomSheet = true
                        isLoading = false
                    }
                }
            }
        ) {
            // Sử dụng let để tránh lỗi smart cast
            userData?.let { data ->
                if (data.interests.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        data.interests.chunked(3).forEach { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chunk.forEach { hobby ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFF4169E1),
                                                        Color(0xFF1E90FF)
                                                    )
                                                )
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = hobby,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                // Fill remaining space if chunk is smaller than 3
                                repeat(3 - chunk.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } else {
                    Text(
                        text = "Chưa có sở thích",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } ?: run {
                Text(
                    text = "Chưa có sở thích",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        ProfileCard(
            title = "Mối Quan Hệ Tìm Kiếm",
            content = userData?.lookingFor ?: "Chưa cập nhật",
            onEditClick = {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime > 500 && !isLoading && !isSaving) {
                    lastClickTime = currentTime
                    coroutineScope.launch {
                        isLoading = true
                        syncDataForEdit(EditSection.LOOKING_FOR)
                        editSection = EditSection.LOOKING_FOR
                        showBottomSheet = true
                        isLoading = false
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(100.dp))
    }

    EditBottomSheet(
        isVisible = showBottomSheet,
        onDismiss = {
            if (isSaving) return@EditBottomSheet
            coroutineScope.launch {
                if (userData != null && currentUserId.isNotEmpty() && editSection != null) {
                    isSaving = true
                    val database = FirebaseDatabase.getInstance()
                    try {
                        when (editSection) {
                            EditSection.INTRO_FORM -> {
                                if (hasDataChanged(EditSection.INTRO_FORM)) {
                                    userInputViewModel.updateSingleField(
                                        database = database,
                                        userId = currentUserId,
                                        field = "fullName",
                                        value = userInputViewModel.fullName.value,
                                        onSuccess = {
                                            userInputViewModel.updateSingleField(
                                                database = database,
                                                userId = currentUserId,
                                                field = "birthday",
                                                value = userInputViewModel.getFormattedBirthday(),
                                                onSuccess = {
                                                    authViewModel.fetchUserData(currentUserId)
                                                    saveStatus = "Lưu thành công"
                                                },
                                                onError = { error ->
                                                    saveStatus = "$error"
                                                }
                                            )
                                        },
                                        onError = { error ->
                                            saveStatus = "$error"
                                        }
                                    )
                                }
                            }

                            EditSection.LOCATION -> {
                                if (hasDataChanged(EditSection.LOCATION)) {
                                    userInputViewModel.updateSingleField(
                                        database = database,
                                        userId = currentUserId,
                                        field = "location",
                                        value = userInputViewModel.location.value,
                                        onSuccess = {
                                            authViewModel.fetchUserData(currentUserId)
                                            saveStatus = "Lưu thành công"
                                        },
                                        onError = { error ->
                                            saveStatus = "$error"
                                        }
                                    )
                                }
                            }

                            EditSection.GENDER -> {
                                if (hasDataChanged(EditSection.GENDER)) {
                                    userInputViewModel.updateSingleField(
                                        database = database,
                                        userId = currentUserId,
                                        field = "gender",
                                        value = userInputViewModel.gender.value,
                                        onSuccess = {
                                            authViewModel.fetchUserData(currentUserId)
                                            saveStatus = "Lưu thành công"
                                        },
                                        onError = { error ->
                                            saveStatus = "$error"
                                        }
                                    )
                                }
                            }

                            EditSection.LOOKING_FOR -> {
                                if (hasDataChanged(EditSection.LOOKING_FOR)) {
                                    userInputViewModel.updateSingleField(
                                        database = database,
                                        userId = currentUserId,
                                        field = "lookingFor",
                                        value = userInputViewModel.lookingFor.value,
                                        onSuccess = {
                                            authViewModel.fetchUserData(currentUserId)
                                            saveStatus = "Lưu thành công"
                                        },
                                        onError = { error ->
                                            saveStatus = "$error"
                                        }
                                    )
                                }
                            }

                            EditSection.INTEREST -> {
                                if (hasDataChanged(EditSection.INTEREST)) {
                                    userInputViewModel.updateSingleField(
                                        database = database,
                                        userId = currentUserId,
                                        field = "interests",
                                        value = userInputViewModel.interests.value,
                                        onSuccess = {
                                            authViewModel.fetchUserData(currentUserId)
                                            saveStatus = "Lưu thành công"
                                        },
                                        onError = { error ->
                                            saveStatus = "$error"
                                        }
                                    )
                                }
                            }

                            EditSection.UPPHOTO -> {
                                val localUris = userInputViewModel.photoUrls.value
                                val uploadedUrls = MutableList(6) { "" }
                                var uploadCount = localUris.count {
                                    it.isNotEmpty() && (it.startsWith("content://") || it.startsWith(
                                        "file://"
                                    ))
                                }
                                var completedUploads = 0

                                if (uploadCount == 0) {
                                    if (hasDataChanged(EditSection.UPPHOTO)) {
                                        userInputViewModel.updateSingleField(
                                            database = database,
                                            userId = currentUserId,
                                            field = "imageUrls",
                                            value = userInputViewModel.photoUrls.value,
                                            onSuccess = {
                                                authViewModel.fetchUserData(currentUserId)
                                                saveStatus = "Lưu thành công"
                                            },
                                            onError = { error ->
                                                saveStatus = "$error"
                                            }
                                        )
                                    }
                                } else {
                                    localUris.forEachIndexed { index, uri ->
                                        if (uri.isNotEmpty() && (uri.startsWith("content://") || uri.startsWith(
                                                "file://"
                                            ))
                                        ) {
                                            val filePath = FileUtil.getPath(
                                                context,
                                                android.net.Uri.parse(uri)
                                            )
                                            if (filePath != null) {
                                                userInputViewModel.uploadPhotoToCloudinary(
                                                    filePath = filePath,
                                                    onSuccess = { url ->
                                                        uploadedUrls[index] = url
                                                        completedUploads++
                                                        if (completedUploads == uploadCount) {
                                                            userInputViewModel.photoUrls.value =
                                                                uploadedUrls
                                                            userInputViewModel.updateSingleField(
                                                                database = database,
                                                                userId = currentUserId,
                                                                field = "imageUrls",
                                                                value = userInputViewModel.photoUrls.value,
                                                                onSuccess = {
                                                                    authViewModel.fetchUserData(
                                                                        currentUserId
                                                                    )
                                                                    saveStatus = "Lưu thành công"
                                                                },
                                                                onError = { error ->
                                                                    saveStatus = "$error"
                                                                }
                                                            )
                                                        }
                                                    },
                                                    onError = { error ->
                                                        saveStatus = "Lỗi khi tải ảnh: $error"
                                                    }
                                                )
                                            }
                                        } else {
                                            uploadedUrls[index] = uri
                                        }
                                    }
                                }
                            }

                            else -> {}
                        }
                    } finally {
                        isSaving = false
                        showBottomSheet = false
                        editSection = null
                    }
                } else {
                    showBottomSheet = false
                    editSection = null
                }
            }
        },
        content = {
            when (editSection) {
                EditSection.INTRO_FORM -> IntroFormUI(userInputViewModel)
                EditSection.LOCATION -> Location(userInputViewModel)
                EditSection.GENDER -> GenderSelectionScreen(userInputViewModel)
                EditSection.LOOKING_FOR -> LookingForUi(userInputViewModel)
                EditSection.INTEREST -> InterestUi(userInputViewModel)
                EditSection.UPPHOTO -> {
                    UpPhotoUi(viewModel = userInputViewModel, context = context)
                    LaunchedEffect(Unit) {
                        val photoUrls = userInputViewModel.photoUrls.value
                        val uris = List(6) { index ->
                            if (photoUrls[index].isNotEmpty()) Uri.parse(photoUrls[index]) else null
                        }
                    }
                }

                else -> Text("Không có nội dung để chỉnh sửa", color = Color.White)
            }
        }
    )

    LaunchedEffect(saveStatus) {
        if (saveStatus != null) {
            delay(2000)
            saveStatus = null
        }
    }

    if (saveStatus != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.systemBars.asPaddingValues())
        ) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF212121)),
                action = {
                    TextButton(onClick = { saveStatus = null }) {
                        Text("OK", color = Color.White)
                    }
                }
            ) {
                Text(saveStatus ?: "", color = Color.White)
            }
        }

    }

    DisposableEffect(Unit) {
        onDispose {
            val imageLoader = ImageLoader(context)
            imageLoader.memoryCache?.clear()
        }
    }
}

@Composable
fun ProfileCard(
    title: String,
    content: String? = null,
    onEditClick: () -> Unit,
    additionalContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFF4081), Color(0xFFF50057))
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Chỉnh sửa",
                        tint = Color.White
                    )
                }
            }
            if (content != null) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF424242),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            additionalContent?.invoke()
        }
    }
}