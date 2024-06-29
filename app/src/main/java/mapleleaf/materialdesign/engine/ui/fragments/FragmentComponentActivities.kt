package mapleleaf.materialdesign.engine.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalFragmentBase
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.utils.SearchTextWatcher
import mapleleaf.materialdesign.engine.utils.toast
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.util.regex.Pattern

class FragmentComponentActivities : UniversalFragmentBase(R.layout.fragment_components) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterComponents
    private lateinit var appsSearchBox: EditText
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

//    override val layoutResId: Int
//        get() = R.layout.fragment_components

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
        recyclerView = rootView.findViewById(R.id.recyclerView)
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout)

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

            val activities = withContext(Dispatchers.IO) {
                getActivities(packageName)
            }

            val visibility = if (activities.isEmpty()) View.GONE else View.VISIBLE
            adapter.setData(activities.toList())
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
                                getActivities(packageName)
                            }
                        }
                        adapter.setData(activities.toList())
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
            val filteredProviders = getFilteredActivities(searchText)
            adapter.setData(filteredProviders)
        } else {
            val packageName = arguments?.getString("packageName") ?: return
            val providers = getActivities(packageName)
            adapter.setData(providers.toList())
        }

        adapter.setSearchText(searchText)
    }

    private fun getFilteredActivities(searchText: String): List<ResolveInfo> {
        val packageName = arguments?.getString("packageName") ?: return emptyList()
        val allActivities = getActivities(packageName)
        val packageManager = requireContext().packageManager
        return allActivities.filter { activity ->
            val activityName = activity.activityInfo.name
            val activityLabel = activity.loadLabel(packageManager).toString()
            activityName.contains(searchText, ignoreCase = true) || activityLabel.contains(
                searchText,
                ignoreCase = true
            )
        }
    }

    private fun getActivities(packageName: String): List<ResolveInfo> {
        return try {
            val packageInfo = requireContext().packageManager.getPackageInfo(
                packageName, PackageManager.GET_ACTIVITIES
            )
            packageInfo.activities?.map { activity ->
                ResolveInfo().apply {
                    this.activityInfo = activity
                }
            }?.sortedBy { it.activityInfo.name } ?: emptyList()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            emptyList()
        }
    }


    class AdapterComponents(
        private val context: Context,
        var activityList: List<ResolveInfo>,
    ) : RecyclerView.Adapter<AdapterComponents.ViewHolder>() {

        private var searchText: String = ""
//        private var diffCallback: DiffCallback<ResolveInfo>? = null

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
            val resolveInfo = activityList[position]
            holder.bind(resolveInfo)
        }

        override fun onViewAttachedToWindow(holder: ViewHolder) {
            super.onViewAttachedToWindow(holder)
            setFadeAnimation(holder.itemView)
        }

        override fun getItemCount(): Int {
            return activityList.size
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setData(newData: List<ResolveInfo>) {
//            diffCallback?.let {
//                val result = DiffUtil.calculateDiff(it)
//                activityList = newData
//                result.dispatchUpdatesTo(this)
//            }
//            diffCallback = DiffCallback(activityList, newData)
            activityList = newData
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
            private val activityIcon: ImageView = itemView.findViewById(R.id.componentIcon)
            private val activityLaunch: MaterialButton = itemView.findViewById(R.id.componentLaunch)
            private val activityLabelTextView: TextView = itemView.findViewById(R.id.componentLabel)
            private val activityNameTextView: TextView = itemView.findViewById(R.id.componentName)
            private val componentMaterialCardView: MaterialCardView =
                itemView.findViewById(R.id.componentCardView)
            private val componentStatus: MaterialSwitch =
                itemView.findViewById(R.id.componentStatus)
            private val softInputTextView: TextView = itemView.findViewById(R.id.softInput)
            private val launchModeTextView: TextView = itemView.findViewById(R.id.launchMode)
            private val taskAffinityTextView: TextView = itemView.findViewById(R.id.taskAffinity)
            private val editShortcutBtn: MaterialButton =
                itemView.findViewById(R.id.editShortcutBtn)
            private val additionalAttributesTextView: TextView =
                itemView.findViewById(R.id.orientationTextView)

            init {
                activityLaunch.setOnClickListener(this)
                editShortcutBtn.setOnClickListener(this)
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

            @SuppressLint("InflateParams")
            override fun onClick(v: View?) {
                val activitiesInfo = activityList[bindingAdapterPosition]
                when (v?.id) {
                    R.id.componentLaunch -> {
                        if (activitiesInfo.activityInfo.exported) {
                            val intent = Intent().apply {
                                component = ComponentName(
                                    activitiesInfo.activityInfo.packageName,
                                    activitiesInfo.activityInfo.name
                                )
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                toast("启动 Activity 失败")
                            } catch (e: SecurityException) {
                                toast("您没有权限运行此 Activity")
                            }
                        } else {
                            try {
                                val command =
                                    "am start -n ${activitiesInfo.activityInfo.packageName}/${activitiesInfo.activityInfo.name}"
                                Runtime.getRuntime().exec(arrayOf("su", "-c", command))
                            } catch (e: SecurityException) {
                                toast("您没有权限运行此 Activity")
                            } catch (e: Exception) {
                                toast("启动 Activity 失败")
                            }
                        }
                    }

                    R.id.editShortcutBtn -> {

                        val dialogView =
                            LayoutInflater.from(context).inflate(R.layout.dialog_create_shortcut, null)
                        val dialog = DialogHelper.customDialog(context, dialogView)
                        dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
                            dialog.dismiss()
                        }
                        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                            dialog.dismiss()
                        }
                    }
                }
            }

            @SuppressLint("SetTextI18n")
            fun bind(resolveInfo: ResolveInfo) {
                val activityLabel = resolveInfo.loadLabel(context.packageManager).toString()
                val activityName = resolveInfo.activityInfo.name
                if (!resolveInfo.activityInfo.exported) {
                    activityNameTextView.setTextColor(ContextCompat.getColor(context, R.color.red))
                } else {
                    activityNameTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.green
                        )
                    )
                }
                if (!resolveInfo.activityInfo.enabled) {
                    activityLabelTextView.paintFlags =
                        activityLabelTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                activityNameTextView.text = activityName.highlightText(searchText)
                activityLabelTextView.text = activityLabel.highlightText(searchText)
                componentStatus.isChecked = resolveInfo.activityInfo.isEnabled
                activityIcon.setImageDrawable(resolveInfo.activityInfo.loadIcon(context.packageManager))

                val softInputMode = resolveInfo.activityInfo.softInputMode
                val permissions = resolveInfo.activityInfo.permission

                if (softInputMode != 0) {
                    softInputTextView.text = "软键盘输入模式：$softInputMode | $permissions"
                }
                if (!permissions.isNullOrEmpty()) {
                    softInputTextView.text = "软键盘输入模式：$softInputMode | $permissions"
                } else {
                    softInputTextView.text = "软键盘输入模式：$softInputMode | 无需权限"
                }

                additionalAttributesTextView.text

                val launchMode = resolveInfo.activityInfo.launchMode
                val screenOrientation = resolveInfo.activityInfo.screenOrientation
                val taskAffinity = resolveInfo.activityInfo.taskAffinity
                val attributes = mutableListOf<String>()

                if (resolveInfo.activityInfo.flags and ActivityInfo.FLAG_ALWAYS_RETAIN_TASK_STATE != 0) {
                    attributes.add("AlwaysRetain")
                }

                if (resolveInfo.activityInfo.flags and ActivityInfo.FLAG_HARDWARE_ACCELERATED != 0) {
                    attributes.add("HardwareAccel")
                }

                if (resolveInfo.activityInfo.flags and ActivityInfo.FLAG_NO_HISTORY != 0) {
                    attributes.add("NoHistory")
                }

                if (resolveInfo.activityInfo.flags and ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS != 0) {
                    attributes.add("ExcludeRecent")
                }

                if (attributes.isNotEmpty()) {
                    additionalAttributesTextView.text = attributes.joinToString(", ")
                } else {
                    additionalAttributesTextView.text = ""
                }

                launchModeTextView.text = "启动模式：${getLaunchModeString(launchMode)} | 屏幕旋转：${
                    getScreenOrientationString(screenOrientation)
                }"
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