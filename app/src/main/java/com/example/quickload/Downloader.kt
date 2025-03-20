package com.example.quickload

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.webkit.URLUtil
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.File

fun downloadFile(
    context: Context,
    fileUrl: String,
    onError: (String) -> Unit,
    onSuccess: () -> Unit
) {
    try {
        if (!Patterns.WEB_URL.matcher(fileUrl).matches()) {
            onError("❌ Invalid URL! Please check spelling.")
            return
        }

        val fileName = URLUtil.guessFileName(fileUrl, null, null) // Detect file name
        val request = DownloadManager.Request(Uri.parse(fileUrl))
            .setTitle(fileName)
            .setDescription("Downloading...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName) // **Save to Downloads**

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // Checks if download is successful
        Handler(Looper.getMainLooper()).postDelayed({
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    onSuccess()
                    // Call scanFile to refresh gallery and file manager
                    val filePath =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            .toString() + "/" + fileName
                    scanFile(context, filePath)
                } else {
                    onError("⚠️ Download failed! Please try again.")
                }
            } else {
                onError("⚠️ Unable to fetch download status.")
            }
            cursor.close()
        }, 3000) // Delay to check status

    } catch (e: Exception) {
        onError("❌ Error: ${e.message ?: "Unknown error occurred"}")
    }
}
fun scanFile(context: Context, filePath: String) {
    MediaScannerConnection.scanFile(context, arrayOf(filePath), null) { _, uri ->
        Toast.makeText(context, "File saved: $uri", Toast.LENGTH_SHORT).show()
    }}

