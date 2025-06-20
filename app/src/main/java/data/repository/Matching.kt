package data.repository

import android.content.Context
import android.util.Log
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random


object Matching {

    private val repositoryScope = CoroutineScope(Dispatchers.Main + Job())
    private val localCache = mutableMapOf<String, Pair<String, Boolean>>()

    suspend fun fetchNewProfiles(
        currentUserId: String,
        database: FirebaseDatabase,
        cachedProfiles: MutableStateFlow<List<AuthViewModel.UserData>>,
        isLoading: MutableStateFlow<Boolean>,
        errorMessage: MutableStateFlow<String?>,
        context: Context // Thêm context để sử dụng Coil
    ) {
        isLoading.value = true
        errorMessage.value = null

        withContext(Dispatchers.IO) {
            try {
                val viewedIds = hashSetOf(currentUserId) // HashSet giúp tra cứu nhanh hơn

                // Lấy thông tin người dùng hiện tại
                val userSnapshot = database.getReference("users/$currentUserId").get().await()
                val currentUserGender = userSnapshot.child("gender").getValue(String::class.java) ?: ""
                val currentUserInterests = userSnapshot.child("interests")
                    .getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                val currentUserLookingFor = userSnapshot.child("lookingFor").getValue(String::class.java) ?: ""

                // Xác định giới tính mong muốn (khác với giới tính người dùng hiện tại)
                val preferredGender = when (currentUserGender.lowercase()) {
                    "male" -> "female"
                    "female" -> "male"
                    else -> "" // Nếu không có giới tính rõ ràng, không lọc theo giới tính
                }

                // Lấy danh sách user đã like & dislike song song
                coroutineScope {
                    val (likesSnapshot, dislikesSnapshot) = listOf(
                        async { database.getReference("likes/$currentUserId").get().await() },
                        async { database.getReference("dislikes/$currentUserId").get().await() }
                    ).awaitAll()

                    // Thêm user đã like/dislike vào danh sách đã xem
                    likesSnapshot.children.mapNotNullTo(viewedIds) { it.key }
                    dislikesSnapshot.children.mapNotNullTo(viewedIds) { it.key }
                }

                Log.d("FilteredUsers", "Danh sách user bị ẩn: $viewedIds")

                // Lấy danh sách tất cả users từ Firebase (tối đa 50 user)
                val snapshot = database.getReference("users").limitToFirst(50).get().await()

                // Tạo danh sách người dùng với điểm tương đồng
                val profilesWithScore = snapshot.children.mapNotNull { childSnapshot ->
                    val userId = childSnapshot.key ?: return@mapNotNull null

                    // Nếu user đã like/dislike hoặc là chính mình -> bỏ qua
                    if (userId in viewedIds) return@mapNotNull null

                    val gender = childSnapshot.child("gender").getValue(String::class.java) ?: ""
                    // Lọc theo giới tính ngược
                    if (preferredGender.isNotEmpty() && gender.lowercase() != preferredGender) {
                        return@mapNotNull null
                    }

                    val interests = childSnapshot.child("interests")
                        .getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                    val lookingFor = childSnapshot.child("lookingFor").getValue(String::class.java) ?: ""

                    // Tính điểm tương đồng
                    val commonInterests = currentUserInterests.intersect(interests.toSet()).size
                    val lookingForMatch = if (currentUserLookingFor == lookingFor && lookingFor.isNotEmpty()) 1 else 0
                    val compatibilityScore = commonInterests + lookingForMatch

                    val userData = AuthViewModel.UserData(
                        userId,
                        childSnapshot.child("fullName").getValue(String::class.java) ?: "",
                        gender,
                        interests,
                        lookingFor,
                        childSnapshot.child("location").getValue(String::class.java) ?: "",
                        childSnapshot.child("birthday").getValue(String::class.java) ?: "",
                        childSnapshot.child("imageUrls")
                            .getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                    )

                    Pair(userData, compatibilityScore)
                }

                // Preload hình ảnh vào cache
                val imageLoader = ImageLoader.Builder(context)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()

                profilesWithScore.take(5).forEach { (userData, _) ->
                    userData.imageUrls.forEach { url ->
                        val request = ImageRequest.Builder(context)
                            .data(url)
                            .size(420, 580)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build()
                        imageLoader.execute(request)
                    }
                }

                // Phân loại và xáo trộn
                val (compatibleProfiles, nonCompatibleProfiles) = profilesWithScore.partition { it.second > 0 }

                // Xáo trộn danh sách tương đồng và không tương đồng
                val shuffledCompatible = compatibleProfiles.shuffled(Random(System.currentTimeMillis()))
                val shuffledNonCompatible = nonCompatibleProfiles.shuffled(Random(System.currentTimeMillis() + 1))

                // Gộp danh sách: tương đồng ở đầu, không tương đồng ở cuối
                val finalProfiles = (shuffledCompatible + shuffledNonCompatible).map { it.first }

                val displayedUserIds = finalProfiles.map { it.userId }
                Log.d("DisplayedUsers", "Danh sách user hiển thị: $displayedUserIds")

                // Nếu danh sách rỗng, không cập nhật `cachedProfiles`
                if (finalProfiles.isEmpty()) {
                    Log.e("FirebaseError", "Không còn profile nào đủ điều kiện để hiển thị!")
                    return@withContext // Dừng tại đây, không cập nhật danh sách rỗng
                }

                cachedProfiles.value = finalProfiles

            } catch (e: Exception) {
                Log.e("FirebaseError", "Lỗi khi tải profiles: ${e.message}", e)
                errorMessage.value = "Không thể tải profiles: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }


    // Hàm preload hình ảnh cho danh sách profiles
    private suspend fun preloadImages(profiles: List<AuthViewModel.UserData>, context: Context) {
        val imageLoader = ImageLoader.Builder(context)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()

        profiles.forEach { userData ->
            userData.imageUrls.forEach { url ->
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .size(420, 580)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                imageLoader.execute(request) // Now safe because we're in a suspend function
            }
        }
    }

    // Hàm preload hình ảnh cho một profile tiếp theo
    suspend fun preloadNextProfile(
        nextProfile: AuthViewModel.UserData?,
        context: Context
    ) {
        if (nextProfile == null) return

        withContext(Dispatchers.IO) {
            preloadImages(listOf(nextProfile), context)
        }
    }

    suspend fun likeProfile(
        currentUserId: String,
        targetUserId: String,
        database: FirebaseDatabase,
        cachedProfiles: MutableStateFlow<List<AuthViewModel.UserData>>,
        matches: MutableStateFlow<List<AuthViewModel.MatchData>>,
        errorMessage: MutableStateFlow<String?>,
        context: Context
    ): Boolean {
        processProfileAction(
            currentUserId,
            targetUserId,
            database,
            cachedProfiles,
            errorMessage,
            true
        )
        // Tải trước hình ảnh của profile tiếp theo
        val nextProfile = cachedProfiles.value.firstOrNull()
        preloadNextProfile(nextProfile, context)
        return checkMutualLike(currentUserId, targetUserId, database, matches)
    }

    suspend fun dislikeProfile(
        currentUserId: String,
        targetUserId: String,
        database: FirebaseDatabase,
        cachedProfiles: MutableStateFlow<List<AuthViewModel.UserData>>,
        errorMessage: MutableStateFlow<String?>,
        context: Context
    ) {
        processProfileAction(
            currentUserId,
            targetUserId,
            database,
            cachedProfiles,
            errorMessage,
            false
        )
        // Tải trước hình ảnh của profile tiếp theo
        val nextProfile = cachedProfiles.value.firstOrNull()
        preloadNextProfile(nextProfile, context)
    }

    private fun processProfileAction(
        currentUserId: String,
        targetUserId: String,
        database: FirebaseDatabase,
        cachedProfiles: MutableStateFlow<List<AuthViewModel.UserData>>,
        errorMessage: MutableStateFlow<String?>,
        isLike: Boolean
    ) {
        localCache[currentUserId] = Pair(targetUserId, isLike)
        repositoryScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val action = if (isLike) "likes" else "dislikes"
                    database.getReference("$action/$currentUserId/$targetUserId").setValue(true)
                        .await()
                    localCache.remove(currentUserId)
                } catch (e: Exception) {
                    Log.e(
                        "FirebaseError",
                        "Lỗi khi ${if (isLike) "Like" else "Dislike"}: ${e.message}",
                        e
                    )
                    errorMessage.value =
                        "Lỗi khi ${if (isLike) "Like" else "Dislike"}: ${e.message}"
                }
            }
        }
        updateCacheAfterAction(targetUserId, cachedProfiles)
    }

    private fun updateCacheAfterAction(
        targetUserId: String,
        cachedProfiles: MutableStateFlow<List<AuthViewModel.UserData>>
    ) {
        cachedProfiles.value = cachedProfiles.value.filterNot { it.userId == targetUserId }
    }

    private suspend fun checkMutualLike(
        currentUserId: String,
        targetUserId: String,
        database: FirebaseDatabase,
        matches: MutableStateFlow<List<AuthViewModel.MatchData>>
    ): Boolean = suspendCoroutine { continuation ->
        database.getReference("likes/$targetUserId/$currentUserId")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.getValue(Boolean::class.java) == true) {
                        val matchId = database.getReference("matches").push().key ?: run {
                            continuation.resume(false)
                            return
                        }
                        val matchData = AuthViewModel.MatchData(
                            matchId,
                            currentUserId,
                            targetUserId,
                            System.currentTimeMillis()
                        )

                        val matchRef = database.getReference("matches/$matchId")
                        matchRef.setValue(matchData)
                            .addOnSuccessListener {
                                // Tạo node chat với tin nhắn mặc định
                                val chatRef = database.getReference("matches/$matchId/chat")
                                val messageId = chatRef.push().key ?: run {
                                    continuation.resume(false)
                                    return@addOnSuccessListener
                                }
                                val welcomeMessage = mapOf(
                                    messageId to mapOf(
                                        "senderId" to "system", // Tin nhắn từ hệ thống
                                        "text" to "Bạn đã được ghép đôi! Bắt đầu trò chuyện nào!",
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                )
                                chatRef.setValue(welcomeMessage)
                                    .addOnSuccessListener {
                                        matches.value = matches.value + matchData
                                        continuation.resume(true)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("FirebaseError", "Lỗi khi tạo chat: ${e.message}")
                                        continuation.resume(false)
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e("FirebaseError", "Lỗi khi lưu match: ${e.message}")
                                continuation.resume(false)
                            }
                    } else {
                        continuation.resume(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Lỗi khi kiểm tra match: ${error.message}")
                    continuation.resume(false)
                }
            })
    }
}
