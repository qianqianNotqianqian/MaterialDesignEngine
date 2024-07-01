package mapleleaf.materialdesign.engine.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.lifecycleScope
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import me.zhanghai.android.fastscroll.FastScrollNestedScrollView
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.util.Calendar

class ActivitySystemIcons : UniversalActivityBase(R.layout.activity_system_icons_style) {
    private lateinit var batteryIcon: ImageView
    private lateinit var loading: AppCompatImageView
    private lateinit var speed: AppCompatImageView
    private lateinit var loop: AppCompatImageView
    private lateinit var finger: AppCompatImageView
    private lateinit var overFlow: AppCompatImageView
    private var isAnimation1Playing = true
    private var isAnimation2Playing = true

    override fun initializeComponents(savedInstanceState: Bundle?) {
        setToolbarTitle(getString(R.string.toolbar_title_activity_system_icons))
        initializeAnimations()
        setupButtonClick()
        setupBatteryReceiver()
        setupFastScroller()
    }

    private fun initializeAnimations() {
        batteryIcon = findViewById(R.id.battery_icon)
        loading = findViewById(R.id.loading)
        speed = findViewById(R.id.speed)
        loop = findViewById(R.id.loop)
        finger = findViewById(R.id.finger)
        overFlow = findViewById(R.id.overFlow)

        // 初始化手指动画
        playAnimatedVectorDrawable(finger, R.drawable.fingerprint_dialog_error_to_fp)
        isAnimation1Playing = false

        // 播放加载动画
        playAnimatedVectorDrawable(loading, R.drawable.progress_loading_manager)

        // 播放速度动画
        playAnimatedVectorDrawable(speed, R.drawable.avd_speed)

        // 播放循环动画
        playAnimatedVectorDrawable(loop, R.drawable.avd_flip)

        //播放溢出菜单折叠动画
        playAnimatedVectorDrawable(overFlow, R.drawable.ft_avd_tooverflow_animation)
    }

    private fun setupButtonClick() {
        val playButton = findViewById<Button>(R.id.playButton)
        playButton.setOnClickListener {

            playAnimatedVectorDrawable(speed, R.drawable.avd_speed)

            playAnimatedVectorDrawable(loop, R.drawable.avd_flip)

            AnimatedVectorDrawableCompat.create(this@ActivitySystemIcons,
                if (isAnimation1Playing) R.drawable.fingerprint_dialog_error_to_fp else R.drawable.fingerprint_dialog_fp_to_error)?.apply {
                finger.setImageDrawable(this)
                start()
            }

            AnimatedVectorDrawableCompat.create(this@ActivitySystemIcons,
                if (isAnimation2Playing) R.drawable.ft_avd_toarrow_animation else R.drawable.ft_avd_tooverflow_animation)?.apply {
                overFlow.setImageDrawable(this)
                start()
            }

            isAnimation1Playing = !isAnimation1Playing
            isAnimation2Playing = !isAnimation2Playing

        }
    }

    private fun playAnimatedVectorDrawable(imageView: ImageView, drawableResId: Int) {
        val animatedVectorDrawable =
            AnimatedVectorDrawableCompat.create(this@ActivitySystemIcons, drawableResId)
        imageView.setImageDrawable(animatedVectorDrawable)
        animatedVectorDrawable?.start()
    }

    private fun setupBatteryReceiver() {
        // 注册电量广播接收器
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    private fun setupFastScroller() {
        // 设置快速滚动
        val nestedScrollView = findViewById<FastScrollNestedScrollView>(R.id.nestedScrollView2)
        FastScrollerBuilder(nestedScrollView).useMd2Style().build()
    }

    // 电量广播接收器
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            lifecycleScope.launch(Dispatchers.Main) {
                delay(500)
                val batteryLevel = withContext(Dispatchers.Default) { getBatteryLevel(intent) }
                val charging = withContext(Dispatchers.Default) { isCharging(intent) }

                val iconResourceId: Int = if (charging) {
                    getChargingBatteryIcon(batteryLevel)
                } else {
                    getRegularBatteryIcon(batteryLevel)
                }

                batteryIcon.setImageResource(iconResourceId)

                // 获取当前时间
                val calendar: Calendar = Calendar.getInstance()
                val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
                val minute: Int = calendar.get(Calendar.MINUTE)

                // 获取时钟的数字位
                val hourFirstDigit: Int = hour / 10
                val hourSecondDigit: Int = hour % 10
                val minuteFirstDigit: Int = minute / 10
                val minuteSecondDigit: Int = minute % 10

                // 设置时钟的数字位
                setDigit(R.id.hour_first_digit, hourFirstDigit)
                setDigit(R.id.hour_second_digit, hourSecondDigit)
                setDigit(R.id.minute_first_digit, minuteFirstDigit)
                setDigit(R.id.minute_second_digit, minuteSecondDigit)
            }
        }
    }

    // 获取电量百分比
    private suspend fun getBatteryLevel(batteryIntent: Intent): Int {
        return withContext(Dispatchers.Default) {
            val level: Int = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            return@withContext (level / scale.toFloat() * 100).toInt()
        }
    }

    // 检测充电状态
    private suspend fun isCharging(batteryIntent: Intent): Boolean {
        return withContext(Dispatchers.Default) {
            val status: Int = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            return@withContext status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        }
    }

    private fun setDigit(viewId: Int, digit: Int) {
        val imageView: ImageView = findViewById(viewId)
        val resourceId: Int = when (digit) {
            0 -> R.drawable.font_a_0
            1 -> R.drawable.font_a_1
            2 -> R.drawable.font_a_2
            3 -> R.drawable.font_a_3
            4 -> R.drawable.font_a_4
            5 -> R.drawable.font_a_5
            6 -> R.drawable.font_a_6
            7 -> R.drawable.font_a_7
            8 -> R.drawable.font_a_8
            9 -> R.drawable.font_a_9
            else -> 0
        }
        imageView.setImageResource(resourceId)
    }

    private fun getChargingBatteryIcon(batteryLevel: Int): Int {
        return when {
            batteryLevel >= 90 -> R.drawable.battery100_charge
            batteryLevel >= 80 -> R.drawable.battery90_charge
            batteryLevel >= 70 -> R.drawable.battery80_charge
            batteryLevel >= 60 -> R.drawable.battery70_charge
            batteryLevel >= 50 -> R.drawable.battery60_charge
            batteryLevel >= 40 -> R.drawable.battery50_charge
            batteryLevel >= 30 -> R.drawable.battery40_charge
            batteryLevel >= 20 -> R.drawable.battery30_charge
            batteryLevel >= 10 -> R.drawable.battery20_charge
            batteryLevel >= 0 -> R.drawable.battery10_charge
            else -> 0
        }
    }

    private fun getRegularBatteryIcon(batteryLevel: Int): Int {
        return when {
            batteryLevel >= 90 -> R.drawable.battery100
            batteryLevel >= 80 -> R.drawable.battery90
            batteryLevel >= 70 -> R.drawable.battery80
            batteryLevel >= 60 -> R.drawable.battery70
            batteryLevel >= 50 -> R.drawable.battery60
            batteryLevel >= 40 -> R.drawable.battery50
            batteryLevel >= 30 -> R.drawable.battery40
            batteryLevel >= 20 -> R.drawable.battery30
            batteryLevel >= 10 -> R.drawable.battery20
            batteryLevel >= 0 -> R.drawable.battery10
            else -> 0
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
    }
}
