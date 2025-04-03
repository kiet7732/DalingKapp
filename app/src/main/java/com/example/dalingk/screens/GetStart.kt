package com.example.dalingk.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dalingk.R
import com.example.dalingk.ui.theme.DalingKTheme

class GetStart : ComponentActivity() {
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
                    GetStarted()
                }
            }
        }
    }
}

// User Images Row
@Composable
fun MaskGroup() {

    Box(
        modifier = Modifier
            .requiredWidth(width = 375.dp)
            .requiredHeight(height = 367.dp)
            .rotate(180f) // Lật ngược toàn bộ Box
    ) {
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = 0.dp, y = 8.dp)
                .requiredWidth(width = 557.dp)
                .requiredHeight(height = 349.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.linebig),
                contentDescription = "Vector",
                alpha = 0.5f,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 0.00006103515625.dp, y = 31.763412475585938.dp)
                    .requiredWidth(width = 557.dp)
                    .requiredHeight(height = 317.dp)
                    .clip(shape = RoundedCornerShape(24.dp))
                    .rotate(degrees = 180f)
            )
            Image(
                painter = painterResource(id = R.drawable.user11),
                contentDescription = "Ellipse 12",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 110.dp, y = 223.dp)
                    .requiredSize(size = 110.dp)
                    .clip(shape = CircleShape)
                    .rotate(degrees = 180f)
                    .border(4.dp, Color.White, CircleShape)
            )
            Image(
                painter = painterResource(id = R.drawable.user11),
                contentDescription = "Ellipse 67",
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 300.dp, y = 102.dp)
                    .requiredSize(size = 68.dp)
                    .clip(shape = CircleShape)
                    .rotate(degrees = 180f)
                    .border(4.dp, Color.White, CircleShape)
            )
            Image(
                painter = painterResource(id = R.drawable.user11),
                contentDescription = "Ellipse 8",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .offset(x = 300.dp, y = 260.dp)
                    .requiredSize(size = 134.dp)
                    .clip(shape = CircleShape)
                    .rotate(degrees = 180f)
                    .border(4.dp, Color.White, CircleShape)
            )
        }
    }
}


@Composable
fun LoginScreenStart(onLoginClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        MaskGroup()

        Spacer(modifier = Modifier.height(20.dp))

        // Main Text
        Text(
            text = "Discover Love Where Your Story Begins.",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Join us to discover your ideal partner and ignite the sparks of romance in your journey.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Button
        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(0.8f)
            ,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6473))
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Phone Icon",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Login with Gmail", color = Color.White,
                modifier = Modifier.padding(4.dp))
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GetStarted() {
    DalingKTheme {
        LoginScreenStart {"PhoneNumberInput" }
    }
}