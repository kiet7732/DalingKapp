package data.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase

import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import data.model.CloudinaryHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

import java.io.File
import java.time.LocalDate
import java.time.Period

class UserInputViewModel : ViewModel() {
    var fullName = mutableStateOf("")
    var birthMonth = mutableStateOf("")
    var birthDay = mutableStateOf("")
    var birthYear = mutableStateOf("")
    var gender = mutableStateOf<String?>(null)
    var lookingFor = mutableStateOf<String?>(null)
    var interests = mutableStateOf<List<String>>(emptyList())
    var photoUrls = mutableStateOf<List<String>>(List(6) { "" }) // Khởi tạo mảng cố định 6 phần tử
    var location = mutableStateOf<String?>(null) // Thêm biến lưu trữ location
    var isLoading = mutableStateOf(false)

    enum class Screen {
        INTRO_FORM, GENDER, LOOKING_FOR, INTEREST, UPPHOTO, LOCATION
    }

    var currentScreen = mutableStateOf(Screen.INTRO_FORM)

    val progress: Float
        get() = when (currentScreen.value) {
            Screen.INTRO_FORM -> 0.166f  // Cập nhật tiến trình với 6 màn hình
            Screen.LOCATION -> 0.333f    // Thêm tiến trình cho Location
            Screen.GENDER -> 0.5f
            Screen.LOOKING_FOR -> 0.666f
            Screen.INTEREST -> 0.833f
            Screen.UPPHOTO -> 1f
        }

    fun addPhotoUrl(url: String) {
        if (photoUrls.value.size < 6) {
            photoUrls.value = photoUrls.value + url
        }
    }

    fun removePhotoUrl(url: String) {
        photoUrls.value = photoUrls.value - url
    }

    fun uploadPhotoToCloudinary(
        filePath: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val fileName = File(filePath).nameWithoutExtension
        isLoading.value = true

        fun generateUniqueFileName(baseName: String): String {
            var newFileName = baseName
            var counter = 1
            while (photoUrls.value.any { it.contains("/$newFileName.jpg") }) {
                newFileName = "$baseName$counter"
                counter++
            }
            return newFileName
        }

        val uniqueFileName = generateUniqueFileName(fileName)

        MediaManager.get().upload(filePath)
            .unsigned(CloudinaryHelper.getUploadPreset())
            .option("public_id", uniqueFileName)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    println("DEBUG: Bắt đầu tải ảnh lên Cloudinary: $uniqueFileName")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val cloudinaryUrl = resultData["secure_url"]?.toString()
                        ?: "https://res.cloudinary.com/dk0dn25hc/image/upload/v1740492619/$uniqueFileName.jpg"
                    addPhotoUrl(cloudinaryUrl)
                    onSuccess(cloudinaryUrl)
                    isLoading.value = false
                    println("DEBUG: Upload thành công, URL: $cloudinaryUrl")
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    onError("Lỗi khi tải ảnh: ${error.description}")
                    isLoading.value = false
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    fun uploadFileToCloudinary(
        filePath: String,
        fileType: FileType,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val file = File(filePath)
        if (!file.exists() || file.length() == 0L) {
            onError("File không tồn tại hoặc rỗng")
            return
        }

        isLoading.value = true
        Log.d("UserInputViewModel", "Starting upload for file: $filePath (fileType: $fileType)")

        // Xác thực định dạng tệp
        val fileExtension = file.extension.lowercase()
        val validExtensions = when (fileType) {
            FileType.IMAGE -> listOf("jpg", "jpeg", "png", "gif")
            FileType.VIDEO -> listOf("mp4", "webm")
            FileType.AUDIO -> listOf("m4a", "mp3")
        }
        if (fileExtension !in validExtensions) {
            onError("Định dạng tệp không được hỗ trợ: $fileExtension")
            isLoading.value = false
            return
        }

        // Đặt resourceType dựa trên fileType
        val resourceType = when (fileType) {
            FileType.IMAGE -> "image"
            FileType.VIDEO, FileType.AUDIO -> "video"
        }

        // Xác thực kích thước tệp
        val maxSizeMB = when (fileType) {
            FileType.IMAGE -> 10 // Hình ảnh: 10MB
            FileType.VIDEO -> 100 // Video: 100MB (Cloudinary free tier)
            FileType.AUDIO -> 10 // Audio: 10MB
        }
        if (file.length() > maxSizeMB * 1024 * 1024) {
            onError("File vượt quá giới hạn $maxSizeMB MB")
            isLoading.value = false
            return
        }

        val fileName = file.nameWithoutExtension
        val uniqueFileName = generateUniqueFileName(fileName)

        // Retry logic
        var retryCount = 0
        val maxRetries = 3
        fun attemptUpload() {
            try {
                MediaManager.get().upload(filePath)
                    .unsigned(CloudinaryHelper.getUploadPreset())
                    .option("public_id", uniqueFileName)
                    .option("resource_type", resourceType)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Log.d("UserInputViewModel", "Upload started: $uniqueFileName ($resourceType)")
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val progress = bytes.toFloat() / totalBytes
                            Log.d("UserInputViewModel", "Upload progress: $progress")
                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val cloudinaryUrl = resultData["secure_url"]?.toString()
                            if (cloudinaryUrl.isNullOrEmpty()) {
                                Log.e("UserInputViewModel", "Empty or invalid URL returned")
                                onError("Không nhận được URL từ Cloudinary")
                            } else {
                                Log.d("UserInputViewModel", "Upload successful: $cloudinaryUrl")
                                if (fileType == FileType.IMAGE) {
                                    addPhotoUrl(cloudinaryUrl)
                                }
                                onSuccess(cloudinaryUrl)
                            }
                            isLoading.value = false
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            val errorMessage = "Upload error: ${error.description}"
                            Log.e("UserInputViewModel", errorMessage)
                            if (retryCount < maxRetries) {
                                retryCount++
                                Log.d("UserInputViewModel", "Retrying upload ($retryCount/$maxRetries)")
                                runBlocking { delay(1000L * retryCount) }
                                attemptUpload()
                            } else {
                                onError(errorMessage)
                                isLoading.value = false
                            }
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            Log.w("UserInputViewModel", "Upload rescheduled: ${error.description}")
                        }
                    })
                    .dispatch()
            } catch (e: Exception) {
                val errorMessage = "File processing error: ${e.message}"
                Log.e("UserInputViewModel", errorMessage, e)
                if (retryCount < maxRetries) {
                    retryCount++
                    Log.d("UserInputViewModel", "Retrying upload ($retryCount/$maxRetries)")
                    runBlocking { delay(1000L * retryCount) }
                    attemptUpload()
                } else {
                    onError(errorMessage)
                    isLoading.value = false
                }
            }
        }

        attemptUpload()
    }

    // Enum để xác định loại tệp
    enum class FileType {
        IMAGE, VIDEO, AUDIO
    }

    // Hàm tạo tên file duy nhất
    private fun generateUniqueFileName(baseName: String): String {
        var newFileName = baseName
        var counter = 1
        while (photoUrls.value.any { it.contains("/$newFileName.") }) {
            newFileName = "$baseName$counter"
            counter++
        }
        return newFileName
    }

    fun isAllDataFilled(): Boolean {
        return fullName.value.isNotEmpty() &&
                birthMonth.value.isNotEmpty() &&
                birthDay.value.isNotEmpty() &&
                birthYear.value.isNotEmpty() &&
                gender.value != null &&
                lookingFor.value != null &&
                interests.value.isNotEmpty() &&
                photoUrls.value.count { it.isNotEmpty() } >= 2 &&
                location.value != null // Thêm kiểm tra location
    }

    fun formatBirthday(): String {
        return "${birthDay.value.padStart(2, '0')}-${
            birthMonth.value.padStart(
                2,
                '0'
            )
        }-${birthYear.value}"
    }

    // Hàm public để lấy birthday đã định dạng
    fun getFormattedBirthday(): String {
        return formatBirthday()
    }

    // Hàm kiểm tra người dùng có trên 18 tuổi không
    fun isOver18(): Boolean {
        val birthDayInt = birthDay.value.toIntOrNull() ?: return false
        val birthMonthInt = birthMonth.value.toIntOrNull() ?: return false
        val birthYearInt = birthYear.value.toIntOrNull() ?: return false

        val currentDate = java.time.LocalDate.now()

        val birthDate = try {
            java.time.LocalDate.of(birthYearInt, birthMonthInt, birthDayInt)
        } catch (e: java.time.DateTimeException) {
            return false
        }
        // Tính tuổi bằng Period và kiểm tra >= 18
        val age = java.time.Period.between(birthDate, currentDate).years
        return age >= 18
    }

    fun Checkinterests(): Boolean {
        return interests.value.size == 3
    }


    fun CheckfullName(): Boolean {
        return fullName.value.length < 17
    }

    // Hàm cập nhật toàn bộ dữ liệu
    fun saveToFirebase(
        database: FirebaseDatabase,
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isAllDataFilled()) {
            onError("Vui lòng nhập đầy đủ thông tin và tải lên ít nhất 2 ảnh")
            return
        }

        if (!isValidBirthday()) {
            onError("Ngày sinh không hợp lệ")
            return
        }

        if (!isOver18()) {
            onError("Bạn phải trên 18 tuổi để đăng ký")
            return
        }

        if (!Checkinterests()) {
            onError("Phải chọn 3 sở thích")
            return
        }

        if (!CheckfullName()) {
            onError("Tên dài quá ngắn lại nhé")
            return
        }

        isLoading.value = true

        Log.d("SaveToFirebase", "Bắt đầu lưu toàn bộ dữ liệu lên Firebase cho userId: $userId")
        val fixedImageUrls = MutableList(6) { "" }
        photoUrls.value.forEachIndexed { index, url ->
            if (index < 6) fixedImageUrls[index] = url
        }
        Log.d("SaveToFirebase", "Mảng imageUrls cố định 6 phần tử: $fixedImageUrls")

        val userData = mapOf(
            "fullName" to fullName.value,
            "birthday" to formatBirthday(),
            "gender" to gender.value,
            "lookingFor" to lookingFor.value,
            "interests" to interests.value,
            "imageUrls" to fixedImageUrls,
            "age" to calculateAge(),
            "location" to location.value,
            "relationshipStatus" to "ban be"
        )

        database.getReference("users").child(userId).setValue(userData)
            .addOnSuccessListener {
                isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener { exception ->
                isLoading.value = false
                onError(exception.message ?: "Lỗi khi lưu dữ liệu")
            }
    }

    // Hàm cập nhật từng mục riêng lẻ
    fun updateSingleField(
        database: FirebaseDatabase,
        userId: String,
        field: String,
        value: Any?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        isLoading.value = true
        Log.d(
            "UpdateSingleField",
            "Bắt đầu cập nhật mục $field cho userId: $userId với giá trị: $value"
        )

        val updates = when (field) {
            "fullName" -> {
                if (!CheckfullName()) {
                    onError("Tên dài quá ngắn lại nhé")
                    return
                }
                mapOf("fullName" to value)
            }

            "birthday" -> {
                // Kiểm tra tuổi nếu cập nhật ngày sinh
                if (!isOver18()) {
                    onError("Bạn phải trên 18 tuổi")
                    return
                }
                mapOf("birthday" to value, "age" to calculateAge())
            }

            "gender" -> mapOf("gender" to value)
            "lookingFor" -> mapOf("lookingFor" to value)
            "interests" -> {

                if (!Checkinterests()) {
                    onError("Phải chọn 3 sở thích")
                    return
                }

                mapOf("interests" to value)
            }

            "imageUrls" -> mapOf("imageUrls" to value)
            "location" -> mapOf("location" to value)
            else -> {
                onError("Trường không hợp lệ: $field")
                return
            }
        }

        database.getReference("users").child(userId).updateChildren(updates)
            .addOnSuccessListener {
                isLoading.value = false
                Log.d("UpdateSingleField", "Cập nhật $field thành công")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                isLoading.value = false
                onError(exception.message ?: "Lỗi khi cập nhật dữ liệu")
            }
    }

    private fun isValidBirthday(): Boolean {
        val month = birthMonth.value.toIntOrNull() ?: return false
        val day = birthDay.value.toIntOrNull() ?: return false
        val year = birthYear.value.toIntOrNull() ?: return false

        val currentYear = LocalDate.now().year

        return month in 1..12 && day in 1..31 && year in 1900..currentYear
    }

    private fun calculateAge(): Int {
        val year = birthYear.value.toIntOrNull() ?: return 0
        return 2025 - year
    }
}