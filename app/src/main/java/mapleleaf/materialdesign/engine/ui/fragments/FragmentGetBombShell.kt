package mapleleaf.materialdesign.engine.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalFragmentBase
import mapleleaf.materialdesign.engine.diffcallback.DiffCallback
import mapleleaf.materialdesign.engine.ui.activities.ActivityFullScreenImage
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.utils.toast
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.SSLHandshakeException

class FragmentGetBombShell : UniversalFragmentBase(R.layout.fragment_get_beautiful) {

    private var isLoading = false
    private var currentPage = 1
    private val pageSize = 10
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterBombShell
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyList: LinearLayout
    private var imageUrls: MutableList<String> = mutableListOf()
    private var progressBar: ProgressBar? = null

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)

        progressBar = rootView.findViewById(R.id.progressBar)
        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                recyclerView = rootView.findViewById(R.id.recyclerView)
                swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout)
                emptyList = rootView.findViewById(R.id.emptyList)
            }

            val toolbar = rootView.findViewById<Toolbar>(R.id.toolbar)
            val baseColor =
                ContextCompat.getColor(MaterialDesignEngine.context, R.color.background_color)
            val primaryColor =
                ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
            val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)
            toolbar.setBackgroundColor(blendedColor)

            recyclerView.layoutManager =
                GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
            adapter = AdapterBombShell(imageUrls, requireContext())
            recyclerView.adapter = adapter
            FastScrollerBuilder(recyclerView).build()

            withContext(Dispatchers.Main) {
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

                progressBar?.isIndeterminate = true

                setToolbarTitle(getString(R.string.toolbar_title_fragment_pictures))
                setToolbarSubTitle(getString(R.string.toolbar_subtitle_fragment_pictures))

                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (dy > 0) {
                            val layoutManager = recyclerView.layoutManager as GridLayoutManager
                            val totalItemCount = layoutManager.itemCount
                            val lastVisibleItemPosition =
                                layoutManager.findLastCompletelyVisibleItemPosition()
                            if (!isLoading && lastVisibleItemPosition == totalItemCount - 1) {
                                loadMoreData()
                            }
                        }
                    }
                })
                swipeRefreshLayout.setOnRefreshListener {
                    fetchDataFromServer()
                    recyclerView.isVisible = true
                    emptyList.isVisible = false
                }
            }
            lifecycleScope.launch {
                swipeRefreshLayout.isRefreshing = true
                fetchDataFromServer()
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMoreData() {
        isLoading = true
        progressBar?.isVisible = true
        lifecycleScope.launch(Dispatchers.IO) {
            delay(1000)
            try {
                val urlString =
                    "https://api.unmz.net/free/api/images/girl/getRandomGirlUrl?size=$pageSize&page=$currentPage"
                val jsonResponse = makeHttpRequest(urlString)
                jsonResponse?.let {
                    val newImageUrls = parseJsonResponse(it)
                    withContext(Dispatchers.Main) {
                        imageUrls.addAll(newImageUrls)
                        adapter.notifyDataSetChanged()
                        currentPage++
                        isLoading = false
                        swipeRefreshLayout.isRefreshing = false
                        progressBar?.isVisible = false
                    }
                }
            } catch (e: Exception) {
                Log.e("WallpaperLoader", "加载更多数据失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    toast("加载更多数据失败: ${e.message}")
                    isLoading = false
                    swipeRefreshLayout.isRefreshing = false
                    progressBar?.isVisible = false
                }
            }
        }
    }

    private fun fetchDataFromServer() {
        progressBar?.isVisible = true
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val urlString = "https://api.unmz.net/free/api/images/girl/getRandomGirlUrl?size=10"
                Log.d("WallpaperLoader", "正在获取数据来自: $urlString")
                val jsonResponse = makeHttpRequest(urlString)
                jsonResponse?.let {
                    if (jsonResponse.contains("当前无法使用此页面")) {
                        withContext(Dispatchers.Main) {
                            toast("当前无法使用此页面")
                            swipeRefreshLayout.isRefreshing = false
                            progressBar?.isVisible = false
                        }
                    } else {
                        Log.d("WallpaperLoader", "响应: $jsonResponse")
                        val newImageUrls = parseJsonResponse(it)
                        withContext(Dispatchers.Main) {
                            updateImageList(newImageUrls)
                        }
                    }

                } ?: run {
                    handleNoResponse()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun updateImageList(newImageUrls: List<String>) {
        val diffResult = DiffUtil.calculateDiff(DiffCallback(imageUrls, newImageUrls))
        imageUrls.clear()
        imageUrls.addAll(newImageUrls)
        diffResult.dispatchUpdatesTo(adapter)
        swipeRefreshLayout.isRefreshing = false
        progressBar?.isVisible = false
    }

    private suspend fun handleNoResponse() {
        withContext(Dispatchers.Main) {
            toast("未收到任何响应")
            recyclerView.isVisible = false
            emptyList.isVisible = true
            swipeRefreshLayout.isRefreshing = false
            progressBar?.isVisible = false
        }
    }

    private suspend fun handleError(e: Exception) {
        withContext(Dispatchers.Main) {
            when (e) {
                is SSLHandshakeException -> {
                    showSslHandshakeErrorDialog()
                    Log.e("WallpaperLoader", "SSL握手异常", e)
                }

                else -> {
                    toast("加载壁纸失败: ${e.message}")
                    Log.e("WallpaperLoader", "加载壁纸失败: ${e.message}", e)
                }
            }
            swipeRefreshLayout.isRefreshing = false
            progressBar?.isVisible = false
        }
    }

    @SuppressLint("InflateParams")
    private fun showSslHandshakeErrorDialog() {
        CoroutineScope(Dispatchers.Main).launch {

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

    private fun makeHttpRequest(urlString: String): String? {
        var result: String? = null
        try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            val inputStream = connection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            result = stringBuilder.toString()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun parseJsonResponse(jsonResponse: String): List<String> {
        val jsonObject = JSONObject(jsonResponse)
        val dataArray = jsonObject.getJSONArray("data")
        val newImageUrls = mutableListOf<String>()
        for (i in 0 until dataArray.length()) {
            val imageUrl = dataArray.getString(i)
            newImageUrls.add(imageUrl)
        }
        return newImageUrls
    }

    class AdapterBombShell(private val imageUrls: List<String>, private val context: Context) :
        RecyclerView.Adapter<AdapterBombShell.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.imageView)
            init {
                val baseColor = ContextCompat.getColor(context, R.color.background)
                val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
                itemView.findViewById<MaterialCardView>(R.id.materialCardView).apply {
                    strokeColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.3f)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_layout, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val imageUrl = imageUrls[position]
            val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
            val columnCount = 2
            val itemPositionInRow = position % columnCount
            layoutParams.topMargin = if (itemPositionInRow == 0 || itemPositionInRow == 1) 18 else 0
            layoutParams.bottomMargin = if (position == itemCount - 1) 18 else 0
            holder.itemView.layoutParams = layoutParams
            Log.d("GlideLog", "从链接加载图片: $imageUrl");

            Glide.with(holder.imageView)
                .load(imageUrl)
                .apply(
                    RequestOptions()
                        .transform(CenterCrop())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.color.background)
                        .error(R.drawable.ic_error)
                )
                .thumbnail(0.5f)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean,
                    ): Boolean {
                        Log.e("GlideLog", "加载图片失败，URL: $imageUrl", e)

                        // 如果有占位图，重新加载占位图
                        if (model is String && model.isNotEmpty()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(1000)
                                Glide.with(context)
                                    .load(model)
                                    .apply(
                                        RequestOptions()
                                            .transform(CenterCrop())
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .placeholder(R.color.background)
                                            .error(R.drawable.ic_error)
                                    )
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .listener(object : RequestListener<Drawable> {
                                        override fun onLoadFailed(
                                            e: GlideException?,
                                            model: Any?,
                                            target: Target<Drawable>,
                                            isFirstResource: Boolean,
                                        ): Boolean {
                                            Log.e("GlideLog", "重新加载图片失败，URL: $model", e)
                                            CoroutineScope(Dispatchers.Main).launch {
                                                delay(1000)
                                                Glide.with(context)
                                                    .load(model)
                                                    .apply(
                                                        RequestOptions()
                                                            .transform(CenterCrop())
                                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                            .placeholder(R.color.background)
                                                            .error(R.drawable.ic_error)
                                                    )
                                                    .transition(DrawableTransitionOptions.withCrossFade())
                                                    .listener(object : RequestListener<Drawable> {
                                                        override fun onLoadFailed(
                                                            e: GlideException?,
                                                            model: Any?,
                                                            target: Target<Drawable>,
                                                            isFirstResource: Boolean,
                                                        ): Boolean {
                                                            Log.e(
                                                                "GlideLog",
                                                                "重新加载图片失败，URL: $model",
                                                                e
                                                            )

                                                            CoroutineScope(Dispatchers.Main).launch {
                                                                delay(1000)
                                                                Glide.with(context)
                                                                    .load(model)
                                                                    .apply(
                                                                        RequestOptions()
                                                                            .transform(CenterCrop())
                                                                            .diskCacheStrategy(
                                                                                DiskCacheStrategy.ALL
                                                                            )
                                                                            .placeholder(R.color.background)
                                                                            .error(R.drawable.ic_error)
                                                                    )
                                                                    .transition(
                                                                        DrawableTransitionOptions.withCrossFade()
                                                                    )
                                                                    .listener(object :
                                                                        RequestListener<Drawable> {
                                                                        override fun onLoadFailed(
                                                                            e: GlideException?,
                                                                            model: Any?,
                                                                            target: Target<Drawable>,
                                                                            isFirstResource: Boolean,
                                                                        ): Boolean {
                                                                            Log.e(
                                                                                "GlideLog",
                                                                                "重新加载图片失败，URL: $model",
                                                                                e
                                                                            )
                                                                            // 显示默认错误图像或其他反馈机制
                                                                            holder.imageView.setImageResource(
                                                                                R.drawable.ic_error
                                                                            )
                                                                            return true
                                                                        }

                                                                        override fun onResourceReady(
                                                                            resource: Drawable,
                                                                            model: Any,
                                                                            target: Target<Drawable>?,
                                                                            dataSource: DataSource,
                                                                            isFirstResource: Boolean,
                                                                        ): Boolean {
                                                                            Log.d(
                                                                                "GlideLog",
                                                                                "重新加载图片成功，URL: $model"
                                                                            )
                                                                            // 清除之前加载失败的错误信息
                                                                            holder.imageView.setImageDrawable(
                                                                                null
                                                                            )
                                                                            return false
                                                                        }
                                                                    })
                                                                    .into(target)
                                                            }
                                                            holder.imageView.setImageResource(R.drawable.ic_error)
                                                            return true
                                                        }

                                                        override fun onResourceReady(
                                                            resource: Drawable,
                                                            model: Any,
                                                            target: Target<Drawable>?,
                                                            dataSource: DataSource,
                                                            isFirstResource: Boolean,
                                                        ): Boolean {
                                                            Log.d(
                                                                "GlideLog",
                                                                "重新加载图片成功，URL: $model"
                                                            )
                                                            // 清除之前加载失败的错误信息
                                                            holder.imageView.setImageDrawable(null)
                                                            return false
                                                        }
                                                    })
                                                    .into(target)
                                            }
                                            return true
                                        }

                                        override fun onResourceReady(
                                            resource: Drawable,
                                            model: Any,
                                            target: Target<Drawable>?,
                                            dataSource: DataSource,
                                            isFirstResource: Boolean,
                                        ): Boolean {
                                            Log.d("GlideLog", "重新加载图片成功，URL: $model")
                                            // 清除之前加载失败的错误信息
                                            holder.imageView.setImageDrawable(null)
                                            return false
                                        }
                                    })
                                    .into(target)
                            }
                        }
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean,
                    ): Boolean {
                        Log.d("GlideLog", "加载图片成功，URL: $imageUrl")
                        return false
                    }
                })
                .into(holder.imageView)
            holder.imageView.setOnClickListener {
                val intent = Intent(context, ActivityFullScreenImage::class.java).apply {
                    putExtra("imageUrls", imageUrls.toTypedArray())
                    putExtra("position", position)
                }
                context.startActivity(intent)
            }

            holder.imageView.setOnLongClickListener {
                showOptionsDialog(imageUrl, position)
                true
            }
        }

        private var lastLoadedDrawable: Drawable? = null

        @SuppressLint("InflateParams")
        private fun showOptionsDialog(imageUrl: String, position: Int) {
            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.dialog_image_preview, null)
            val dialog = DialogHelper.customDialog(context, dialogView)

            val imageViewPreview = dialogView.findViewById<ImageView>(R.id.image_view_preview)
            Glide.with(context)
                .load(imageUrl)
                .apply(
                    RequestOptions()
                        .transform(CenterCrop())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.color.background)
                        .error(R.drawable.ic_error)
                )
                .thumbnail(0.5f)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(object : CustomTarget<Drawable>() {

                    override fun onLoadStarted(placeholder: Drawable?) {
                        Log.d("FullScreenImageActivity", "图片开始加载")
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?,
                    ) {
                        Log.d("FullScreenImageActivity", "图片加载成功")
                        imageViewPreview.setImageDrawable(resource)
                        lastLoadedDrawable = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        Log.d("FullScreenImageActivity", "图片加载已清除")
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        Log.d("FullScreenImageActivity", "图片加载失败")
                    }

                })

            dialogView.findViewById<View>(R.id.btn_save).setOnClickListener {
                dialog.dismiss()
                lastLoadedDrawable?.let {

                    val bitmap = (it as BitmapDrawable).bitmap
                    saveImage(context, bitmap)
                } ?: run {
                    toast("图片未加载完成，无法保存")
                }
            }
            dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
            }
        }

        private val STORAGE_PERMISSION_REQUEST_CODE = 1

        private val coroutineScope = CoroutineScope(Dispatchers.Main)

        private fun saveImage(context: Context, bitmap: Bitmap) {
            // 检查写入外部存储的权限
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
            }

            saveBitmapToPublicDirectory(context, bitmap)
        }

        private fun saveBitmapToPublicDirectory(context: Context, bitmap: Bitmap) {

            val picturesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

            val mdEngineDir = File(picturesDir, "MDEngine")

            if (!mdEngineDir.exists()) {
                mdEngineDir.mkdirs()
            }

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
                Log.d("ImagePagerAdapter", "保存成功: ${imageFile.absolutePath}")
                coroutineScope.launch {
                    toast("保存成功: ${imageFile.absolutePath}")
                }
            } catch (e: IOException) {
                Log.e("ImagePagerAdapter", "保存失败: ${e.message}")
                coroutineScope.launch {
                    toast("保存失败: ${e.message}")
                }
            }
        }

        override fun getItemCount(): Int = imageUrls.size
    }
}