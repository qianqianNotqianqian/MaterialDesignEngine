package mapleleaf.materialdesign.engine.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
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
import mapleleaf.materialdesign.engine.utils.SearchTextWatcher
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.util.regex.Pattern

class FragmentComponentReceivers : UniversalFragmentBase(R.layout.fragment_components) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterComponents
    private lateinit var appsSearchBox: EditText
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    fun isSearchBoxNotEmpty(): Boolean {
        return appsSearchBox.text.toString().isNotEmpty()
    }

    // Clear the search box
    fun clearSearchBox() {
        appsSearchBox.text.clear()
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)

        val packageName = arguments?.getString("packageName") ?: return

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
            adapter = AdapterComponents(requireContext(), emptyList())
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

            val receivers = withContext(Dispatchers.IO) {
                getReceivers(packageName)
            }

            val visibility = if (receivers.isEmpty()) View.GONE else View.VISIBLE
            adapter.setData(receivers.toList())
            recyclerView.adapter = adapter
            recyclerView.visibility = visibility
            appsSearchBox.visibility = visibility
            swipeRefreshLayout.visibility = visibility
            imageView.visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE

            val defaultSearchText = appsSearchBox.text
            searchApp(defaultSearchText)
            setupSearchBox(appsSearchBox)

            swipeRefreshLayout.setOnRefreshListener {
                if (::adapter.isInitialized) {
                    clearEditFocus()
                    val searchQuery = appsSearchBox.text.toString()
                    if (searchQuery.isNotBlank()) {
                        searchApp(appsSearchBox.text)
                        swipeRefreshLayout.isRefreshing = false
                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            withContext(Dispatchers.IO) {
                                getReceivers(packageName)
                            }
                        }
                        adapter.setData(receivers.toList())
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
            val filteredReceivers = getFilteredReceivers(searchText)
            adapter.setData(filteredReceivers)
        } else {
            val packageName = arguments?.getString("packageName") ?: return
            val receivers = getReceivers(packageName)
            adapter.setData(receivers.toList())
        }
        adapter.setSearchText(searchText)
    }

    private fun getFilteredReceivers(searchText: String): List<ActivityInfo> {
        val packageName = arguments?.getString("packageName") ?: return emptyList()
        val allReceivers = getReceivers(packageName)
        val packageManager = requireContext().packageManager
        return allReceivers.filter { service ->
            val receiversLabel = service.loadLabel(packageManager).toString()
            service.name.contains(searchText, ignoreCase = true)
                    || receiversLabel.contains(searchText, ignoreCase = true)
        }
    }

    private fun getReceivers(packageName: String): List<ActivityInfo> {
        return try {
            val packageInfo = requireContext().packageManager.getPackageInfo(
                packageName, PackageManager.GET_RECEIVERS
            )
            val providers = packageInfo.receivers ?: emptyArray()
            return providers.sortedBy { it.name }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            emptyList()
        }
    }

    class AdapterComponents(
        private val context: Context,
        private var receiverList: List<ActivityInfo>,
    ) : RecyclerView.Adapter<AdapterComponents.ViewHolder>() {

        private var searchText: String = ""
//        private var diffCallback: DiffCallback<ActivityInfo>? = null

        @SuppressLint("NotifyDataSetChanged")
        fun setSearchText(text: String) {
            searchText = text
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_component, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val receiverInfo = receiverList[position]
            holder.bind(receiverInfo)
        }

        override fun onViewAttachedToWindow(holder: ViewHolder) {
            super.onViewAttachedToWindow(holder)
            setFadeAnimation(holder.itemView)
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setData(newData: List<ActivityInfo>) {
//            diffCallback?.let {
//                val result = DiffUtil.calculateDiff(it)
//                receiverList = newData
//                result.dispatchUpdatesTo(this)
//            }
//            diffCallback = DiffCallback(receiverList, newData)
            receiverList = newData
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return receiverList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val receiverIcon: ImageView = itemView.findViewById(R.id.componentIcon)
            private val receiverLabelTextView: TextView = itemView.findViewById(R.id.componentLabel)
            private val receiverNameTextView: TextView = itemView.findViewById(R.id.componentName)
            private val orientationTextView: TextView = itemView.findViewById(R.id.orientationTextView)
            private val actionLaunchAndShortcut: LinearLayoutCompat = itemView.findViewById(R.id.action_launch_and_shortcut)
            private val taskAffinityTextView: TextView = itemView.findViewById(R.id.taskAffinity)
            private val launchModeTextView: TextView = itemView.findViewById(R.id.launchMode)
            private val softInput: TextView = itemView.findViewById(R.id.softInput)
            private val componentMaterialCardView: MaterialCardView =
                itemView.findViewById(R.id.componentCardView)
            private val componentStatus: MaterialSwitch =
                itemView.findViewById(R.id.componentStatus)

            init {
                actionLaunchAndShortcut.isVisible = false
                softInput.isVisible = false
                val baseColor = ContextCompat.getColor(context, R.color.background)
                val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
                componentMaterialCardView.setCardBackgroundColor(
                    ColorUtils.blendARGB(
                        baseColor,
                        primaryColor,
                        0.15f
                    )
                )
            }

            @SuppressLint("SetTextI18n")
            fun bind(receiverInfo: ActivityInfo) {
                val receiverLabel = receiverInfo.loadLabel(context.packageManager).toString()
                val receiverName = receiverInfo.name
                val packageName = receiverInfo.packageName

                // 获取服务声明的权限信息
                val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_RECEIVERS or PackageManager.GET_PERMISSIONS)
                val receivers = packageInfo.receivers ?: emptyArray()

                val servicePermissions = mutableListOf<String>()
                for (receiver in receivers) {
                    if (receiver.name == receiverName) {
                        val permissions = receiver.permission
                        permissions?.let {
                            servicePermissions.addAll(listOf(it))
                        }
                        break
                    }
                }
                val permissionsText = if (servicePermissions.isEmpty()) {
                    "无需权限"
                } else {
                    servicePermissions.joinToString(", ")
                }
                orientationTextView.text = permissionsText
                if (!receiverInfo.exported) {
                    receiverNameTextView.setTextColor(ContextCompat.getColor(context, R.color.red))
                } else {
                    receiverNameTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.green
                        )
                    )
                }
                val launchMode = receiverInfo.launchMode
                val screenOrientation = receiverInfo.screenOrientation
                val taskAffinity = receiverInfo.taskAffinity

                if (!receiverInfo.enabled) {
                    receiverLabelTextView.paintFlags =
                        receiverLabelTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }

                receiverNameTextView.text = receiverName.highlightText(searchText)
                receiverLabelTextView.text = receiverLabel.highlightText(searchText)

                componentStatus.isChecked = receiverInfo.isEnabled

                receiverIcon.setImageDrawable(receiverInfo.loadIcon(context.packageManager))

                launchModeTextView.text = "启动模式：${getLaunchModeString(launchMode)} | 屏幕旋转：${getScreenOrientationString(screenOrientation)}"
                taskAffinityTextView.text = "任务关联：$taskAffinity"
            }

            private fun getLaunchModeString(launchMode: Int): String {
                return when (launchMode) {
                    ActivityInfo.LAUNCH_SINGLE_TOP -> "栈顶部模式"
                    ActivityInfo.LAUNCH_SINGLE_TASK -> "单任务模式"
                    ActivityInfo.LAUNCH_SINGLE_INSTANCE -> "单实例模式"
                    else -> "标准模式"
                }
            }

            private fun getScreenOrientationString(screenOrientation: Int): String {
                return when (screenOrientation) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> "竖屏"
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> "横屏"
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT -> "反向竖屏"
                    ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> "反向横屏"
                    else -> "未指定"
                }
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