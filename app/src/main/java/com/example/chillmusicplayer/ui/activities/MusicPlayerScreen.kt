import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun MusicPlayerScreen(
    viewModel: MusicPlayerViewModel
) {
    val state by viewModel.playerState.observeAsState(PlayerState())
    val exoPlayer = viewModel.exoPlayer
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
        Text(text = "Current Track: ${state.currentTrack?.name}")
        Text(text = "Next Track: ${state.nextTrack?.name ?: "None"}")

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.onPlayPauseToggle() }) {
                Text(if (isPlaying) "Pause" else "Play")
            }

            Button(onClick = { viewModel.playPreviousTrack() }) {
                Text("Previous")
            }

            Button(onClick = { viewModel.playNextTrack() }) {
                Text("Next")
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${(exoPlayer.currentPosition / 1000).toInt()} sec / ${(exoPlayer.duration / 1000).toInt()} sec",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = sliderPosition,
            onValueChange = { newValue ->
                // Update the slider position in the ViewModel
                viewModel.onSliderChange(newValue)

                // Calculate the new position and seek to it immediately
                val newPosition = (newValue * duration).toLong()
                exoPlayer.seekTo(newPosition)
            },
            onValueChangeFinished = {
                // After the user has finished changing the slider, you might want to update
                // the slider position in the ViewModel based on the current playback position
                val currentPosition = exoPlayer.currentPosition.toFloat()
                val newSliderPosition = currentPosition / duration.toFloat()
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
            val exoPlayer = viewModel.exoPlayer
            val progress = exoPlayer.currentPosition.toFloat() / exoPlayer.duration.toFloat()
            viewModel.onSliderChange(progress)
            handler.postDelayed(this, 1000)
        }
    }
}
