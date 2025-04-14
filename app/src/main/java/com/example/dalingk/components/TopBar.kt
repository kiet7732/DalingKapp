package com.example.dalingk.components

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dalingk.screens.CustomLogo
import com.example.dalingk.R
import com.example.dalingk.components.ui.theme.DalingKTheme

class TopBar : ComponentActivity() {
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
                    GreetingPreview7()
                }
            }
        }
    }
}

@Composable
fun CircularIconButton(
    iconResId: Int, // Resource ID của icon
    onClick: () -> Unit, // Hàm xử lý sự kiện click
    modifier: Modifier = Modifier, // Modifier để tùy chỉnh ngoài
    backgroundColor: Color = Color(0xffffdbe0), // Màu nền mặc định
    iconSize: Dp = 16.dp, // Kích thước icon
    buttonSize: Dp = 32.dp, // Kích thước nút
    icon: Any,
    contentDescription: String,
    iconTint: Color
) {
    IconButton(
        onClick = onClick, // Hành động khi click
        modifier = modifier
            .clip(shape = RoundedCornerShape(100.dp))
            .background(color = backgroundColor)
            .size(buttonSize)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(buttonSize)
        ) {
            Image(
                painter = painterResource(id = iconResId), // Icon resource ID
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(iconSize) // Kích thước icon
            )
        }
    }
}

@Composable
fun TopBarU(modifier : Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .padding(WindowInsets.systemBars.asPaddingValues()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CircularIconButton(
            iconResId = R.drawable.icon_h1,
            onClick = { println("Nút được nhấn! - left") },
            iconSize = 20.dp,
            buttonSize = 44.dp,
            icon = Icons.Default.ArrowBack,
            contentDescription = "Back to Login",
            iconTint = Color.Black
        )

        Spacer(modifier = Modifier.weight(1f)) // Đẩy logo vào giữa

        CustomLogo(
            modifier = Modifier.width(200.dp) // Điều chỉnh kích thước logo
        )

        Spacer(modifier = Modifier.weight(1f)) // Đẩy icon bên phải ra xa

        CircularIconButton(
            iconResId = R.drawable.icon_h2,
            onClick = { println("Nút được nhấn! - right") },
            iconSize = 20.dp,
            buttonSize = 44.dp,
            icon = Icons.Default.ArrowBack,
            contentDescription = "Back to Login",
            iconTint = Color.Black
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview7() {
    DalingKTheme {
        TopBarU()
    }
}