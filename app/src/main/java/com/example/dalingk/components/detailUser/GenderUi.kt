package com.example.dalingk.components.detailUser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dalingk.R
import com.example.dalingk.ui.theme.DalingKTheme
import data.viewmodel.UserInputViewModel

class GenderUi : ComponentActivity() {
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
                    GenderUI()
                }

            }
        }
    }
}


@Composable
fun GenderSelectionScreen(viewModel: UserInputViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
//        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "What's Your Gender?",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Tell us about your gender",
            style = TextStyle(
                fontSize = 16.sp,
                color = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(48.dp))

        ChoiceGender(viewModel)
    }
}

@Composable
fun ChoiceGender(viewModel: UserInputViewModel, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(48.dp, Alignment.Top),
        modifier = modifier
    ) {
        ChoiceGenderItem(
            title = "Male",
            imageResId = R.drawable.male,
            isSelected = viewModel.gender.value == "Male",
            onClick = { viewModel.gender.value = "Male" }
        )

        ChoiceGenderItem(
            title = "Female",
            imageResId = R.drawable.female,
            isSelected = viewModel.gender.value == "Female",
            onClick = { viewModel.gender.value = "Female" }
        )
    }
}


@Composable
fun ChoiceGenderItem(
    title: String,
    imageResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(shape = CircleShape)
            .requiredSize(150.dp)
            .clickable { onClick(

            ) }
    ) {
        Box(
            modifier = Modifier
                .requiredSize(150.dp)
                .clip(shape = CircleShape)
                .background(color = if (isSelected) Color(0xfffe506b) else Color.Gray)
        )
        Text(
            text = title,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 0.dp, y = 98.dp)
        )
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = title,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 46.dp, y = 31.dp)
                .requiredSize(59.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GenderUI() {
    DalingKTheme {
//        GenderSelectionScreen()
    }
}