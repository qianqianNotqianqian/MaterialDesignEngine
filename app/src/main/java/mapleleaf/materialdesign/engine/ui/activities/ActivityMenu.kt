package mapleleaf.materialdesign.engine.ui.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.ColorUtils
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.store.SpfConfig
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.ui.dialog.DialogMonitor
import mapleleaf.materialdesign.engine.ui.dialog.DialogPower
import mapleleaf.materialdesign.engine.utils.helper.MDEngineHelpers
import mapleleaf.materialdesign.engine.utils.openUrlByBrowser
import mapleleaf.materialdesign.engine.utils.toast
import me.zhanghai.android.fastscroll.FastScrollNestedScrollView
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import xzr.perfmon.ActivityPerfmonPlus
import java.io.File
import java.lang.reflect.Method
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLHandshakeException
import kotlin.math.abs
import kotlin.random.Random

class ActivityMenu : UniversalActivityBase(R.layout.activity_menu) {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var adapter: IconAdapter

    @SuppressLint("InflateParams", "SetTextI18n", "CutPasteId")
    override fun initializeComponents(savedInstanceState: Bundle?) {
        val scrollingView = findViewById<FastScrollNestedScrollView>(R.id.nestedScrollView)
        FastScrollerBuilder(scrollingView).build()
        drawerLayout = findViewById(R.id.drawer_layout)

        val appBarLayout = findViewById<AppBarLayout>(R.id.appBarLayout)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            getToolbar(),
            0,
            0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.text_color)

//        DialogX.globalTheme = DialogX.THEME.AUTO
//        DialogX.globalStyle = MaterialYouStyle.style()

        setNavigationViewMenuItem(
            R.id.item_download_project,
            R.drawable.ic_github,
            R.string.visit_github
        ) {
            openUrlByBrowser(
                this@ActivityMenu,
                "https://github.com/qianqianNotqianqian/MaterialDesignEngine"
            )
        }
        setNavigationViewMenuItem(
            R.id.item_app_manager,
            R.drawable.ic_app_manager,
            R.string.application_manager
        ) {
            this@ActivityMenu.startActivity(
                Intent(
                    this@ActivityMenu,
                    ActivityAllApplications::class.java
                )
            )
        }
        setNavigationViewMenuItem(
            R.id.item_support,
            R.drawable.ic_support_developer,
            R.string.menu_action_support_developers
        ) {
            openUrlByBrowser(
                this@ActivityMenu,
                "https://github.com/qianqianNotqianqian/MaterialDesignEngine/issues"
            )
        }
        setNavigationViewMenuItem(
            R.id.item_feedback,
            R.drawable.ic_support_feedback,
            R.string.menu_action_feedback
        ) {
            openUrlByBrowser(
                this@ActivityMenu,
                "https://github.com/qianqianNotqianqian/MaterialDesignEngine/issues"
            )
        }
        setNavigationViewMenuItem(
            R.id.item_about,
            R.drawable.ic_support_development,
            R.string.about_this
        ) {
            MDEngineHelpers.showAbout(this)
        }
        setNavigationViewMenuItem(
            R.id.item_share,
            R.drawable.ic_share_this_app,
            R.string.menu_share
        ) {
            try {
                val appInfo: ApplicationInfo = packageManager.getApplicationInfo(packageName, 0)
                val apkFilePath: String = appInfo.sourceDir
                val apkUri: Uri =
                    FileProvider.getUriForFile(this, "$packageName.shareAPK", File(apkFilePath))
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, apkUri)
                    type = "*/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(sendIntent, "分享应用"))
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("包管理", "PackageManager.NameNotFoundException occurred", e)
                toast(getString(R.string.menu_get_app_information_failed))
            }
        }
        setNavigationViewMenuItem(
            R.id.item_perfmon_plus,
            R.drawable.ic_perfmon,
            R.string.perfmon_plus
        ) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_info, null)
            val dialog = DialogHelper.customDialog(this, dialogView)

            dialogView.findViewById<TextView>(R.id.confirm_message).text = getString(R.string.menu_confirm_perfmon_plus)

            dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
                dialog.dismiss()
                val intent = Intent(this@ActivityMenu, ActivityPerfmonPlus::class.java)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this@ActivityMenu,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                ActivityCompat.startActivity(this@ActivityMenu, intent, options.toBundle())
            }
            dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
            }
        }

        setNavigationViewMenuItem(R.id.item_sex, R.drawable.ic_image, R.string.sex_picture) {
            val random = Random.Default
            val num1 = random.nextInt(100)
            var num2 = random.nextInt(100)
            val operation = random.nextInt(4)
            val operationSymbol = when (operation) {
                0 -> "+"
                1 -> "-"
                2 -> "*"
                3 -> "/"
                else -> ""
            }

            if (operationSymbol == "/" && num2 == 0) {
                do {
                    num2 = random.nextInt(100)
                } while (num2 == 0)
            }
            val answer = calculateAnswer(num1, num2, operationSymbol)

            val dialogView = layoutInflater.inflate(R.layout.dialog_math, null)
            val dialog = DialogHelper.customDialog(this, dialogView)
            dialogView.findViewById<TextView>(R.id.confirm_title).text =
                getString(R.string.dialog_title)
            dialogView.findViewById<TextView>(R.id.confirm_message).text =
                "$num1$operationSymbol$num2 = ${String.format("%.2f", answer)}"
            val answerInputEdit = dialogView.findViewById<EditText>(R.id.answer_input_edit)

            dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
                val inputStr = answerInputEdit.text.toString()
                if (inputStr.isNotEmpty()) {
                    try {
                        val userAnswer = inputStr.toDouble()
                        val correctAnswer = calculateAnswer(num1, num2, operationSymbol)
                        if (abs(userAnswer - correctAnswer) < 0.01) {
                            toast(getString(R.string.menu_input_success) + userAnswer)

                            startActivity(
                                Intent(
                                    this,
                                    ActivityWallpaper::class.java
                                ).putExtra("isAnswerCorrect", userAnswer)
                            )
                            dialog.dismiss()
                        } else {
                            toast(getString(R.string.menu_input_incorrect_answer))
                        }
                    } catch (e: NumberFormatException) {
                        toast(getString(R.string.menu_input_number))
                    }
                } else {
                    toast(getString(R.string.menu_input_answer))
                }
            }
            dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
            }

        }
        setNavigationViewMenuItem(R.id.item_sex_line, R.drawable.ic_pixiv, R.string.sex_sentance) {

            val dialogView = layoutInflater.inflate(R.layout.dialog_sex_sentence, null)
            val dialog = DialogHelper.customDialog(this, dialogView)
            dialogView.findViewById<TextView>(R.id.confirm_title).text =
                getString(R.string.sex_sentance)
            val messageTextView = dialogView.findViewById<TextView>(R.id.confirm_message)
            val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)

            dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
                dialog.dismiss()
            }
            progressBar.isIndeterminate = true
            progressBar.isVisible = true

            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()

            val job = CoroutineScope(Dispatchers.Main).launch {
                try {
                    val responseBody = withContext(Dispatchers.IO) {
                        val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
                        val body = "format=json".toRequestBody(mediaType)
                        val request = Request.Builder()
                            .url("https://api.2su.cc/api/101/")
                            .post(body)
                            .build()

                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful && response.body != null) {
                                response.body!!.string()
                            } else {
                                null
                            }
                        }
                    }

                    if (!responseBody.isNullOrEmpty()) {
                        val jsonObject = JSONObject(responseBody)
                        if (jsonObject.optInt("code") == 200) {
                            val newsList = jsonObject.optJSONObject("newslist")
                            val content = newsList?.optString("content")
                            messageTextView.text = content
                            progressBar.isVisible = false
                        }
                    }
                } catch (e: Exception) {
                    Log.e("网络请求", "网络请求中出现异常", e)
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                delay(10000)
                if (job.isActive) {
                    job.cancel()
                    Log.e("网络请求", "请求超时")
                    // 这里可以添加超时后的处理逻辑
                }
            }
        }

        setNavigationViewMenuItem(
            R.id.item_dialog_style,
            R.drawable.ic_tab_system_app,
            R.string.dialog_style
        ) {
            this@ActivityMenu.startActivity(
                Intent(
                    this@ActivityMenu,
                    ActivityDialogStyle::class.java
                )
            )
        }

        setNavigationViewMenuItem(R.id.item_exit, R.drawable.ic_exit, R.string.menu_action_exit) {

            val dialogView = layoutInflater.inflate(R.layout.dialog_exit, null)
            val dialog = DialogHelper.customDialog(this, dialogView)
            dialogView.findViewById<TextView>(R.id.confirm_title).text =
                getString(R.string.dialog_title)
            val messageTextView = dialogView.findViewById<TextView>(R.id.confirm_message)
            messageTextView.text = getString(R.string.menu_action_confirm_exit)

            dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
            }
            dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
                dialog.dismiss()
                finish()
            }

        }

        setNavigationViewMenuItem(
            R.id.item_bilibili,
            R.drawable.ic_bilibili,
            R.string.visit_bilibili
        ) {
            openUrlByBrowser(
                this@ActivityMenu,
                "https://space.bilibili.com/1644220257?plat_id=1&share_from=space&share_medium=android&share_plat=android&share_session_id=1c322cf4-e901-43dd-8afd-5cbe42c88f03&share_source=COPY&share_tag=s_i&timestamp=1712632580&unique_k=VQ4DzmX"
            )
        }
        setNavigationViewMenuItem(R.id.item_acfun, R.drawable.ic_acfun, R.string.visit_acfun) {
            openUrlByBrowser(
                this@ActivityMenu,
                "https://www.acfun.cn/"
            )
        }

        val menuItems = arrayOf(
            MenuItemInfo(
                R.id.Browser,
                R.drawable.ic_bilibili,
                R.string.menu_start_browser,
                ActivityBrowser::class.java
            ),
            MenuItemInfo(
                R.id.Accelerometer,
                R.drawable.ic_bilibili,
                R.string.menu_start_accelerometer,
                ActivityAccelerometer::class.java
            ),
            MenuItemInfo(
                R.id.AppManager,
                R.drawable.ic_bilibili,
                R.string.menu_start_application_manager,
                ActivityApplications::class.java
            ),
            MenuItemInfo(
                R.id.DeskClock,
                R.drawable.ic_bilibili,
                R.string.menu_start_desk_clock,
                ActivityDeskClock::class.java
            ),
            MenuItemInfo(
                R.id.Functions,
                R.drawable.ic_bilibili,
                R.string.menu_start_functions,
                ActivityFunctions::class.java
            ),
            MenuItemInfo(
                R.id.SystemVibration,
                R.drawable.ic_bilibili,
                R.string.menu_start_system_vibration,
                ActivitySystemVibration::class.java
            ),
            MenuItemInfo(
                R.id.SystemInformation,
                R.drawable.ic_bilibili,
                R.string.menu_start_system_information,
                ActivitySystemInfo::class.java
            ),
            MenuItemInfo(
                R.id.SystemSensorInfo,
                R.drawable.ic_bilibili,
                R.string.menu_start_system_sensors,
                ActivitySystemSensors::class.java
            ),
            MenuItemInfo(
                R.id.SystemIconStyle,
                R.drawable.ic_bilibili,
                R.string.menu_start_system_icons,
                ActivitySystemIcons::class.java
            ),
            MenuItemInfo(
                R.id.SystemProcess,
                R.drawable.ic_bilibili,
                R.string.menu_start_system_process,
                ActivityProcess::class.java
            ),
            MenuItemInfo(
                R.id.SystemSundry,
                R.drawable.ic_bilibili,
                R.string.menu_start_system_sundry,
                ActivitySystemModify::class.java
            ),
            MenuItemInfo(
                R.id.SystemOverView,
                R.drawable.ic_bilibili,
                R.string.menu_start_system_overview,
                ActivityCheckPermission::class.java
            ),
            MenuItemInfo(
                R.id.SystemCpuControl,
                R.drawable.ic_bilibili,
                R.string.menu_start_system_cpu_control,
                ActivityCpuControl::class.java
            ),
            MenuItemInfo(
                R.id.Colors,
                R.drawable.ic_bilibili,
                R.string.menu_start_colors,
                ActivityColors::class.java
            ),
            MenuItemInfo(
                R.id.J2box,
                R.drawable.ic_bilibili,
                R.string.menu_start_j2box,
                ActivityJ2box::class.java
            ),
            MenuItemInfo(
                R.id.Text2MD5,
                R.drawable.ic_bilibili,
                R.string.menu_start_text2md5,
                ActivityText2MD5::class.java
            ),
            MenuItemInfo(
                R.id.TTS,
                R.drawable.ic_bilibili,
                R.string.menu_start_tts,
                ActivityTTS::class.java
            ),
            MenuItemInfo(
                R.id.Music,
                R.drawable.ic_bilibili,
                R.string.menu_start_music,
                ActivityMusic::class.java
            ),
            MenuItemInfo(
                R.id.AnimeQuotes,
                R.drawable.ic_bilibili,
                R.string.menu_start_anime_quotes,
                ActivityAnimeQuotes::class.java
            ),
            MenuItemInfo(
                R.id.RPG,
                R.drawable.ic_bilibili,
                R.string.menu_start_rpg,
                ActivityRandomPersonality::class.java
            ),
            MenuItemInfo(
                R.id.HotSearch,
                R.drawable.ic_bilibili,
                R.string.menu_start_hot_search,
                ActivityHotSearch::class.java
            ),
            MenuItemInfo(
                R.id.InfiniteList,
                R.drawable.ic_bilibili,
                R.string.menu_start_infinite_list,
                ActivityInfiniteList::class.java
            ),
            MenuItemInfo(
                R.id.BilibiliParse,
                R.drawable.ic_bilibili,
                R.string.menu_start_bilibili_parse,
                ActivityBilibiliParse::class.java
            ),
            MenuItemInfo(
                R.id.Dictionary,
                R.drawable.ic_bilibili,
                R.string.menu_start_dictionary,
                ActivityDictionary::class.java
            ),
            MenuItemInfo(
                R.id.MarqueeView,
                R.drawable.ic_bilibili,
                R.string.menu_start_marquee_view,
                ActivityMarqueeView::class.java
            )
        )

        for (menuItem in menuItems) {
            setMenuItem(menuItem)
        }

        contractAction()
        loadYiYanData()
        loadHeaderImage()
        loadDate()

        getToolbar().setBackgroundColor(getColor(R.color.transparent))
        setToolbarTitle(getString(R.string.toolbar_title_activity_start))
        setToolbarSubtitle(getString(R.string.toolbar_title_activity_dream_beginning))

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    moveTaskToBack(true)
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        val baseColor = ContextCompat.getColor(context, R.color.background_color)
        val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
        val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)
        appBarLayout.setBackgroundColor(blendedColor)

    }

    private fun loadDate() {
        val calendar = Calendar.getInstance()
        val day = calendar[Calendar.DAY_OF_MONTH]
        val week = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
        val month = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)

        findViewById<TextView>(R.id.item_now_day)?.text = day.toString()
        findViewById<TextView>(R.id.item_now_week_tv)?.text = week
        findViewById<TextView>(R.id.item_now_month_tv)?.text = month
    }

    private fun setNavigationViewMenuItem(
        itemId: Int,
        iconResId: Int,
        textResId: Int,
        onClickListener: () -> Unit,
    ) {
        val itemView = findViewById<View>(itemId)
        val imageView = itemView.findViewById<AppCompatImageView>(R.id.ivIcon)
        val textView = itemView.findViewById<MaterialTextView>(R.id.tvText)

        imageView.setImageResource(iconResId)
        textView.setText(textResId)
        itemView.setOnClickListener { onClickListener.invoke() }
    }

    private fun setMenuItem(menuItemInfo: MenuItemInfo) {
        val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
        findViewById<View>(menuItemInfo.itemId).apply {
            findViewById<MaterialCardView>(menuItemInfo.itemId).apply {
                val baseColor = ContextCompat.getColor(context, R.color.background)
                setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.15f))
            }
            findViewById<AppCompatImageView>(R.id.menuIcon).setImageResource(menuItemInfo.iconResId)
            findViewById<MaterialTextView>(R.id.menuText).apply {
                setText(menuItemInfo.textResId)
                val baseColor = ContextCompat.getColor(context, R.color.text_color)
                setTextColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.2f))
            }
            setOnClickListener {
                context.startActivity(Intent(context, menuItemInfo.activityClass))
            }
        }
    }

    data class MenuItemInfo(
        val itemId: Int,
        val iconResId: Int,
        val textResId: Int,
        val activityClass: Class<*>,
    )

    private fun loadHeaderImage() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .build()
                    val url = "https://imgapi.cn/api.php?fl=dongman&gs=json"
                    val request = Request.Builder()
                        .url(url)
                        .get()
                        .build()

                    val response = client.newCall(request).execute()
                    try {
                        if (response.isSuccessful) {
                            val myResponse = response.body?.string()
                            try {
                                val jsonResponse = JSONObject(myResponse ?: "")
                                val imageUrl = jsonResponse.getString("imgurl")

                                withContext(Dispatchers.Main) {
                                    val imageView =
                                        findViewById<AppCompatImageView>(R.id.nav_header_cover)
                                    Glide.with(applicationContext)
                                        .load(imageUrl)
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .into(object : CustomTarget<Drawable>() {
                                            override fun onResourceReady(
                                                resource: Drawable,
                                                transition: Transition<in Drawable>?,
                                            ) {
                                                imageView.setImageDrawable(resource)
                                                val fadeInAnimation =
                                                    AlphaAnimation(0f, 1f).apply { duration = 320 }
                                                imageView.startAnimation(fadeInAnimation)

                                                fadeInAnimation.setAnimationListener(object :
                                                    Animation.AnimationListener {
                                                    override fun onAnimationStart(animation: Animation?) {}
                                                    override fun onAnimationRepeat(animation: Animation?) {}
                                                    override fun onAnimationEnd(animation: Animation?) {
                                                        val scaleAnimation = ScaleAnimation(
                                                            1f, 1.5f, 1f, 1.5f,
                                                            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                                                            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
                                                        ).apply {
                                                            duration = 10000
                                                            repeatMode = ScaleAnimation.REVERSE
                                                            repeatCount = ScaleAnimation.INFINITE
                                                        }
                                                        imageView.startAnimation(scaleAnimation)
                                                    }
                                                })
                                            }

                                            override fun onLoadCleared(placeholder: Drawable?) {}
                                        })
                                }
                            } catch (e: JSONException) {
                                Log.e("解析JSON", "JSON解析失败", e)
                            }
                        } else {
                            Log.e("网络请求", "服务器响应失败")
                        }
                    } catch (e: Exception) {
                        Log.e("网络请求", "服务器响应失败", e)
                    } finally {
                        response.close()
                    }
                }
            } catch (e: Exception) {
                Log.e("网络请求", "网络请求中出现异常", e)
            }
        }
    }

    private fun loadYiYanData() {
        val mediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()
        val body = "format=json".toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://tenapi.cn/v2/yiyan")
            .post(body)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .build()

                    val call = client.newCall(request)
                    val response = call.execute()
                    if (isActive) {
                        try {
                            if (response.isSuccessful && response.body != null) {
                                val responseBody = response.body!!.string()
                                try {
                                    val jsonObject = JSONObject(responseBody)
                                    if (jsonObject.optInt("code") == 200) {
                                        val data = jsonObject.optJSONObject("data")
                                        val yiYan = data?.optString("hitokoto")
                                        withContext(Dispatchers.Main) {
                                            val itemZuiMeiContentTv =
                                                findViewById<TextView>(R.id.item_now_content_tv)
                                            itemZuiMeiContentTv.text = yiYan
                                        }
                                    } else {
                                        withContext(Dispatchers.IO) {
                                            toast("服务器返回错误码: ${jsonObject.optInt("code")}")
                                        }
                                    }
                                } catch (jsonException: JSONException) {
                                    Log.e("JSON解析", "JSON解析失败", jsonException)
                                    withContext(Dispatchers.IO) {
                                        toast("JSON解析失败")
                                    }
                                }
                            } else {
                                Log.e("网络请求", "服务器响应失败")
                                withContext(Dispatchers.IO) {
                                    toast("服务器响应失败")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("网络请求", "网络请求中出现异常", e)
                            withContext(Dispatchers.IO) {
                                toast("网络请求中出现异常")
                            }
                        } finally {
                            response.close()
                        }
                    }
                }
            } catch (sslHandshakeException: SSLHandshakeException) {
                withContext(Dispatchers.IO) {
                    toast("SSL握手失败")
                }
                Log.e("SSL握手失败", "SSL握手失败", sslHandshakeException)
            } catch (timeoutException: SocketTimeoutException) {
                if (isNetworkConnected(this@ActivityMenu)) {
                    loadYiYanData()
                } else {
                    withContext(Dispatchers.IO) {
                        // 否则提示无网络连接
                        toast("无网络连接")
                    }
                    Log.e("无网络", "无网络连接", timeoutException)
                }
            } catch (unknownHostException: UnknownHostException) {
                withContext(Dispatchers.IO) {
                    toast("无网络连接")
                }
                Log.e("无网络", "无网络连接", unknownHostException)
            } catch (e: Exception) {
                withContext(Dispatchers.IO) {
                    toast("网络请求中出现异常")
                }
                Log.e("网络请求", "网络请求中出现异常", e)
            }
        }
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    @SuppressLint("InflateParams")
    private fun contractAction() {
        val globalSPF = getSharedPreferences("GLOBAL_SPF", MODE_PRIVATE)
        val hasAgreed = globalSPF.getBoolean(SpfConfig.GLOBAL_SPF_CONTRACT, false)
        if (!hasAgreed) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_danger_agreement, null)
            val dialog = DialogHelper.customDialog(this, dialogView, false)
            val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)
            val agreement = dialogView.findViewById<CompoundButton>(R.id.agreement)
            val timer = Timer()
            var timeout = 30
            var clickItems = 0

            timer.schedule(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        if (timeout > 0) {
                            timeout--
                            btnConfirm.text = timeout.toString()
                        } else {
                            timer.cancel()
                            btnConfirm.setText(R.string.menu_agree_agreement)
                        }
                    }
                }
            }, 0, 1000)

            dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                timer.cancel()
                dialog.dismiss()
                finish()
                globalSPF.edit().putBoolean(SpfConfig.GLOBAL_SPF_CONTRACT, false).apply()
            }

            btnConfirm.setOnClickListener {
                if (!agreement.isChecked) {
                    return@setOnClickListener
                }
                if (timeout > 0 && clickItems < 9) {
                    clickItems++
                    return@setOnClickListener
                }
                timer.cancel()
                dialog.dismiss()
                globalSPF.edit().putBoolean(SpfConfig.GLOBAL_SPF_CONTRACT, true).apply()
                checkPermissions()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_start_select, menu)
        if (menu.javaClass.simpleName.equals("MenuBuilder", ignoreCase = true)) {
            try {
                val method: Method = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible",
                    Boolean::class.javaPrimitiveType
                )
                method.isAccessible = true
                method.invoke(menu, true)
            } catch (e: Exception) {
                Log.e("创建菜单", "Exception occurred in onCreateOptionsMenu", e)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("InflateParams")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.start_menu_graph -> {
                if (Settings.canDrawOverlays(this)) {
                    DialogMonitor(this).show()
                } else {
                    Intent().apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                        data = Uri.fromParts("package", this@ActivityMenu.packageName, null)
                    }
                    toast(getString(R.string.permission_float))
                }
                return true
            }

            R.id.start_menu_power -> {
                DialogPower(this).showPowerMenu()
                return true
            }

            R.id.start_menu_check_permission -> {
                checkPermissions()
                return true
            }

            R.id.start_menu_change_icon -> {
                val dialogView = layoutInflater.inflate(R.layout.layout_change_icons, null)
                val dialog = DialogHelper.customDialog(this, dialogView)
                dialogView.findViewById<TextView>(R.id.confirm_title).text =
                    getString(R.string.dialog_choose_icon)
                dialogView.findViewById<TextView>(R.id.confirm_message).text = "选择图标样式并切换"
                val recyclerView = dialogView.findViewById<RecyclerView>(R.id.iconRecyclerView)
                val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)

                progressBar.isVisible = true

                lifecycleScope.launch(Dispatchers.Main) {
                    delay(300)
                    recyclerView.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapter = IconAdapter(emptyList())
                    recyclerView.adapter = adapter

                    val loadedIcons = withContext(Dispatchers.Main) {
                        loadIcons()
                    }
                    adapter.updateIcons(loadedIcons)
                    progressBar.isVisible = false
                }

                dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                    dialog.dismiss()
                }

                dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
                    dialog.dismiss()
                    val selectedIcon = adapter.getSelectedIcon()
                    selectedIcon?.let {

                        toast("选中了 $selectedIcon")

                        val packageManager = applicationContext.packageManager

                        val selectedIconAlias = when (selectedIcon) {
                            "NoxIcon" -> "mapleleaf.materialdesign.engine.NoxIcon"
                            "PremiumIcon" -> "mapleleaf.materialdesign.engine.PremiumIcon"
                            "AquaIcon" -> "mapleleaf.materialdesign.engine.AquaIcon"
                            "TurboIcon" -> "mapleleaf.materialdesign.engine.TurboIcon"
                            "VintageIcon" -> "mapleleaf.materialdesign.engine.VintageIcon"
                            "NoxIconRound" -> "mapleleaf.materialdesign.engine.NoxIconRound"
                            "PremiumIconRound" -> "mapleleaf.materialdesign.engine.PremiumIconRound"
                            "AquaIconRound" -> "mapleleaf.materialdesign.engine.AquaIconRound"
                            "TurboIconRound" -> "mapleleaf.materialdesign.engine.TurboIconRound"
                            "VintageIconRound" -> "mapleleaf.materialdesign.engine.VintageIconRound"
                            "MainActivityIcon" -> "mapleleaf.materialdesign.engine.ui.activities.ActivityMenu"
                            else -> ""
                        }

                        val activityAliases = listOf(
                            "mapleleaf.materialdesign.engine.NoxIcon",
                            "mapleleaf.materialdesign.engine.PremiumIcon",
                            "mapleleaf.materialdesign.engine.AquaIcon",
                            "mapleleaf.materialdesign.engine.TurboIcon",
                            "mapleleaf.materialdesign.engine.VintageIcon",
                            "mapleleaf.materialdesign.engine.NoxIconRound",
                            "mapleleaf.materialdesign.engine.PremiumIconRound",
                            "mapleleaf.materialdesign.engine.AquaIconRound",
                            "mapleleaf.materialdesign.engine.TurboIconRound",
                            "mapleleaf.materialdesign.engine.VintageIconRound",
                            "mapleleaf.materialdesign.engine.ui.activities.ActivityMenu"
                        )

                        val mainActivityAlias =
                            "mapleleaf.materialdesign.engine.ui.activities.ActivityMenu"
                        val mainActivityComponentName =
                            ComponentName(packageName, mainActivityAlias)
                        packageManager.setComponentEnabledSetting(
                            mainActivityComponentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP
                        )

                        var alreadyEnabled = false
                        for (alias in activityAliases) {
                            val componentName = ComponentName(packageName, alias)
                            val currentState =
                                packageManager.getComponentEnabledSetting(componentName)
                            if (alias == selectedIconAlias) {
                                alreadyEnabled =
                                    currentState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                            }
                            if (!alreadyEnabled && alias != selectedIconAlias) {

                                packageManager.setComponentEnabledSetting(
                                    componentName,
                                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                    PackageManager.DONT_KILL_APP
                                )
                            }
                        }

                        if (!alreadyEnabled && selectedIconAlias.isNotBlank()) {
                            val componentName = ComponentName(packageName, selectedIconAlias)
                            packageManager.setComponentEnabledSetting(
                                componentName,
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP
                            )
                        }
                    } ?: run {
                        val mainActivityAlias =
                            "mapleleaf.materialdesign.engine.ui.activities.ActivityMenu"
                        val mainActivityComponentName =
                            ComponentName(packageName, mainActivityAlias)
                        packageManager.setComponentEnabledSetting(
                            mainActivityComponentName,
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP
                        )
                    }
                }
                return true
            }

            R.id.start_menu_default_icon -> {
                val packageManager = applicationContext.packageManager
                val mainActivityAlias = "mapleleaf.materialdesign.engine.ui.activities.ActivityMenu"
                val activityAliases = listOf(
                    "mapleleaf.materialdesign.engine.NoxIcon",
                    "mapleleaf.materialdesign.engine.PremiumIcon",
                    "mapleleaf.materialdesign.engine.AquaIcon",
                    "mapleleaf.materialdesign.engine.TurboIcon",
                    "mapleleaf.materialdesign.engine.VintageIcon",
                    "mapleleaf.materialdesign.engine.NoxIconRound",
                    "mapleleaf.materialdesign.engine.PremiumIconRound",
                    "mapleleaf.materialdesign.engine.AquaIconRound",
                    "mapleleaf.materialdesign.engine.TurboIconRound",
                    "mapleleaf.materialdesign.engine.VintageIconRound"
                )

                for (alias in activityAliases) {
                    if (alias != mainActivityAlias) {
                        val componentName = ComponentName(packageName, alias)
                        packageManager.setComponentEnabledSetting(
                            componentName,
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP
                        )
                    }
                }

                val mainActivityComponentName = ComponentName(packageName, mainActivityAlias)
                packageManager.setComponentEnabledSetting(
                    mainActivityComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
                toast("恢复成功")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // 加载图标的挂起函数
    private suspend fun loadIcons(): List<Pair<Int, String>> = withContext(Dispatchers.IO) {
        listOf(
            Pair(R.mipmap.ic_launcher_sa, "MainActivityIcon"),
            Pair(R.mipmap.icon_2_launcher_sa, "NoxIcon"),
            Pair(R.mipmap.icon_3_launcher_sa, "PremiumIcon"),
            Pair(R.mipmap.icon_4_launcher_sa, "AquaIcon"),
            Pair(R.mipmap.icon_5_launcher_sa, "TurboIcon"),
            Pair(R.mipmap.icon_6_launcher_sa, "VintageIcon"),
            Pair(R.mipmap.icon_2_launcher, "NoxIconRound"),
            Pair(R.mipmap.icon_3_launcher, "PremiumIconRound"),
            Pair(R.mipmap.icon_4_launcher, "AquaIconRound"),
            Pair(R.mipmap.icon_5_launcher, "TurboIconRound"),
            Pair(R.mipmap.icon_6_launcher, "VintageIconRound")
        )
    }

    open class IconAdapter(private var icons: List<Pair<Int, String>>) :
        RecyclerView.Adapter<IconAdapter.IconViewHolder>() {
        private var selectedItem = RecyclerView.NO_POSITION

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_icon, parent, false)
            return IconViewHolder(view)
        }

        override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
            holder.bind(icons[position], position == selectedItem)
        }

        override fun getItemCount(): Int {
            return icons.size
        }

        inner class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val iconImageView: ImageView = itemView.findViewById(R.id.iconPreview)
            private val titleTextView: TextView = itemView.findViewById(R.id.iconTitle)
            private val materialCardView: MaterialCardView = itemView.findViewById(R.id.materialCardView)

            @SuppressLint("NotifyDataSetChanged")
            fun bind(icon: Pair<Int, String>, isSelected: Boolean) {
                iconImageView.setImageResource(icon.first)
                titleTextView.text = icon.second

                val selectedColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
                val selectCancel = ContextCompat.getColor(context, R.color.transparent)

                materialCardView.setCardBackgroundColor(if (isSelected) selectedColor else selectCancel)

                itemView.setOnClickListener {
                    if (bindingAdapterPosition == selectedItem) {
                        return@setOnClickListener
                    }

                    val previousSelected = selectedItem
                    selectedItem = bindingAdapterPosition

                    if (previousSelected != RecyclerView.NO_POSITION) {
                        notifyItemChanged(previousSelected)
                    }
                    notifyItemChanged(selectedItem)

                }
            }
        }

        // 更新图标数据
        @SuppressLint("NotifyDataSetChanged")
        fun updateIcons(newIcons: List<Pair<Int, String>>) {
            icons = newIcons
            notifyDataSetChanged()
        }

        // 获取选中的图标
        fun getSelectedIcon(): String? {
            return if (selectedItem != RecyclerView.NO_POSITION) {
                icons[selectedItem].second
            } else {
                null
            }
        }

    }

    @SuppressLint("InflateParams")
    private fun checkPermissions() {
        XXPermissions.with(this)
            .permission(Permission.READ_MEDIA_IMAGES)
            .permission(Permission.READ_MEDIA_VIDEO)
            .permission(Permission.READ_MEDIA_AUDIO)
            .permission(Permission.POST_NOTIFICATIONS)
            .permission(Permission.SYSTEM_ALERT_WINDOW)
            .permission(Permission.WRITE_SETTINGS)
            .request { _, all ->
                if (!all) {
                    toast(getString(R.string.permission_granted_complete))
                } else {
                    val dialogView =
                        layoutInflater.inflate(R.layout.dialog_check_permission, null)
                    val dialog = DialogHelper.customDialog(this, dialogView)
                    dialogView.findViewById<TextView>(R.id.confirm_title).text =
                        getString(R.string.dialog_title)
                    dialogView.findViewById<TextView>(R.id.confirm_message).text =
                        getString(R.string.permission_granted_complete)

                    dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
                        dialog.dismiss()
                    }
                }
            }
    }

    private fun calculateAnswer(num1: Int, num2: Int, operationSymbol: String): Double {
        val result: Double = when (operationSymbol) {
            "+" -> (num1 + num2).toDouble()
            "-" -> (num1 - num2).toDouble()
            "*" -> (num1 * num2).toDouble()
            "/" -> if (num2 != 0) num1.toDouble() / num2 else Double.NaN
            else -> 0.0
        }
        return result
    }

}
