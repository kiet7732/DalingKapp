package com.example.dalingk.components.detailUser

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import com.example.dalingk.components.detailUser.ui.theme.DalingKTheme

class DisplayPhotos : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DalingKTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                )
                {

                }
            }
        }
    }
}


@Composable
fun DisplayUserPhotos(imageUrls: List<String>?, context: Context) {
    val photos = remember(imageUrls) {
        if (imageUrls == null) {
            List(6) { "" }
        } else {
            (imageUrls + List(6 - imageUrls.size.coerceAtMost(6)) { "" }).take(6)
        }
    }

    // Sử dụng LazyColumn để lazy loading
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 510.dp), // Giới hạn chiều cao tối đa
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column {
                    ReusableFrameUser(
                        width = 206.dp,
                        height = 210.dp,
                        imageUri = photos[0].takeIf { it.isNotEmpty() }?.let { Uri.parse(it) }
                    )
                    Row {
                        for (i in 1..2) {
                            ReusableFrameUser(
                                width = 100.dp,
                                height = 100.dp,
                                imageUri = photos[i].takeIf { it.isNotEmpty() }
                                    ?.let { Uri.parse(it) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    for (i in 3..5) {
                        ReusableFrameUser(
                            width = 100.dp,
                            height = 100.dp,
                            imageUri = photos[i].takeIf { it.isNotEmpty() }?.let { Uri.parse(it) }
                        )
                    }
                }
            }
        }
    }
    // Xóa bộ nhớ đệm khi composable bị hủy
    DisposableEffect(Unit) {
        onDispose {
            val imageLoader = ImageLoader(context)
            imageLoader.memoryCache?.clear() // Xóa bộ nhớ đệm ảnh
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview17() {
    DalingKTheme {

    }
}