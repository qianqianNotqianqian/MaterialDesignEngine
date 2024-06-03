package mapleleaf.materialdesign.engine.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.interfaces.BottomDialogSlideEventLifecycleCallback
import com.kongzue.dialogx.interfaces.OnBindView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalFragmentBase
import mapleleaf.materialdesign.engine.ui.activities.ActivityAppComponents
import mapleleaf.materialdesign.engine.ui.activities.ActivityApplicationDetails
import mapleleaf.materialdesign.engine.ui.activities.ActivityApplicationDex
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.utils.SearchTextWatcher
import mapleleaf.materialdesign.engine.utils.toast
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.util.regex.Pattern

class FragmentComponentActivities : UniversalFragmentBase() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterComponents
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

        val packageName = arguments?.getString("packageName") ?: return

        val imageView = rootView.findViewById<ImageView>(R.id.null_list)
        appsSearchBox = rootView.findViewById(R.id.apps_search_box)
        recyclerView = rootView.findViewById(R.id.recyclerView)
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout)

        val materialCardViewEdit = rootView.findViewById<MaterialCardView>(R.id.materialCardViewEdit)
        val baseColor = ContextCompat.getColor(requireContext(), R.color.background)
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        materialCardViewEdit.setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.15f))

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
        val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
            private val activityLabelTextView: TextView = itemView.findViewById(R.id.componentLabel)
            private val activityNameTextView: TextView = itemView.findViewById(R.id.componentName)
            private val componentMaterialCardView: MaterialCardView =
                itemView.findViewById(R.id.componentCardView)
            private val componentStatus: MaterialSwitch =
                itemView.findViewById(R.id.componentStatus)

            init {
                componentMaterialCardView.setOnClickListener(this)
                val baseColor = ContextCompat.getColor(context, R.color.background)
                val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
                componentMaterialCardView.setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.15f))
            }

            @SuppressLint("InflateParams")
            override fun onClick(v: View?) {
                val activitiesInfo = activityList[bindingAdapterPosition]

                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_components_detail, null)
                val dialog = DialogHelper.customDialog(context, dialogView)

                dialogView.findViewById<View>(R.id.btn_launch).apply {
                    isVisible = true
                    setOnClickListener {
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
                                // 构建 shell 命令
                                val command =
                                    "am start -n ${activitiesInfo.activityInfo.packageName}/${activitiesInfo.activityInfo.name}"
                                // 获取 root 权限并执行命令
                                Runtime.getRuntime().exec(arrayOf("su", "-c", command))
                            } catch (e: SecurityException) {
                                toast("您没有权限运行此 Activity")
                            } catch (e: Exception) {
                                // 处理其他异常情况
                                toast("启动 Activity 失败")
                            }
                        }
                    }
                }
                dialogView.apply {
                    findViewById<LinearLayout>(R.id.activity_full_name).isVisible = true
                    findViewById<LinearLayout>(R.id.package_full_name).isVisible = true
                    findViewById<LinearLayout>(R.id.component_full_name).isVisible = false
                }

                fun setTextAndColor(textView: TextView, value: Boolean) {
                    textView.text = value.toString()
                    textView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            if (value) R.color.green else R.color.red
                        )
                    )
                }


                val enabledTextView = dialogView.findViewById<TextView>(R.id.state_enable)
                setTextAndColor(enabledTextView, activitiesInfo.activityInfo.enabled)

                val exportedTextView = dialogView.findViewById<TextView>(R.id.state_exported)
                setTextAndColor(exportedTextView, activitiesInfo.activityInfo.exported)

                dialogView.findViewById<ImageView>(R.id.imageView_icon)
                    .setImageDrawable(activitiesInfo.activityInfo.loadIcon(context.packageManager))

                fun setTextToEditText(editText: EditText, text: String) {
                    editText.setText(text)
                }
                setTextToEditText(
                    dialogView.findViewById(R.id.edit_title),
                    activitiesInfo.activityInfo.loadLabel(context.packageManager).toString()
                )
                setTextToEditText(
                    dialogView.findViewById(R.id.edit_pkg),
                    activitiesInfo.activityInfo.packageName
                )
                setTextToEditText(
                    dialogView.findViewById(R.id.edit_activity),
                    activitiesInfo.activityInfo.name
                )
                dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                    dialog.dismiss()
                }
            }

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
                    activityLabelTextView.paintFlags = activityLabelTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                activityNameTextView.text = activityName.highlightText(searchText)
                activityLabelTextView.text = activityLabel.highlightText(searchText)
                componentStatus.isChecked = resolveInfo.activityInfo.isEnabled
                activityIcon.setImageDrawable(resolveInfo.activityInfo.loadIcon(context.packageManager))
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