package com.example.dalingk.navigation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dalingk.ui.theme.DalingKTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.example.dalingk.R
import com.example.dalingk.components.InterestsScreenUI
import com.example.dalingk.components.TopBarU
import com.example.dalingk.screens.AvatarDetail
import com.example.dalingk.screens.chatUI.ChatListUI
import com.example.dalingk.screens.HomeMatch
import data.chat.viewmodel.ChatListViewModel
import data.repository.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import util.AppState

class MainLayOutMatch : ComponentActivity() {
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
                    GreetingPreview2()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppState.setAppForeground(true)
    }

    override fun onPause() {
        super.onPause()
        AppState.setAppForeground(false)
    }
}


@Composable
fun MainScreen(navController: NavController, context: Context, viewModel: AuthViewModel) {
    var selectedIndex by remember {
        mutableStateOf(
            navController.previousBackStackEntry?.savedStateHandle?.get<Int>(Routes.DetailU) ?: 0
        )
    }

    val userId = viewModel.auth.currentUser?.uid ?: ""

    Log.d("DEBUG", "ID người dùng hiện tại MainScreen: $userId")

    val profiles by viewModel.cachedProfiles.collectAsState(initial = emptyList())

    val chatListViewModel = remember { ChatListViewModel(context) }
    val notificationMessage by chatListViewModel.notificationMessage.collectAsState()

    LaunchedEffect(selectedIndex) {
        if (userId != null) {
            withContext(Dispatchers.IO) {
                viewModel.fetchUserData(userId)
                if (selectedIndex <= 2) {
                    viewModel.loadNewProfiles(context)
                }
            }
        } else {
            viewModel._errorMessage.value = "Lỗi"
        }

        navController.currentBackStackEntry?.savedStateHandle?.get<Int>(Routes.DetailU)?.let { index ->
            selectedIndex = index
            navController.currentBackStackEntry?.savedStateHandle?.remove<Int>(Routes.DetailU)
        }
    }

    LaunchedEffect(viewModel.cachedProfiles) {
        Log.d("DEBUG", "Profiles mới sau khi load: ${viewModel.cachedProfiles.value.size}")
    }

    Scaffold(
        topBar = { TopBarU(notificationMessage = notificationMessage) },
        bottomBar = {
            NavMLayout(
                selectedIndex = selectedIndex,
                onActionClicked = { index -> selectedIndex = index }
            )
        },
        containerColor = Color(0xFFFFF5F5)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedIndex) {
                0 -> HomeMatch(profiles = profiles, context, viewModel)
                1 -> InterestsScreenUI(navController = navController, viewModel)
                2 -> ChatListUI(navController = navController)
                3 -> AvatarDetail(navController = navController, context)
            }
        }
    }
}

@Composable
fun NavMLayout(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onActionClicked: (Int) -> Unit
) {
    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Row(
        modifier = modifier
            .background(Color.White)
            .padding(bottom = bottomPadding)
            .fillMaxWidth()
            .then(Modifier),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icons = listOf(
            R.drawable.iconm1,
            R.drawable.iconm2,
            R.drawable.iconm3,
            R.drawable.iconm4
        )

        icons.forEachIndexed { index, icon ->
            IconButton(
                onClick = { onActionClicked(index) },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp) // Nhẹ hơn 15dp nếu bạn dùng padding toàn hàng
            ) {
                Box(
                    modifier = Modifier
                        .requiredWidth(94.dp)
                        .requiredHeight(70.dp)
                ) {
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        colorFilter = if (index == selectedIndex) {
                            ColorFilter.tint(Color(0xffff5069))
                        } else {
                            ColorFilter.tint(Color(0xffadafbb))
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .requiredSize(31.dp)
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    DalingKTheme {
//        MainScreen()
    }
}