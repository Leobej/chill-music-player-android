import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

import java.io.File

data class PlayerState(
    val sliderPosition: Float = 0f,
    val duration: Float = 0f,
    val isPlaying: Boolean = false,
    val hasEnded: Boolean = false,
    val currentTrack: File? = null,
    val nextTrack: File? = null,
    val currentIndex: Int = 0,
    val playlist: List<File> = emptyList()
)

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val _playerState = MutableLiveData(PlayerState())
    val playerState: LiveData<PlayerState> = _playerState

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application.applicationContext).build()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                // Ensure that the player state is reflected correctly
                if (state == Player.STATE_ENDED) {
                    playNextTrack()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // Update the playing state
                _playerState.value = _playerState.value?.copy(isPlaying = isPlaying)

                // Update the slider position when playback starts or stops
                val currentPosition = exoPlayer.currentPosition.toFloat()
                val newSliderPosition = currentPosition / exoPlayer.duration.toFloat()
                _playerState.value = _playerState.value?.copy(sliderPosition = newSliderPosition)
            }
        })
    }

    fun loadPlaylist(playlist: List<File>) {
        _playerState.value = _playerState.value?.copy(playlist = playlist)
        if (playlist.isNotEmpty()) {
            // Prepare the player without starting playback
            prepareTrackAtIndex(0)
        }
    }

    private fun prepareTrackAtIndex(index: Int) {
        if (index < 0 || index >= _playerState.value?.playlist?.size ?: 0) return

        val track = _playerState.value?.playlist?.get(index)
        val nextTrack = if (index + 1 < _playerState.value?.playlist?.size ?: 0) {
            _playerState.value?.playlist?.get(index + 1)
        } else {
            null
        }

        track?.let {
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(it)))
            exoPlayer.prepare() // Prepare the media item
            _playerState.value = _playerState.value?.copy(
                currentTrack = it,
                nextTrack = nextTrack,
                currentIndex = index,
                duration = exoPlayer.duration.toFloat(), // Update the duration here
                isPlaying = false // Start in a paused state
            )
        }
    }

    private fun playTrackAtIndex(index: Int) {
        if (index < 0 || index >= _playerState.value?.playlist?.size ?: 0) return

        val track = _playerState.value?.playlist?.get(index)
        val nextTrack = if (index + 1 < _playerState.value?.playlist?.size ?: 0) {
            _playerState.value?.playlist?.get(index + 1)
        } else {
            null
        }

        track?.let {
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(it)))
            exoPlayer.prepare()
            exoPlayer.play()
            _playerState.value = _playerState.value?.copy(
                currentTrack = it,
                nextTrack = nextTrack,
                currentIndex = index,
                duration = exoPlayer.duration.toFloat(),
                isPlaying = exoPlayer.isPlaying
            )
        }
    }

    fun playNextTrack() {
        _playerState.value?.let {
            val nextIndex = it.currentIndex + 1
            if (nextIndex < it.playlist.size) {
                playTrackAtIndex(nextIndex)
            }
        }
    }

    fun playPreviousTrack() {
        _playerState.value?.let {
            val prevIndex = it.currentIndex - 1
            if (prevIndex >= 0) {
                playTrackAtIndex(prevIndex)
            }
        }
    }

    fun onPlayPauseToggle() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }

        // Update the player state after the action is taken
        _playerState.value = _playerState.value?.copy(isPlaying = exoPlayer.isPlaying)
    }

    fun onSliderChange(newSliderPosition: Float) {
        _playerState.value = _playerState.value?.copy(sliderPosition = newSliderPosition)
        exoPlayer.seekTo((newSliderPosition * exoPlayer.duration).toLong())
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}
