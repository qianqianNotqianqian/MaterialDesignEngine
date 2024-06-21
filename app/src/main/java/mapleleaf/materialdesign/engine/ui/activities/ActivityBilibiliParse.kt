package mapleleaf.materialdesign.engine.ui.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.ui.dialog.DialogProgressBar
import mapleleaf.materialdesign.engine.utils.toast
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ActivityBilibiliParse : UniversalActivityBase(R.layout.activity_bilibili_parse) {

    private lateinit var titleTextView: TextView
    private lateinit var statViewTextView: TextView
    private lateinit var statDanmuTextView: TextView
    private lateinit var qualityTextView: TextView
    private lateinit var totalTextView: TextView
    private lateinit var statDianzanTextView: TextView
    private lateinit var statToubiTextView: TextView
    private lateinit var statShoucangTextView: TextView
    private lateinit var statZhuanfaTextView: TextView
    private lateinit var videoCoverUrlImageView: AppCompatImageView
    private lateinit var editText: EditText
    private lateinit var progressBar: DialogProgressBar
    private lateinit var button: FloatingActionButton

    override fun initializeComponents(savedInstanceState: Bundle?) {

        setToolbarTitle(getString(R.string.toolbar_title_activity_bilibili_parse))
        progressBar = DialogProgressBar(this)
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
//
                editText = findViewById(R.id.edit_text)
                titleTextView = findViewById(R.id.title)
                statViewTextView = findViewById(R.id.stat_view)
                statDanmuTextView = findViewById(R.id.stat_danmu)
                qualityTextView = findViewById(R.id.quality)
                totalTextView = findViewById(R.id.total)
                statDianzanTextView = findViewById(R.id.stat_dianzan)
                statToubiTextView = findViewById(R.id.stat_toubi)
                statShoucangTextView = findViewById(R.id.stat_shoucang)
                statZhuanfaTextView = findViewById(R.id.stat_zhuanfa)
                videoCoverUrlImageView = findViewById(R.id.videoCoverUrlImageView)

                button = findViewById(R.id.floatingActionButton)
                restoreDataFromLocal()

                button.setOnClickListener {
                    val text = editText.text.toString().trim()
                    if (text.isEmpty()) {
                        toast("请输入链接再解析！(注意符号)")
                    } else {
                        fetchVideoInfo(text)
                        saveEditTextContents(text)
                    }
                }
                val sharedPreferences =
                    getSharedPreferences("BiliBiliEditTextContents", Context.MODE_PRIVATE)
                editText.setText(sharedPreferences.getString("editText", ""))
            }
        }
    }

    private fun saveEditTextContents(text: String) {
        val sharedPreferences =
            getSharedPreferences("BiliBiliEditTextContents", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("editText", text)
        editor.apply()
    }

    private fun restoreDataFromLocal() {
        val sharedPreferences = getSharedPreferences("VideoInfo", Context.MODE_PRIVATE)
        titleTextView.text = sharedPreferences.getString("title", "")
        statViewTextView.text = sharedPreferences.getString("views", "")
        statDanmuTextView.text = sharedPreferences.getString("danmu", "")
        qualityTextView.text = sharedPreferences.getString("quality", "")
        totalTextView.text = sharedPreferences.getString("total", "")
        statDianzanTextView.text = sharedPreferences.getString("dianzan", "")
        statToubiTextView.text = sharedPreferences.getString("toubi", "")
        statShoucangTextView.text = sharedPreferences.getString("shoucang", "")
        statZhuanfaTextView.text = sharedPreferences.getString("zhuanfa", "")
        val videocoverurl = sharedPreferences.getString("videocoverurl", "")
        if (videocoverurl != "") {
            // 使用 Glide 库加载图片并设置到 ImageView 中
            Glide.with(this@ActivityBilibiliParse)
                .load(videocoverurl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(videoCoverUrlImageView)
        }
    }

    private fun fetchVideoInfo(text: String) {
        progressBar.showDialog()
        Log.d("fetchVideoInfo", "获取视频 ID 的视频信息: $text")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url("https://api.aag.moe/api/bzspjx?url=https://www.bilibili.com/video/$text")
                    .build()

                withContext(Dispatchers.IO) {
                    val response = client.newCall(request).execute()
                    if (!isActive) return@withContext

                    if (response.isSuccessful) {
                        val jsonData = JSONObject(response.body?.string() ?: "")
                        val title = jsonData.getString("title")
                        val videocoverurl = jsonData.getString("videocoverurl")
                        val views = formatDianzanCount(jsonData.getString("stat_view"))
                        val danmu = jsonData.getString("stat_danmu")
                        val dianzan = formatDianzanCount(jsonData.getString("stat_dianzan"))
                        val toubi = formatDianzanCount(jsonData.getString("stat_toubi"))
                        val shoucang = formatDianzanCount(jsonData.getString("stat_shoucang"))
                        val zhuanfa = jsonData.getString("stat_zhuanfa")
                        val total = jsonData.getString("total")
                        val quality = jsonData.getString("quality")
                        val videourl = jsonData.getString("videourl")
                        val audiourl = jsonData.getString("audiourl")

                        if (isActive) {
                            saveDataToLocal(
                                title,
                                videocoverurl,
                                views,
                                danmu,
                                dianzan,
                                toubi,
                                shoucang,
                                zhuanfa,
                                total,
                                quality,
                                videourl,
                                audiourl
                            )

                            withContext(Dispatchers.Main) {
                                if (isActive) {
                                    progressBar.hideDialog()
                                    setTextViewData(
                                        title,
                                        videocoverurl,
                                        views,
                                        danmu,
                                        dianzan,
                                        toubi,
                                        shoucang,
                                        zhuanfa,
                                        total,
                                        quality,
                                        videourl,
                                        audiourl
                                    )
                                }
                            }
                        }
                    } else {
                        Log.e("fetchVideoInfo", "请求失败，代码: ${response.code}")
                        withContext(Dispatchers.Main) {
                            progressBar.hideDialog()
                            toast("请求失败")
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("fetchVideoInfo", "网络错误: ${e.message}")
                withContext(Dispatchers.Main) {
                    progressBar.hideDialog()
                    toast("网络错误: ${e.message}")
                }
            } catch (e: JSONException) {
                Log.e("fetchVideoInfo", "解析错误: ${e.message}")
                withContext(Dispatchers.Main) {
                    progressBar.hideDialog()
                    toast("解析错误: ${e.message}")
                }
            }
        }
    }

    private fun saveDataToLocal(
        title: String,
        videocoverurl: String, views: String, danmu: String,
        dianzan: String, toubi: String, shoucang: String,
        zhuanfa: String, total: String, quality: String,
        videourl: String, audiourl: String,
    ) {
        val sharedPreferences = getSharedPreferences("VideoInfo", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("title", title)
        editor.putString("videocoverurl", videocoverurl)
        editor.putString("views", views)
        editor.putString("danmu", danmu)
        editor.putString("dianzan", dianzan)
        editor.putString("toubi", toubi)
        editor.putString("shoucang", shoucang)
        editor.putString("zhuanfa", zhuanfa)
        editor.putString("total", total)
        editor.putString("quality", quality)
        editor.putString("videourl", videourl)
        editor.putString("audiourl", audiourl)
        editor.apply()
    }

    private fun setTextViewData(
        title: String, videocoverurl: String, views: String,
        danmu: String, dianzan: String, toubi: String,
        shoucang: String, zhuanfa: String, total: String,
        quality: String, videourl: String, audiourl: String,
    ) {
        titleTextView.text = title
        statViewTextView.text = views
        statDanmuTextView.text = danmu
        qualityTextView.text = quality
        totalTextView.text = total
        statDianzanTextView.text = dianzan
        statToubiTextView.text = toubi
        statShoucangTextView.text = shoucang
        statZhuanfaTextView.text = zhuanfa

        Glide.with(this@ActivityBilibiliParse)
            .load(videocoverurl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(videoCoverUrlImageView)
    }

    private fun formatDianzanCount(count: String): String {
        val dianzanCount = count.toIntOrNull() ?: return count
        return when {
            dianzanCount >= 100000000 -> {
                String.format("%.1f亿", dianzanCount.toFloat() / 100000000)
            }

            dianzanCount >= 10000 -> {
                String.format("%.1f万", dianzanCount.toFloat() / 10000)
            }

            else -> {
                count
            }
        }
    }
}