package util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream


object FileUtil {
    fun getPath(context: Context, uri: Uri): String? {
        return when (uri.scheme) {
            "content" -> {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (columnIndex >= 0) {
                            val fileName = it.getString(columnIndex)
                            val file = File(context.cacheDir, fileName)
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                file.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            file.absolutePath
                        } else null
                    } else null
                }
            }
            "file" -> uri.path
            else -> null
        }
    }
}
