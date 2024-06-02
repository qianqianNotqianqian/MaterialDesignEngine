package mapleleaf.materialdesign.engine.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ProviderInfo
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

class FragmentComponentProviders : UniversalFragmentBase() {

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

        val materialCardViewEdit = rootView.findViewById<MaterialCardView>(R.id.materialCardViewEdit)
        val baseColor = ContextCompat.getColor(requireContext(), R.color.background)
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        materialCardViewEdit.setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.15f))

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

            val providers = withContext(Dispatchers.IO) {
                getProviders(packageName)
            }

            val visibility = if (providers.isEmpty()) View.GONE else View.VISIBLE
            adapter.setData(providers.toList())
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
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                getProviders(packageName)
                            }
                        }
                        adapter.setData(providers.toList())
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
            val filteredProviders = getFilteredProviders(searchText)
            adapter.setData(filteredProviders)
        } else {
            val packageName = arguments?.getString("packageName") ?: return
            val providers = getProviders(packageName)
            adapter.setData(providers.toList())
        }
        adapter.setSearchText(searchText)
    }

    private fun getFilteredProviders(searchText: String): List<ProviderInfo> {
        val packageName = arguments?.getString("packageName") ?: return emptyList()
        val allProviders = getProviders(packageName)
        val packageManager = requireContext().packageManager
        return allProviders.filter { provider ->
            val providerLabel = provider.loadLabel(packageManager).toString()
            provider.name.contains(searchText, ignoreCase = true)
                    || providerLabel.contains(searchText, ignoreCase = true)
        }
    }

    private fun getProviders(packageName: String): List<ProviderInfo> {
        return try {
            val packageInfo = requireContext().packageManager.getPackageInfo(
                packageName, PackageManager.GET_PROVIDERS
            )
            val providers = packageInfo.providers ?: emptyArray()
            providers.sortedBy { it.name }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            emptyList()
        }
    }

    class AdapterComponents(
        private val context: Context,
        var providerList: List<ProviderInfo>,
    ) : RecyclerView.Adapter<AdapterComponents.ViewHolder>() {

        private var searchText: String = ""
//        private var diffCallback: DiffCallback<ProviderInfo>? = null

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
            val providerInfo = providerList[position]
            holder.bind(providerInfo)
        }

        override fun onViewAttachedToWindow(holder: ViewHolder) {
            super.onViewAttachedToWindow(holder)
            setFadeAnimation(holder.itemView)
        }

        override fun getItemCount(): Int {
            return providerList.size
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setData(newData: List<ProviderInfo>) {
//            diffCallback?.let {
//                val result = DiffUtil.calculateDiff(it)
//                providerList = newData
//                result.dispatchUpdatesTo(this)
//            }
//            diffCallback = DiffCallback(providerList, newData)
            providerList = newData
            notifyDataSetChanged()
        }


        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
            private val providerIcon: ImageView = itemView.findViewById(R.id.componentIcon)
            private val providerLabelTextView: TextView = itemView.findViewById(R.id.componentLabel)
            private val providerNameTextView: TextView = itemView.findViewById(R.id.componentName)
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
                val providerInfo = providerList[bindingAdapterPosition]

                MessageDialog.show(null, null,  "确定", null)
                    .setDialogLifecycleCallback(object : BottomDialogSlideEventLifecycleCallback<MessageDialog>() {
                        override fun onShow(dialog: MessageDialog) {
                            super.onShow(dialog)
                            dialog.dialogImpl.txtDialogTip.setPadding(0, 12, 0, 0)
                        }
                    })
                    .setCustomView(object : OnBindView<MessageDialog>(R.layout.dialog_components_detail) {
                        override fun onBind(dialog: MessageDialog, view: View) {

                            fun setTextAndColor(textView: TextView, value: Boolean) {
                                textView.text = value.toString()
                                textView.setTextColor(
                                    ContextCompat.getColor(
                                        context,
                                        if (value) R.color.green else R.color.red
                                    )
                                )
                            }

                            val enabledTextView = view.findViewById<TextView>(R.id.state_enable)
                            setTextAndColor(enabledTextView, providerInfo.enabled)

                            val exportedTextView = view.findViewById<TextView>(R.id.state_exported)
                            setTextAndColor(exportedTextView, providerInfo.exported)

                            view.findViewById<ImageView>(R.id.imageView_icon)
                                .setImageDrawable(providerInfo.loadIcon(context.packageManager))
                            view.findViewById<EditText>(R.id.edit_title)
                                .setText(providerInfo.loadLabel(context.packageManager))
                            view.findViewById<EditText>(R.id.edit_label).setText(providerInfo.name)
                        }
                    })

                    .setOkButton { dialog, v ->
                        false
                    }

            }

            fun bind(providerInfo: ProviderInfo) {
                val providerLabel = providerInfo.loadLabel(context.packageManager).toString()
                val providerName = providerInfo.name
                if (!providerInfo.exported) {
                    providerNameTextView.setTextColor(ContextCompat.getColor(context, R.color.red))
                } else {
                    providerNameTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.green
                        )
                    )
                }
                if (!providerInfo.enabled) {
                    providerLabelTextView.paintFlags = providerLabelTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                providerNameTextView.text = providerName.highlightText(searchText)
                providerLabelTextView.text = providerLabel.highlightText(searchText)

                componentStatus.isChecked = providerInfo.isEnabled
                providerIcon.setImageDrawable(providerInfo.loadIcon(context.packageManager))
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