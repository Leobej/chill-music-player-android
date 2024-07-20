import android.app.Application
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chillmusicplayer.R
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

    var mediaPlayer: MediaPlayer? = null
        private set

    init {
        _playerState.value = _playerState.value?.copy(
            duration = mediaPlayer?.duration?.toFloat() ?: 0f
        )
    }

    fun loadPlaylist(playlist: List<File>) {
        _playerState.value = _playerState.value?.copy(playlist = playlist)
        if (playlist.isNotEmpty()) {
            playTrackAtIndex(0)
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
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(it.absolutePath)
                prepare()
                start()
            }
            _playerState.value = _playerState.value?.copy(
                currentTrack = it,
                nextTrack = nextTrack,
                currentIndex = index,
                duration = mediaPlayer?.duration?.toFloat() ?: 0f,
                isPlaying = true
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
        _playerState.value = _playerState.value?.let { state ->
            val isPlaying = !state.isPlaying
            if (isPlaying) {
                mediaPlayer?.start()
            } else {
                mediaPlayer?.pause()
            }
            state.copy(isPlaying = isPlaying)
        }
    }

    fun onStop() {
        mediaPlayer?.stop()
        mediaPlayer?.prepare()
        _playerState.value = _playerState.value?.copy(
            isPlaying = false,
            sliderPosition = 0f
        )
    }

    fun onSliderChange(newSliderPosition: Float) {
        _playerState.value = _playerState.value?.copy(sliderPosition = newSliderPosition)
        mediaPlayer?.seekTo((newSliderPosition * mediaPlayer!!.duration).toInt())
    }

    fun onPlaybackEnded() {
        _playerState.value = _playerState.value?.copy(
            sliderPosition = 0f,
            isPlaying = false,
            hasEnded = true
        )
        mediaPlayer?.seekTo(0)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
