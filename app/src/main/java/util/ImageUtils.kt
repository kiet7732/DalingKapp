package util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    fun compressImageToFile(context: Context, uri: Uri, maxSizeKB: Int = 200): File {
        // Đọc ảnh từ URI thành Bitmap
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Tạo file tạm để lưu ảnh nén
        val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(compressedFile)

        // Nén ảnh với chất lượng ban đầu
        val output = ByteArrayOutputStream()
        var quality = 100
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)

        // Giảm chất lượng cho đến khi kích thước dưới maxSizeKB
        while (output.size() / 1024 > maxSizeKB && quality > 10) {
            output.reset()
            quality -= 10
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
        }

        // Ghi dữ liệu nén vào file
        output.writeTo(outputStream)
        outputStream.close()
        output.close()

        return compressedFile
    }
}