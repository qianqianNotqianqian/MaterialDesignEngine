package mapleleaf.materialdesign.engine.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ortiz.touchview.TouchImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.utils.toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ActivityFullScreenImage : UniversalActivityBase(R.layout.activity_fullscreen_image) {

    private lateinit var viewPager: ViewPager
    private var progressBar: ProgressBar? = null
    private lateinit var imagePosition: TextView
    private lateinit var imageSize: TextView

//    override fun getLayoutResourceId() = R.layout.activity_fullscreen_image

    override fun initializeComponents(savedInstanceState: Bundle?) {

        viewPager = findViewById(R.id.viewPager)
        imagePosition = findViewById(R.id.image_position)
        imageSize = findViewById(R.id.image_size)
        progressBar = findViewById(R.id.progressBar)

        val imageUrls = intent.getStringArrayExtra("imageUrls") ?: emptyArray()
        val position = intent.getIntExtra("position", 0)

        progressBar?.isVisible = true
        progressBar?.isIndeterminate = true
        setToolbarTitle(getString(R.string.toolbar_full_screen_image))

        val totalImages = imageUrls.size
        updateImagePosition(position + 1, totalImages)

        if (imageUrls.isNotEmpty()) {
            getImageSize(imageUrls[position]) { width, height ->
                val sizeText = "$width x $height"
                imageSize.text = sizeText
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            viewPager.adapter = ImagePagerAdapter(imageUrls)
            viewPager.currentItem = position
            progressBar?.isVisible = false

        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int,
            ) {

            }

            override fun onPageSelected(position: Int) {

                updateImagePosition(position + 1, totalImages)
                // 切换图片时，获取并显示当前图片的大小
                getImageSize(imageUrls[position]) { width, height ->
                    val sizeText = "$width x $height"
                    imageSize.text = sizeText
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

    }

    private fun getImageSize(url: String, callback: (Int, Int) -> Unit) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    callback.invoke(resource.width, resource.height)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Do nothing
                }
            })
    }

    private fun updateImagePosition(currentPosition: Int, totalImages: Int) {
        val imagePositionTextView: TextView = findViewById(R.id.image_position)
        val positionText = "${currentPosition}/${totalImages}"
        imagePositionTextView.text = positionText
    }

    class ImagePagerAdapter(private val imageUrls: Array<String>) : PagerAdapter() {
        override fun getCount(): Int = imageUrls.size

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val touchImageView = TouchImageView(container.context)
            Glide.with(container.context)
                .asBitmap()
                .load(imageUrls[position])
                .apply(
                    RequestOptions()
                        .transform(CenterCrop())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.color.background)
                        .error(R.drawable.ic_error)
                        .downsample(DownsampleStrategy.AT_MOST)
                )
                .priority(Priority.NORMAL)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        bitmap: Bitmap,
                        transition: Transition<in Bitmap>?,
                    ) {

                        // 记录图片加载成功的日志以及图片大小
                        val sizeInBytes = bitmap.byteCount
                        val sizeInMb = sizeInBytes / (1024 * 1024)
                        Log.d("FullScreenImageActivity", "载入成功，大小: $sizeInMb MB")
                        val alphaAnimation = AlphaAnimation(0f, 1f)
                        alphaAnimation.duration = 320
                        touchImageView.startAnimation(alphaAnimation)
                        touchImageView.setImageBitmap(bitmap)

                        touchImageView.setOnLongClickListener {
                            showSaveAndShareDialog(container.context, bitmap)
                            true
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Handle load cleared if needed
                        Log.d("FullScreenImageActivity", "Image load cleared")
                    }
                })
            container.addView(touchImageView)
            return touchImageView
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        private fun showSaveAndShareDialog(context: Context, bitmap: Bitmap) {
            val dialogItems = arrayOf("保存", "分享")
            MaterialAlertDialogBuilder(context)
                .setTitle("选择操作")
                .setItems(dialogItems) { _, which ->
                    when (which) {
                        0 -> saveImage(context, bitmap)
                        1 -> shareImage(context, bitmap)
                    }
                }
                .setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        private fun shareImage(context: Context, bitmap: Bitmap) {
            CoroutineScope(Dispatchers.Main).launch {
                val result = withContext(Dispatchers.IO) {
                    saveBitmapToTempFile(context, bitmap)
                }

                if (result != null) {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "image/*"
                    intent.putExtra(
                        Intent.EXTRA_STREAM,
                        FileProvider.getUriForFile(
                            context,
                            context.packageName + ".shareImage",
                            result
                        )
                    )
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(Intent.createChooser(intent, "分享图片"))
                } else {
                    toast("分享失败")
                }
            }
        }

        private fun saveBitmapToTempFile(context: Context, bitmap: Bitmap): File? {
            val tempDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File(tempDir, "${System.currentTimeMillis()}.jpg")

            return try {
                FileOutputStream(imageFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.flush()
                }
                imageFile
            } catch (e: IOException) {
                Log.e("ImagePagerAdapter", "Error saving bitmap to temporary file: ${e.message}")
                null
            }
        }

        private val STORAGE_PERMISSION_REQUEST_CODE = 1

        private fun saveImage(context: Context, bitmap: Bitmap) {

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    (context as Activity),
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), STORAGE_PERMISSION_REQUEST_CODE
                )
                Log.e("ImagePagerAdapter", "无法获取权限：WRITE_EXTERNAL_STORAGE")
                return
            } else {
                // 已经授予权限，直接保存图片
                saveBitmapToPublicDirectory(context, bitmap)
            }

        }

        @OptIn(DelicateCoroutinesApi::class)
        private fun saveBitmapToPublicDirectory(context: Context, bitmap: Bitmap) {
            GlobalScope.launch(Dispatchers.IO) {

                val picturesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

                val mdEngineDir = File(picturesDir, "MDEngine")

                if (!mdEngineDir.exists()) {
                    mdEngineDir.mkdirs()
                }
                // 构建唯一的文件名
                var uniqueFileName = "${System.currentTimeMillis()}.jpg"
                var imageFile = File(mdEngineDir, uniqueFileName)

                var count = 1
                while (imageFile.exists()) {
                    uniqueFileName = "${System.currentTimeMillis()}_${count++}.jpg"
                    imageFile = File(mdEngineDir, uniqueFileName)
                }
                try {
                    FileOutputStream(imageFile).use { fos ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        fos.flush()
                    }
                    withContext(Dispatchers.Main) {
                        Log.d("ImagePagerAdapter", "保存成功: ${imageFile.absolutePath}")
                        toast("保存成功: ${imageFile.absolutePath}")
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        Log.e("ImagePagerAdapter", "保存失败: ${e.message}")
                        toast("保存失败: ${e.message}")
                    }
                }
            }
        }
    }
}