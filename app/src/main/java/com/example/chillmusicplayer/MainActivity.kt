package com.example.chillmusicplayer

import MusicPlayerScreen
import MusicPlayerViewModel
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.chillmusicplayer.ui.theme.ChillMusicPlayerTheme
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        val viewModel = MusicPlayerViewModel(application)
        super.onCreate(savedInstanceState)

        setContent {
            ChillMusicPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MusicPlayerScreen(viewModel = viewModel)
                }
            }
        }
    }
}
