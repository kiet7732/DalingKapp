package com.example.dalingk.components.matches

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dalingk.ui.theme.DalingKTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.example.dalingk.R

class MatchActionButton : ComponentActivity() {
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
                    GreetingPreview4()
                }
            }
        }
    }
}

@Composable
fun MatchButton(
    modifier: Modifier = Modifier,
    size: Dp,
    backgroundColor: Color,
    iconResId: Int,
    iconSize: Dp,
    iconTint: Color,
    iconOffsetX: Dp,
    iconOffsetY: Dp,
    contentDescription: String? = null,
    onClick: () -> Unit // Thêm hành động khi bấm
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .requiredSize(size = size)
            .clickable(onClick = onClick) // Thêm hành động bấm
    ) {
        // Vòng tròn nền
        Box(
            modifier = Modifier
                .requiredSize(size = size)
                .clip(CircleShape)
                .background(color = backgroundColor)
        )
        // Icon hoặc Image
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = iconOffsetX, y = iconOffsetY)
                .requiredSize(size = iconSize)
        )
    }
}

@Composable
fun ActionButtonsGroup(
    onDislikeClick: () -> Unit,
    onLikeClick: () -> Unit,
    onSuperLikeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(350.dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MatchButton(
            size = 65.dp, // Tăng 5dp
            backgroundColor = Color.White,
            iconResId = R.drawable.ic_close,
            iconSize = 26.dp, // Tăng icon để cân đối
            iconTint = Color(0xffff5069),
            iconOffsetX = 20.dp,
            iconOffsetY = 20.dp,
            contentDescription = "close",
            onClick = onDislikeClick
        )

        MatchButton(
            size = 85.dp, // Tăng 5dp
            backgroundColor = Color(0xffff5069),
            iconResId = R.drawable.ic_heart,
            iconSize = 45.dp, // Tăng icon để cân đối
            iconTint = Color.White,
            iconOffsetX = 21.dp,
            iconOffsetY = 21.dp,
            contentDescription = "like",
            onClick = onLikeClick
        )

        MatchButton(
            size = 65.dp, // Tăng 5dp
            backgroundColor = Color(0xffffb431),
            iconResId = R.drawable.ic_star,
            iconSize = 26.dp, // Tăng icon để cân đối
            iconTint = Color.White,
            iconOffsetX = 20.dp,
            iconOffsetY = 20.dp,
            contentDescription = "star",
            onClick = onSuperLikeClick
        )
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview4() {
    DalingKTheme {
        ActionButtonsGroup(
            onDislikeClick = {
                // Xử lý hành động Dislike
                println("Dislike clicked!")
            },
            onLikeClick = {
                // Xử lý hành động Like
                println("Like clicked!")
            },
            onSuperLikeClick = {
                // Xử lý hành động Super Like
                println("Super Like clicked!")
            }
        )
    }
}