package com.example.dalingk.components

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dalingk.components.ui.theme.DalingKTheme

import androidx.compose.runtime.getValue
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.dalingk.R
import com.example.dalingk.screens.GetStarted

class DynamicLottieAnimation : ComponentActivity() {
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
                    GreetingPreview20()
                }
            }
        }
    }
}

@Composable
fun LottieAnimationCus(
    jsonFileName: Int, // Changed to Int for resource ID
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    loop: Boolean = true
) {

    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(jsonFileName) // Uses resource ID
    )

    LottieAnimation(
        composition = composition,
        modifier = modifier,
        iterations = if (loop) LottieConstants.IterateForever else 1,
        isPlaying = isPlaying
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview20() {
    DalingKTheme {
        LottieAnimationCus(
            jsonFileName = R.raw.heart_amin,
//            modifier = Modifier.fillMaxSize(),
        )

    }
}