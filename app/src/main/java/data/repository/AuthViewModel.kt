package data.repository

import android.content.Context
import android.net.Uri
import android.util.Log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dalingk.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await


public class AuthViewModel : ViewModel() {
    val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val database = FirebaseDatabase.getInstance()

//    val viewModelScope = CoroutineScope(Dispatchers.Main + Job())

    private val _authState = MutableStateFlow<FirebaseAuth?>(null)
    val authState: StateFlow<FirebaseAuth?> = _authState

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    private val _cachedProfiles = MutableStateFlow<List<UserData>>(emptyList())
    val cachedProfiles: StateFlow<List<UserData>> = _cachedProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _matches = MutableStateFlow<List<MatchData>>(emptyList())
    val matches: StateFlow<List<MatchData>> = _matches

    init {
        _authState.value = auth
    }

    // Make UserData public
    data class UserData(
        val userId: String = "", // Thêm userId
        val fullName: String = "", // Tên đầy đủ của người dùng
        val gender: String = "", // Giới tính (Nam, Nữ, Khác,...)
        val interests: List<String> = emptyList(), // Sở thích
        val lookingFor: String = "", // Tình trạng quan hệ
        val location: String = "", // Địa điểm
        val birthday: String = "", // Ngày sinh định dạng dd-mm-yyyy
        val imageUrls: List<String> = emptyList(), // Danh sách URL hình ảnh từ Cloudinary
        val age: Int = calculateAge(birthday) // Tính tuổi từ birthday
    ) {
        companion object {
            fun calculateAge(birthday: String): Int {
                if (birthday.isEmpty() || !birthday.matches("\\d{2}-\\d{2}-\\d{4}".toRegex())) {
                    return 0 // Trả về 0 nếu birthday không hợp lệ
                }

                try {
                    val parts = birthday.split("-")
                    val day = parts[0].toInt()
                    val month = parts[1].toInt()
                    val year = parts[2].toInt()

                    val today = java.time.LocalDate.now()
                    val birthDate = java.time.LocalDate.of(year, month, day)

                    return java.time.Period.between(birthDate, today).years
                } catch (e: Exception) {
                    return 0 // Trả về 0 nếu có lỗi khi parse
                }
            }
        }
    }


    fun fetchUserData(userId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        // Use Firebase's callback-based API instead of await()
        database.getReference("users").get()
            .addOnSuccessListener { snapshot ->
                val userList = mutableListOf<UserData>()

                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        val currentUserId = childSnapshot.key ?: continue
                        val fullName = childSnapshot.child("fullName").getValue(String::class.java) ?: ""
                        val gender = childSnapshot.child("gender").getValue(String::class.java) ?: ""
                        val hobbies = childSnapshot.child("interests")
                            .getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                        val relationshipStatus = childSnapshot.child("lookingFor").getValue(String::class.java) ?: ""
                        val location = childSnapshot.child("location").getValue(String::class.java) ?: ""
                        val birthday = childSnapshot.child("birthday").getValue(String::class.java) ?: ""
                        val imageUrls = childSnapshot.child("imageUrls")
                            .getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

                        val user = UserData(
                            userId = currentUserId,
                            fullName = fullName,
                            gender = gender,
                            interests = hobbies,
                            lookingFor = relationshipStatus,
                            location = location,
                            birthday = birthday,
                            imageUrls = imageUrls
                        )

                        if (currentUserId == userId) {
                            _userData.value = user
                        } else {
                            userList.add(user)
                        }
                    }

                    if (_userData.value == null) {
                        Log.e("FirebaseError", "fetchUserData Không tìm thấy thông tin người dùng với userId: $userId")
                    }
                } else {
                    Log.e("FirebaseError", "Không tìm thấy danh sách người dùng")
                    _errorMessage.value = "Không tìm thấy danh sách người dùng"
                }
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseError", "Lỗi khi tải dữ liệu: ${e.message}", e)
                _errorMessage.value = "Không thể tải dữ liệu: ${e.message}"
                _isLoading.value = false
            }
    }

    // Kiểm tra Firebase Authentication
    fun checkCurrentUserIdExists(onResult: (Boolean) -> Unit, onError: (String) -> Unit) {
        _isLoading.value = true
        _errorMessage.value = null

        val currentUser = auth.currentUser
        if (currentUser != null) {
            _isLoading.value = false
            onResult(true) // Có trong Authentication
        } else {
            _isLoading.value = false
            onResult(false) // Không có trong Authentication
        }
    }

    // Kiểm tra Realtime Database và quyết định navigation
    fun checkCurrentUserDataExists(
        onResult: (String) -> Unit, // Thay đổi để trả về route đích
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        _isLoading.value = true

        if (currentUser == null) {
            // Trường hợp 1: Không có trong Authentication
            database.getReference("users").get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // Có dữ liệu trong Realtime Database nhưng không có trong Authentication
                        auth.signOut() // Xóa thông tin đăng nhập hiện tại
                        _userData.value = null
                        _isLoading.value = false
                        onResult(Routes.TrailerScreen)
                    } else {
                        // Không có trong cả hai
                        _isLoading.value = false
                        onResult(Routes.TrailerScreen)
                    }
                }
                .addOnFailureListener { exception ->
                    _isLoading.value = false
                    onError(exception.message ?: "Unknown error")
                    onResult(Routes.TrailerScreen)
                }
        } else {
            // Có trong Authentication, kiểm tra Realtime Database
            val userId = currentUser.uid
            val databaseRef = database.getReference("users").child(userId)

            databaseRef.get().addOnSuccessListener { snapshot ->
                _isLoading.value = false
                if (snapshot.exists()) {
                    // Trường hợp 2: Có trong cả Authentication và Realtime Database
                    onResult(Routes.MainMatch)
                } else {
                    // Trường hợp 3: Có trong Authentication nhưng không có trong Realtime Database
                    onResult(Routes.InputDetail)
                }
            }.addOnFailureListener { exception ->
                _isLoading.value = false
                onError(exception.message ?: "Unknown error")
                onResult(Routes.TrailerScreen) // Default về TrailerScreen khi có lỗi
            }
        }
    }


    // Đăng ký tài khoản (existing function, unchanged)
    fun registerUser(
        context: Context,
        email: String,
        password: String,
        phoneNumber: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isEmpty() || password.length < 6 || phoneNumber.isEmpty()) {
            onError("Email, mật khẩu hoặc số điện thoại không hợp lệ")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""

                    val userMap = hashMapOf(
                        "email" to email,
                        "phone" to phoneNumber,
                        "userId" to userId
                    )

                    db.collection("users")
                        .document(userId)
                        .set(userMap)
                        .addOnSuccessListener {
                            // Lưu userId vào DataStore
                            UserPreferences.saveUserId(context, userId)
                            println("DEBUG: Đăng ký thành công, userId: $userId đã được lưu vào DataStore")
                            onSuccess() // Gọi onSuccess sau khi tất cả hoàn tất
                        }
                        .addOnFailureListener {
                            onError("Lỗi khi lưu thông tin tài khoản")
                        }

                    UserPreferences.saveUserId(context, userId)
                    println("DEBUG: Đăng ký thành công, userId: $userId đã được lưu vào DataStore 1")
                    val storedUserId = UserPreferences.getUserId(context)
                    println("DEBUG: userId lấy từ DataStore: $storedUserId")
                    onSuccess()

                } else {
                    onError(task.exception?.message ?: "Đăng ký thất bại")
                }
            }
    }

    // Đăng nhập (existing function, unchanged)
    fun loginUser(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid ?: ""
                    onSuccess(userId)
                } else {
                    onError(task.exception?.message ?: "Lỗi đăng nhập")
                }
            }
    }

    data class MatchData(
        val matchId: String,
        val user1Id: String,
        val user2Id: String,
        val timestamp: Long
    )

    // Gọi MatchingRepository
    suspend fun loadNewProfiles(context: Context){
        Matching.fetchNewProfiles(
            currentUserId = auth.currentUser?.uid ?: return,
            database = database,
            cachedProfiles = _cachedProfiles,
            isLoading = _isLoading,
            errorMessage = _errorMessage,
            context = context
        )
    }

    private val _showMatchNotification = MutableStateFlow(false)
    val showMatchNotification: StateFlow<Boolean> = _showMatchNotification.asStateFlow()

    private val _matchedUserName = MutableStateFlow("")
    val matchedUserName: StateFlow<String> = _matchedUserName.asStateFlow()

    suspend fun like(targetUserId: String, fullName: String): Boolean {
        val currentUserId = auth.currentUser?.uid ?: return false

        val isMatched = Matching.likeProfile(
            currentUserId = currentUserId,
            targetUserId = targetUserId,
            database = database,
            cachedProfiles = _cachedProfiles,
            matches = _matches,
            errorMessage = _errorMessage
        )

        if (isMatched) {
            _matchedUserName.value = fullName // Lấy từ dữ liệu
            _showMatchNotification.value = true
        }

        return isMatched
    }

    suspend fun dislike(targetUserId: String) {
        Matching.dislikeProfile(
            currentUserId = auth.currentUser?.uid ?: return,
            targetUserId = targetUserId,
            database = database,
            cachedProfiles = _cachedProfiles,
            errorMessage = _errorMessage
        )
    }

    fun dismissMatchNotification() {
        _showMatchNotification.value = false
    }

    // Đăng xuất (existing function, unchanged)
    fun logout() {
        auth.signOut()
        _userData.value = null // Đặt lại userData để đảm bảo làm mới khi đăng nhập lại
        _cachedProfiles.value = emptyList() // (Tùy chọn) Xóa danh sách profiles đã lưu nếu cần
    }

    override fun onCleared() {
        viewModelScope.cancel() // Hủy scope khi ViewModel bị hủy
        super.onCleared()
    }




}
