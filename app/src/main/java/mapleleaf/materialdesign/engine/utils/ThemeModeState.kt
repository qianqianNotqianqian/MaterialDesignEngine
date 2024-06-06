package mapleleaf.materialdesign.engine.utils

import android.app.Activity
import android.app.UiModeManager
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.ui.dialog.DialogProgressBar
import java.io.File
import java.io.FileOutputStream

object ThemeModeState {
    private var themeMode: ThemeMode = ThemeMode()
    private var wallpaperFile: File? = null
    private lateinit var progressBar: DialogProgressBar

    private suspend fun loadWallpaper(activity: Activity, nightMode: Boolean) {
        val wallpaperManager = WallpaperManager.getInstance(activity)
        progressBar = DialogProgressBar(activity)
        progressBar.showDialog()
        if (ThemeConfig(activity).getAllowTransparentUI()) {
            if (nightMode) {
                themeMode.isDarkMode = true
                activity.setTheme(R.style.CustomMaterial3Theme)
            } else {
                themeMode.isDarkMode = false
                activity.setTheme(R.style.CustomMaterial3Theme)
            }
            if (wallpaperFile == null) {
                withContext(Dispatchers.IO) {
                    val wallpaperBitmap = (wallpaperManager.drawable as BitmapDrawable).bitmap
                    wallpaperFile = saveBitmapToFile(activity, wallpaperBitmap)
                }
            }
            progressBar.hideDialog()
            // Set wallpaper from cached file
            wallpaperFile?.let { file ->
                withContext(Dispatchers.Main) {
                    activity.window.setBackgroundDrawable(
                        BitmapDrawable(
                            activity.resources,
                            file.absolutePath
                        )
                    )
                }
            }
        } else {
            if (nightMode) {
                themeMode.isDarkMode = true
                themeMode.isLightStatusBar = false
            } else {
                themeMode.isDarkMode = false
            }
        }
    }

    private fun saveBitmapToFile(context: Context, bitmap: Bitmap): File {
        val wallpaperDir = File(context.filesDir, "wallpapers")
        wallpaperDir.mkdirs()
        val wallpaperFile = File(wallpaperDir, "wallpaper.jpg")

        FileOutputStream(wallpaperFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        return wallpaperFile
    }

//    fun switchTheme(activity: Activity? = null): ThemeMode {
//        if (activity != null) {
//            CoroutineScope(Dispatchers.Main).launch {
//                val uiModeManager =
//                    activity.applicationContext.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
//                val nightMode = (uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES)
//                loadWallpaper(activity, nightMode)
//                progressBar.hideDialog()
//                if (!themeMode.isDarkMode) {
//                    themeMode.isLightStatusBar = (true)
//                    activity.window.run {
//                        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//                        decorView.systemUiVisibility =
//                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            decorView.systemUiVisibility =
//                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
//                        } else {
//                            decorView.systemUiVisibility =
//                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//                        }
//                    }
//                }
//            }
//        }
//        return themeMode
//    }

    fun switchTheme(activity: Activity? = null): ThemeMode {
        if (activity != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val uiModeManager =
                    activity.applicationContext.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
                val nightMode = (uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES)
                loadWallpaper(activity, nightMode)
                progressBar.hideDialog()

                if (!themeMode.isDarkMode) {
                    themeMode.isLightStatusBar = (true)
                    activity.window.run {
                        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val controller = insetsController
                            controller?.setSystemBarsAppearance(
                                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                            )
                            controller?.setSystemBarsAppearance(
                                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                            )
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            decorView.systemUiVisibility = (
                                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                                            or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                                    )
                        } else {
                            decorView.systemUiVisibility = (
                                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                            or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                                    )
                        }

                        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    }
                }
            }
        }
        return themeMode
    }

}