package com.example.dalingk.auth

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dalingk.ui.theme.DalingKTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import data.repository.AuthViewModel

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import com.example.dalingk.R
import com.example.dalingk.navigation.Routes
import com.example.dalingk.screens.updateLanguage
import data.model.LanguagePreferences
import kotlinx.coroutines.launch

class StartUI : ComponentActivity() {
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
                    LoginGmail()
                }
            }
        }
    }
}

@Composable
fun PhoneNumberInput(
    viewModel: AuthViewModel,
    navController: NavController,
    onNext: () -> Unit,
    onNextSs2: () -> Unit,
    context: Context
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>("") }
    var showSuccess by remember { mutableStateOf(false) }
    // Collect loading state
    val isLoading by viewModel.isLoading.collectAsState()
    // Get current language
    val languageFlow = LanguagePreferences.getLanguage(context)
    var currentLanguage by remember { mutableStateOf("vi") }

    // Update currentLanguage when language changes
    LaunchedEffect(Unit) {
        languageFlow.collect { lang ->
            currentLanguage = lang
        }
    }

    // Use LaunchedEffect or coroutine scope to handle suspend functions
    val scope = rememberCoroutineScope()

    // Get the keyboard controller in the composable scope
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.textintro_6),
            color = Color.Black,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.requiredWidth(266.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))

        CustomOutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "Enter your ",
            isPassword = false
        )

        Spacer(modifier = Modifier.height(10.dp))

        CustomOutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "Enter your ",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(10.dp))
        errorMessage?.let {
            Text(text = it, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(35.dp))

        Button(
            onClick = {
                // Hide the keyboard
                keyboardController?.hide()

                // Trim inputs to avoid whitespace issues
                val trimmedEmail = email.trim()
                val trimmedPassword = password.trim()

                // Validate inputs
                if (trimmedEmail.isBlank() || trimmedPassword.isBlank()) {
                    errorMessage = "Email và mật khẩu không được để trống"
                    return@Button
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                    errorMessage = "Email không hợp lệ (ví dụ: user@example.com)"
                    return@Button
                }
                if (trimmedPassword.length < 6) {
                    errorMessage = "Mật khẩu phải có ít nhất 6 ký tự"
                    return@Button
                }

                scope.launch {
                    viewModel.loginUser(trimmedEmail, trimmedPassword, { userId ->
                        UserPreferences.saveUserId(context, userId)
                        val updatedUserId = UserPreferences.getUserId(context) ?: ""
                        Log.d("DEBUG", "Updated userId after login updatedUserId: $updatedUserId")
                        Log.d("DEBUG", "Updated userId after login userId: $userId")

                        viewModel.fetchUserData(userId)
                        viewModel.checkCurrentUserDataExists(
                            onResult = { route ->
                                navController.navigate(route)
                            },
                            onError = { error ->
                                Log.e("AppNavigation", "Lỗi kiểm tra dữ liệu: $error")
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                navController.navigate(Routes.TrailerScreen)
                            }
                        )
                        showSuccess = true
                    }, { error ->
                        // Customize the error message for better user feedback
                        errorMessage = when {
                            error.contains(
                                "incorrect",
                                ignoreCase = true
                            ) -> "Email hoặc mật khẩu không đúng"

                            error.contains(
                                "malformed",
                                ignoreCase = true
                            ) -> "Email không hợp lệ, vui lòng kiểm tra lại"

                            error.contains(
                                "expired",
                                ignoreCase = true
                            ) -> "Phiên đăng nhập đã hết hạn, vui lòng thử lại"

                            else -> "Lỗi đăng nhập: $error"
                        }
                    })
                }
            },
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xfffe506b)),
            contentPadding = PaddingValues(horizontal = 30.dp, vertical = 17.dp),
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp), // Match the approximate height of the text
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp), // Smaller size
                        color = Color.White,
                        strokeWidth = 2.dp // Thinner ring
                    )
                }
            } else {
                Text(
                    text = stringResource(id = R.string.textintro_7),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 18.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.textintro_8), color = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(id = R.string.textintro_9),
                color = Color(0xFFFF6473),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    onNext()
                }
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.textintro_10),
                color = Color(0xFFFF0018),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    onNextSs2()
                }
            )
        }

        // Thêm hai nút ngôn ngữ hình tròn
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            LanguageButton(
                languageCode = "vi",
                iconResId = R.drawable.ic_flag_vn, // Thay bằng ID biểu tượng cờ Việt Nam
                isSelected = currentLanguage == "vi",
                onClick = { updateLanguage(context, "vi") }
            )
            Spacer(modifier = Modifier.width(16.dp))
            LanguageButton(
                languageCode = "en",
                iconResId = R.drawable.ic_flag_uk, // Thay bằng ID biểu tượng cờ Anh
                isSelected = currentLanguage == "en",
                onClick = { updateLanguage(context, "en") }
            )
        }

        InLine()

        BtnLogin("google")
        BtnLogin("facebook")
    }
}

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var isShowPassword by remember { mutableStateOf(false) }

    val modifier = Modifier.pointerInput(Unit) {
        detectTapGestures {
            keyboardController?.hide()
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        ),
        label = { Text(label) },
        placeholder = { Text(text = placeholder) },
        leadingIcon = {
            Icon(
                imageVector = if (isPassword) Icons.Filled.Key else Icons.Filled.Person,
                contentDescription = null
            )
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onDone = { keyboardController?.hide() }
        ),
        modifier = modifier
            .width(395.dp)
            .height(70.dp),
        shape = RoundedCornerShape(8.dp),
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { isShowPassword = !isShowPassword }) {
                    Icon(
                        imageVector = if (isShowPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Password Visibility"
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !isShowPassword) PasswordVisualTransformation() else VisualTransformation.None,

        )
}

@Composable
fun BtnLogin(text: String) {

    val iconResource = when (text.lowercase()) {
        "google" -> R.drawable.gg  // Replace with actual Google drawable resource
        "facebook" -> R.drawable.face
        else -> R.drawable.face // Fallback resource
    }
    Button(
        onClick = {},
        modifier = Modifier
            .requiredWidth(360.dp)
            .requiredHeight(58.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(color = Color.White)
            .border(0.5.dp, Color.Black, CircleShape),
        contentPadding = PaddingValues(0.dp), // Remove default padding
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White),
            contentAlignment = Alignment.Center // Align content centrally
        ) {
            Icon(
                painter = painterResource(id = iconResource),
                contentDescription = "Icon",
                tint = Color(0xff1877f2),
                modifier = Modifier
                    .align(Alignment.CenterStart) // Align at the start vertically centered
                    .padding(start = 13.dp) // Add padding for spacing
                    .size(30.dp) // Adjust size
            )
            Text(
                text = stringResource(id = R.string.textintro_11) + "$text",
                color = Color.Black,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.align(Alignment.Center) // Center text
            )
        }
    }
    Spacer(modifier = Modifier.height(35.dp))
}


@Composable
fun InLine() {
    Spacer(modifier = Modifier.height(40.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.line),  // Replace with your line drawable
            contentDescription = "Line 1",
            modifier = Modifier
                .requiredWidth(79.dp)
        )

        Spacer(modifier = Modifier.width(7.dp))  // Space between the line and "OR" text

        Text(
            text = "OR",
            color = Color.Black,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 14.sp),
            modifier = Modifier
        )

        Spacer(modifier = Modifier.width(8.dp))  // Space between the "OR" text and the line

        Icon(
            painter = painterResource(id = R.drawable.line),  // Replace with your line drawable
            contentDescription = "Line 2",
            modifier = Modifier
                .requiredWidth(79.dp)
        )
    }
}


// Composable cho nút ngôn ngữ hình tròn
@Composable
fun LanguageButton(
    languageCode: String,
    iconResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, Color(0xfffe506b), CircleShape)
            .clickable {
                if (isSelected) {
                } else onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = "Language $languageCode",
            modifier = Modifier.size(30.dp),
            tint = Color.Unspecified
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginGmail() {
    DalingKTheme {
        val authViewModel = viewModel<AuthViewModel>()
//        PhoneNumberInput(authViewModel)
    }
}