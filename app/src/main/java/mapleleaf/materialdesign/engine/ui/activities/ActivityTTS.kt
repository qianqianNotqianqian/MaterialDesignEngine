package mapleleaf.materialdesign.engine.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.utils.toast
import java.io.IOException
import java.net.URL

class ActivityTTS : UniversalActivityBase(R.layout.activity_speech_synthesis) {

    private lateinit var editText: EditText
    private lateinit var textView: TextView
    private lateinit var button: FloatingActionButton
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var progressBar: ProgressBar
    private lateinit var msgMaterialCardView: MaterialCardView

    override fun initializeComponents(savedInstanceState: Bundle?) {

        setToolbarTitle(getString(R.string.toolbar_title_activity_speech_synthesis))

        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                editText = findViewById(R.id.edit_text)
                val speakerSpinner: Spinner = findViewById(R.id.speakerSpinner)
                val lengthSpinner: Spinner = findViewById(R.id.lengthSpinner)
                val noisewSpinner: Spinner = findViewById(R.id.noisewSpinner)
                val sdpSpinner: Spinner = findViewById(R.id.sdpSpinner)
                val noiseSpinner: Spinner = findViewById(R.id.noiseSpinner)

                textView = findViewById(R.id.textView)
                button = findViewById(R.id.floatingActionButton)
                progressBar = findViewById(R.id.progressBar)

                progressBar.isIndeterminate = true
                msgMaterialCardView = findViewById(R.id.msgMaterialCardView)
                setupSpinner(speakerSpinner, R.array.speaker_options)
                speakerSpinner.setSelection(0)
                setupSpinner(lengthSpinner, R.array.length_options)
                lengthSpinner.setSelection(0)
                setupSpinner(noisewSpinner, R.array.noisew_options)
                noisewSpinner.setSelection(7)
                setupSpinner(sdpSpinner, R.array.sdp_options)
                sdpSpinner.setSelection(3)
                setupSpinner(noiseSpinner, R.array.noise_options)
                noiseSpinner.setSelection(5)
                mediaPlayer = MediaPlayer()

                button.setOnClickListener {
                    val msg = editText.text.toString().trim()
                    val speaker = speakerSpinner.selectedItem.toString()
                    val length = lengthSpinner.selectedItem.toString()
                    val noisew = noisewSpinner.selectedItem.toString()
                    val sdp = sdpSpinner.selectedItem.toString()
                    val noise = noiseSpinner.selectedItem.toString()
                    if (msg.isEmpty()) {
                        toast("请输入文字再合成！(不支持英文)")
                    } else {
                        fetchAudio(msg, speaker, length, noisew, sdp, noise)
                        progressBar.isVisible = true
                    }
                }

            }
        }
    }

    private suspend fun setupSpinner(spinner: Spinner, optionsArray: Int) {
        withContext(Dispatchers.IO) {
            val options = resources.getStringArray(optionsArray)
            val adapter =
                ArrayAdapter(spinner.context, android.R.layout.simple_spinner_item, options)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            withContext(Dispatchers.Main) {
                spinner.adapter = adapter
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchAudio(
        msg: String,
        selectedSpeaker: String,
        length: String,
        noisew: String,
        sdp: String,
        noise: String,
    ) {
        val apiUrl = "https://api.lolimi.cn/API/yyhc/y.php?" +
                "msg=$msg" +
                "&speaker=$selectedSpeaker" +
                "&Length=$length" +
                "&noisew=$noisew" +
                "&sdp=$sdp" +
                "&noise=$noise" +
                "&type=1"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = URL(apiUrl).readText()
                Log.d("Response", response)

                val musicUrl = response.substringAfter("music\": \"").substringBefore("\"")
                Log.d("Music URL", musicUrl)

                val msg = response.substringAfter("msg\": \"").substringBefore("\"")
                Log.d("Message", msg)

                withContext(Dispatchers.Main) {
                    textView.text = "合成的语音：$msg"
                    playAudioInBackground(msg, musicUrl)
                    progressBar.isVisible = false
                    msgMaterialCardView.isVisible = true
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // 处理错误情况，例如显示错误信息等
                }
            }
        }
    }

    private var isPlaying = false
    private var mediaPlayerCoroutine: Job? = null

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun playAudioInBackground(msg: String, audioUrl: String) {
        Log.d("Event", "Creating dialog")
        mediaPlayer.release()
        mediaPlayer = MediaPlayer()
        mediaPlayer.reset()

        val layoutInflater =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val dialogView = layoutInflater.inflate(R.layout.dialog_audio_progress, null)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.seekBar)
        dialogView.findViewById<TextView>(R.id.player_title).text = "音乐播放器"
        dialogView.findViewById<TextView>(R.id.musicFileNameTextView).text = msg
        val playPauseButton = dialogView.findViewById<Button>(R.id.playPauseButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val totalDurationTextView = dialogView.findViewById<TextView>(R.id.totalDurationTextView)
        val currentPositionTextView =
            dialogView.findViewById<TextView>(R.id.currentPositionTextView)

        val dialog = DialogHelper.customDialog(this, dialogView)

        playPauseButton.setOnClickListener {
            if (isPlaying) {
                mediaPlayer.pause()
                playPauseButton.text = "播放"
            } else {
                if (mediaPlayer.currentPosition >= mediaPlayer.duration) {
                    mediaPlayer.seekTo(0)
                }
                mediaPlayer.start()
                playPauseButton.text = "暂停"
            }
            isPlaying = !isPlaying
        }

        cancelButton.setOnClickListener {
            mediaPlayer.stop()
            mediaPlayer.release()
            isPlaying = false
            dialog.dismiss()
            mediaPlayerCoroutine?.cancel()
        }

        mediaPlayer.setOnCompletionListener {
            playPauseButton.text = "播放"
            isPlaying = false
            seekBar.progress = 0
            currentPositionTextView.text = "00:00"
        }

        mediaPlayerCoroutine = lifecycleScope.launch(Dispatchers.IO) {
            Log.d("Event", "Waiting for audio to load")
            try {
                mediaPlayer.setDataSource(audioUrl)
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mediaPlayer.prepare()
                mediaPlayer.start()
                isPlaying = true

                while (isActive) {
                    val currentPosition = mediaPlayer.currentPosition
                    val totalDuration = mediaPlayer.duration

                    withContext(Dispatchers.Main) {
                        seekBar.max = totalDuration
                        seekBar.progress = currentPosition
                        currentPositionTextView.text = formatDuration(currentPosition)
                        totalDurationTextView.text = formatDuration(totalDuration)
                    }

                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        mediaPlayerCoroutine?.cancel()
    }

    private val durationCache = HashMap<Int, String>()

    private fun formatDuration(duration: Int): String {
        // 检查缓存，如果已经计算过则直接返回缓存的结果
        if (durationCache.containsKey(duration)) {
            return durationCache[duration] ?: ""
        }

        // 计算分钟和秒
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60

        // 使用 StringBuilder 构建结果字符串
        val formattedDuration = StringBuilder()
        formattedDuration.append(String.format("%02d:", minutes))
        formattedDuration.append(String.format("%02d", seconds))

        // 缓存结果
        durationCache[duration] = formattedDuration.toString()

        return formattedDuration.toString()
    }
}