package com.example.dalingk.auth

import android.content.Context
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import data.repository.AuthViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dalingk.R

class Register : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            com.example.dalingk.ui.theme.DalingKTheme {
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
fun RegisterScreen(
    viewModel: AuthViewModel,
    navController: NavController,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    context: Context
) {
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(Color(0xFFFFE9E9))
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp) // Đẩy nội dung xuống để không bị che bởi mũi tên
        ) {
            // Tiêu đề
            Text(
                text = stringResource(id = R.string.textintro_12),
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Nhập email
            com.example.dalingk.components.detailUser.CustomOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email,
                placeholder = "Enter your email",
                isError = errorMessage != null
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Nhập số điện thoại
            com.example.dalingk.components.detailUser.CustomOutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = "Phone Number",
                keyboardType = KeyboardType.Phone,
                placeholder = "Enter your phone",
                isError = errorMessage != null

            )

            Spacer(modifier = Modifier.height(10.dp))

            // Nhập mật khẩu
            com.example.dalingk.components.detailUser.CustomOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                keyboardType = KeyboardType.Password,
                placeholder = "Enter your password",
                isError = errorMessage != null
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Hiển thị lỗi nếu có
            errorMessage?.let {
                Text(text = it, color = Color.Red, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nút đăng ký
            Button(
                onClick = {
                    errorMessage = when {
                        email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                            .matches() ->
                            "Invalid email format"

                        phoneNumber.isBlank() ->
                            "Phone number cannot be empty"

                        phoneNumber.length < 10 ->
                            "Phone must be at least 10 characters"

                        password.isBlank() ->
                            "Password cannot be empty"

                        password.length < 6 ->
                            "Password must be at least 6 characters"

                        else -> null
                    }

                    if (errorMessage == null) {
                        viewModel.registerUser(context, email, password, phoneNumber, {
                            onSuccess()
                        }, {
                            println("DEBUG: Lỗi đăng ký: $it")
                            errorMessage = it
                        })

                    }
                },

                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffe506b)),
                contentPadding = PaddingValues(horizontal = 30.dp, vertical = 17.dp),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(
                    text = stringResource(id = R.string.textintro_13),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 18.sp)
                )
            }

            Text(
                text = stringResource(id = R.string.textintro_14),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFE506B), // Màu đỏ
                modifier = Modifier
                    .padding(start = 10.dp, top = 20.dp, bottom = 40.dp)
                    .clickable { onBack() } // Gọi callback để quay về InputLogin
            )

        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview8() {

}