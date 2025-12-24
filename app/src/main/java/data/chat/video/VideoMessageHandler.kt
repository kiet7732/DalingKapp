package com.example.dalingk.data.chat.video // Đảm bảo package đúng

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.SnackbarHostState
import data.chat.video.compressVideo
import data.viewmodel.UserInputViewModel

// Định nghĩa data class VideoLaunchers
data class VideoLaunchers(
    val cameraPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    val storagePermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    val captureVideoLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
    val pickVideoLauncher: ManagedActivityResultLauncher<String, Uri?>,
    val videoUri: () -> Uri?,
    val setVideoUri: (Uri?) -> Unit
)

object VideoMessageHandler {
    @Composable
    fun setupVideoLaunchers(
        matchId: String,
        currentUserId: String,
        userInputViewModel: UserInputViewModel,
        coroutineScope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        onVideoSent: (String) -> Unit
    ): VideoLaunchers {
        val context = LocalContext.current
        var videoUri by remember { mutableStateOf<Uri?>(null) }

        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Cần quyền camera để quay video")
                }
            }
        }

        val storagePermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Cần quyền truy cập bộ nhớ để chọn video")
                }
            }
        }

        val captureVideoLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CaptureVideo()
        ) { isSuccess ->
            if (isSuccess && videoUri != null) {
                handleVideo(context, videoUri!!, userInputViewModel, coroutineScope, snackbarHostState, matchId, currentUserId, onVideoSent)
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Không thể quay video")
                }
            }
        }

        val pickVideoLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                handleVideo(context, it, userInputViewModel, coroutineScope, snackbarHostState, matchId, currentUserId, onVideoSent)
            } ?: run {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Không thể chọn video")
                }
            }
        }

        return VideoLaunchers(
            cameraPermissionLauncher = cameraPermissionLauncher,
            storagePermissionLauncher = storagePermissionLauncher,
            captureVideoLauncher = captureVideoLauncher,
            pickVideoLauncher = pickVideoLauncher,
            videoUri = { videoUri },
            setVideoUri = { videoUri = it }
        )
    }

    private fun handleVideo(
        context: Context,
        videoUri: Uri,
        userInputViewModel: UserInputViewModel,
        coroutineScope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        matchId: String,
        currentUserId: String,
        onVideoSent: (String) -> Unit
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            var compressedFile: File? = null
            try {
                Log.d("VideoMessageHandler", "Starting video processing for URI: $videoUri")
                compressedFile = compressVideo(context, videoUri, maxSizeMB = 5)
                if (!compressedFile.exists() || compressedFile.length() == 0L) {
                    throw Exception("Compressed file is invalid: ${compressedFile.absolutePath}")
                }
                Log.d("VideoMessageHandler", "Compressed file ready: ${compressedFile.absolutePath}, size: ${compressedFile.length() / 1024} KB")

                userInputViewModel.uploadFileToCloudinary(
                    filePath = compressedFile.absolutePath,
                    fileType = UserInputViewModel.FileType.VIDEO,
                    onSuccess = { videoUrl ->
                        Log.d("VideoMessageHandler", "Video uploaded successfully: $videoUrl")
                        onVideoSent(videoUrl)
                        compressedFile.delete()
                    },
                    onError = { error ->
                        Log.e("VideoMessageHandler", "Error uploading video: $error")
                        coroutineScope.launch(Dispatchers.Main) {
                            snackbarHostState.showSnackbar("Lỗi tải video: $error")
                        }
                        compressedFile.delete()
                    }
                )
            } catch (e: Exception) {
                Log.e("VideoMessageHandler", "Exception during video processing: ${e.message}", e)
                coroutineScope.launch(Dispatchers.Main) {
                    snackbarHostState.showSnackbar("Lỗi xử lý video: ${e.message}")
                }
                compressedFile?.delete()
            }
        }
    }

    fun launchCaptureVideo(
        context: Context,
        cameraPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
        captureVideoLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
        setVideoUri: (Uri?) -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val videoFile = File(context.cacheDir, "video_${System.currentTimeMillis()}.mp4")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", videoFile)
            setVideoUri(uri)
            captureVideoLauncher.launch(uri)
            Log.d("VideoMessageHandler", "Launching video capture with URI: $uri")
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun launchPickVideo(
        context: Context,
        storagePermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
        pickVideoLauncher: ManagedActivityResultLauncher<String, Uri?>
    ) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            pickVideoLauncher.launch("video/mp4")
            Log.d("VideoMessageHandler", "Launching video picker")
        } else {
            storagePermissionLauncher.launch(permission)
        }
    }

    fun cleanup(context: Context, videoUri: Uri?) {
        videoUri?.let {
            try {
                context.contentResolver.delete(it, null, null)
                Log.d("VideoMessageHandler", "Cleaned up video URI: $it")
            } catch (e: Exception) {
                Log.e("VideoMessageHandler", "Error deleting video URI: ${e.message}")
            }
        }
    }
}