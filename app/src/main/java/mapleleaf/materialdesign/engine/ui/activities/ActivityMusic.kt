package mapleleaf.materialdesign.engine.ui.activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.interfaces.MusicScanner
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class ActivityMusic : UniversalActivityBase(R.layout.activity_music),
    MusicScanner.MusicScanListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var musicAdapter: AdapterMusic
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var materialCardView: MaterialCardView
    private var progressBar: ProgressBar? = null

    override fun initializeComponents(savedInstanceState: Bundle?) {

        setToolbarTitle(getString(R.string.toolbar_title_activity_music))
        recyclerView = findViewById(R.id.recyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        materialCardView = findViewById(R.id.materialCardView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        musicAdapter = AdapterMusic()
        recyclerView.adapter = musicAdapter
        FastScrollerBuilder(recyclerView).build()

        progressBar?.isVisible = true
        progressBar?.isIndeterminate = true

        val colorRed = ContextCompat.getColor(this, R.color.red1)
        val colorGreen = ContextCompat.getColor(this, R.color.lawngreen)
        val colorBlue = ContextCompat.getColor(this, R.color.blue)
        val colorOrange = ContextCompat.getColor(this, R.color.orange2)
        val progressColors = ContextCompat.getColor(this, R.color.swipe_refresh_layout_progress)
        swipeRefreshLayout.setColorSchemeColors(colorRed, colorGreen, colorBlue, colorOrange)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(progressColors)
        val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
        val baseColor = ContextCompat.getColor(context, R.color.background)

        materialCardView.apply {
            strokeColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.3f)
            setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.2f))
        }

        musicScan()

        swipeRefreshLayout.setOnRefreshListener {
            musicScan()
        }
    }

    private fun musicScan() {
        swipeRefreshLayout.isRefreshing = true
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                val musicScanner = MusicScanner(this@ActivityMusic)
                musicScanner.scanMusic(this@ActivityMusic)
                progressBar?.isVisible = false
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    override fun onMusicScanComplete(musicList: List<Music>) {
        runOnUiThread {
            musicAdapter.setMusicList(musicList)
        }
    }

    class AdapterMusic : RecyclerView.Adapter<AdapterMusic.MusicViewHolder>() {

        private var mediaPlayer: MediaPlayer? = null
        private var musicList = listOf<Music>()
        private var mediaPlayerReleased = false
        private var coroutineScope = CoroutineScope(Dispatchers.Main)
        private var shouldPlayAudio = true

        @SuppressLint("NotifyDataSetChanged")
        fun setMusicList(newList: List<Music>) {
//            val diffResult = DiffUtil.calculateDiff(DiffCallback(musicList, newList))
//            musicList = newList
//            diffResult.dispatchUpdatesTo(this)

            musicList = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_music, parent, false)
            return MusicViewHolder(view)
        }

        override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
            val music = musicList[position]
//            val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
//            layoutParams.topMargin = if (position == 0) 18 else 0
//            holder.itemView.layoutParams = layoutParams
            holder.bind(music)
        }

        override fun onViewAttachedToWindow(holder: MusicViewHolder) {
            super.onViewAttachedToWindow(holder)
            setFadeAnimation(holder.itemView)
        }

        override fun getItemCount(): Int {
            return musicList.size
        }

        fun releaseMediaPlayer() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
                mediaPlayer = null
            }
        }

        fun pauseMediaPlayer() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
        }

        fun resumeMediaPlayer() {
            mediaPlayer?.start()
        }

        inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
            private val artistTextView: TextView = itemView.findViewById(R.id.artistTextView)
            private val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)

            private val musicIconImageView: ImageView =
                itemView.findViewById(R.id.musicIconImageView)

            init {
                val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
                val baseColor = ContextCompat.getColor(context, R.color.background)

                itemView.findViewById<MaterialCardView>(R.id.musicMaterialCardView).apply {
                    strokeColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.3f)
                    setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.2f))
                    setOnClickListener {
                        val position = bindingAdapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val music = musicList[position]
                            CoroutineScope(Dispatchers.Main).launch {
                                playMusic(music, it.context)
                                mediaPlayerReleased = false
                            }
                        }
                    }
                }
            }


            @SuppressLint("InflateParams")
            private fun playMusic(
                music: Music,
                context: Context,
            ) {
                if (!shouldPlayAudio) {
                    return
                }

                try {
                    if (mediaPlayer == null) {
                        mediaPlayer = MediaPlayer()
                    } else {
                        mediaPlayer?.reset()
                    }

                    val layoutInflater =
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val dialogView = layoutInflater.inflate(R.layout.dialog_playback, null)
                    dialogView.findViewById<TextView>(R.id.player_title).text = "音乐播放器"
                    val totalDurationTextView =
                        dialogView.findViewById<TextView>(R.id.totalDurationTextView)
                    val currentPositionTextView =
                        dialogView.findViewById<TextView>(R.id.currentPositionTextView)
                    dialogView.findViewById<TextView>(R.id.musicFileNameTextView).text = music.title
                    val seekBar: SeekBar = dialogView.findViewById(R.id.seekBar)
                    val btnPlayPause: Button = dialogView.findViewById(R.id.btnPlayPause)
                    val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)

                    mediaPlayer?.apply {
                        setDataSource(context, music.uri)
                        prepareAsync()
                        setOnPreparedListener { mp ->
                            seekBar.max = mp.duration
                            totalDurationTextView.text = formatTime(mp.duration)
                            updateSeekBar(seekBar, currentPositionTextView)
                            updateCurrentPosition(seekBar, currentPositionTextView)
                            mp.start()
                            btnPlayPause.text = "暂停"
                        }
                        setOnCompletionListener {
                            btnPlayPause.text = "播放"
                        }
                    }

                    val dialog = DialogHelper.customDialog(context, dialogView)

                    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar,
                            progress: Int,
                            fromUser: Boolean,
                        ) {
                            if (fromUser) mediaPlayer?.seekTo(progress)
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar) {}
                    })

                    btnPlayPause.setOnClickListener {
                        mediaPlayer?.let { player ->
                            if (player.isPlaying) {
                                player.pause()
                                btnPlayPause.text = "播放"
                                coroutineScope.cancel()
                            } else {
                                player.start()
                                btnPlayPause.text = "暂停"
                                CoroutineScope(Dispatchers.Main).launch {
                                    updateSeekBar(seekBar, currentPositionTextView)
                                    updateCurrentPosition(seekBar, currentPositionTextView)
                                }
                            }
                        }
                    }

                    btnCancel.setOnClickListener {
                        if (!mediaPlayerReleased) {
                            CoroutineScope(Dispatchers.IO).launch {
                                releaseMediaPlayer()
                                mediaPlayerReleased = true
                                mediaPlayer = null
                            }
                        }
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    Log.e("ActivityMusic", "播放音乐时发生异常", e)
                } finally {
//                    isPlayingMusic = false
                }
            }

            private fun updateCurrentPosition(seekBar: SeekBar, currentPositionTextView: TextView) {
                CoroutineScope(Dispatchers.Main).launch {
                    while (true) {
                        mediaPlayer?.let { player ->
                            if (mediaPlayerIsPrepared(player) && player.isPlaying) {
                                val currentPosition = player.currentPosition
                                seekBar.progress = currentPosition
                                currentPositionTextView.text = formatTime(currentPosition)
                            }
                        }
                        delay(1000)
                    }
                }
            }

            private fun updateSeekBar(seekBar: SeekBar, currentPositionTextView: TextView) {
                CoroutineScope(Dispatchers.Main).launch {
                    while (true) {
                        mediaPlayer?.let { player ->
                            if (mediaPlayerIsPrepared(player) && player.isPlaying) {
                                val currentPosition = player.currentPosition
                                seekBar.progress = currentPosition
                                currentPositionTextView.text = formatTime(currentPosition)
                            }
                        }
                        delay(1000)
                    }
                }
            }

            private fun mediaPlayerIsPrepared(player: MediaPlayer): Boolean {
                return try {
                    player.isPlaying && player.duration > 0
                } catch (e: IllegalStateException) {
                    false
                }
            }

            private fun formatTime(millis: Int): String {
                val seconds = millis / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                return String.format("%02d:%02d", minutes, remainingSeconds)
            }

            fun bind(music: Music) {
                titleTextView.text = music.title
                artistTextView.text = music.artist
                val durationInSeconds = music.duration / 1000
                val minutes = durationInSeconds / 60
                val seconds = durationInSeconds % 60
                val durationString = String.format("%02d:%02d", minutes, seconds)
                durationTextView.text = durationString
            }
        }

        private fun setFadeAnimation(view: View) {
            val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            animator.duration = 320
            animator.start()
        }
    }

    data class Music(
        val id: Long,
        val title: String,
        val artist: String,
        val duration: Long,
        val uri: Uri,
    )

    override fun onDestroy() {
        super.onDestroy()
        musicAdapter.releaseMediaPlayer()
    }

    override fun onPause() {
        super.onPause()
        musicAdapter.pauseMediaPlayer()
    }

    override fun onResume() {
        super.onResume()
        musicAdapter.resumeMediaPlayer()
    }

}
