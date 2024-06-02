package mapleleaf.materialdesign.engine.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.UiModeManager
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.view.View
import android.view.WindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.ui.dialog.DialogProgressBar
import java.io.File
import java.io.FileOutputStream

@SuppressLint("StaticFieldLeak")
object ThemeModeState {
    private var themeMode: ThemeMode = ThemeMode()
    private lateinit var progressBar: DialogProgressBar

    @SuppressLint("StaticFieldLeak")
    private suspend fun loadWallpaper(activity: Activity, nightMode: Boolean) {
        val wallpaperManager = WallpaperManager.getInstance(activity)
        progressBar = DialogProgressBar(activity)
        progressBar.showDialog()
        if (ThemeConfig(activity).getAllowTransparentUI()) {
            if (nightMode) {
                themeMode.isDarkMode = true
                activity.setTheme(R.style.CustomMaterial3Theme_Dark)
            } else {
                themeMode.isDarkMode = false
                activity.setTheme(R.style.CustomMaterial3Theme_Light)
            }
            val wallpaperBitmap = withContext(Dispatchers.IO) {
                (wallpaperManager.drawable as BitmapDrawable).bitmap
            }
            setWallpaperAsWindowBackground(activity, wallpaperBitmap)
        } else {
            if (nightMode) {
                themeMode.isDarkMode = true
                themeMode.isLightStatusBar = false
            } else {
                themeMode.isDarkMode = false
            }
        }
        progressBar.hideDialog()
    }

    private fun setWallpaperAsWindowBackground(context: Context, bitmap: Bitmap) {
        val window = (context as Activity).window
        window.setBackgroundDrawable(BitmapDrawable(context.resources, bitmap))
    }

    fun switchTheme(activity: Activity? = null): ThemeMode {
        if (activity != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val uiModeManager =
                    activity.applicationContext.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
                val nightMode = (uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES)
                loadWallpaper(activity, nightMode)
                if (!themeMode.isDarkMode) {
                    themeMode.isLightStatusBar = true
                    val window = activity.window
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    } else {
                        window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                }
            }
        }
        return themeMode
    }
}
