package mapleleaf.materialdesign.engine.ui.activities

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.animator.AlphaInAnimation
import mapleleaf.materialdesign.engine.animator.BigToNormalAlpha
import mapleleaf.materialdesign.engine.animator.BottomToTopAlpha
import mapleleaf.materialdesign.engine.animator.BottomToTopBounce
import mapleleaf.materialdesign.engine.animator.EndToStartBounce
import mapleleaf.materialdesign.engine.animator.ItemAnimator
import mapleleaf.materialdesign.engine.animator.ScaleInAnimation
import mapleleaf.materialdesign.engine.animator.SlideInBottomAnimation
import mapleleaf.materialdesign.engine.animator.SlideInLeftAnimation
import mapleleaf.materialdesign.engine.animator.SlideInRightAnimation
import mapleleaf.materialdesign.engine.animator.SmallToNormalAlpha
import mapleleaf.materialdesign.engine.animator.StartToEndBounce
import mapleleaf.materialdesign.engine.animator.TopToBottomAlpha
import mapleleaf.materialdesign.engine.animator.TopToBottomBounce
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.utils.SearchTextWatcher
import mapleleaf.materialdesign.engine.utils.toast
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.io.File
import java.io.IOException
import java.text.Collator
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.log10
import kotlin.math.pow

class ActivityAllApplications : UniversalActivityBase(R.layout.activity_all_applications) {

    private var includeSystemApps: Boolean = true
    private var includeUserApps: Boolean = true
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterAllAppList
    private lateinit var viewModel: AppListViewModel
    private lateinit var appsSearchBox: EditText
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loading: AppCompatImageView
    private lateinit var buttonClear: AppCompatImageButton
    private lateinit var spinner: MaterialSpinner
    private var animatedVectorDrawable: AnimatedVectorDrawable? = null

    override fun initializeComponents(savedInstanceState: Bundle?) {
        recyclerView = findViewById(R.id.recyclerView)
        appsSearchBox = findViewById(R.id.apps_search_box)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        buttonClear = findViewById(R.id.buttonClear)
        spinner = findViewById(R.id.spinner)
        loading = findViewById(R.id.loading)
        val colorRed = ContextCompat.getColor(this, R.color.red1)
        val colorGreen = ContextCompat.getColor(this, R.color.lawngreen)
        val colorBlue = ContextCompat.getColor(this, R.color.blue)
        val colorOrange = ContextCompat.getColor(this, R.color.orange2)
        val progressColors = ContextCompat.getColor(this, R.color.swipe_refresh_layout_progress)
        swipeRefreshLayout.setColorSchemeColors(colorRed, colorGreen, colorBlue, colorOrange)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(progressColors)
        animatedVectorDrawable = AppCompatResources.getDrawable(
            this,
            R.drawable.progress_loading_manager
        ) as AnimatedVectorDrawable
        loading.setImageDrawable(animatedVectorDrawable)
        animatedVectorDrawable!!.start()
        swipeRefreshLayout.isEnabled = false
        appsSearchBox.isVisible = false

        setToolbarTitle(getString(R.string.toolbar_title_activity_application_manager))

        val materialCardView = findViewById<MaterialCardView>(R.id.materialCardView)
        val baseColor = ContextCompat.getColor(context, R.color.background)
        val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
        materialCardView.setCardBackgroundColor(
            ColorUtils.blendARGB(
                baseColor,
                primaryColor,
                0.15f
            )
        )

//        recyclerView.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = AdapterAllAppList(this@ActivityAllApplications, recyclerView).apply {
            // 打开 Adapter 的动画
            animationEnable = true
            // 是否是首次显示时候加载动画
            isAnimationFirstOnly = false
        }
        recyclerView.adapter = adapter
        FastScrollerBuilder(recyclerView).build()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            delay(100)
                            adapter.setScrolling(false)
                            adapter.loadVisibleIcons()
                        }
                    }

                    RecyclerView.SCROLL_STATE_DRAGGING, RecyclerView.SCROLL_STATE_SETTLING -> {
                        adapter.setScrolling(true)
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(100)
                    adapter.setScrolling(false)
                    adapter.loadVisibleIcons()
                }
                clearEditFocus()
            }
        })

        appsSearchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 不需要在这里做任何操作
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString() ?: ""
                buttonClear.visibility = if (searchText.isEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
                // 不需要在这里做任何操作
            }
        })

        buttonClear.setOnClickListener {
            appsSearchBox.text.clear()
        }

        viewModel = ViewModelProvider(this)[AppListViewModel::class.java]

        viewModel.apps.observe(this) { apps ->
            lifecycleScope.launch(Dispatchers.Main) {
                delay(300)
                adapter.setScrolling(false)
                adapter.loadVisibleIcons()
            }
            searchApp(appsSearchBox.text)

            adapter.setAppsList(apps)
            animatedVectorDrawable!!.stop()
            swipeRefreshLayout.isEnabled = true
            loading.isVisible = false
            appsSearchBox.isVisible = true
            swipeRefreshLayout.isRefreshing = false
        }

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.loadApps(this@ActivityAllApplications, includeSystemApps, includeUserApps)
        }

        swipeRefreshLayout.setOnRefreshListener {
            lifecycleScope.launch {
                clearEditFocus()
                val searchQuery = appsSearchBox.text.toString()
                if (searchQuery.isNotBlank()) {
                    searchApp(appsSearchBox.text)
                } else {
                    viewModel.loadApps(
                        this@ActivityAllApplications,
                        includeSystemApps,
                        includeUserApps
                    )
                }
                animatedVectorDrawable!!.stop()
                loading.isVisible = false
                swipeRefreshLayout.isRefreshing = false
            }
        }

        appsSearchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchApp(appsSearchBox.text)
            }
            true
        }
        val searchTextWatcher = SearchTextWatcher {
            searchApp(appsSearchBox.text)
        }
        appsSearchBox.addTextChangedListener(searchTextWatcher)

        initMenu()
    }

    private fun clearEditFocus() {
        appsSearchBox.clearFocus()
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // 在失去焦点后隐藏输入法键盘
        appsSearchBox.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                inputMethodManager.hideSoftInputFromWindow(appsSearchBox.windowToken, 0)
            }
        }
    }

    private fun searchApp(text: Editable?) {
        val searchText = text?.toString()?.trim() ?: ""
        if (searchText.isNotEmpty()) {
            val filteredList = viewModel.apps.value?.filter { appInfo ->
                appInfo.appName.contains(searchText, ignoreCase = true) ||
                        appInfo.packageName.contains(searchText, ignoreCase = true)
            }
            adapter.setAppsList(filteredList.orEmpty())
        } else {
            adapter.setAppsList(viewModel.apps.value.orEmpty())
        }
        adapter.setSearchText(searchText)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_all_applist, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.menu_instructions -> {
                showInstructionsDialog()
                return true
            }

            R.id.menu_filter -> {
                showFilterDialog()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("InflateParams")
    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter, null)
        val dialog = DialogHelper.customDialog(this, dialogView)
        dialogView.findViewById<TextView>(R.id.confirm_title).text = "筛选应用"

        val switchSystem = dialogView.findViewById<SwitchCompat>(R.id.switch_system)
        val switchUser = dialogView.findViewById<SwitchCompat>(R.id.switch_user)

        // 根据布尔值设置开关状态
        switchSystem.isChecked = includeSystemApps
        switchUser.isChecked = includeUserApps

        dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            if (!switchSystem.isChecked && !switchUser.isChecked) {
                // 显示提示信息
                toast("请选择至少一种类型")
                return@setOnClickListener
            }
            dialog.dismiss()
            // 直接修改类级别的成员变量
            includeSystemApps = switchSystem.isChecked
            includeUserApps = switchUser.isChecked

            lifecycleScope.launch {
                try {
                    loading.isVisible = true
                    animatedVectorDrawable?.start()

                    filterAppsByType(includeSystemApps, includeUserApps)
                    searchApp(appsSearchBox.text)
                } finally {
                    loading.isVisible = false
                    animatedVectorDrawable?.stop()
                }
            }
        }
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
    }

    private suspend fun filterAppsByType(includeSystemApps: Boolean, includeUserApps: Boolean) {
        viewModel.loadApps(this, includeSystemApps, includeUserApps)
    }

    @SuppressLint("InflateParams")
    private fun showInstructionsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_help_info, null)
        val dialog = DialogHelper.customDialog(this, dialogView)
        dialogView.findViewById<TextView>(R.id.confirm_title).text =
            getString(R.string.dialog_title)
        dialogView.findViewById<TextView>(R.id.confirm_message).text =
            getString(R.string.usage_instructions)

        dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()
        }
    }

    class AppListViewModel : ViewModel() {
        val apps = MutableLiveData<List<AppInfo>>()

        suspend fun loadApps(
            context: Context,
            includeSystemApps: Boolean,
            includeUserApps: Boolean,
        ) {
            val packageManager = context.packageManager
            val installedApps = withContext(Dispatchers.Default) {
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            }

            val filteredApps = installedApps.filter { appInfo ->
                val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                (isSystemApp && includeSystemApps) || (!isSystemApp && includeUserApps)
            }

            val newApps = withContext(Dispatchers.Default) {
                filteredApps.mapNotNull { appInfo ->
                    val packageInfo = packageManager.getPackageInfo(appInfo.packageName, 0)
                    val versionName = packageInfo.versionName
                    val size = getApkSize(context, appInfo.packageName)
                    AppInfo(
                        appInfo,
                        versionName,
                        size,
                        packageManager.getApplicationLabel(appInfo).toString(),
                        appInfo.packageName,
                        null
                    )
                }.sortedWith { app1, app2 ->
                    compareNames(
                        packageManager.getApplicationLabel(app1.applicationInfo).toString(),
                        packageManager.getApplicationLabel(app2.applicationInfo).toString()
                    )
                }
            }

            apps.postValue(newApps)
        }

        private fun getApkSize(context: Context, packageName: String): String {
            try {
                val packageInfo: PackageInfo = context.packageManager.getPackageInfo(packageName, 0)
                val apkPath = packageInfo.applicationInfo.sourceDir
                val apkFile = File(apkPath)
                val apkSize = apkFile.length()
                return formatFileSize(apkSize)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return "N/A"
        }

        private fun formatFileSize(size: Long): String {
            if (size <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
            return String.format(
                "%.2f %s",
                size / 1024.0.pow(digitGroups.toDouble()),
                units[digitGroups]
            )
        }

        private fun compareNames(name1: String, name2: String): Int {
            return if (name1[0].isDigit() && name2[0].isDigit()) {
                name1.compareTo(name2)
            } else if (name1[0].isDigit()) {
                -1
            } else if (name2[0].isDigit()) {
                1
            } else {
                val collator = Collator.getInstance(Locale.getDefault())
                collator.compare(name1, name2)
            }
        }
    }

    /**
     * Init menu
     * 初始化下拉菜单
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun initMenu() {
        spinner.setItems(
            "渐变进入",
            "从小到正常平移",
            "从底部平移",
            "从左边平移",
            "从右边平移",
            "从小到正常渐入",
            "从大到正常渐入",
            "从左到右弹跳",
            "从右到左弹跳",
            "从下到上渐入",
            "从上到下渐入",
            "从下到上弹跳",
            "从上到下弹跳"
        )

        spinner.setOnItemSelectedListener { _, position, _, _ ->
            when (position) {
                0 -> adapter.setItemAnimation(AdapterAllAppList.AnimationType.AlphaIn)
                1 -> adapter.setItemAnimation(AdapterAllAppList.AnimationType.ScaleIn)
                2 -> adapter.setItemAnimation(AdapterAllAppList.AnimationType.SlideInBottom)
                3 -> adapter.setItemAnimation(AdapterAllAppList.AnimationType.SlideInLeft)
                4 -> adapter.setItemAnimation(AdapterAllAppList.AnimationType.SlideInRight)
                5 -> adapter.setItemAnimation(AdapterAllAppList.AnimationType.SmallToNormalAlpha)
                6 -> adapter.setItemAnimation(AdapterAllAppList.AnimationType.BigToNormalAlpha)
                7 -> adapter.setItemAnimation(AdapterAllAppList.AnimationType.StartToEndBounce)
                8 -> adapter.setItemAnimation(AdapterAllAppList.AnimationType.EndToStartBounce)
                9 -> adapter.setItemAnimation(AdapterAllAppList.AnimationType.BottomToTopAlpha)
                10 -> adapter.setItemAnimation(AdapterAllAppList.AnimationType.TopToBottomAlpha)
                11 -> adapter.setItemAnimation(AdapterAllAppList.AnimationType.BottomToTopBounce)
                12 -> adapter.setItemAnimation(AdapterAllAppList.AnimationType.TopToBottomBounce)
                else -> {}
            }
            adapter.notifyDataSetChanged()

        }
    }

    open class AdapterAllAppList(
        private val context: Context,
        private val recyclerView: RecyclerView,
    ) : RecyclerView.Adapter<AdapterAllAppList.AllApplicationViewHolder>() {

        private var isScrolling = false
        private val coroutineScope = CoroutineScope(Dispatchers.Main)
        private val iconCache: MutableMap<String, Drawable?> = HashMap()
        private var searchText: String = ""
        private var appsList: MutableList<AppInfo> = mutableListOf()
        private var mLastPosition = -1

        /**
         * Whether enable animation.
         * 是否打开动画
         */
        var animationEnable: Boolean = false

        /**
         * Whether the animation executed only the first time.
         * 动画是否仅第一次执行
         */
        var isAnimationFirstOnly = true

        /**
         * Set custom animation.
         * 设置自定义动画
         */
        private var itemAnimation: ItemAnimator? = null
            set(value) {
                animationEnable = true
                field = value
            }

        @SuppressLint("NotifyDataSetChanged")
        fun setSearchText(text: String) {
            searchText = text
            notifyDataSetChanged()
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setAppsList(newAppsList: List<AppInfo>) {

            appsList.clear()
            appsList.addAll(newAppsList)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): AllApplicationViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_application_all, parent, false)
            return AllApplicationViewHolder(view, context)
        }

        override fun onBindViewHolder(holder: AllApplicationViewHolder, position: Int) {
            val appInfo = appsList[position]
            holder.bind(appInfo)

            holder.appIconImageView.setImageDrawable(null)
            val iconDrawable = iconCache[appInfo.packageName]
            if (iconDrawable != null) {
                holder.appIconImageView.setImageDrawable(iconDrawable)
                Log.d(
                    "Adapter",
                    "Using cached icon for position: $position, packageName: ${appInfo.packageName}"
                )
            } else {
                holder.appIconImageView.setImageDrawable(null)
                Log.d(
                    "Adapter",
                    "Loading icon for position: $position, packageName: ${appInfo.packageName}"
                )

                if (!isScrolling) {
                    coroutineScope.launch {
                        val icon = getAppIconAsync(context, appInfo.packageName)
                        if (holder.packageName == appInfo.packageName) {
                            holder.appIconImageView.alpha = 0f
                            holder.appIconImageView.setImageDrawable(icon)
                            holder.appIconImageView.animate().alpha(1f).setDuration(300).start()
                            iconCache[appInfo.packageName] = icon
                        }
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return appsList.size
        }

        override fun onViewAttachedToWindow(holder: AllApplicationViewHolder) {
            super.onViewAttachedToWindow(holder)
            runAnimator(holder)
        }

        inner class AllApplicationViewHolder(itemView: View, private val context: Context) :
            RecyclerView.ViewHolder(itemView) {
            private val appNameTextView: TextView = itemView.findViewById(R.id.app_name_text_view)
            private val appPackageTextView: TextView =
                itemView.findViewById(R.id.app_package_text_view)
            private val appVersionTextView: TextView =
                itemView.findViewById(R.id.app_version_text_view)
            private val appSizeTextView: TextView = itemView.findViewById(R.id.app_size_text_view)
            val appIconImageView: ImageView = itemView.findViewById(R.id.app_icon_image_view)
            private val appMaterialCardView: MaterialCardView =
                itemView.findViewById(R.id.app_material_cardView)
            var packageName: String? = null

            init {
                val materialCardView =
                    itemView.findViewById<MaterialCardView>(R.id.app_material_cardView)
                val baseColor = ContextCompat.getColor(context, R.color.background)
                val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
                materialCardView.setCardBackgroundColor(
                    ColorUtils.blendARGB(
                        baseColor,
                        primaryColor,
                        0.15f
                    )
                )
                appMaterialCardView.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val selectedApp: AppInfo = appsList[position]
                        showAppDetailsDialog(selectedApp)
                    } else {
                        toast("应用已被删除")
                    }
                }
            }

            fun bind(appInfo: AppInfo) {
                appNameTextView.text = appInfo.appName.highlightText(searchText)
                appPackageTextView.text = appInfo.packageName.highlightText(searchText)
                appVersionTextView.text = appInfo.versionName
                appSizeTextView.text = getApkSize(context, appInfo.packageName)
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

        @SuppressLint("InflateParams")
        private fun showAppDetailsDialog(appInfo: AppInfo) {
            val packageManager = context.packageManager
            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.dialog_all_app_operation, null)
            val dialog = DialogHelper.customDialog(context, dialogView)
            dialogView.findViewById<TextView>(R.id.appName).text = appInfo.appName
            dialogView.findViewById<TextView>(R.id.appPackage).text = appInfo.packageName
            dialogView.findViewById<TextView>(R.id.appVersionName).text = appInfo.versionName
            dialogView.findViewById<TextView>(R.id.appSize).text =
                getApkSize(context, appInfo.packageName)

            val appIcon = packageManager.getApplicationIcon(appInfo.packageName)
            dialogView.findViewById<ImageView>(R.id.appIcon).setImageDrawable(appIcon)

            dialogView.findViewById<View>(R.id.buttonOpen).setOnClickListener {
                dialog.dismiss()
                val packageName = appInfo.packageName
                packageManager.getLaunchIntentForPackage(packageName)?.also { intent ->
                    context.startActivity(intent)
                } ?: toast("无法打开该应用程序")

            }
            dialogView.findViewById<View>(R.id.buttonDetails).setOnClickListener {
                dialog.dismiss()
                Intent(context, ActivityApplicationDetails::class.java).apply {
                    putExtra("packageName", appInfo.packageName)
                }.also { context.startActivity(it) }

            }
            dialogView.findViewById<View>(R.id.buttonUninstall).setOnClickListener {
                dialog.dismiss()
                uninstallApp(appInfo.packageName)
            }
            dialogView.findViewById<View>(R.id.buttonShare).setOnClickListener {
                dialog.dismiss()
                shareApp(appInfo.packageName)
            }
            dialogView.findViewById<View>(R.id.buttonSystemDetails).setOnClickListener {
                dialog.dismiss()
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + appInfo.packageName)
                ).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(it)
                }

            }
            dialogView.findViewById<View>(R.id.buttonSaveApk).setOnClickListener {
                dialog.dismiss()
                CoroutineScope(Dispatchers.Main).launch {
                    saveApkToLocal(appInfo.packageName)
                }
            }
            dialogView.findViewById<View>(R.id.buttonComponent).setOnClickListener {
                dialog.dismiss()
                Intent(context, ActivityAppComponents::class.java).apply {
                    putExtra("packageName", appInfo.packageName)
                }.also { context.startActivity(it) }

            }
            dialogView.findViewById<View>(R.id.buttonDex).setOnClickListener {
                dialog.dismiss()
                Intent(context, ActivityApplicationDex::class.java).apply {
                    putExtra("packageName", appInfo.packageName)
                }.also { context.startActivity(it) }

            }
            dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
            }
        }

        private fun uninstallApp(packageName: String) {
            Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName")).also { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        private fun shareApp(packageName: String) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                    val apkFilePath: String = appInfo.sourceDir
                    val apkUri = FileProvider.getUriForFile(
                        context, context.packageName + ".shareAPK", File(apkFilePath)
                    )
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, apkUri)
                        type = "*/*"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(Intent.createChooser(this, "分享应用"))
                    }

                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        toast("无法获取应用信息")
                    }
                }
            }
        }

        @SuppressLint("InflateParams")
        private suspend fun saveApkToLocal(packageName: String) {
            coroutineScope {
                launch(Dispatchers.Main) {
                    val layoutInflater = LayoutInflater.from(context)
                    val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
                    val textView = dialogView.findViewById<TextView>(R.id.dialog_text)
                    val progressBar =
                        dialogView.findViewById<ProgressBar>(R.id.dialog_app_details_progress)
                    progressBar.isVisible = true

                    val alert = DialogHelper.customDialog(context, dialogView)

                    withContext(Dispatchers.IO) {
                        try {
                            val packageInfo: PackageInfo =
                                context.packageManager.getPackageInfo(packageName, 0)
                            val apkPath = packageInfo.applicationInfo.sourceDir
                            val sourceFile = File(apkPath)
                            val destinationFile =
                                File(context.getExternalFilesDir(null), "${packageName}.apk")

                            withContext(Dispatchers.Main) {
                                progressBar.isVisible = true
                                textView.text = "正在保存安装包..."
                            }

                            sourceFile.copyTo(destinationFile, true)

                            withContext(Dispatchers.Main) {
                                progressBar.isVisible = false
                                alert.dismiss()
                                toast("安装包已保存到 ${destinationFile.absolutePath}")
                            }
                        } catch (e: PackageManager.NameNotFoundException) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                toast("无法获取应用信息")
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                toast("保存安装包失败")
                            }
                        }
                    }
                }
            }
        }

        fun setScrolling(scrolling: Boolean) {
            isScrolling = scrolling
            Log.d("Adapter", "Scrolling state changed: $scrolling")
            if (!scrolling) {
                coroutineScope.launch {
                    Log.d("Adapter", "Loading visible icons...")
                    loadIconForVisibleItems()
                }
            }
        }

        fun loadVisibleIcons() {
            Log.d("Adapter", "Loading visible icons...")
            coroutineScope.launch {
                loadIconForVisibleItems()
            }
        }

        private suspend fun loadIconForVisibleItems() {
            if (!isScrolling) {
                recyclerView.layoutManager?.let { layoutManager ->
                    val firstVisibleItemPosition =
                        (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                    for (position in firstVisibleItemPosition..lastVisibleItemPosition) {
                        if (position >= 0 && position < appsList.size) { // 添加索引检查
                            val appInfo = appsList[position]
                            if (!iconCache.containsKey(appInfo.packageName)) {
                                val holder =
                                    recyclerView.findViewHolderForAdapterPosition(position) as? AllApplicationViewHolder
                                holder?.let {
                                    Log.d(
                                        "Adapter",
                                        "Loading icon for position: $position, packageName: ${appInfo.packageName}"
                                    )
                                    val icon = getAppIconAsync(context, appInfo.packageName)
                                    iconCache[appInfo.packageName] = icon
                                    holder.appIconImageView.alpha = 0f
                                    holder.appIconImageView.setImageDrawable(icon)
                                    holder.appIconImageView.animate().alpha(1f).setDuration(300)
                                        .start()
                                }
                            }
                        }
                    }
                }
            }
        }

        private suspend fun getAppIconAsync(context: Context, packageName: String): Drawable? =
            withContext(Dispatchers.Default) {
                val pm = context.packageManager
                try {
                    val icon = pm.getApplicationIcon(packageName)
                    Log.d("Adapter", "Icon retrieved successfully for packageName: $packageName")
                    icon
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                    Log.e(
                        "Adapter",
                        "Failed to retrieve icon for packageName: $packageName, error: ${e.message}"
                    )
                    null
                }
            }

        private fun getApkSize(context: Context, packageName: String): String {
            try {
                val packageInfo: PackageInfo = context.packageManager.getPackageInfo(packageName, 0)
                val apkPath = packageInfo.applicationInfo.sourceDir
                val apkFile = File(apkPath)
                val apkSize = apkFile.length()
                return formatFileSize(apkSize)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return "N/A"
        }

        private fun formatFileSize(size: Long): String {
            if (size <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
            return String.format(
                "%.2f %s",
                size / 1024.0.pow(digitGroups.toDouble()),
                units[digitGroups]
            )
        }

        /**
         * run animation when you want to show time
         *
         * @param holder
         */
        private fun runAnimator(holder: RecyclerView.ViewHolder) {
            if (animationEnable) {
                if (!isAnimationFirstOnly || holder.layoutPosition > mLastPosition) {
                    val animation: ItemAnimator = itemAnimation ?: AlphaInAnimation()
                    animation.animator(holder.itemView).apply {
                        startItemAnimator(this, holder)
                    }
                    mLastPosition = holder.layoutPosition
                }
            }
        }

        /**
         * start executing animation
         * override this method to execute more actions
         * 开始执行动画方法
         * 可以重写此方法，实行更多行为
         *
         * @param anim
         * @param holder
         */
        protected open fun startItemAnimator(anim: Animator, holder: RecyclerView.ViewHolder) {
            anim.start()
        }

        /**
         * 内置默认动画类型
         */
        enum class AnimationType {
            AlphaIn, ScaleIn, SlideInBottom, SlideInLeft, SlideInRight, SmallToNormalAlpha, BigToNormalAlpha, StartToEndBounce, EndToStartBounce, BottomToTopAlpha, TopToBottomAlpha, BottomToTopBounce, TopToBottomBounce
        }

        /**
         * use preset animations
         * 使用内置默认动画设置
         * @param animationType AnimationType
         */
        fun setItemAnimation(animationType: AnimationType) {
            itemAnimation = when (animationType) {
                AnimationType.AlphaIn -> AlphaInAnimation()
                AnimationType.ScaleIn -> ScaleInAnimation()
                AnimationType.SlideInBottom -> SlideInBottomAnimation()
                AnimationType.SlideInLeft -> SlideInLeftAnimation()
                AnimationType.SlideInRight -> SlideInRightAnimation()
                AnimationType.SmallToNormalAlpha -> SmallToNormalAlpha()
                AnimationType.BigToNormalAlpha -> BigToNormalAlpha()
                AnimationType.StartToEndBounce -> StartToEndBounce()
                AnimationType.EndToStartBounce -> EndToStartBounce()
                AnimationType.BottomToTopAlpha -> BottomToTopAlpha()
                AnimationType.TopToBottomAlpha -> TopToBottomAlpha()
                AnimationType.BottomToTopBounce -> BottomToTopBounce()
                AnimationType.TopToBottomBounce -> TopToBottomBounce()
            }
        }
    }

    data class AppInfo(
        val applicationInfo: ApplicationInfo,
        val versionName: String,
        val size: String,
        val appName: String,
        val packageName: String,
        var icon: Drawable?,
    )
}