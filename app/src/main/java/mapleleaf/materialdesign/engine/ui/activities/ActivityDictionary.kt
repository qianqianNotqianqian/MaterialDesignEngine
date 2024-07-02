package mapleleaf.materialdesign.engine.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.utils.toast
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ActivityDictionary : UniversalActivityBase(R.layout.activity_dictionary) {

    private lateinit var editTextHanzi: EditText
    private lateinit var textViewPinyin: TextView
    private lateinit var textViewBiHua: TextView
    private lateinit var textViewWord: TextView
    private lateinit var textViewBuShou: TextView
    private lateinit var textViewBasicExplain: TextView
    private lateinit var textViewDetailExplain: TextView
    private lateinit var buttonQuery: FloatingActionButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private var isFabVisible = true
    private var isLoading = false
    val regex = Regex("[^\u4e00-\u9fa5]")

    override fun initializeComponents(savedInstanceState: Bundle?) {

        setToolbarTitle(getString(R.string.toolbar_title_activity_dictionary))

        editTextHanzi = findViewById(R.id.editTextHanzi)
        textViewPinyin = findViewById(R.id.textViewPinyin)
        textViewBiHua = findViewById(R.id.textViewBiHua)
        textViewWord = findViewById(R.id.textViewWord)
        textViewBuShou = findViewById(R.id.textViewBuShou)
        textViewBasicExplain = findViewById(R.id.textViewBasicExplain)
        textViewDetailExplain = findViewById(R.id.textViewDetailExplain)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        buttonQuery = findViewById(R.id.buttonQuery)
        progressBar = findViewById(R.id.progressBar)
        val nestedScrollView = findViewById<NestedScrollView>(R.id.nestedScrollView2)
        FastScrollerBuilder(nestedScrollView).useMd2Style().build()
        progressBar.isIndeterminate = true

        val colorRed = ContextCompat.getColor(this, R.color.red1)
        val colorGreen = ContextCompat.getColor(this, R.color.lawngreen)
        val colorBlue = ContextCompat.getColor(this, R.color.blue)
        val colorOrange = ContextCompat.getColor(this, R.color.orange2)
        val progressColors = ContextCompat.getColor(this, R.color.swipe_refresh_layout_progress)
        swipeRefreshLayout.setColorSchemeColors(colorRed, colorGreen, colorBlue, colorOrange)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(progressColors)

        buttonQuery.setOnClickListener {
            clearEditFocus()
            buttonQuery.hide()
            if (!isLoading) {
                swipeRefreshLayout.isEnabled = false
                progressBar.isVisible = true
                isLoading = true
                buttonQuery.hide()
                searchText()
            } else {
                toast("正在加载")
                buttonQuery.hide()
            }
        }

        nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            clearEditFocus()
            if (scrollY > 0 && isFabVisible) {
                buttonQuery.hide()
                isFabVisible = false
            } else if (scrollY == 0 && !isFabVisible) {
                buttonQuery.show()
                isFabVisible = true
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            clearEditFocus()
            progressBar.isVisible = true
            buttonQuery.hide()
            searchText()
        }

        customizeCardView(findViewById(R.id.materialCardView))
        customizeCardView(findViewById(R.id.textViewBasicExplainMaterialCardView))
        customizeCardView(findViewById(R.id.textViewDetailExplainMaterialCardView))
        customizeCardView(findViewById(R.id.textViewWordMaterialCardView))

    }
    
    private fun clearEditFocus() {
        editTextHanzi.clearFocus()
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // 在失去焦点后隐藏输入法键盘
        editTextHanzi.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                inputMethodManager.hideSoftInputFromWindow(editTextHanzi.windowToken, 0)
            }
        }
    }

    private fun searchText() {
        val hanzi = editTextHanzi.text.toString()
        if (hanzi.isEmpty()) {
            toast("请输入文字再搜索！(不支持英文)")
            progressBar.isVisible = false
            swipeRefreshLayout.isRefreshing = false
            swipeRefreshLayout.isEnabled = true
            isLoading = false
            buttonQuery.show()
        } else if (regex.find(hanzi) != null) {
            toast("请输入中文文字进行搜索！")
            progressBar.isVisible = false
            swipeRefreshLayout.isRefreshing = false
            swipeRefreshLayout.isEnabled = true
            isLoading = false
            buttonQuery.show()
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                fetchHanziInfo(hanzi)
            }
        }
    }

    private suspend fun fetchHanziInfo(text: String) {
        try {
            val url = "https://api.oioweb.cn/api/txt/dict?text=$text"
            val jsonData = withContext(Dispatchers.IO) {
                kotlin.coroutines.suspendCoroutine { continuation ->
                    val client = okhttp3.OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS) // 设置连接超时时间为30秒
                        .readTimeout(30, TimeUnit.SECONDS) // 设置读取超时时间为30秒
                        .build()
                    val request = okhttp3.Request.Builder()
                        .url(url)
                        .build()
                    client.newCall(request).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                            // 捕获请求超时异常
                            if (e is java.net.SocketTimeoutException) {
                                continuation.resumeWith(Result.failure(Exception("Request timed out")))
                            } else {
                                continuation.resumeWith(Result.failure(e))
                            }
                        }

                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            if (response.isSuccessful) {
                                val responseData = response.body!!.string()
                                continuation.resumeWith(Result.success(responseData))
                            } else {
                                continuation.resumeWith(Result.failure(Exception("Response unsuccessful")))
                            }
                        }
                    })
                }
            }
            updateUI(jsonData)
        } catch (e: Exception) {
            // 处理网络异常
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                toast("无网络连接")
                swipeRefreshLayout.isRefreshing = false
                progressBar.isVisible = false
                isLoading = false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun updateUI(json: String) {
        try {
            val jsonObject = JSONObject(json)
            val result = jsonObject.getJSONObject("result")
            val pinYin = result.getString("pinyin")
            val words = result.getString("words")
            val biHua = result.getInt("bihua")
            val buShou = result.getString("bushou")

            val basicExplainArray = result.getJSONArray("basic_explain")
            val basicExplains = StringBuilder()
            for (i in 0 until basicExplainArray.length()) {
                basicExplains.append(basicExplainArray.getString(i)).append("\n")
            }

            val detailExplainArray = result.getJSONArray("detail_explain")
            val detailExplains = StringBuilder()
            for (i in 0 until detailExplainArray.length()) {
                detailExplains.append(detailExplainArray.getString(i)).append("\n")
            }

            // Update UI
            withContext(Dispatchers.Main) {
                isLoading = false
                buttonQuery.show()
                swipeRefreshLayout.isEnabled = true
                swipeRefreshLayout.isRefreshing = false
                val textViewDetailExplainMaterialCardView =
                    findViewById<MaterialCardView>(R.id.textViewBasicExplainMaterialCardView)
                val textViewBasicExplainMaterialCardView =
                    findViewById<MaterialCardView>(R.id.textViewDetailExplainMaterialCardView)
                val textViewWordMaterialCardView =
                    findViewById<MaterialCardView>(R.id.textViewWordMaterialCardView)
                val linearLayout = findViewById<LinearLayout>(R.id.linearLayout)
                textViewPinyin.text = pinYin
                textViewBuShou.text = buShou
                textViewBiHua.text = "$biHua"
                textViewWord.text = "组词: $words"
                textViewBasicExplain.text = "基本解释:\n$basicExplains"
                textViewDetailExplain.text = "详细解释:\n$detailExplains"

                val fadeInAnimation = AnimationUtils.loadAnimation(
                    this@ActivityDictionary,
                    R.anim.fade_in_up
                )
                textViewDetailExplainMaterialCardView.isVisible = true
                textViewBasicExplainMaterialCardView.isVisible = true
                textViewWordMaterialCardView.isVisible = true
                linearLayout.isVisible = true
                progressBar.isVisible = false
                textViewDetailExplainMaterialCardView.startAnimation(fadeInAnimation)
                textViewBasicExplainMaterialCardView.startAnimation(fadeInAnimation)
                textViewWordMaterialCardView.startAnimation(fadeInAnimation)
                linearLayout.startAnimation(fadeInAnimation)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
