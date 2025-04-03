package com.example.dalingk.components

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dalingk.components.ui.theme.DalingKTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dalingk.components.detailUser.cities
import com.example.dalingk.screens.GreetingPreview12
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class WheelPickerUI : ComponentActivity() {
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
fun WheelPicker(
    items: List<String>,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemCount: Int = 5,
    textStyle: TextStyle = TextStyle(fontSize = 20.sp, color = Color.Black),
    selectedTextStyle: TextStyle = TextStyle(fontSize = 24.sp, color = Color(0xffff5069)),
    listState: LazyListState
) {
    val itemHeight = 60.dp
    val coroutineScope = rememberCoroutineScope()

    val selectedIndex by remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            val itemSize = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: itemHeight.value.toInt()
            val middleOffset = (itemHeight.value * (visibleItemCount / 2)).toInt()

            val adjustedIndex = if (itemSize > 0) {
                (firstVisible + (offset + middleOffset) / itemSize).coerceIn(0, items.size - 1)
            } else {
                firstVisible.coerceIn(0, items.size - 1)
            }
            adjustedIndex
        }
    }

    LaunchedEffect(selectedIndex) {
        onItemSelected(items[selectedIndex])
    }

    Box(
        modifier = modifier
            .height(itemHeight * visibleItemCount)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    val scrollAmount = (dragAmount / itemHeight.value).toInt()
                    val newIndex = (listState.firstVisibleItemIndex + scrollAmount).coerceIn(0, items.size - 1)
                    coroutineScope.launch {
                        listState.animateScrollToItem(newIndex)
                    }
                }
            }
    ) {
        LazyColumn(
            state = listState, // Sử dụng listState từ tham số
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeight * (visibleItemCount / 2))
        ) {
            items(items.size) { index ->
                val isSelected = index == selectedIndex
                Text(
                    text = items[index],
                    style = if (isSelected) selectedTextStyle.copy(fontWeight = FontWeight.Bold) else textStyle,
                    modifier = Modifier
                        .height(itemHeight)
                        .wrapContentHeight(CenterVertically)
                        .alpha(if (isSelected) 1f else 0.5f)
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview18() {
    DalingKTheme {

    }
}