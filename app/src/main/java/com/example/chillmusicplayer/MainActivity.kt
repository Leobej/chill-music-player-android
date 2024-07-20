package com.example.chillmusicplayer

import MusicPlayerScreen
import MusicPlayerViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.chillmusicplayer.util.MusicFileManager

class MainActivity : ComponentActivity() {
    private val viewModel: MusicPlayerViewModel by viewModels()
    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadMusicFiles()
        } else {
            // Permission denied, handle appropriately
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicPlayerScreen(viewModel)
        }
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadMusicFiles()
            }

            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionRequest.launch(Manifest.permission.READ_MEDIA_AUDIO)
                }
            }
        }
    }

    private fun loadMusicFiles() {
        val musicFiles = MusicFileManager.getAllMp3Files(this)
        if (musicFiles.isNotEmpty()) {
            viewModel.loadPlaylist(musicFiles)
        } else {
            // Handle case where no music files are found
        }
    }
}
