package mapleleaf.materialdesign.engine.ui.activities

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.utils.toast
import me.zhanghai.android.fastscroll.FastScrollNestedScrollView
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ActivityRandomPersonality : UniversalActivityBase(R.layout.activity_random_personality) {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loading: AppCompatImageView
    private lateinit var emptyList: LinearLayout
    private var animatedVectorDrawable: AnimatedVectorDrawable? = null

    override fun initializeComponents(savedInstanceState: Bundle?) {

        val scrollingView = findViewById<FastScrollNestedScrollView>(R.id.nestedScrollView2)
        FastScrollerBuilder(scrollingView).useMd2Style().build()
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        loading = findViewById(R.id.loading)
        emptyList = findViewById(R.id.emptyList)
        animatedVectorDrawable = AppCompatResources.getDrawable(
            this,
            R.drawable.progress_loading_manager
        ) as AnimatedVectorDrawable
        loading.setImageDrawable(animatedVectorDrawable)
        animatedVectorDrawable?.start()

        val colorRed = ContextCompat.getColor(this, R.color.red1)
        val colorGreen = ContextCompat.getColor(this, R.color.lawngreen)
        val colorBlue = ContextCompat.getColor(this, R.color.blue)
        val colorOrange = ContextCompat.getColor(this, R.color.orange2)
        val progressColors = ContextCompat.getColor(this, R.color.swipe_refresh_layout_progress)
        swipeRefreshLayout.setColorSchemeColors(colorRed, colorGreen, colorBlue, colorOrange)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(progressColors)

        swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                randomPersonality()
            }
        }
        lifecycleScope.launch {
            randomPersonality()
        }

        setToolbarTitle(getString(R.string.toolbar_title_activity_random_personality))

        val baseColor = ContextCompat.getColor(context, R.color.background)
        val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)

        findViewById<MaterialCardView>(R.id.random_personality_materialCardView).apply {
            strokeColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.3f)
            setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.2f))
        }

    }


    private suspend fun randomPersonality() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // 检查网络是否连接
        if (!isNetworkAvailable(connectivityManager)) {
            withContext(Dispatchers.Main) {
                toast("无网络连接")
                swipeRefreshLayout.isRefreshing = false
                emptyList.isVisible = true
                animatedVectorDrawable?.stop()
                loading.isVisible = false
            }
            return
        }

        val client = OkHttpClient()
        val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
        val body = "format=json".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://www.hhlqilongzhu.cn/api/suiji_renshe.php")
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Execute the request with timeout
                val response = withTimeoutOrNull(10000) {  // 10 seconds timeout
                    client.newCall(request).execute()
                }

                response?.use { httpResponse ->
                    if (httpResponse.isSuccessful && httpResponse.body != null) {
                        val responseBody = httpResponse.body!!.string()
                        withContext(Dispatchers.Main) {
                            // Update UI on the main thread
                            val fadeInAnimation = AnimationUtils.loadAnimation(
                                this@ActivityRandomPersonality,
                                R.anim.fade_in_up
                            )
                            emptyList.isVisible = false
                            animatedVectorDrawable?.stop()
                            loading.isVisible = false
                            val randomPersonalityTextView =
                                findViewById<TextView>(R.id.random_personality_textview)
                            randomPersonalityTextView.text = responseBody
                            val randomPersonalityMaterialCardView =
                                findViewById<MaterialCardView>(R.id.random_personality_materialCardView)
                            randomPersonalityMaterialCardView.isVisible = true
                            randomPersonalityMaterialCardView.startAnimation(fadeInAnimation)
                            swipeRefreshLayout.isRefreshing = false
                        }
                    } else {
                        Log.e("网络请求", "请求失败: ${httpResponse.code}")
                    }
                } ?: run {
                    Log.e("网络请求", "请求超时")
                    // Handle timeout: show message on the main thread
                    withContext(Dispatchers.Main) {
                        toast("请求超时，请检查网络连接后重试")
                        emptyList.isVisible = true
                        animatedVectorDrawable?.stop()
                        loading.isVisible = false
                    }
                }
            } catch (e: IOException) {
                Log.e("网络请求", "网络请求中出现异常", e)
                emptyList.isVisible = true
                animatedVectorDrawable?.stop()
                loading.isVisible = false
            }
        }
    }

    // 检查设备的网络连接状态
    private fun isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo?.isConnected ?: false
        }
    }

}
