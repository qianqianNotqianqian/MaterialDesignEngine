package mapleleaf.materialdesign.engine.ui.activities

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.utils.toast

class ActivitySystemVibration : UniversalActivityBase(R.layout.activity_vibration_control) {
    private lateinit var vibrator: Vibrator
    private lateinit var startVibrationButton: Button
    private lateinit var stopVibrationButton: Button
    private lateinit var btnHasvibrator: Button
    private lateinit var btnShort: Button
    private lateinit var btnLong: Button
    private lateinit var btnRhythm: Button
    private lateinit var durationEditText: EditText

    override fun initializeComponents(savedInstanceState: Bundle?) {
        setToolbarTitle(getString(R.string.toolbar_title_activity_vibration_control))
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        durationEditText = findViewById(R.id.durationEditText)
        startVibrationButton = findViewById(R.id.startVibrationButton)
        stopVibrationButton = findViewById(R.id.stopVibrationButton)
        btnHasvibrator = findViewById(R.id.btn_hasVibrator)
        btnShort = findViewById(R.id.btn_short)
        btnLong = findViewById(R.id.btn_long)
        btnRhythm = findViewById(R.id.btn_rhythm)

        var vibrationTimer: CountDownTimer? = null

        startVibrationButton.setOnClickListener {
            val durationText = durationEditText.text.toString()
            if (durationText.isNotEmpty()) {
                val durationInSeconds = durationText.toLong()
                if (durationInSeconds > 0) {
                    // 计算震动时长
                    val durationInMillis = durationInSeconds * 1000
                    val pattern = longArrayOf(0, durationInMillis)
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    startVibrationButton.isVisible = false
                    stopVibrationButton.isVisible = true
                    toast("开始震动，时长：$durationInSeconds 秒")

                    vibrationTimer?.cancel()
                    vibrationTimer = object : CountDownTimer(durationInMillis, 1000) {
                        override fun onTick(millisUntilFinished: Long) {}

                        override fun onFinish() {
                            // 当震动结束时
                            stopVibrationButton.isVisible = false
                            startVibrationButton.isVisible = true
                            toast("震动已结束")
                        }
                    }.start()
                } else {
                    toast("请输入一个大于零的数字")
                }
            } else {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 1000, 1000), 0))
                startVibrationButton.isVisible = false
                stopVibrationButton.isVisible = true
                toast("开始震动")
            }
        }

        stopVibrationButton.setOnClickListener {
            // 取消计时器，即使震动未结束，用户手动点击了停止按钮也会触发此操作
            vibrationTimer?.cancel()
            vibrator.cancel() // 停止震动
            startVibrationButton.isVisible = true
            stopVibrationButton.isVisible = false
            toast("震动已停止")
        }

        btnHasvibrator.setOnClickListener {
            if (vibrator.hasVibrator()) {
                toast("设备支持震动")
            } else {
                toast("设备没有震动功能")
            }
        }

        btnShort.setOnClickListener {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        btnLong.setOnClickListener {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        }

        btnRhythm.setOnClickListener { v: View? ->
            val patterns: MutableList<VibrationPattern> =
                ArrayList()
            patterns.add(
                VibrationPattern(
                    "Pattern 1",
                    longArrayOf(0, 500, 200, 500, 200, 500)
                )
            )
            patterns.add(
                VibrationPattern(
                    "Pattern 2",
                    longArrayOf(0, 300, 100, 300, 100, 300)
                )
            )
            patterns.add(
                VibrationPattern(
                    "Pattern 3",
                    longArrayOf(0, 200, 400, 200, 400, 200)
                )
            )
            patterns.add(
                VibrationPattern(
                    "Pattern 4",
                    longArrayOf(0, 100, 600, 100, 600, 100)
                )
            )
            patterns.add(
                VibrationPattern(
                    "Pattern 5",
                    longArrayOf(0, 1000, 300, 1000, 300, 1000)
                )
            )
            patterns.add(
                VibrationPattern(
                    "Pattern 6",
                    longArrayOf(0, 400, 200, 400, 200, 400)
                )
            )
            patterns.add(
                VibrationPattern(
                    "Pattern 7",
                    longArrayOf(0, 800, 200, 800, 200, 800)
                )
            )
            patterns.add(
                VibrationPattern(
                    "Pattern 8",
                    longArrayOf(0, 150, 450, 150, 450, 150)
                )
            )
            patterns.add(
                VibrationPattern(
                    "Pattern 9",
                    longArrayOf(0, 200, 100, 200, 100, 200)
                )
            )
            patterns.add(
                VibrationPattern(
                    "Pattern 10",
                    longArrayOf(0, 600, 300, 600, 300, 600)
                )
            )
            patterns.add(
                VibrationPattern(
                    "Pattern 11",
                    longArrayOf(0, 700, 400, 700, 400, 700)
                )
            )
            patterns.add(
                VibrationPattern(
                    "Pattern 12",
                    longArrayOf(0, 250, 350, 250, 350, 250)
                )
            )
            val builder =
                MaterialAlertDialogBuilder(this@ActivitySystemVibration)
            builder.setTitle("选择震动模式")
            val patternNames =
                arrayOfNulls<CharSequence>(patterns.size)
            for (i in patterns.indices) {
                patternNames[i] = patterns[i].name
            }
            builder.setItems(
                patternNames
            ) { _: DialogInterface?, which: Int ->
                val selectedPattern: VibrationPattern = patterns[which]
                val effect =
                    VibrationEffect.createWaveform(selectedPattern.pattern, -1)
                vibrator.vibrate(effect)
                v?.let { toast("已选择震动模式：" + selectedPattern.name) }
            }
            builder.show()
        }

        val rootView = findViewById<View>(android.R.id.content)
        rootView.setOnClickListener {
            // 当用户点击了除编辑框以外的任何地方时，取消编辑框的焦点
            durationEditText.clearFocus()
        }
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // 在失去焦点后隐藏输入法键盘
        durationEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                inputMethodManager.hideSoftInputFromWindow(durationEditText.windowToken, 0)
            }
        }

        val baseColor = ContextCompat.getColor(MaterialDesignEngine.context, R.color.background)
        val primaryColor = ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
        val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)
        findViewById<MaterialCardView>(R.id.materialCardView).apply {
            strokeColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.3f)
            setCardBackgroundColor(blendedColor)
        }
    }

    @JvmRecord
    private data class VibrationPattern(val name: String, val pattern: LongArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as VibrationPattern
            if (name != other.name) return false
            if (!pattern.contentEquals(other.pattern)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + pattern.contentHashCode()
            return result
        }
    }
}