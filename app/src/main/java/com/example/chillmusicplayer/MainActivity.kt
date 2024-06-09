package com.example.chillmusicplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chillmusicplayer.ui.theme.ChillMusicPlayerTheme

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var hasEnded by mutableStateOf(false)
    var isPlaying by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChillMusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MusicPlayerScreen()
                }
            }
        }
    }

    @Composable
    fun MusicPlayerScreen() {
        var sliderPosition by remember { mutableStateOf(0f) }

        var duration by remember { mutableStateOf(0f) }

        LaunchedEffect(isPlaying) {
            if (isPlaying) {
                handler.post(updateSlider(sliderPosition, duration) { newSliderPosition ->
                    sliderPosition = newSliderPosition
                })
            } else {
                handler.removeCallbacksAndMessages(null)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                if (isPlaying) {
                    pauseMusic()
                } else {
                    playMusic()
                    duration = mediaPlayer?.duration?.toFloat() ?: 0f
                }
                isPlaying = !isPlaying
                hasEnded = false
            }) {
                Text(if (isPlaying) "Pause" else "Play")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                stopMusic()
                isPlaying = false
                sliderPosition = 0f
            }) {
                Text("Stop")
            }

            Text(
                text = "${(sliderPosition * duration / 1000).toInt()} sec / ${(duration / 1000).toInt()} sec",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Slider(
                value = sliderPosition,
                onValueChange = { newValue ->
                    if (mediaPlayer != null && duration > 0) {
                        if (newValue >= 1f) {
                            sliderPosition = 0f
                            mediaPlayer?.seekTo(0)
                            hasEnded = false
                        } else {
                            sliderPosition = newValue
                            mediaPlayer?.seekTo((newValue * duration).toInt())
                        }

                        if (hasEnded) {
                            playMusic()
                            isPlaying = true
                            hasEnded = false
                        } else if (!isPlaying) {
                            mediaPlayer?.start()
                            isPlaying = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        DisposableEffect(Unit) {
            onDispose {
                handler.removeCallbacksAndMessages(null)
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }
    }

    private fun updateSlider(
        sliderPosition: Float,
        duration: Float,
        setSliderPosition: (Float) -> Unit
    ): Runnable {
        return object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    val progress = it.currentPosition / duration
                    setSliderPosition(progress)
                    if (it.currentPosition >= it.duration) {
                        handler.removeCallbacks(this)
                        setSliderPosition(0f)
                        it.seekTo(0)
                        it.pause()
                        hasEnded = true
                        isPlaying = false
                    } else {
                        handler.postDelayed(this, 1000)
                    }
                }
            }
        }
    }

    private fun playMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.sample_music)
        }
        mediaPlayer?.start()
    }

    private fun pauseMusic() {
        mediaPlayer?.pause()
        handler.removeCallbacksAndMessages(null)
    }

    private fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }
}

@Composable
fun MusicPlayerScreen(
    sliderPosition: Float,
    duration: Float,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    onStop: () -> Unit,
    onSliderPositionChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { onPlayPauseToggle() }) {
            Text(if (isPlaying) "Pause" else "Play")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onStop() }) {
            Text("Stop")
        }

        Text(
            text = "${(sliderPosition * duration / 1000).toInt()} sec / ${(duration / 1000).toInt()} sec",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = sliderPosition,
            onValueChange = onSliderPositionChange,
            modifier = Modifier.fillMaxWidth()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChillMusicPlayerTheme {
        MusicPlayerScreen(
            sliderPosition = 0f,
            duration = 100f,
            isPlaying = false,
            onPlayPauseToggle = {},
            onStop = {},
            onSliderPositionChange = {}
        )
    }
}
