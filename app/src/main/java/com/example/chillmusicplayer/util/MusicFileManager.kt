package com.example.chillmusicplayer.util

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File

object MusicFileManager {
    private const val TAG = "MusicFileManager"

    fun getAllMp3Files(context: Context): List<File> {
        val musicFiles = mutableListOf<File>()

        // Force MediaStore to reindex the files
        reindexMediaStore(context)

        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            Log.d(TAG, "Cursor has ${cursor.count} items.")
            while (cursor.moveToNext()) {
                val filePath = cursor.getString(dataColumn)
                Log.d(TAG, "File path: $filePath")
                val file = File(filePath)
                if (file.exists()) {
                    musicFiles.add(file)
                    Log.d(TAG, "File added: $filePath")
                } else {
                    Log.w(TAG, "File does not exist: $filePath")
                }
            }
        }

        if (musicFiles.isEmpty()) {
            Log.w(TAG, "No MP3 files found after querying.")
        } else {
            Log.i(TAG, "Found ${musicFiles.size} MP3 files")
        }

        return musicFiles
    }

    private fun reindexMediaStore(context: Context) {
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        Log.d(TAG, "Reindexing directory: $musicDir")
        if (musicDir.exists()) {
            val files = musicDir.listFiles { _, name ->
                name.endsWith(".mp3", ignoreCase = true)
            }
            files?.forEach { file ->
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
                Log.d(TAG, "Reindexed file: ${file.absolutePath}")
            }
        }
    }
}
