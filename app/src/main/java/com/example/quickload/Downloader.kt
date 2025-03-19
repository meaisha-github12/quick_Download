package com.example.quickload

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast

fun downloadFile(context: Context, url: String) {
    try {
        val uri = Uri.parse(url)
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)?.lowercase() ?: ""
        val destinationDirectory = when (extension) {
            "jpg", "jpeg", "png", "gif" -> Environment.DIRECTORY_PICTURES
            else -> Environment.DIRECTORY_DOWNLOADS
        }
        val fileName = "downloadedFile.${if (extension.isNotEmpty()) extension else "file"}"

        val request = DownloadManager.Request(uri)
            .setTitle("Downloading File")
            .setDescription("Please wait...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(destinationDirectory, fileName)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        Log.d("Downloader", "Enqueued download with ID: $downloadId")

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    Toast.makeText(context, "Download Complete", Toast.LENGTH_SHORT).show()
                    Log.d("Downloader", "Download completed with ID: $id")
                    context.unregisterReceiver(this)
                }
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        Toast.makeText(context, "Download Started", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("Downloader", "Download failed", e)
        Toast.makeText(context, "Invalid URL or Download Failed", Toast.LENGTH_SHORT).show()
    }
}
