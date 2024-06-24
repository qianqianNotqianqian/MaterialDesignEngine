package mapleleaf.materialdesign.engine.ui.activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.utils.SearchTextWatcher
import mapleleaf.materialdesign.engine.utils.toast
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class ActivityInfiniteList : UniversalActivityBase(R.layout.activity_infinite_recycler_view) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appsSearchBox: EditText
    private lateinit var adapter: InfiniteListAdapter
    private lateinit var loading: AppCompatImageView
    private var animatedVectorDrawable: AnimatedVectorDrawable? = null

    override fun initializeComponents(savedInstanceState: Bundle?) {

        setToolbarTitle(getString(R.string.toolbar_title_activity_infinite_view))

        loading = findViewById(R.id.loading)
        appsSearchBox = findViewById(R.id.apps_search_box)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = InfiniteListAdapter()

        animatedVectorDrawable = AppCompatResources.getDrawable(
            this@ActivityInfiniteList,
            R.drawable.progress_loading_manager
        ) as AnimatedVectorDrawable
        loading.setImageDrawable(animatedVectorDrawable)
        animatedVectorDrawable!!.start()
        loading.isVisible = true

        // 将加载更多项目的回调设置到适配器中
        adapter.setOnLoadMoreListener {
            loadMoreItems()
        }

        recyclerView.adapter = adapter
        FastScrollerBuilder(recyclerView).build()

        val searchTextWatcher = SearchTextWatcher {
            // 获取编辑框中输入的条目数
            val position = appsSearchBox.text.toString().toIntOrNull() ?: return@SearchTextWatcher
            if (position < 0 || position >= adapter.itemCount) {
                // 显示提示信息
                toast("输入的位置超出范围，请重新输入")
                return@SearchTextWatcher
            }
            // 滚动到指定位置
            recyclerView.scrollToPosition(position)
        }
        appsSearchBox.addTextChangedListener(searchTextWatcher)

        loadMoreItems()
        val materialCardView = findViewById<MaterialCardView>(R.id.materialCardView)
        val baseColor = ContextCompat.getColor(context, R.color.background)
        val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
        val fusionColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.15f)
        materialCardView.setCardBackgroundColor(fusionColor)
    }

    // 适配器中的回调接口
    private fun loadMoreItems() {
        CoroutineScope(Dispatchers.Main).launch {
            val newData = adapter.generateData(adapter.currentDataSize, adapter.pageSize)
            adapter.addData(newData)
            animatedVectorDrawable!!.stop()
            loading.isVisible = false
        }
    }

    class InfiniteListAdapter :
        RecyclerView.Adapter<InfiniteListAdapter.ViewHolder>() {

        private val dataList = mutableListOf<Long>()
        private var isLoading = false
        private var currentPage = 0

        //        val pageSize = 8388608
        val pageSize = 128

        private var onLoadMoreListener: (() -> Unit)? = null

        fun setOnLoadMoreListener(listener: () -> Unit) {
            onLoadMoreListener = listener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_colors, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (position == itemCount - 1 && !isLoading) {
                onLoadMoreListener?.invoke()
            }
            val data = dataList[position]
            holder.bind(data)
        }

        override fun onViewAttachedToWindow(holder: ViewHolder) {
            super.onViewAttachedToWindow(holder)
            setFadeAnimation(holder.itemView)
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
            private val textView: TextView = itemView.findViewById(R.id.textView)
            private val colorMaterialCardView: MaterialCardView =
                itemView.findViewById(R.id.colorMaterialCardView)
            val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)

            init {
                colorMaterialCardView.setOnClickListener(this)
                val baseColor = ContextCompat.getColor(context, R.color.background)
                colorMaterialCardView.setCardBackgroundColor(
                    ColorUtils.blendARGB(
                        baseColor,
                        primaryColor,
                        0.15f
                    )
                )
            }

            fun bind(data: Long) {
                textView.text = data.toString()
            }

            override fun onClick(v: View?) {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    toast("当前条目: $position")
                }
            }
        }

        private fun setFadeAnimation(view: View) {
            val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            animator.duration = 320
            animator.start()
        }

        @SuppressLint("NotifyDataSetChanged")
        fun addData(newData: List<Long>) {
            currentPage++
            dataList.addAll(newData)
            notifyDataSetChanged()
        }

        val currentDataSize: Int
            get() = dataList.size

        fun generateData(startIndex: Int, count: Int): List<Long> {
            val newData = mutableListOf<Long>()
            for (i in startIndex until startIndex + count) {
                newData.add(i.toLong())
            }
            return newData
        }
    }
}