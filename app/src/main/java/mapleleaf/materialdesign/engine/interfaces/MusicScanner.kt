package mapleleaf.materialdesign.engine.interfaces

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import mapleleaf.materialdesign.engine.ui.activities.ActivityMusic
import kotlin.concurrent.thread

class MusicScanner(private val context: Context) {

    interface MusicScanListener {
        fun onMusicScanComplete(musicList: List<ActivityMusic.Music>)
    }

    fun scanMusic(listener: MusicScanListener) {
        thread {
            val musicList = mutableListOf<ActivityMusic.Music>()
            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
            )
            val selection =
                "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 60000"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn)
                    val artist = it.getString(artistColumn)
                    val duration = it.getLong(durationColumn)

                    val musicUri: Uri = Uri.withAppendedPath(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )
                    val music = ActivityMusic.Music(id, title, artist, duration, musicUri)
                    musicList.add(music)
                }
            }

            listener.onMusicScanComplete(musicList)
        }
    }
}
