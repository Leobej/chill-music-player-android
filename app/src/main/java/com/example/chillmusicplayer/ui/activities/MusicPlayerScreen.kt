import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun MusicPlayerScreen(
    viewModel: MusicPlayerViewModel
) {
    val state by viewModel.playerState.observeAsState(PlayerState())

    val sliderPosition = state.sliderPosition
    val duration = state.duration
    val isPlaying = state.isPlaying

    val handler = remember { Handler(Looper.getMainLooper()) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            handler.post(updateSlider(viewModel, handler))
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
        Button(onClick = { viewModel.onPlayPauseToggle() }) {
            Text(if (isPlaying) "Pause" else "Play")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.onStop() }) {
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
                viewModel.onSliderChange(newValue)
                // Provide immediate feedback by seeking the media player
                val mediaPlayer = viewModel.mediaPlayer
                mediaPlayer?.seekTo((newValue * duration).toInt())
            },
            onValueChangeFinished = {
                // Update the slider position to the exact position after seek
                val mediaPlayer = viewModel.mediaPlayer
                val currentPosition = mediaPlayer?.currentPosition?.toFloat() ?: 0f
                val newSliderPosition = currentPosition / (mediaPlayer?.duration?.toFloat() ?: 1f)
                viewModel.onSliderChange(newSliderPosition)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            handler.removeCallbacksAndMessages(null)
        }
    }
}

private fun updateSlider(
    viewModel: MusicPlayerViewModel,
    handler: Handler
): Runnable {
    return object : Runnable {
        override fun run() {
            val mediaPlayer = viewModel.mediaPlayer
            mediaPlayer?.let {
                val progress = it.currentPosition.toFloat() / (it.duration.toFloat())
                viewModel.onSliderChange(progress)
                if (it.currentPosition >= it.duration) {
                    handler.removeCallbacks(this)
                    viewModel.onPlaybackEnded()
                } else {
                    handler.postDelayed(this, 1000)
                }
            }
        }
    }
}