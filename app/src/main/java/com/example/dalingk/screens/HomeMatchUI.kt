package com.example.dalingk.screens

import android.content.Context
import android.os.Bundle
import android.text.Layout
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dalingk.FullScreenLoading
import com.example.dalingk.ui.theme.DalingKTheme
import com.example.dalingk.components.matches.SwipeScreen
import data.repository.AuthViewModel

class HomeMatchUI : ComponentActivity() {
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
                    GreetingPreview6()
                }
            }
        }
    }
}

@Composable
fun HomeMatch(profiles: List<AuthViewModel.UserData>, context: Context, viewModel: AuthViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        Column {
//              Text(text = "Số lượng người dùng: ${profiles.size - 1} + 1", fontSize = 18.sp, color = Color.Black)

            LaunchedEffect(profiles) {
                Log.d("DEBUG", "Số lượng profiles: ${profiles.size}")
            }

            val isLoading by viewModel.isLoading.collectAsState()

            val currentUserId = viewModel.auth.currentUser?.uid ?: "Không có người dùng"
            Log.d("DEBUG", "ID người dùng hiện tại HomeMatch: $currentUserId")

            if (isLoading) {
//                FullScreenLoading(isLoading)
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }else{
                SwipeScreen(profiles = profiles, viewModel)
            }
//            SwipeScreen(profiles = profiles)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview6() {
    DalingKTheme {
//        HomeMatch()
    }
}