package data.model

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryHelper {
    private const val CLOUD_NAME = "dk0dn25hc"
    private const val API_KEY = "756142988118212"
    private const val API_SECRET = "sC-P5fnaYaa_n4jC3DhRu821TZ0"
    private const val UPLOAD_PRESET = "DalingKApp"

    private var isInitialized = false // Biến để theo dõi trạng thái khởi tạo

    fun initialize(context: Context) {
        if (!isInitialized) {
            val config = mapOf(
                "cloud_name" to CLOUD_NAME,
                "api_key" to API_KEY,
                // "api_secret" to API_SECRET // Đã được comment trong code gốc
            )
            try {
                MediaManager.init(context, config)
                isInitialized = true
            } catch (e: IllegalStateException) {
                // Bỏ qua nếu MediaManager đã được khởi tạo
            }
        }
    }

    fun getUploadPreset(): String = UPLOAD_PRESET
}