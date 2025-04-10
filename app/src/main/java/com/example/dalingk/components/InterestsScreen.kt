package com.example.dalingk.components

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudinary.transformation.Expression.height
import com.example.dalingk.R
import com.example.dalingk.components.ui.theme.DalingKTheme
import com.example.dalingk.screens.GreetingPreview15

class InterestsScreen : ComponentActivity() {
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
                    GreetingPreview21()
                }
            }
        }
    }
}

@Composable
fun CardInterests(
    backgroundColor: Color,
    jsonFileName: Int,
    text: String,
    badgeText: String,
    height: Dp = 280.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(15.dp)
    ) {
        // Badge in the top-right corner
        if (badgeText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Icon centered in the Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            val animationModifier = if (height == 280.dp) {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .scale(1.3f)
            } else {
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.9f)
            }

            LottieAnimationCus(
                jsonFileName = jsonFileName,
                modifier = animationModifier,
                loop = false
            )
        }

        // Text at the bottom-left corner
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Start,
            modifier = Modifier
                .align(Alignment.BottomStart)
        )
    }
}


@Composable
fun InterestsScreenUI() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Chào mừng đến với thẻ khám phá",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp,
                modifier = Modifier.padding(5.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Fourth Card (Bottom Right)
                CardInterests(
                    backgroundColor = Color(0xFFFF8A5A), // Orange
                    jsonFileName = R.raw.heart_a,
                    text = "Tìm người yêu",
                    badgeText = "",
                    modifier = Modifier
                        .weight(1f), height = 250.dp
                )
            }
        }

        item {
            Text(
                text = "Hẹn hò chung mục đích",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp,
                modifier = Modifier.padding(top = 5.dp)
            )

            Text(
                text = "Tìm kiếm những người có chung mục đích hẹn hò",
                color = Color.Gray,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 5.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // First Card (Top Left)
                CardInterests(
                    backgroundColor = Color(0xFFFF5A5F), // Red
                    jsonFileName = R.raw.moon_a,
                    text = "Rảnh tối nay",
                    badgeText = "",
                    modifier = Modifier.weight(1f)
                )

                // Second Card (Top Right)
                CardInterests(
                    backgroundColor = Color(0xFF5A9CFF), // Blue
                    jsonFileName = R.raw.passport_a,
                    text = "Du lịch",
                    badgeText = "",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Third Card (Bottom Left)
                CardInterests(
                    backgroundColor = Color(0xFF8A5AFF), // Purple
                    jsonFileName = R.raw.movie_a,
                    text = "Hội mê Phím",
                    badgeText = "",
                    modifier = Modifier.weight(1f)
                )

                // Fourth Card (Bottom Right)
                CardInterests(
                    backgroundColor = Color(0xFFFF8A5A), // Orange
                    jsonFileName = R.raw.sport_a,
                    text = "Yêu thể thao",
                    badgeText = "",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview21() {
    DalingKTheme {
        InterestsScreenUI()


    }
}