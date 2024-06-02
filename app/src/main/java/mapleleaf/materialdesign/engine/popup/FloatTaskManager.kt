package mapleleaf.materialdesign.engine.popup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.library.shell.ProcessUtils2
import mapleleaf.materialdesign.engine.ui.adapter.AdapterFloatProcess
import mapleleaf.materialdesign.engine.utils.toast
import java.util.Timer
import java.util.TimerTask

class FloatTaskManager(private val context: Context) {
    companion object {
        var mView: View? = null
        private var locked = false
        private var lastTouchDown = 0L
        val show: Boolean
            get() {
                return mView != null
            }
        private var timer: Timer? = null
    }

    val supported: Boolean
        get() {
            return processUtils.supported(context)
        }

    /**
     * dp转换成px
     */
    private fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun showPopupWindow() {
        if (mView != null) {
            return
        }

        setup()

        val monitorStorage =
            context.getSharedPreferences("float_task_storage", Context.MODE_PRIVATE)

        // 获取WindowManager
        val mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams()
        // 类型
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//6.0+
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        params.format = PixelFormat.TRANSLUCENT

        params.width = WindowManager.LayoutParams.WRAP_CONTENT // dp2px(context,180f)
        params.height = WindowManager.LayoutParams.WRAP_CONTENT // dp2px(context,220f)

        params.gravity = Gravity.TOP or Gravity.START
        params.x = monitorStorage.getInt("x", 0)
        params.y = monitorStorage.getInt("y", 0)

        params.flags =
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_FULLSCREEN

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        mWindowManager.addView(mView, params)

        // 添加触摸事件
        mView!!.setOnTouchListener(object : View.OnTouchListener {
            private var isTouchDown = false
            private var touchStartX = 0f
            private var touchStartY = 0f
            private var touchStartRawX = 0f
            private var touchStartRawY = 0f

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (locked) {
                    return false
                }
                if (event != null) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            touchStartX = event.getX()
                            touchStartY = event.getY()
                            touchStartRawX = event.rawX
                            touchStartRawY = event.rawY
                            isTouchDown = true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            if (isTouchDown) {
                                params.x = (event.rawX - touchStartX).toInt()
                                params.y = (event.rawY - touchStartY).toInt()
                                mWindowManager.updateViewLayout(v, params)
                            }
                        }

                        MotionEvent.ACTION_UP -> {
                            monitorStorage.edit().putInt("x", params.x).putInt("y", params.y)
                                .apply()
                        }

                        MotionEvent.ACTION_OUTSIDE,
                        MotionEvent.ACTION_CANCEL,
                        -> {
                            isTouchDown = false
                        }
                    }
                }
                return false
            }
        })

        val fwFloatPin = mView?.findViewById<View>(R.id.fw_float_pin)!!

        fwFloatPin.setOnLongClickListener {
            params.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            mView!!.setBackgroundColor(Color.argb(128, 255, 255, 255))
            mWindowManager.updateViewLayout(mView, params)
            true
        }

        this.startTimer()
    }

    private val handle = Handler(Looper.getMainLooper())
    private val processUtils = ProcessUtils2()

    // 更新任务列表
    private fun updateData() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTouchDown < 2000) {
            return
        }
        val data = processUtils.allProcess
        if (currentTime - lastTouchDown < 3000) {
            return
        }
        handle.post {
            (mView?.findViewById<ListView>(R.id.process_list)?.adapter as AdapterFloatProcess?)?.setList(
                data
            )
        }
    }

    @SuppressLint("InflateParams")
    private fun setup() {
        locked = false

        mView = LayoutInflater.from(context).inflate(R.layout.fw_process, null)
        // mView?.setBackgroundColor(Color.WHITE)

        val processList = mView?.findViewById<ListView>(R.id.process_list)!!.apply {
            adapter = AdapterFloatProcess(this.context)
        }
        val fwFloatMinimize = mView?.findViewById<ImageButton>(R.id.fw_float_minimize)!!
        val processFilter = mView?.findViewById<TextView>(R.id.process_filter)!!
        val fwFloatPin = mView?.findViewById<View>(R.id.fw_float_pin)!!

        var filterMode = AdapterFloatProcess.FILTER_ANDROID
        // 过滤筛选
        processFilter.setOnClickListener {
            filterMode =
                if (filterMode == AdapterFloatProcess.FILTER_ANDROID) AdapterFloatProcess.FILTER_ALL else AdapterFloatProcess.FILTER_ANDROID
            (processList.adapter as AdapterFloatProcess).updateFilterMode(filterMode)
            processFilter.text =
                if (filterMode == AdapterFloatProcess.FILTER_ANDROID) "应用" else "全部"
        }

        var lastClick: Int? = null
        processList.setOnItemClickListener { _, _, position, _ ->
            val current = System.currentTimeMillis()
            val adapter = (processList.adapter as AdapterFloatProcess)
            val processInfo = adapter.getItem(position)
            if (processInfo.name.equals(context.packageName)) {
                toast("自杀是不允许的~")
                return@setOnItemClickListener
            }
            if (current - lastTouchDown > 3000 || processInfo.pid != lastClick) {
                lastTouchDown = System.currentTimeMillis()
                lastClick = processInfo.pid
                toast("如需结束进程，请再次点击")
            } else {
                processUtils.killProcess(processInfo)
                adapter.removeItem(position)
            }
        }

        // 锁定位置
        fwFloatPin.setOnClickListener {
            locked = !locked
            if (locked) {
                it.alpha = 1f
                toast("已锁定悬浮窗位置，你也可以 [长按] 此图标，使悬浮窗不可操作")
            } else {
                it.alpha = 0.3f
            }
        }

        // 关闭
        val fwFloatClose = mView?.findViewById<ImageButton>(R.id.fw_float_close)!!
        fwFloatClose.setOnClickListener {
            hidePopupWindow()
        }

        // 切换最小化
        fwFloatMinimize.setOnClickListener {
            if (processList.visibility == View.VISIBLE) {
                processList.visibility = View.GONE
                processFilter.visibility = View.GONE
                fwFloatClose.visibility = View.GONE
                fwFloatMinimize.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_maximize
                    )
                )

                this.stopUpdate()
            } else {
                processList.visibility = View.VISIBLE
                processFilter.visibility = View.VISIBLE
                fwFloatClose.visibility = View.VISIBLE
                fwFloatMinimize.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_minimize
                    )
                )
                this.startTimer()
            }
//             processFilter.visibility = if (processFilter.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    private fun stopUpdate() {
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
    }

    private fun startTimer() {
        this.stopUpdate()
        if (timer == null) {
            timer = Timer()
            timer!!.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    updateData()
                }
            }, 0, 3000)
        }
    }

    fun hidePopupWindow() {
        this.stopUpdate()
        mView?.run {
            // 获取WindowManager
            val mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            mWindowManager.removeViewImmediate(mView)
            mView = null
        }
    }
}