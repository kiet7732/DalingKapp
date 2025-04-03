package com.example.dalingk.components.detailUser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dalingk.ui.theme.DalingKTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import data.viewmodel.UserInputViewModel

class LookingFor : ComponentActivity() {
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
                    LookingForUI()
                }
            }
        }
    }
}



@Composable
fun SelectableOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .requiredWidth(325.dp)
            .requiredHeight(54.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White)
            .border(
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.5.dp,
                    color = if (isSelected) Color(0xFFFF5069) else Color(0xFFFFFFFF)
                ),
                shape = RoundedCornerShape(100.dp)
            )
            .clickable(onClick = onClick)
    ) {
        // Circle Indicator
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-20).dp)
                .requiredSize(24.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White)
                .border(
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.5.dp,
                        color = if (isSelected) Color(0xFFFF5069) else Color(0xFF999999)
                    ),
                    shape = RoundedCornerShape(50.dp)
                )
        )
        // Text Label
        Text(
            text = text,
            color = Color.Black,
            style = TextStyle(fontSize = 16.sp),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 20.dp)
        )
    }
}


@Composable
fun LookingForUi(viewModel: UserInputViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {

//        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "I AM LOOKING FOR...",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Provide us with further insights into your preferences",
            style = TextStyle(
                fontSize = 16.sp,
                color = Color.Gray
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(280.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Frame34(viewModel)
    }
}

@Composable
fun Frame34(viewModel: UserInputViewModel, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .requiredWidth(375.dp)
            .requiredHeight(296.dp)
    ) {
        val options = listOf(
            "A relationship",
            "Something casual",
            "I’m not sure yet",
            "Prefer not to say"
        )

        options.forEach { option ->
            SelectableOption(
                text = option,
                isSelected = viewModel.lookingFor.value == option,
                onClick = { viewModel.lookingFor.value = option }
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun LookingForUI() {
    DalingKTheme {
//        LookingForUi()
    }
}