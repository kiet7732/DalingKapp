package com.example.dalingk.components.detailUser

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.ImageLoader
import com.example.dalingk.FullScreenLoading
import com.example.dalingk.navigation.Routes
import com.example.dalingk.components.EditBottomSheet
import com.google.firebase.database.FirebaseDatabase
import data.viewmodel.UserInputViewModel
import data.repository.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
//                    ProfileScreenU()
                }
            }
        }
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
                    Log.d(
                        "ProfileScreenU",
                        "Birthday from userData: ${data.birthday}, split parts: $birthdayParts"
                    )
                    if (birthdayParts.size == 3) {
                        userInputViewModel.birthDay.value = birthdayParts[0]
                        userInputViewModel.birthMonth.value = birthdayParts[1]
                        userInputViewModel.birthYear.value = birthdayParts[2]
                    } else {
                        Log.w(
                            "ProfileScreenU",
                            "Invalid birthday format: ${data.birthday}, resetting to empty"
                        )
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
                    Log.d(
                        "ProfileScreenU",
                        "Đồng bộ ảnh để hiển thị trong UpPhotoUi: ${userInputViewModel.photoUrls.value}"
                    )
                }
            }
            Log.d("ProfileScreenU", "Đồng bộ dữ liệu cho $section hoàn tất")
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
                    !currentInterests.equals(originalInterests) // Sử dụng equals để so sánh List
                }

                EditSection.UPPHOTO -> userInputViewModel.photoUrls.value != (data.imageUrls
                    ?: List(6) { "" })
            }
        }
        return false // Nếu không có userData, không cập nhật
    }

    val imageUrls = remember(userData) { userData?.imageUrls ?: emptyList() }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp)
            .verticalScroll(scrollState)
            .padding(WindowInsets.systemBars.asPaddingValues()),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                navController.currentBackStackEntry?.savedStateHandle?.set(Routes.DetailU, 3)
                navController.navigate(Routes.MainMatch)
            }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint  = Color.Black
                )
            }
            Text(
                text = "Hồ Sơ",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextCus(label = "Họ và tên") {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > 500 && !isLoading && !isSaving) {
                lastClickTime = currentTime
                coroutineScope.launch {
                    isLoading = true
                    syncDataForEdit(EditSection.INTRO_FORM)
                    editSection = EditSection.INTRO_FORM
                    showBottomSheet = true
                    isLoading = false
                    Log.d(
                        "ProfileScreenU",
                        "Mở EditBottomSheet cho INTRO_FORM: showBottomSheet=$showBottomSheet"
                    )
                }
            } else {
                Log.d(
                    "ProfileScreenU",
                    "Nhấn bị chặn: isLoading=$isLoading, isSaving=$isSaving, timeDiff=${currentTime - lastClickTime}"
                )
            }
        }
        Row(modifier = Modifier.padding(end = 8.dp)) {
            Text(
                text = userData?.fullName ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextCus(label = "Dịa chỉ") {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > 500 && !isLoading && !isSaving) {
                lastClickTime = currentTime
                coroutineScope.launch {
                    isLoading = true
                    syncDataForEdit(EditSection.LOCATION)
                    editSection = EditSection.LOCATION
                    showBottomSheet = true
                    isLoading = false
                    Log.d(
                        "ProfileScreenU",
                        "Mở EditBottomSheet cho LOCATION: showBottomSheet=$showBottomSheet"
                    )
                }
                Log.d("ProfileScreenU", "userData.location: ${userData?.location}")
            } else {
                Log.d(
                    "ProfileScreenU",
                    "Nhấn bị chặn: isLoading=$isLoading, isSaving=$isSaving, timeDiff=${currentTime - lastClickTime}"
                )
            }
        }

        Row(modifier = Modifier.padding(end = 8.dp)) {
            Text(
                text = userData?.location ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextCus(label = "Ảnh") {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > 500 && !isLoading && !isSaving) {
                lastClickTime = currentTime
                coroutineScope.launch {
                    isLoading = true
                    syncDataForEdit(EditSection.UPPHOTO)
                    editSection = EditSection.UPPHOTO
                    showBottomSheet = true
                    isLoading = false
                    Log.d(
                        "ProfileScreenU",
                        "Mở EditBottomSheet cho UPPHOTO: showBottomSheet=$showBottomSheet"
                    )
                }
            } else {
                Log.d(
                    "ProfileScreenU",
                    "Nhấn bị chặn: isLoading=$isLoading, isSaving=$isSaving, timeDiff=${currentTime - lastClickTime}"
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth()) {
            DisplayUserPhotos(imageUrls = imageUrls, context)
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextCus(label = "Giới tính") {
//            val currentTime = System.currentTimeMillis()
//            if (currentTime - lastClickTime > 500 && !isLoading && !isSaving) {
//                lastClickTime = currentTime
//                coroutineScope.launch {
//                    isLoading = true
//                    syncDataForEdit(EditSection.GENDER)
//                    editSection = EditSection.GENDER
//                    showBottomSheet = true
//                    isLoading = false
//                    Log.d(
//                        "ProfileScreenU",
//                        "Mở EditBottomSheet cho GENDER: showBottomSheet=$showBottomSheet"
//                    )
//                }
//            } else {
//                Log.d(
//                    "ProfileScreenU",
//                    "Nhấn bị chặn: isLoading=$isLoading, isSaving=$isSaving, timeDiff=${currentTime - lastClickTime}"
//                )
//            }
            saveStatus = "Không thể đổi giới tính"

        }
        Row(modifier = Modifier.padding(end = 8.dp)) {
            Text(
                text = userData?.gender ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextCus(label = "Sở thích") {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > 500 && !isLoading && !isSaving) {
                lastClickTime = currentTime
                coroutineScope.launch {
                    isLoading = true
                    syncDataForEdit(EditSection.INTEREST)
                    editSection = EditSection.INTEREST
                    showBottomSheet = true
                    isLoading = false
                    Log.d(
                        "ProfileScreenU",
                        "Mở EditBottomSheet cho INTEREST: showBottomSheet=$showBottomSheet"
                    )
                }
            } else {
                Log.d(
                    "ProfileScreenU",
                    "Nhấn bị chặn: isLoading=$isLoading, isSaving=$isSaving, timeDiff=${currentTime - lastClickTime}"
                )
            }
        }
        Column {
            (userData?.interests ?: emptyList()).forEachIndexed { index, hobby ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFF4169E1), shape = MaterialTheme.shapes.medium)
                        .padding(8.dp)
                ) {
                    Text(
                        text = hobby.ifEmpty { "" },
                        color = if (hobby.isEmpty()) Color.Gray else Color.White,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextCus(label = "Mối quan hệ tìm kiếm") {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > 500 && !isLoading && !isSaving) {
                lastClickTime = currentTime
                coroutineScope.launch {
                    isLoading = true
                    syncDataForEdit(EditSection.LOOKING_FOR)
                    editSection = EditSection.LOOKING_FOR
                    showBottomSheet = true
                    isLoading = false
                    Log.d(
                        "ProfileScreenU",
                        "Mở EditBottomSheet cho LOOKING_FOR: showBottomSheet=$showBottomSheet"
                    )
                }
            } else {
                Log.d(
                    "ProfileScreenU",
                    "Nhấn bị chặn: isLoading=$isLoading, isSaving=$isSaving, timeDiff=${currentTime - lastClickTime}"
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color(0xFF4169E1), shape = MaterialTheme.shapes.medium)
                .padding(8.dp)
        ) {
            Text(
                text = userData?.lookingFor?.ifEmpty { "" } ?: "",
                color = if (userData?.lookingFor.isNullOrEmpty()) Color.Gray else Color.White,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(100.dp))
    }

    EditBottomSheet(
        isVisible = showBottomSheet,
        onDismiss = {
            if (isSaving) {
                Log.d("ProfileScreenU", "Đóng bị chặn vì đang lưu: isSaving=$isSaving")
                return@EditBottomSheet
            }
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
                                } else {
                                    Log.d(
                                        "ProfileScreenU",
                                        "Không có thay đổi trong INTRO_FORM, không cập nhật"
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
                                    Log.d(
                                        "ProfileScreenU",
                                        "userData.location: ${userData?.location}"
                                    )
                                } else {
                                    Log.d(
                                        "ProfileScreenU",
                                        "Không có thay đổi trong LOCATION, không cập nhật"
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
                                } else {
                                    Log.d(
                                        "ProfileScreenU",
                                        "Không có thay đổi trong GENDER, không cập nhật"
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
                                } else {
                                    Log.d(
                                        "ProfileScreenU",
                                        "Không có thay đổi trong LOOKING_FOR, không cập nhật"
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
                                } else {
                                    Log.d(
                                        "ProfileScreenU",
                                        "Không có thay đổi trong INTEREST, không cập nhật"
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
                                    // Nếu không có ảnh mới cần tải lên, kiểm tra thay đổi và lưu trực tiếp
                                    if (hasDataChanged(EditSection.UPPHOTO)) {
                                        userInputViewModel.updateSingleField(
                                            database = database,
                                            userId = currentUserId,
                                            field = "imageUrls",
                                            value = userInputViewModel.photoUrls.value,
                                            onSuccess = {
                                                authViewModel.fetchUserData(currentUserId)
                                                saveStatus = "Lưu thành công"
                                                Log.d(
                                                    "ProfileScreenU",
                                                    "Ảnh đã được cập nhật: ${userInputViewModel.photoUrls.value}"
                                                )
                                            },
                                            onError = { error ->
                                                saveStatus = "$error"
                                            }
                                        )
                                    } else {
                                        Log.d(
                                            "ProfileScreenU",
                                            "Không có thay đổi trong UPPHOTO, không cập nhật"
                                        )
                                    }
                                } else {
                                    // Nếu có ảnh mới (content:// hoặc file://), tải lên Cloudinary
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
                                                            // Cập nhật photoUrls với các URL đã tải lên
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
                                                                    Log.d(
                                                                        "ProfileScreenU",
                                                                        "Ảnh đã được cập nhật: ${userInputViewModel.photoUrls.value}"
                                                                    )
                                                                },
                                                                onError = { error ->
                                                                    saveStatus = "$error"
                                                                }
                                                            )
                                                        }
                                                    },
                                                    onError = { error ->
                                                        saveStatus = "Lỗi khi tải ảnh: $error"
                                                        Log.e(
                                                            "ProfileScreenU",
                                                            "Lỗi khi tải ảnh lên Cloudinary: $error"
                                                        )
                                                    }
                                                )
                                            }
                                        } else {
                                            // Giữ nguyên URL cũ nếu không phải là ảnh mới
                                            uploadedUrls[index] = uri
                                        }
                                    }
                                }
                            }

                            else -> {
                                Log.d("ProfileScreenU", "Không có editSection hợp lệ")
                            }
                        }
                    } finally {
                        isSaving = false
                        showBottomSheet = false
                        editSection = null
                        Log.d(
                            "ProfileScreenU",
                            "Đóng EditBottomSheet: isSaving=$isSaving, showBottomSheet=$showBottomSheet, editSection=$editSection"
                        )
                    }
                } else {
                    showBottomSheet = false
                    editSection = null
                    Log.d(
                        "ProfileScreenU",
                        "Đóng EditBottomSheet (không có dữ liệu): showBottomSheet=$showBottomSheet, editSection=$editSection"
                    )
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
                        Log.d("ProfileScreenU", "Đồng bộ ảnh vào UpPhotoUi: $uris")
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
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = { Button(onClick = { saveStatus = null }) { Text("OK") } }
        ) {
            Text(saveStatus ?: "")
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
fun TextCus(
    label: String,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Hiển thị nhãn (Label)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )
        // Biểu tượng chỉnh sửa
        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                tint = Color.Red
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview9() {
    DalingKTheme {
//        ProfileScreenU()
    }
}