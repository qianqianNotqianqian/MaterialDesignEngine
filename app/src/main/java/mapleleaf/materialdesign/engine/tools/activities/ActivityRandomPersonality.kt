package mapleleaf.materialdesign.engine.tools.activities

import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import me.zhanghai.android.fastscroll.FastScrollNestedScrollView
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class ActivityRandomPersonality : UniversalActivityBase() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_random_personality
    }

    override fun initializeComponents(savedInstanceState: Bundle?) {
        randomPersonality()
        val scrollingView = findViewById<FastScrollNestedScrollView>(R.id.nestedScrollView2)
        FastScrollerBuilder(scrollingView).build()
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        val colorRed = ContextCompat.getColor(this, R.color.red1)
        val colorGreen = ContextCompat.getColor(this, R.color.lawngreen)
        val colorBlue = ContextCompat.getColor(this, R.color.blue)
        val colorOrange = ContextCompat.getColor(this, R.color.orange2)
        val progressColors = ContextCompat.getColor(this, R.color.swipe_refresh_layout_progress)
        swipeRefreshLayout.setColorSchemeColors(colorRed, colorGreen, colorBlue, colorOrange)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(progressColors)

        swipeRefreshLayout.setOnRefreshListener {
            randomPersonality()
        }

        setToolbarTitle(getString(R.string.toolbar_title_activity_random_personality))

        val materialCardView =
            findViewById<MaterialCardView>(R.id.random_personality_materialCardView)
        val baseColor = ContextCompat.getColor(MaterialDesignEngine.context, R.color.background)
        val primaryColor =
            ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
        materialCardView.setCardBackgroundColor(
            ColorUtils.blendARGB(
                baseColor,
                primaryColor,
                0.15f
            )
        )
    }

    private fun randomPersonality() {
        val client = OkHttpClient()
        val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
        val body = "format=json".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://www.hhlqilongzhu.cn/api/suiji_renshe.php")
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful && response.body != null) {
                        val responseBody = response.body!!.string()
                        runOnUiThread {
                            val fadeInAnimation = AnimationUtils.loadAnimation(
                                this@ActivityRandomPersonality,
                                R.anim.fade_in_up
                            )
                            val randomPersonalityTextView =
                                findViewById<TextView>(R.id.random_personality_textview)
                            randomPersonalityTextView.text = responseBody
                            val randomPersonalityMaterialCardView =
                                findViewById<MaterialCardView>(R.id.random_personality_materialCardView)
                            randomPersonalityMaterialCardView.isVisible = true
                            randomPersonalityMaterialCardView.startAnimation(fadeInAnimation)
                            swipeRefreshLayout.isRefreshing = false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("网络请求", "网络请求中出现异常", e)
            }
        }
    }
}
