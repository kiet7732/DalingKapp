package com.example.dalingk.components.detailUser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dalingk.ui.theme.DalingKTheme
import data.viewmodel.UserInputViewModel

class InputDetailUi : ComponentActivity() {
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
                    InputDetail()
                }
            }
        }
    }
}

@Composable
fun IntroFormUI(viewModel: UserInputViewModel) {
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text(
            text = "Oh hey! Let’s start with an intro.",
            fontSize = 24.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your first name",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        CustomOutlinedTextField(
            value = viewModel.fullName.value,
            onValueChange = { viewModel.fullName.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            label = "Full name",
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            shape = RoundedCornerShape(15.dp),
            placeholder = "Enter your full name"
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your birthday",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomOutlinedTextField(
                value = viewModel.birthMonth.value,
                onValueChange = {
                    if (it.length <= 2 && it.all { char -> char.isDigit() }) viewModel.birthMonth.value = it
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp),
                label = "Month",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
                shape = RoundedCornerShape(15.dp),
                placeholder = "MM"
            )
            CustomOutlinedTextField(
                value = viewModel.birthDay.value,
                onValueChange = {
                    if (it.length <= 2 && it.all { char -> char.isDigit() }) viewModel.birthDay.value = it
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp),
                label = "Day",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
                shape = RoundedCornerShape(15.dp),
                placeholder = "DD"
            )
            CustomOutlinedTextField(
                value = viewModel.birthYear.value,
                onValueChange = {
                    if (it.length <= 4 && it.all { char -> char.isDigit() }) viewModel.birthYear.value = it
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp),
                label = "Year",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                shape = RoundedCornerShape(15.dp),
                placeholder = "YYYY"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "It’s never too early to count down",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}


@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    shape: RoundedCornerShape = RoundedCornerShape(15.dp),
    placeholder: String
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current  // Quản lý focus để ẩn bàn phím
    val interactionSource = remember { MutableInteractionSource() }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        modifier = modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null) {
                focusManager.clearFocus() // Ẩn bàn phím khi nhấn vào khoảng trống
            },
        shape = shape,
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = keyboardType
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus() // Ẩn bàn phím khi nhấn "Done"
            }
        ),
    )
}


@Preview(showBackground = true)
@Composable
fun InputDetail() {
    DalingKTheme {
//        IntroFormUI()
    }
}