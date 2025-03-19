package com.example.quickload

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

import android.webkit.URLUtil.isValidUrl
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch

import android.util.Patterns

class MainActivity : ComponentActivity() {
    private val STORAGE_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            }
        }

        setContent {
            DownloaderApp()
        }
    }
}

@Composable
fun DownloaderApp() {
    var url by remember { mutableStateOf(TextFieldValue()) }
    var isDownloading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("QuickLoad", fontSize = 28.sp, modifier = Modifier.padding(bottom = 16.dp))

        // URL Input Field
        BasicTextField(
            value = url,
            onValueChange = {
                url = it
                errorMessage = null  // Reset error message when typing
            },
            textStyle = TextStyle(fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray, shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
        )

        // Show error message if exists
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Download Button
        Button(
            onClick = {
                val urlText = url.text.trim()
                if (!isValidUrl(urlText)) {
                    errorMessage = "Invalid URL! Please check spelling."
                } else {
                    coroutineScope.launch {
                        isDownloading = true
                        downloadFile(context, urlText) { error ->
                            errorMessage = error
                        }
                        isDownloading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isDownloading
        ) {
            if (isDownloading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text("Download File")
            }
        }
    }
}


fun isValidUrl(url: String): Boolean {
    return Patterns.WEB_URL.matcher(url).matches()
}
