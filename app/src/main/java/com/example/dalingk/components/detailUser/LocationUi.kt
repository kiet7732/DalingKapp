package com.example.dalingk.components.detailUser

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dalingk.components.WheelPicker
import com.example.dalingk.components.detailUser.ui.theme.DalingKTheme
import data.viewmodel.UserInputViewModel

class LocationUi : ComponentActivity() {
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
                    GreetingPreview19()
                }
            }
        }
    }
}


@Composable
fun Location(viewModel: UserInputViewModel) {
    val cities = cities
    val locationState = remember { viewModel.location }
    val currentLocation = locationState.value

//    Log.d("LocationDebug", "Current Location: $currentLocation")

    val initialIndex = cities.indexOf(currentLocation).takeIf { it >= 0 } ?: 0
//    Log.d("LocationDebug", "Initial Index: $initialIndex")

    val listState = rememberLazyListState()

    // Cuộn đến vị trí ban đầu khi khởi tạo
    LaunchedEffect(Unit) {
        listState.scrollToItem(initialIndex)
    }

    WheelPicker(
        items = cities,
        onItemSelected = { newLocation ->
            locationState.value = newLocation
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        visibleItemCount = 5,
        listState = listState // Truyền listState vào WheelPicker
    )
}

val cities = listOf(
    "Hồ Chí Minh",
    "Hà Nội",
    "Đà Nẵng",
    "Bình Dương",
    "Hải Phòng",
    "Cần Thơ",
    "Huế",
    "Nha Trang",
    "Đà Lạt",
    "Quy Nhơn",
    "Vũng Tàu"
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview19() {
    DalingKTheme {
        val viewModel: UserInputViewModel = viewModel()
        Location(viewModel)
    }
}