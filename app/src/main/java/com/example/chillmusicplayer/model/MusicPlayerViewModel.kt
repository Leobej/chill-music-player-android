import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chillmusicplayer.R

data class PlayerState(
    val sliderPosition: Float = 0f,
    val duration: Float = 0f,
    val isPlaying: Boolean = false,
    val hasEnded: Boolean = false
)

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val _playerState = MutableLiveData(PlayerState())
    val playerState: LiveData<PlayerState> = _playerState

    var mediaPlayer: MediaPlayer? = MediaPlayer.create(application.applicationContext, R.raw.sample_music)
        private set

    init {
        _playerState.value = _playerState.value?.copy(
            duration = mediaPlayer?.duration?.toFloat() ?: 0f
        )
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