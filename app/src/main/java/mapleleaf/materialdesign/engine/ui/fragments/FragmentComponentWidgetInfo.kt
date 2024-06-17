package mapleleaf.materialdesign.engine.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalFragmentBase
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.utils.SearchTextWatcher
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.util.regex.Pattern

class FragmentComponentWidgetInfo : UniversalFragmentBase() {

    private var packageName: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WidgetAdapter
    private lateinit var appsSearchBox: EditText
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override val layoutResId: Int
        get() = R.layout.fragment_components

    fun isSearchBoxNotEmpty(): Boolean {
        return appsSearchBox.text.toString().isNotEmpty()
    }

    // Clear the search box
    fun clearSearchBox() {
        appsSearchBox.text.clear()
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)

        packageName = arguments?.getString("packageName") ?: return

        val imageView = rootView.findViewById<ImageView>(R.id.null_list)
        appsSearchBox = rootView.findViewById(R.id.apps_search_box)

        val materialCardViewEdit =
            rootView.findViewById<MaterialCardView>(R.id.materialCardViewEdit)
        val baseColor = ContextCompat.getColor(requireContext(), R.color.background)
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        materialCardViewEdit.setCardBackgroundColor(
            ColorUtils.blendARGB(
                baseColor,
                primaryColor,
                0.15f
            )
        )

        lifecycleScope.launch(Dispatchers.Main) {

            recyclerView = rootView.findViewById(R.id.recyclerView)

            swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout)
            recyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = WidgetAdapter(requireContext(), emptyList())
            recyclerView.adapter = adapter
            FastScrollerBuilder(recyclerView).build()

            val colorRed = ContextCompat.getColor(requireContext(), R.color.red1)
            val colorGreen = ContextCompat.getColor(requireContext(), R.color.lawngreen)
            val colorBlue = ContextCompat.getColor(requireContext(), R.color.blue)
            val colorOrange = ContextCompat.getColor(requireContext(), R.color.orange2)
            val progressColors =
                ContextCompat.getColor(requireContext(), R.color.swipe_refresh_layout_progress)
            swipeRefreshLayout.setColorSchemeColors(colorRed, colorGreen, colorBlue, colorOrange)
            swipeRefreshLayout.setProgressBackgroundColorSchemeColor(progressColors)

            val widgetInfo = withContext(Dispatchers.IO) {
                getWidgetInfo(packageName!!)
            }

            val visibility = if (widgetInfo.isEmpty()) View.GONE else View.VISIBLE
            adapter.setWidgetInfo(widgetInfo.toList())
            recyclerView.adapter = adapter
            recyclerView.visibility = visibility
            appsSearchBox.visibility = visibility
            swipeRefreshLayout.visibility = visibility
            imageView.visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE

            val defaultSearchText = appsSearchBox.text
            setupSearchBox(appsSearchBox)
            searchApp(defaultSearchText)

            swipeRefreshLayout.setOnRefreshListener {
                if (::adapter.isInitialized) {
                    clearEditFocus()
                    val searchQuery = appsSearchBox.text.toString()
                    if (searchQuery.isNotBlank()) {
                        searchApp(appsSearchBox.text)
                        swipeRefreshLayout.isRefreshing = false
                    } else {
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                getWidgetInfo(packageName!!)
                            }
                        }
                        adapter.setWidgetInfo(widgetInfo.toList())
                        swipeRefreshLayout.isRefreshing = false
                    }
                }
            }

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    clearEditFocus()
                }
            })
        }
    }

    private fun clearEditFocus() {
        appsSearchBox.clearFocus()
        val inputMethodManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // 在失去焦点后隐藏输入法键盘
        appsSearchBox.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                inputMethodManager.hideSoftInputFromWindow(appsSearchBox.windowToken, 0)
            }
        }
    }

    private fun setupSearchBox(appsSearchBox: EditText) {
        appsSearchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchApp(appsSearchBox.text)
            }
            true
        }

        val searchTextWatcher = SearchTextWatcher {
            searchApp(appsSearchBox.text)
            activity?.invalidateOptionsMenu()
        }
        appsSearchBox.addTextChangedListener(searchTextWatcher)
    }

    private fun searchApp(text: Editable?) {
        val searchText = text?.toString() ?: ""
        if (searchText.isNotEmpty()) {
            val filteredWidgets = getFilteredWidgets(searchText)
            adapter.setWidgetInfo(filteredWidgets)
        } else {
            val packageName = arguments?.getString("packageName") ?: return
            val providers = getWidgetInfo(packageName)
            adapter.setWidgetInfo(providers.toList())
        }

        adapter.setSearchText(searchText)
    }

    private fun getFilteredWidgets(searchText: String): List<AppWidgetProviderInfo> {
        val packageName = arguments?.getString("packageName") ?: return emptyList()
        val allWidgets = getWidgetInfo(packageName)
        val packageManager = requireContext().packageManager
        return allWidgets.filter { widget ->
            val widgetName = widget.activityInfo.name
            val activityLabel = widget.loadLabel(packageManager).toString()
            widgetName.contains(searchText, ignoreCase = true) || activityLabel.contains(
                searchText,
                ignoreCase = true
            )
        }
    }

    private fun getWidgetInfo(packageName: String): List<AppWidgetProviderInfo> {
        val widgetInfo = mutableListOf<AppWidgetProviderInfo>()
        val appWidgetManager = AppWidgetManager.getInstance(requireContext())
        val appWidgetProviderInfo =
            appWidgetManager.getInstalledProvidersForPackage(packageName, null)
        for (info in appWidgetProviderInfo) {
            widgetInfo.add(info)
        }
        return widgetInfo.sortedBy { it.provider.flattenToString() }
    }

    inner class WidgetAdapter(
        private val context: Context,
        private var widgetList: List<AppWidgetProviderInfo>,
    ) : RecyclerView.Adapter<WidgetAdapter.WidgetViewHolder>() {

        private var searchText: String = ""

        @SuppressLint("NotifyDataSetChanged")
        fun setSearchText(text: String) {
            searchText = text
            notifyDataSetChanged()
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setWidgetInfo(newWidget: List<AppWidgetProviderInfo>) {
            widgetList = newWidget
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_component, parent, false)
            return WidgetViewHolder(view)
        }

        override fun onBindViewHolder(holder: WidgetViewHolder, position: Int) {
            val widgetInfo = widgetList[position]
            holder.bind(widgetInfo)
        }

        override fun onViewAttachedToWindow(holder: WidgetViewHolder) {
            super.onViewAttachedToWindow(holder)
            setFadeAnimation(holder.itemView)
        }

        override fun getItemCount(): Int {
            return widgetList.size
        }

        inner class WidgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val widgetIcon: ImageView = itemView.findViewById(R.id.componentIcon)
            private val nameTextView: TextView = itemView.findViewById(R.id.componentName)
            private val labelTextView: TextView = itemView.findViewById(R.id.componentLabel)
            private val actionLaunchAndShortcut: LinearLayoutCompat = itemView.findViewById(R.id.action_launch_and_shortcut)
            private val componentMaterialCardView: MaterialCardView =
                itemView.findViewById(R.id.componentCardView)
            private val componentStatus: MaterialSwitch =
                itemView.findViewById(R.id.componentStatus)
            private val taskAffinity: TextView = itemView.findViewById(R.id.taskAffinity)
            private val launchMode: TextView = itemView.findViewById(R.id.launchMode)
            private val softInput: TextView = itemView.findViewById(R.id.softInput)

            init {
                actionLaunchAndShortcut.isVisible = false
                taskAffinity.isVisible = false
                launchMode.isVisible = false
                softInput.isVisible = false
                val baseColor = ContextCompat.getColor(requireContext(), R.color.background)
                val primaryColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                componentMaterialCardView.setCardBackgroundColor(
                    ColorUtils.blendARGB(
                        baseColor,
                        primaryColor,
                        0.15f
                    )
                )
            }

            fun bind(widgetInfo: AppWidgetProviderInfo) {
                if (!widgetInfo.activityInfo.exported) {
                    nameTextView.setTextColor(ContextCompat.getColor(context, R.color.red))
                } else {
                    nameTextView.setTextColor(ContextCompat.getColor(context, R.color.green))
                }
                if (!widgetInfo.activityInfo.enabled) {
                    labelTextView.paintFlags =
                        labelTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
//                nameTextView.text = widgetInfo.provider.flattenToString().highlightText(searchText)
                nameTextView.text = widgetInfo.activityInfo.name.highlightText(searchText)
                labelTextView.text =
                    widgetInfo.loadLabel(context.packageManager).highlightText(searchText)
                componentStatus.isChecked = widgetInfo.activityInfo.isEnabled
                widgetIcon.setImageDrawable(widgetInfo.activityInfo.loadIcon(context.packageManager))
            }

            private fun String.highlightText(searchText: String): SpannableString {
                val spannableString = SpannableString(this)
                if (searchText.isNotEmpty()) {
                    val pattern =
                        Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE)
                    val matcher = pattern.matcher(this)
                    while (matcher.find()) {
                        val start = matcher.start()
                        val end = matcher.end()
                        spannableString.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                return spannableString
            }
        }

        private fun setFadeAnimation(view: View) {
            val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            animator.duration = 320
            animator.start()
        }
    }
}