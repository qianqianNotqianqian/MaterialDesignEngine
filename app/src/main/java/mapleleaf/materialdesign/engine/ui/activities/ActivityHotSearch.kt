package mapleleaf.materialdesign.engine.ui.activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.xuexiang.xui.widget.textview.MarqueeTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.utils.toast
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ActivityHotSearch : UniversalActivityBase(R.layout.activity_hot_search) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterHotSearch
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var hotSearchList: ArrayList<HotSearchItem>
    private lateinit var loading: AppCompatImageView
    private var animatedVectorDrawable: AnimatedVectorDrawable? = null
    private var currentUrl: String? = "https://api.vvhan.com/api/hotlist/wbHot"

    override fun initializeComponents(savedInstanceState: Bundle?) {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        hotSearchList = ArrayList()
        adapter = AdapterHotSearch(this, hotSearchList)
        recyclerView.adapter = adapter
        FastScrollerBuilder(recyclerView).build()

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        loading = findViewById(R.id.loading)
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

        lifecycleScope.launch {
            fetchHotSearchData(currentUrl!!)
        }

        swipeRefreshLayout.setOnRefreshListener {
            currentUrl?.let { url ->
                lifecycleScope.launch {
                    fetchHotSearchData(url)
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        setToolbarTitle(getString(R.string.toolbar_title_activity_hot_search))
        setToolbarSubtitle(getString(R.string.toolbar_subtitle_activity_hot_search))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_hot_search, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_hot_search_weibo -> {
                currentUrl = "https://api.vvhan.com/api/hotlist/wbHot"
                lifecycleScope.launch {
                    swipeRefreshLayout.isRefreshing = true
                    fetchHotSearchData(currentUrl!!)
                }
                true
            }

            R.id.menu_hot_search_toutiao -> {
                currentUrl = "https://api.vvhan.com/api/hotlist/toutiao"
                lifecycleScope.launch {
                    swipeRefreshLayout.isRefreshing = true
                    fetchHotSearchData(currentUrl!!)
                }
                true
            }

            R.id.menu_hot_search_hupu -> {
                currentUrl = "https://api.vvhan.com/api/hotlist/huPu"
                lifecycleScope.launch {
                    swipeRefreshLayout.isRefreshing = true
                    fetchHotSearchData(currentUrl!!)
                }
                true
            }

            R.id.menu_hot_search_zhihu -> {
                currentUrl = "https://api.vvhan.com/api/hotlist/zhihuHot"
                lifecycleScope.launch {
                    swipeRefreshLayout.isRefreshing = true
                    fetchHotSearchData(currentUrl!!)
                }
                true
            }

            R.id.menu_hot_search_zhihuDay -> {
                currentUrl = "https://api.vvhan.com/api/hotlist/zhihuDay"
                lifecycleScope.launch {
                    swipeRefreshLayout.isRefreshing = true
                    fetchHotSearchData(currentUrl!!)
                }
                true
            }

            R.id.menu_hot_search_36ke -> {
                currentUrl = "https://api.vvhan.com/api/hotlist/36Ke"
                lifecycleScope.launch {
                    swipeRefreshLayout.isRefreshing = true
                    fetchHotSearchData(currentUrl!!)
                }
                true
            }

            R.id.menu_hot_search_bilibili -> {
                currentUrl = "https://api.vvhan.com/api/hotlist/bili"
                lifecycleScope.launch {
                    swipeRefreshLayout.isRefreshing = true
                    fetchHotSearchData(currentUrl!!)
                }
                true
            }

            R.id.menu_hot_search_baiduRD -> {
                currentUrl = "https://api.vvhan.com/api/hotlist/baiduRD"
                lifecycleScope.launch {
                    swipeRefreshLayout.isRefreshing = true
                    fetchHotSearchData(currentUrl!!)
                }
                true
            }

            R.id.menu_hot_search_douYin -> {
                currentUrl = "https://api.vvhan.com/api/hotlist/douyinHot"
                lifecycleScope.launch {
                    swipeRefreshLayout.isRefreshing = true
                    fetchHotSearchData(currentUrl!!)
                }
                true
            }

            R.id.menu_hot_search_douBan -> {
                currentUrl = "https://api.vvhan.com/api/hotlist/douban"
                lifecycleScope.launch {
                    swipeRefreshLayout.isRefreshing = true
                    fetchHotSearchData(currentUrl!!)
                }
                true
            }

            R.id.menu_hot_itNew -> {
                currentUrl = "https://api.vvhan.com/api/hotlist/itNews"
                lifecycleScope.launch {
                    swipeRefreshLayout.isRefreshing = true
                    fetchHotSearchData(currentUrl!!)
                }
                true
            }

            R.id.menu_hot_search_huxiu -> {
                currentUrl = "https://api.vvhan.com/api/hotlist/huXiu"
                lifecycleScope.launch {
                    swipeRefreshLayout.isRefreshing = true
                    fetchHotSearchData(currentUrl!!)
                }
                true
            }

            R.id.menu_hot_search_woShiPm -> {
                currentUrl = "https://api.vvhan.com/api/hotlist/woShiPm"
                lifecycleScope.launch {
                    swipeRefreshLayout.isRefreshing = true
                    fetchHotSearchData(currentUrl!!)
                }
                true
            }
            // 处理更多菜单项的逻辑
            else -> super.onOptionsItemSelected(item)
        }
    }

    data class HotSearchItem(
        val index: Int,
        val title: String,
        val hot: String,
        val url: String,
    )

    private suspend fun fetchHotSearchData(url: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }
            val responseData = response.body?.string()
            val dataArray = responseData?.let { JSONObject(it).optJSONArray("data") }
            val newList = mutableListOf<HotSearchItem>()
            dataArray?.let {
                for (i in 0 until it.length()) {
                    val dataObject = it.getJSONObject(i)
                    val index = dataObject.getInt("index")
                    val title = dataObject.getString("title")
                    val hot = dataObject.getString("hot")
                    val itemUrl = dataObject.getString("url")
                    newList.add(HotSearchItem(index, title, hot, itemUrl))
                }
            }
            withContext(Dispatchers.Main) {
                // 如果当前选中的链接与加载的链接相同，则刷新列表
                if (currentUrl == url) {
                    adapter.updateList(newList)
                }
                animatedVectorDrawable?.stop()
                loading.isVisible = false
                swipeRefreshLayout.isRefreshing = false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                toast("发生错误: ${e.message}")
                animatedVectorDrawable?.stop()
                loading.isVisible = false
                swipeRefreshLayout.isRefreshing = false
            }
        } catch (e: JSONException) {
            toast("解析JSON错误: ${e.message}")
            animatedVectorDrawable?.stop()
            loading.isVisible = false
            swipeRefreshLayout.isRefreshing = false
        }
    }

    class AdapterHotSearch(
        private val context: Context,
        private val hotSearchList: ArrayList<HotSearchItem>,
    ) : RecyclerView.Adapter<AdapterHotSearch.HotSearchViewHolder>() {

        @SuppressLint("NotifyDataSetChanged")
        fun updateList(newList: List<HotSearchItem>) {
//            val diffResult = DiffUtil.calculateDiff(DiffCallback(hotSearchList, newList))
//            hotSearchList.clear()
//            hotSearchList.addAll(newList)
//            diffResult.dispatchUpdatesTo(this)

            hotSearchList.clear()
            hotSearchList.addAll(newList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotSearchViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_hot_search, parent, false)
            return HotSearchViewHolder(view)
        }

        override fun onBindViewHolder(holder: HotSearchViewHolder, position: Int) {
            val hotSearchItem = hotSearchList[position]
            holder.indexTextView.text = hotSearchItem.index.toString()
            holder.titleTextView.text = hotSearchItem.title
            holder.hotTextView.text = hotSearchItem.hot

            // 设置顶部边距
            val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
            layoutParams.topMargin = if (position == 0) 18 else 0
//            layoutParams.bottomMargin = if (position == itemCount - 1) 18 else 0
            holder.itemView.layoutParams = layoutParams
        }

        override fun onViewAttachedToWindow(holder: HotSearchViewHolder) {
            super.onViewAttachedToWindow(holder)
            setFadeAnimation(holder.itemView)
        }

        override fun getItemCount(): Int {
            return hotSearchList.size
        }

        val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
        val baseColor = ContextCompat.getColor(context, R.color.text_color)
        val fusionColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)

        inner class HotSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
            val indexTextView: TextView =
                itemView.findViewById<MaterialTextView>(R.id.indexTextView)
            val titleTextView: TextView = itemView.findViewById<MarqueeTextView>(R.id.titleTextView)
            val hotTextView: TextView = itemView.findViewById<MaterialTextView>(R.id.hotTextView)

            init {
                indexTextView.setTextColor(fusionColor)
                titleTextView.setTextColor(fusionColor)
                hotTextView.setTextColor(fusionColor)

                val baseColor = ContextCompat.getColor(context, R.color.background)
                val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
                itemView.findViewById<MaterialCardView>(R.id.hotMaterialCardView).apply {
                    strokeColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.3f)
                    setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.2f))
                    setOnClickListener(this@HotSearchViewHolder)
                }
            }

            override fun onClick(view: View) {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    hotSearchList[position].let { hotSearchItem ->
                        Intent(view.context, ActivityBrowser::class.java).apply {
                            putExtra("url", hotSearchItem.url)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }.also { intent ->
                            view.context.startActivity(intent)
                        }
                    }
//                    val message = "Clicked: ${hotSearchItem.title}, Link: ${hotSearchItem.url}"
//                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun setFadeAnimation(view: View) {
            val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            animator.duration = 320
            animator.start()
        }
    }
}
