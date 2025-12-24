package data.chat.video

import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun VideoPlayer(
    videoUrl: String,
    isSentByCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var hasPlayed by remember { mutableStateOf(false) } // Track if video has played once

    // Update progress bar while playing
    LaunchedEffect(isPlaying) {
        while (isPlaying && exoPlayer.isPlaying) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration
            delay(100)
        }
    }

    // Listen to playback state changes
    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_ENDED -> {
                        isPlaying = false
                        hasPlayed = true
                        exoPlayer.pause()
                        exoPlayer.seekTo(0)
                        currentPosition = 0
                    }
                    Player.STATE_READY -> {
                        isLoading = false
                        duration = exoPlayer.duration
                    }
                    Player.STATE_BUFFERING -> {
                        isLoading = true
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .heightIn(max = 290.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSentByCurrentUser)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color(0xFFEDEDED)
            )
    ) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!isPlaying && !isLoading) {
            IconButton(
                onClick = {
                    if (!hasPlayed || !isPlaying) {
                        isLoading = true
                        if (exoPlayer.playbackState == Player.STATE_IDLE) {
                            exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
                            exoPlayer.prepare()
                        }
                        exoPlayer.playWhenReady = true
                        isPlaying = true
                    } else {
                        // Allow replay if user clicks again after video has ended
                        exoPlayer.seekTo(0)
                        exoPlayer.playWhenReady = true
                        isPlaying = true
                        hasPlayed = false
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Phát video",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else if (isPlaying) {
            IconButton(
                onClick = {
                    exoPlayer.pause()
                    isPlaying = false
                },
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Tạm dừng video",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        LinearProgressIndicator(
            progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomStart),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.Gray.copy(alpha = 0.3f)
        )
    }
}

fun formatTime(millis: Int): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}