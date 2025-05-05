package com.example.dalingk.components.detailUser

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.example.dalingk.FullScreenLoading
import com.example.dalingk.R
import com.example.dalingk.navigation.Routes
import com.example.dalingk.components.EditBottomSheet
import com.example.dalingk.navigation.GreetingPreview2
import com.example.dalingk.ui.theme.DalingKTheme
import com.google.firebase.database.FirebaseDatabase
import data.repository.AuthViewModel
import data.viewmodel.UserInputViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import util.AppState
import util.FileUtil

class ProfileScreen : ComponentActivity() {
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

    override fun onResume() {
        super.onResume()
        AppState.setAppForeground(true)
    }

    override fun onPause() {
        super.onPause()
        AppState.setAppForeground(false)
    }
}

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
    var selectedPhotoIndex by remember { mutableStateOf(0) }


    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty() && userData == null) {
            authViewModel.fetchUserData(currentUserId)
        }
    }

    // Hàm đồng bộ dữ liệu bất đồng bộ (giữ nguyên logic)
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

    // Hàm kiểm tra xem dữ liệu có thay đổi hay không (giữ nguyên logic)
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
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF6A11CB),
            Color(0xFF2575FC)
        )
    )

    // Thiết kế giao diện mới
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
    ) {
        // Hình nền trên cùng
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(gradientBackground)
        )

        // Nội dung chính
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            Routes.DetailU,
                            3
                        )
                        navController.navigate(Routes.MainMatch)
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White.copy(alpha = 0.9f), CircleShape)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color(0xFF6A11CB)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(id = R.string.textdetailuser_1),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ảnh đại diện chính và thông tin nổi bật
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Ảnh chính làm avatar
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                            .shadow(8.dp, CircleShape)
                            .clickable {
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
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "Thêm ảnh",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }

                        // Nút chỉnh sửa ảnh
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = (-8).dp, y = (-8).dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF4081))
                                .clickable {
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
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Chỉnh sửa ảnh",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Họ tên và độ tuổi
                    Text(
                        text = userData?.fullName ?: "Chưa cập nhật",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Thông tin giới tính và tìm kiếm
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val genderIcon = if (userData?.gender == "Nam") {
                            Icons.Outlined.Male
                        } else {
                            Icons.Outlined.Female
                        }

                        Icon(
                            imageVector = genderIcon,
                            contentDescription = "Giới tính",
                            tint = Color(0xFF6A11CB),
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = userData?.gender ?: "Chưa cập nhật",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4B5563)
                        )

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF9CA3AF))
                        )

                        Text(
                            text = "Tìm kiếm: ${userData?.lookingFor ?: "Chưa cập nhật"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4B5563)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bộ sưu tập ảnh
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.textdetailuser_2),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(5) { index ->
                            val actualIndex = index + 1
                            val imageUrl = if (imageUrls.size > actualIndex) imageUrls[actualIndex] else ""

                            Box(
                                modifier = Modifier
                                    .size(120.dp, 160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .clickable {
                                        val currentTime = System.currentTimeMillis()
                                        if (currentTime - lastClickTime > 500 && !isLoading && !isSaving) {
                                            lastClickTime = currentTime
                                            selectedPhotoIndex = actualIndex
                                            coroutineScope.launch {
                                                isLoading = true
                                                syncDataForEdit(EditSection.UPPHOTO)
                                                editSection = EditSection.UPPHOTO
                                                showBottomSheet = true
                                                isLoading = false
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = "Ảnh hồ sơ phụ ${index + 1}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFFE0E0E0),
                                                        Color(0xFFEEEEEE)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AddPhotoAlternate,
                                                contentDescription = "Thêm ảnh",
                                                tint = Color(0xFF9CA3AF),
                                                modifier = Modifier.size(32.dp)
                                            )

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = stringResource(id = R.string.textdetailuser_3),
                                                color = Color(0xFF9CA3AF),
                                                fontSize = 12.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Thông tin cá nhân
            ProfileInfoSection(
                title = stringResource(id = R.string.textdetailuser_4),
                items = listOf(
                    ProfileInfoItem(
                        icon = Icons.Outlined.Person,
                        label = stringResource(id = R.string.textdetailuser_5),
                        value = userData?.fullName ?: "Chưa cập nhật",
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
                    ),
                    ProfileInfoItem(
                        icon = Icons.Outlined.LocationOn,
                        label = stringResource(id = R.string.textdetailuser_6),
                        value = userData?.location ?: "Chưa cập nhật",
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
                    ),
                    ProfileInfoItem(
                        icon = if (userData?.gender == "Nam") Icons.Outlined.Male else Icons.Outlined.Female,
                        label = stringResource(id = R.string.textdetailuser_8),
                        value = userData?.gender ?: "Chưa cập nhật",
                        onEditClick = {
                            saveStatus = ""
                        }
                    ),
                    ProfileInfoItem(
                        icon = Icons.Outlined.Favorite,
                        label = stringResource(id = R.string.textdetailuser_9),
                        value = userData?.lookingFor ?: "Chưa cập nhật",
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
                )
            )

            // Sở thích
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Stars,
                                contentDescription = "Sở thích",
                                tint = Color(0xFF6A11CB),
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = stringResource(id = R.string.textdetailuser_10),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                        }

                        IconButton(
                            onClick = {
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
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
                                    )
                                )
                                .size(28.dp) // Kích thước nền nhỏ lại
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Chỉnh sửa ",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp) // Icon vẫn to hơn
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    userData?.let { data ->
                        if (data.interests.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
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
                                                                Color(0xFF6A11CB),
                                                                Color(0xFF2575FC)
                                                            )
                                                        )
                                                    )
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = hobby,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    textAlign = TextAlign.Center
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF3F4F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.textdetailuser_12),
                                    color = Color(0xFF9CA3AF),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } ?: run {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.textdetailuser_12),
                                color = Color(0xFF9CA3AF),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Snackbar thông báo
        AnimatedVisibility(
            visible = saveStatus != null,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
            exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1F2937)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = saveStatus ?: "",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    TextButton(
                        onClick = { saveStatus = null },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF6A11CB)
                        )
                    ) {
                        Text(
                            "Đóng",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Hiển thị màn hình loading
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .blur(4.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF6A11CB),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }

    // BottomSheet (giữ nguyên logic xử lý)
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

    DisposableEffect(Unit) {
        onDispose {
            val imageLoader = ImageLoader(context)
            imageLoader.memoryCache?.clear()
        }
    }
}

// Thành phần mới cho mục thông tin cá nhân
@Composable
fun ProfileInfoSection(
    title: String,
    items: List<ProfileInfoItem>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items.forEach { item ->
                    ProfileInfoRow(item)
                }
            }
        }
    }
}

// Dữ liệu cho mỗi mục thông tin
data class ProfileInfoItem(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val onEditClick: () -> Unit
)

// Hàng thông tin cá nhân
@Composable
fun ProfileInfoRow(item: ProfileInfoItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF6A11CB).copy(alpha = 0.2f),
                            Color(0xFF2575FC).copy(alpha = 0.2f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = Color(0xFF6A11CB),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Thông tin
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.label,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )

            Text(
                text = item.value,
                fontSize = 16.sp,
                color = Color(0xFF1F2937),
                fontWeight = FontWeight.Medium
            )
        }

        // Nút chỉnh sửa
        IconButton(
            onClick = item.onEditClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
                    )
                )
                .size(28.dp) // Kích thước nền nhỏ lại
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Chỉnh sửa ${item.label}",
                tint = Color.White,
                modifier = Modifier.size(16.dp) // Icon vẫn to hơn
            )
        }

    }
}