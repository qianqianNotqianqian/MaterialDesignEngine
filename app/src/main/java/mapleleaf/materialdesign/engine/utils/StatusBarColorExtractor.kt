package mapleleaf.materialdesign.engine.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.shape.MaterialShapeDrawable

object StatusBarColorExtractor {
    fun extractToolbarColor(@NonNull activity: AppCompatActivity, @NonNull toolbar: Toolbar) {
        var toolbarColor = extractToolbarColorFromDrawable(toolbar)
        if (toolbarColor == Color.TRANSPARENT) {
            toolbarColor = extractToolbarColorFromMaterialShapeDrawable(toolbar)
        }
        applyStatusBarColor(activity, toolbarColor)
    }

    private fun extractToolbarColorFromDrawable(@NonNull toolbar: Toolbar): Int {
        var toolbarColor = Color.TRANSPARENT
        if (toolbar.background is ColorDrawable) {
            toolbarColor = (toolbar.background as ColorDrawable).color
        }
        return toolbarColor
    }

    private fun extractToolbarColorFromMaterialShapeDrawable(@NonNull toolbar: Toolbar): Int {
        var toolbarColor = Color.TRANSPARENT
        if (toolbar is MaterialToolbar) {
            val materialShapeDrawable = toolbar.background as? MaterialShapeDrawable
            materialShapeDrawable?.let {
                toolbarColor = it.fillColor!!.defaultColor
            }
        }
        return toolbarColor
    }

    private fun applyStatusBarColor(@NonNull activity: AppCompatActivity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color

            // 根据状态栏背景颜色选择文字颜色
            val textColor = if (isColorDark(color)) Color.BLACK else Color.WHITE
            val decorView: View = window.decorView
            var flags: Int = decorView.systemUiVisibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() // 清除浅色状态栏标志位
            }
            decorView.systemUiVisibility =
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // 设置深色状态栏标志位
        }
    }

    private fun isColorDark(color: Int): Boolean {
        val darkness =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }
}
