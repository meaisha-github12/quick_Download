package com.example.quickload

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.webkit.URLUtil.isValidUrl
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloaderApp() {
    val fontSemi = FontFamily(Font(R.font.inter_semi_bold))
    val fontLight = FontFamily(Font(R.font.inter_extra_light))
    var url by remember { mutableStateOf(TextFieldValue()) }
    var isDownloading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF62686F)), // SAME BACKGROUND

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Image(painter = painterResource(R.drawable.icon), contentDescription =" App Icon",
                Modifier.size(280.dp)
            )

            // **App Title**
            Text(
                text = "QuickLoad",
                fontSize = 32.sp,
                fontFamily = fontSemi,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )

        // **URL Input Field**
       TextField(
            value = url,
            onValueChange = {
                url = it
                errorMessage = null  // Reset error message when typing
            },
           placeholder = { Text(text = "Enter URL", color = Color.Gray, fontSize = 16.sp) },

            textStyle = TextStyle(fontSize = 16.sp),
           colors = TextFieldDefaults.textFieldColors(
               containerColor = Color.Transparent,
               focusedIndicatorColor = Color.Transparent,
               unfocusedIndicatorColor = Color.Transparent,
               disabledIndicatorColor = Color.Transparent
           ),

            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(14.dp))

        )
            Text(
                text = "💡 Tip: Enter multiple URLs on separate lines.",
                color = Color.LightGray,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.inter_extra_light)),
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(top = 6.dp)
            )

        // **Show error message if exists**
        errorMessage?.let {
            Text(
                text = it,
                color = Color(0xFFFF8989),
                fontSize = 14.sp,
                fontFamily = fontLight,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // **Download Button with Loading Indicator**
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(50.dp)

                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF41444B), Color(0xFF777269),
                            Color(0xFF41444B)
                        )
                    ),

                    shape = RoundedCornerShape(50)
                )

                .clickable(enabled = !isDownloading) {
                    val urls = url.text.split("\n", ",", " ").map { it.trim() }.filter { it.isNotEmpty() }

                    if (urls.isEmpty() || urls.any { !isValidUrl(it) }) {
                        errorMessage = "❌ Invalid URL(s)! Please check spelling."
                        return@clickable
                    }

                    isDownloading = true
                    errorMessage = "⏳ Downloading files..."

                    // Launch multiple downloads concurrently
                    urls.forEach { link ->
                        coroutineScope.launch {
                            downloadFile(
                                context,
                                link,
                                onError = { error -> errorMessage = error },
                                onSuccess = { errorMessage = "✅ All files downloaded!" }
                            )
                        }
                    }

                    url = TextFieldValue() // Clear input field after starting downloads
                    isDownloading = false
                }
            ,
            contentAlignment = Alignment.Center
        ) {
            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(
                    text = "Download File",
                    fontSize = 18.sp, // Adjust text size
                    fontFamily = FontFamily(Font(R.font.inter_semi_bold)), // Apply custom font
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp)
                    )
            }
        }
    }
    }
}

fun isValidUrl(url: String): Boolean {
    return Patterns.WEB_URL.matcher(url).matches()
}

