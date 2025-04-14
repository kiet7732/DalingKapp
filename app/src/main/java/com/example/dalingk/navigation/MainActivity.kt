package com.example.dalingk.navigation

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import com.example.dalingk.ui.theme.DalingKTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dalingk.auth.PhoneNumberInput
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.example.dalingk.auth.RegisterScreen
import com.example.dalingk.components.detailUser.ArrowScreen
import com.example.dalingk.screens.AvatarDetail
import com.example.dalingk.screens.ChatListUI
import com.example.dalingk.screens.ChatScreen
import com.example.dalingk.screens.LoginScreen
import com.example.dalingk.components.detailUser.ProfileScreenU
import data.model.CloudinaryHelper
import data.repository.AuthViewModel
import data.viewmodel.UserInputViewModel

//import components.profiles

class MainActivity : ComponentActivity() {
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
                    FirebaseApp.initializeApp(this)
                    val firebaseAppCheck = FirebaseAppCheck.getInstance()
                    firebaseAppCheck.installAppCheckProviderFactory(
                        PlayIntegrityAppCheckProviderFactory.getInstance()
                    )

                    CloudinaryHelper.initialize(this)
                    AppNavigation()
                }
            }
        }
    }
}

// Set up navigation in your NavHost:
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel = viewModel<AuthViewModel>()
    val context = LocalContext.current
    val viewModel: UserInputViewModel = viewModel()
    var startDestination by remember { mutableStateOf(Routes.TrailerScreen) }
    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        Log.d("AppNavigation", "Bắt đầu kiểm tra trạng thái người dùng")
        authViewModel.checkCurrentUserIdExists(
            onResult = { isLoggedIn ->
                if (isLoggedIn) {
                    // Nếu có trong Authentication, kiểm tra tiếp Database
                    authViewModel.checkCurrentUserDataExists(
                        onResult = { route ->
                            startDestination = route
                            isChecking = false
                        },
                        onError = { error ->
                            Log.e("AppNavigation", "Lỗi kiểm tra dữ liệu: $error")
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            startDestination = Routes.TrailerScreen
                            isChecking = false
                        }
                    )
                } else {
                    // Không có trong Authentication, kiểm tra Database
                    authViewModel.checkCurrentUserDataExists(
                        onResult = { route ->
                            startDestination = route
                            isChecking = false
                        },
                        onError = { error ->
                            Log.e("AppNavigation", "Lỗi kiểm tra dữ liệu: $error")
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            startDestination = Routes.TrailerScreen
                            isChecking = false
                        }
                    )
                }
            },
            onError = { error ->
                Log.e("AppNavigation", "Lỗi kiểm tra Authentication: $error")
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                startDestination = Routes.TrailerScreen
                isChecking = false
            }
        )
    }

    if (!isChecking) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFE9E9))
        ) {
            NavHost(navController = navController, startDestination = startDestination) {
                composable(Routes.TrailerScreen) {
                    LoginScreen(navController = navController, currentScreen = "1", context)
                }

                composable(Routes.InputDetail) {
                    ArrowScreen(navController = navController, context, viewModel, authViewModel)
                }

                composable(Routes.Register) {
                    RegisterScreen(
                        viewModel = authViewModel,
                        navController = navController,
                        onBack = { navController.navigateUp() },
                        onSuccess = { navController.navigate(Routes.InputDetail) },
                        context
                    )
                }

                composable(Routes.InputLogin) {
                    PhoneNumberInput(
                        viewModel = authViewModel,
                        navController = navController,
                        onNext = { navController.navigate(Routes.Register) },
                        context
                    )
                }

                composable(Routes.MainMatch) {
                    MainScreen(navController = navController, context, authViewModel)
                }
                composable(Routes.DetailU) {
                    AvatarDetail(navController = navController, context)
                }
                composable(Routes.Profile) {
                    ProfileScreenU(navController = navController, context)
                }
                composable(Routes.ChatList) {
                    ChatListUI(navController = navController)
                }
                composable(
                    route = "chat/{matchId}",
                    arguments = listOf(navArgument("matchId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val matchId = backStackEntry.arguments?.getString("matchId") ?: return@composable
                    Log.d("AppNavigation", "Điều hướng đến ChatScreen với matchId = $matchId")
                    ChatScreen(navController = navController, matchId = matchId)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFE9E9)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            val transition = rememberInfiniteTransition()
            val yOffset by transition.animateFloat(
                initialValue = 0f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 300, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 0..2) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .offset(y = if (i % 2 == 0) yOffset.dp else -yOffset.dp)
                            .background(Color(0xffff5069), shape = CircleShape)
                    )
                }
            }

        }
    }
}


@Preview(showBackground = true)
@Composable
fun ViewMain() {
    DalingKTheme {
        var currentScreen by remember { mutableStateOf("LogoStart") } // Quản lý trạng thái màn hình

        // Crossfade for smooth screen transitions
//            Crossfade(targetState = currentScreen, animationSpec = tween(durationMillis = 1000)) { screen ->
//                when (screen) {
//                    "LogoStart" -> {
//                        LogoStart { currentScreen = "LoginScreen" }
//                    }
//                    "LoginScreen" -> {
//                        LoginScreen()
//                    }
//                    "PhoneNumberInput" -> {
//                        PhoneNumberInput { currentScreen = "ArrowScreen" }
//                    }
////                    "ArrowScreen" -> {
////                        IntroFormUI()
////                    }
//                }
//            }

    }
}