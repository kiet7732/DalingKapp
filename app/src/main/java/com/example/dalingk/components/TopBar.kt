package com.example.dalingk.components

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dalingk.screens.CustomLogo
import com.example.dalingk.R
import com.example.dalingk.components.ui.theme.DalingKTheme
import kotlinx.coroutines.delay
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.zIndex

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
    override fun onResume() {
        super.onResume()
        util.AppState.setAppForeground(true)
    }

    override fun onPause() {
        super.onPause()
        util.AppState.setAppForeground(false)
    }
}

@Composable
fun CircularIconButton(
    iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xffffdbe0),
    iconSize: Dp = 16.dp,
    buttonSize: Dp = 32.dp,
    contentDescription: String? = null,
    iconTint: Color? = null
) {
    IconButton(
        onClick = onClick,
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
                painter = painterResource(id = iconResId),
                contentDescription = contentDescription,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(iconSize),
                colorFilter = iconTint?.let { androidx.compose.ui.graphics.ColorFilter.tint(it) }
            )
        }
    }
}

@Composable
fun TopBarU(
    modifier: Modifier = Modifier,
    notificationMessage: String?
) {
    var showNotification by remember { mutableStateOf(false) }
    var currentMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(notificationMessage) {
        notificationMessage?.let { message ->
            currentMessage = message
            showNotification = true
            Log.d("TopBarU","--------------- ${ currentMessage }")
                delay(3000)
            showNotification = false
            currentMessage = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        // Thông báo Snackbar nằm trên
        AnimatedVisibility(
            visible = showNotification,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
            exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .zIndex(1f) // Đảm bảo nằm trên Column
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1F2937)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentMessage ?: "",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Nội dung chính bên dưới (Column bị đè lên)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(0f) // hoặc bỏ zIndex nếu không cần
                .padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CircularIconButton(
                    iconResId = R.drawable.icon_h1,
                    onClick = { println("Nút được nhấn! - left") },
                    iconSize = 20.dp,
                    buttonSize = 44.dp,
                    contentDescription = "Left Action",
                )

                Spacer(modifier = Modifier.weight(1f))

                CustomLogo(
                    modifier = Modifier.width(200.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                CircularIconButton(
                    iconResId = R.drawable.icon_h2,
                    onClick = { println("Nút được nhấn! - right") },
                    iconSize = 20.dp,
                    buttonSize = 44.dp,
                    contentDescription = "Right Action",
                )
            }
        }
    }

}



@Preview(showBackground = true)
@Composable
fun GreetingPreview7() {
    DalingKTheme {
    }
}