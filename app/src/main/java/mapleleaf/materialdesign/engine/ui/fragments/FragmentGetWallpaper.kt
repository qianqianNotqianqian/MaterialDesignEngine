package mapleleaf.materialdesign.engine.ui.fragments

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalFragmentBase
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.utils.toast
import java.io.File
import java.io.FileOutputStream

class FragmentGetWallpaper : UniversalFragmentBase(R.layout.fragment_get_image) {

    private var isWallpaperLoaded = false
    private var progressBar: ProgressBar? = null
    private lateinit var wallpaperImageView: ImageView
    private lateinit var relativeLayoutImg: LinearLayout
    private lateinit var menuDownload: FloatingActionButton
    private lateinit var menuShare: FloatingActionButton
    private lateinit var menuMore: FloatingActionButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)
        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                wallpaperImageView = rootView.findViewById(R.id.wallpaperImageView)
                progressBar = rootView.findViewById(R.id.progressBar)
                relativeLayoutImg = rootView.findViewById(R.id.relativeLayout_img)
                menuDownload = rootView.findViewById(R.id.get_image_download)
                menuShare = rootView.findViewById(R.id.get_image_share)
                menuMore = rootView.findViewById(R.id.get_image_more)
                swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout)
            }

            val toolbar = rootView.findViewById<Toolbar>(R.id.toolbar)
            val baseColor =
                ContextCompat.getColor(MaterialDesignEngine.context, R.color.background_color)
            val primaryColor =
                ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
            val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)
            toolbar.setBackgroundColor(blendedColor)

            withContext(Dispatchers.Main) {
                progressBar?.isVisible = true
                progressBar?.isIndeterminate = true

                val colorRed = ContextCompat.getColor(requireContext(), R.color.red1)
                val colorGreen = ContextCompat.getColor(requireContext(), R.color.lawngreen)
                val colorBlue = ContextCompat.getColor(requireContext(), R.color.blue)
                val colorOrange = ContextCompat.getColor(requireContext(), R.color.orange2)
                val progressColors =
                    ContextCompat.getColor(requireContext(), R.color.swipe_refresh_layout_progress)
                swipeRefreshLayout.setColorSchemeColors(
                    colorRed,
                    colorGreen,
                    colorBlue,
                    colorOrange
                )
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(progressColors)

            }
        }

        // 设置标题和副标题
        setToolbarTitle(getString(R.string.toolbar_title_fragment_get_wallpaper))
        setToolbarSubTitle(getString(R.string.toolbar_subtitle_fragment_get_wallpaper))
        if (!isWallpaperLoaded) {
            lifecycleScope.launch {
                loadDeviceWallpaper()
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                loadDeviceWallpaper()
            }
        }

        menuDownload.setOnClickListener {
            progressBar?.isVisible = true
            lifecycleScope.launch {
                val wallpaper = wallpaperImageView.drawable
                if (wallpaper != null) {
                    val success = saveWallpaper(wallpaper)
                    if (success) {
                        toast("保存成功，在 /storage/emulated/0/Pictures/MDEngine/ 中")
                    } else {
                        toast("保存失败")
                    }
                } else {
                    toast("载入壁纸失败")
                }
                progressBar?.isVisible = false
            }
        }

        menuShare.setOnClickListener {
            progressBar?.isVisible = true
            lifecycleScope.launch {
                val wallpaper = wallpaperImageView.drawable
                if (wallpaper != null) {
                    withContext(Dispatchers.IO) {
                        shareWallpaper(wallpaper)
                    }
                } else {
                    toast("载入壁纸失败")
                }
                progressBar?.isVisible = false
            }
        }

        menuMore.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), it)
            popupMenu.menuInflater.inflate(R.menu.menu_more_action, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_item_1 -> {
                        loadWallpaperFromUrl("https://t.mwm.moe/mp")
                        true
                    }

                    R.id.menu_item_2 -> {
                        loadWallpaperFromUrl("https://api.r10086.com/樱道随机图片api接口.php?图片系列=日本COS中国COS")
                        true
                    }

                    R.id.menu_item_3 -> {
                        loadWallpaperFromUrl("https://img.moehu.org/pic.php?id=xjj")
                        true
                    }

                    R.id.menu_item_4 -> {
                        loadWallpaperFromUrl("https://api.lolimi.cn/API/meizi/api.php?type=image")
                        true
                    }

                    R.id.menu_item_5 -> {
                        loadWallpaperFromUrl("https://image.anosu.top/pixiv/direct?keyword=genshin&r18=0")
                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()
        }
    }

    private suspend fun loadDeviceWallpaper() {
        progressBar?.isVisible = true
        menuDownload.hide()

        val (wallpaper, size) = withContext(Dispatchers.IO) {
            loadWallpaperInBackground()
        }

        if (isAdded) {
            withContext(Dispatchers.Main) {
                wallpaperImageView.setImageDrawable(wallpaper)

                wallpaperImageView.alpha = 0f
                wallpaperImageView.animate()
                    .alpha(1f)
                    .setDuration(240)
                    .start()

                progressBar?.isVisible = false
                swipeRefreshLayout.isRefreshing = false
                setToolbarTitle(
                    getString(R.string.toolbar_title_fragment_get_wallpaper) + " (${
                        resources.getString(
                            R.string.wallpaper_px_text,
                            size.first,
                            size.second
                        )
                    })"
                )
                isWallpaperLoaded = true
                menuDownload.show()
            }
        }
    }

    private suspend fun loadWallpaperInBackground(): Pair<Drawable?, Pair<Int, Int>> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val wallpaperManager = WallpaperManager.getInstance(requireContext())
                val wallpaper = wallpaperManager.drawable
                val width = wallpaper!!.intrinsicWidth
                val height = wallpaper.intrinsicHeight
                Pair(wallpaper, Pair(width, height))
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(null, Pair(0, 0))
            }
        }

    private fun loadWallpaperFromUrl(url: String) {
        progressBar?.isVisible = true
        menuDownload.hide()

        lifecycleScope.launch {
            try {
                val drawable = withContext(Dispatchers.IO) {
                    Glide.with(requireContext())
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .submit()
                        .get()
                }
                if (isActive) {
                    if (drawable != null) {
                        withContext(Dispatchers.Main) {
                            val alphaAnimation = AlphaAnimation(0f, 1f)
                            alphaAnimation.duration = 240
                            wallpaperImageView.startAnimation(alphaAnimation)
                            wallpaperImageView.setImageDrawable(drawable)
                            val size = getImageSize(drawable)
                            setToolbarTitle(
                                getString(R.string.toolbar_title_fragment_get_wallpaper) + " (${
                                    resources.getString(
                                        R.string.wallpaper_px_text,
                                        size.first,
                                        size.second
                                    )
                                })"
                            )
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            toast("加载壁纸超时")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    toast("加载壁纸失败: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    progressBar?.isVisible = false
                    swipeRefreshLayout.isRefreshing = false
                    menuDownload.show()
                }
            }
        }
    }

    private suspend fun getImageSize(drawable: Drawable?): Pair<Int, Int> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                if (drawable is BitmapDrawable) {
                    val bitmap = drawable.bitmap
                    Pair(bitmap.width, bitmap.height)
                } else {
                    Pair(0, 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(0, 0)
            }
        }

    @SuppressLint("InflateParams")
    private fun showSslHandshakeErrorDialog() {
        lifecycleScope.launch {

            val dialogView = layoutInflater.inflate(R.layout.dialog_confirm, null)
            dialogView.findViewById<TextView>(R.id.confirm_title).text = "安全连接错误"
            dialogView.findViewById<TextView>(R.id.confirm_message).text =
                "无法安全地连接到服务器。这可能是由于服务器的安全证书存在问题。请稍后再试或联系管理员。"
            val dialog = DialogHelper.customDialog(requireContext(), dialogView)

            dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
            }
            dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    private suspend fun shareWallpaper(wallpaper: Drawable) {
        withContext(Dispatchers.IO) {
            try {
                val bitmap = (wallpaper as BitmapDrawable).bitmap
                val cachePath = File(requireContext().cacheDir, "images")
                cachePath.mkdirs()
                val stream = FileOutputStream("$cachePath/image.png")
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()
                val imagePath = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".shareSingleImage",
                    File("$cachePath/image.png")
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, imagePath)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "分享壁纸"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun saveWallpaper(wallpaper: Drawable): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            // 保存壁纸到指定路径
            val picturesDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val mdeEngineDirectory = File(picturesDirectory, "MDEngine")
            mdeEngineDirectory.mkdirs()
            // 生成唯一的文件名
            var fileName = "wallpaper.jpg"
            var file = File(mdeEngineDirectory, fileName)
            var counter = 1
            while (file.exists()) {
                fileName = "wallpaper_$counter.jpg"
                file = File(mdeEngineDirectory, fileName)
                counter++
            }
            val outputStream = FileOutputStream(file)
            val bitmap = (wallpaper as BitmapDrawable).bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}
