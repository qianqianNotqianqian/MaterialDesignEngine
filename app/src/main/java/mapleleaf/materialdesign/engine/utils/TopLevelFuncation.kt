package mapleleaf.materialdesign.engine.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import java.lang.ref.WeakReference

/**
 * Kotlin 顶层函数
 */

/**
 * 设置状态栏图标颜色
 * @param dark true 为黑色，false 为白色
 */
fun setStatusBarIconColor(activity: Activity, dark: Boolean) {
    StatusbarColorUtils.setStatusBarDarkIcon(activity, dark)
}

var sToastRef: WeakReference<Toast>? = null

/**
 * 全局 toast
 */
fun toast(msg: String) {
    runOnMainThread {
        sToastRef?.get()?.cancel()
        val toast = Toast.makeText(MaterialDesignEngine.context, msg, Toast.LENGTH_SHORT)
        toast.show()
        sToastRef = WeakReference(toast)
    }
}

/**
 * 运行在主线程，更新 UI
 */
fun runOnMainThread(runnable: Runnable) {
    Handler(Looper.getMainLooper()).post(runnable)
}


/**
 * dp 转 px
 */
fun dp2px(dp: Float): Float = dp * MaterialDesignEngine.context.resources.displayMetrics.density

/**
 * dp
 */
fun Int.dp(): Int {
    return dp2px(this.toFloat()).toInt()
}

/**
 * 获取系统当前时间
 */
fun getCurrentTime(): Long {
    return System.currentTimeMillis()
}

/**
 * 通过浏览器打开网页
 * @param context
 * @url 网址
 */
fun openUrlByBrowser(context: Context, url: String) {
    if (url != "") {
        try {
            val intent = Intent()
            intent.action = "android.intent.action.VIEW"
            val contentUrl = Uri.parse(url)
            intent.data = contentUrl
            ContextCompat.startActivity(context, intent, Bundle())
        } catch (e: Exception) {
            toast("启动外部浏览器失败")
        }
    }
}

/**
 * 毫秒转日期
 */
fun msTimeToFormatDate(msTime: Long): String {
    return TimeUtil.msTimeToFormatDate(msTime)
}

/**
 * 获取状态栏高度
 * @return px 值
 */
fun getStatusBarHeight(window: Window, context: Context): Int {
    return StatusBarUtil.getStatusBarHeight(window, context)
}

/**
 * 获取底部导航栏高度
 */
fun getNavigationBarHeight(activity: Activity): Int {
    return ScreenUtil.getNavigationBarHeight(activity)
}

var lastClickTime = 0L
