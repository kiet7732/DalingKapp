package com.example.dalingk.screens

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.dalingk.auth.PhoneNumberInput
import com.example.dalingk.R
import com.example.dalingk.auth.ForgotPassword
import com.example.dalingk.navigation.Routes
import com.example.dalingk.ui.theme.DalingKTheme
import com.example.dalingk.auth.RegisterScreen
import data.repository.AuthViewModel

class IntroUI : ComponentActivity() {
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
//                    LoginScreen()
                }
            }
        }
    }
}

@Composable
fun LoginScreen(navController: NavController, currentScreen: String? = null, context: Context) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9E9))
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        val authViewModel = viewModel<AuthViewModel>()
        // Logo
//        Spacer(modifier = Modifier.height(0.1.dp))
        CustomLogo()
        var screenState by remember { mutableStateOf(if (currentScreen.isNullOrBlank()) Routes.InputLogin else Routes.TrailerScreen) }

//        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(visible = screenState == Routes.TrailerScreen) {
            LoginScreenStart { screenState = Routes.InputLogin }
        }

        AnimatedVisibility(visible = screenState == Routes.InputLogin) {
            PhoneNumberInput(
                viewModel = authViewModel,
                navController = navController,
                onNext = { screenState = Routes.Register },
                onNextSs2 = { navController.navigate(Routes.ForgotPassword) },
                context
            )
        }

        AnimatedVisibility(visible = screenState == Routes.Register) {
            RegisterScreen(
                viewModel = authViewModel,
                navController = navController,
                onBack = {
                    screenState = Routes.InputLogin
                }, // Quay lại màn hình nhập số điện thoại
                onSuccess = { navController.navigate(Routes.InputDetail) }, // Thêm callback mới
                context
            )
        }

        AnimatedVisibility(visible = screenState == Routes.ForgotPassword) {
            ForgotPassword(
                viewModel = authViewModel,
                navController = navController,
                onBack = {
                    screenState = Routes.InputLogin
                }, // Quay lại màn hình nhập số điện thoại
                context
            )
        }

        // Sign Up Link
        Row(
        ) {
            Text(text = "Don’t have an account?", color = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))

            Button(
                onClick = {
                    screenState = Routes.Register
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent, // Loại bỏ hình nền
                    contentColor = Color(0xFFFF6473) // Màu chữ
                ),
                contentPadding = PaddingValues(0.dp), // Loại bỏ padding
                elevation = null, // Loại bỏ hiệu ứng nổi
                modifier = Modifier
                    .background(Color.Transparent) // Đảm bảo không có hình nền
                    .offset(y = -13.dp)
            ) {
                Text(
                    text = "Register",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFFFF6473),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { }
                )
            }

        }

    }
}

// CustomLogo(logoSize = 80.dp, textSize = 24.sp, textOffset = (-10).dp) goi ham custom

@Composable
fun CustomLogo(
    modifier: Modifier = Modifier,
    logoSize: Dp = 50.dp, // Kích cỡ logo (mặc định: 60.dp)
    textSize: TextUnit = 20.sp, // Kích cỡ chữ (mặc định: 20.sp)
    textOffset: Dp = (-20).dp, // Độ lệch ngang của chữ
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Cupid Arrow Logo",
            modifier = Modifier
                .size(logoSize)
        )
        // Text
        Text(
            text = "DalingK",
            style = TextStyle(fontSize = textSize, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .align(Alignment.Bottom)
                .offset(x = textOffset),
            color = Color.Black
        )
    }
}


@Preview(showBackground = true)
@Composable
fun IntroUio() {
    DalingKTheme {
//        LoginScreen()
//        CustomLogo()
    }
}