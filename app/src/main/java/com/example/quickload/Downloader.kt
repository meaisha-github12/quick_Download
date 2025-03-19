package com.example.quickload

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.File

fun downloadFile(context: Context, fileUrl: String, onError: (String) -> Unit) {
    try {
        val fileName = fileUrl.substringAfterLast("/")
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // Define the destination directory (Downloads)
        val destinationDirectory = Environment.DIRECTORY_DOWNLOADS

        // Create request
        val request = DownloadManager.Request(Uri.parse(fileUrl))
            .setTitle("Downloading...")
            .setDescription("Downloading $fileName")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(destinationDirectory, fileName)

        // Enqueue download
        val downloadId = downloadManager.enqueue(request)

        // Register a receiver to scan the file so it appears in the gallery
        val filePath = "${Environment.getExternalStoragePublicDirectory(destinationDirectory)}/$fileName"
        MediaScannerConnection.scanFile(
            context,
            arrayOf(filePath),
            null
        ) { path, uri ->
            Log.d("Download", "File Scanned: $path, URI: $uri")
        }

        Toast.makeText(context, "Download Started", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("Download Error", "Failed to download: ${e.message}")
        onError("Failed to download file. Please check the URL.")
    }
}
