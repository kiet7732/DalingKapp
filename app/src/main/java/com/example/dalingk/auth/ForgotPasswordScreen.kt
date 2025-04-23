package com.example.dalingk.auth

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dalingk.auth.ui.theme.DalingKTheme
import com.google.firebase.auth.FirebaseAuth
import data.repository.AuthViewModel

class ForgotPasswordScreen : ComponentActivity() {
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

                }
            }
        }
    }
}


@Composable
fun ForgotPassword(
    viewModel: AuthViewModel,
    navController: NavController,
    onBack: () -> Unit,
    context: Context
) {
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9E9))
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 80.dp)
        ) {
            // Title
            Text(
                text = "Reset Your Password",
                color = Color.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Instruction text
            Text(
                text = "Enter your email address and we'll send you a link to reset your password",
                color = Color.Gray,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 16.sp),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Email input
            com.example.dalingk.components.detailUser.CustomOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "Enter your email",
                keyboardType = KeyboardType.Email,
                isError = errorMessage != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error message
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 14.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Success message
            successMessage?.let {
                Text(
                    text = it,
                    color = Color(0xFF4CAF50),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 14.sp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Reset Password button
            Button(
                onClick = {
                    if (isLoading) return@Button

                    errorMessage = when {
                        email.isBlank() -> "Please enter your email"
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                            "Invalid email format"
                        else -> null
                    }

                    if (errorMessage == null) {
                        isLoading = true
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    successMessage = "Password reset email sent. Please check your inbox."
                                    errorMessage = null
                                } else {
                                    errorMessage = task.exception?.message
                                        ?: "Failed to send reset email. Please try again."
                                    successMessage = null
                                }
                            }
                    }
                },
                enabled = !isLoading && successMessage == null, // Chặn nhấn sau khi thành công
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE506B)),
                contentPadding = PaddingValues(horizontal = 30.dp, vertical = 17.dp),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(
                    text = if (isLoading) "Sending..." else "Send Reset Link",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 18.sp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back to login
            Text(
                text = "Back to Login",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFE506B),
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview22() {
    DalingKTheme {
    }
}