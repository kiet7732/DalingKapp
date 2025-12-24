package data.chat.video

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

suspend fun compressVideo(context: Context, videoUri: Uri, maxSizeMB: Int = 5): File {
    return withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val inputFile = File(context.cacheDir, "input_video_$timestamp.mp4")
        val outputFile = File(context.cacheDir, "compressed_video_$timestamp.mp4")

        try {
            // Copy data from videoUri to inputFile
            context.contentResolver.openInputStream(videoUri)?.use { input ->
                FileOutputStream(inputFile).use { output ->
                    input.copyTo(output)
                    output.flush()
                    output.fd.sync()
                    Log.d("VideoUtils", "Copied input file: ${inputFile.absolutePath}, size: ${inputFile.length() / 1024} KB")
                }
            } ?: throw Exception("Cannot open input stream for video: $videoUri")

            if (!inputFile.exists() || inputFile.length() == 0L) {
                throw Exception("Input file is invalid or empty: ${inputFile.absolutePath}")
            }

            // Validate video format (basic check for MP4)
            val mimeType = context.contentResolver.getType(videoUri)
            if (mimeType != "video/mp4") {
                throw Exception("Unsupported video format: $mimeType. Only MP4 is supported.")
            }

            // Check file size
            val fileSizeMB = inputFile.length() / (1024 * 1024)
            if (fileSizeMB > maxSizeMB) {
                // TODO: Integrate FFmpeg for compression
                // For now, throw an error to inform the user
                throw Exception("Video size ($fileSizeMB MB) exceeds limit of $maxSizeMB MB. Compression not implemented.")
            }

            // Copy to output file (no compression for now)
            inputFile.copyTo(outputFile, overwrite = true)
            FileOutputStream(outputFile, true).use { output ->
                output.flush()
                output.fd.sync()
            }

            if (!outputFile.exists() || outputFile.length() == 0L) {
                throw Exception("Failed to create compressed file: ${outputFile.absolutePath}")
            }
            Log.d("VideoUtils", "Created output file: ${outputFile.absolutePath}, size: ${outputFile.length() / 1024} KB")

            inputFile.delete()
            outputFile
        } catch (e: Exception) {
            Log.e("VideoUtils", "Error compressing video: ${e.message}", e)
            inputFile.delete()
            outputFile.delete()
            throw e
        }
    }
}