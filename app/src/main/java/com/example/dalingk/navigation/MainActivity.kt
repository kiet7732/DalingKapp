package com.example.dalingk.navigation

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import com.example.dalingk.ui.theme.DalingKTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dalingk.auth.ForgotPassword
import com.example.dalingk.auth.PhoneNumberInput
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.example.dalingk.auth.RegisterScreen
import com.example.dalingk.components.detailUser.ArrowScreen
import com.example.dalingk.screens.AvatarDetail
import com.example.dalingk.screens.chatUI.ChatListUI
import com.example.dalingk.screens.chatUI.ChatScreen
import com.example.dalingk.screens.LoginScreen
import com.example.dalingk.components.detailUser.ProfileScreenU
import com.example.dalingk.navigation.Routes.ForgotPassword
import data.model.CloudinaryHelper
import data.repository.AuthViewModel
import data.viewmodel.UserInputViewModel
import androidx.core.app.NotificationCompat
import data.model.LanguagePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

//import components.profiles
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Đăng ký launcher để yêu cầu quyền thông báo
        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Quyền thông báo bị từ chối", Toast.LENGTH_SHORT).show()
            }
        }

        // Yêu cầu quyền POST_NOTIFICATIONS trên Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Lấy ngôn ngữ từ DataStore và áp dụng Locale
        CoroutineScope(Dispatchers.Main).launch {
            val language = LanguagePreferences.getLanguage(this@MainActivity).first()
            Log.d("MainActivity", "Applying language: $language")
            val locale = Locale(language)
            Locale.setDefault(locale)
            val configuration = Configuration(resources.configuration)
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }

        setContent {
            DalingKTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Khởi tạo Firebase
                    FirebaseApp.initializeApp(this)
                    val firebaseAppCheck = FirebaseAppCheck.getInstance()
                    firebaseAppCheck.installAppCheckProviderFactory(
                        PlayIntegrityAppCheckProviderFactory.getInstance()
                    )

                    // Khởi tạo Cloudinary
                    CloudinaryHelper.initialize(this)

                    // Điều hướng ứng dụng
                    AppNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        util.AppState.setAppForeground(true)
    }

    override fun onPause() {
        super.onPause()
        util.AppState.setAppForeground(false)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        CoroutineScope(Dispatchers.Main).launch {
            val language = LanguagePreferences.getLanguage(this@MainActivity).first()
            Log.d("MainActivity", "Reapplying language on config change: $language")
            val locale = Locale(language)
            Locale.setDefault(locale)
            val configuration = Configuration(newConfig)
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
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

    // Sử dụng NetworkCallback để lắng nghe thay đổi mạng thay vì dùng BroadcastReceiver
    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Toast.makeText(context, "Đã kết nối mạng", Toast.LENGTH_SHORT).show()
            }

            override fun onLost(network: Network) {
                Toast.makeText(context, "Mất kết nối mạng", Toast.LENGTH_SHORT).show()
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    LaunchedEffect(Unit) {
        Log.d("AppNavigation", "Bắt đầu kiểm tra trạng thái người dùng")
        authViewModel.checkCurrentUserIdExists(
            onResult = { isLoggedIn ->
                if (isLoggedIn) {
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
                        onNextSs2 = { navController.navigate(Routes.ForgotPassword) },
                        context
                    )
                }
                composable(Routes.ForgotPassword) {
                    ForgotPassword(
                        viewModel = authViewModel,
                        navController = navController,
                        onBack = { navController.navigateUp() },
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