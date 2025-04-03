package util

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import okio.Path.Companion.toOkioPath // Thay vì toPath
import coil3.PlatformContext


// tối uuw kiểu éo hiinhf dữ liệu tránh tràn bộ nhớ

// Trong Application class hoặc một singleton
object ImageLoaderFactory {
    fun create(context: Context): ImageLoader {
        // Ép kiểu hoặc sử dụng context.applicationContext để đảm bảo tính tương thích
        val platformContext = context.applicationContext

        return ImageLoader.Builder(platformContext)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizeBytes(
                        (Runtime.getRuntime().maxMemory() * 0.25).toLong() // Manual calculation
                    ) // 25% bộ nhớ ứng dụng
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(platformContext.cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(100L * 1024 * 1024) // 100MB
                    .build()
            }
            .crossfade(true)
            .build()
    }
}