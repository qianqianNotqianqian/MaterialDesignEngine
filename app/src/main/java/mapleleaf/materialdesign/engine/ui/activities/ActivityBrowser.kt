package mapleleaf.materialdesign.engine.ui.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.util.SparseArray
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.ClientCertRequest
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.asynctask.ImageSaver
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.utils.clickListener.WebViewLongClickListener
import mapleleaf.materialdesign.engine.utils.toast
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.lang.reflect.Method
import java.net.URISyntaxException

class ActivityBrowser : UniversalActivityBase(R.layout.activity_browser) {

    private val keyMyView = "key_my_view"
    private val permissionRequestCode = 102

    private val tag: String = this::class.java.name
    private var myView: View? = null
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var titleTextView: TextView
    private var pendingPermissionRequest: PermissionRequest? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun initializeComponents(savedInstanceState: Bundle?) {
        setToolbarTitle("")
        progressBar = findViewById(R.id.progressBar)
        webView = findViewById(R.id.webView)
        titleTextView = findViewById(R.id.titleTextView)

        FastScrollerBuilder(webView).build()
        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.settings.databaseEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.setSupportZoom(true)

        val webViewClient = MyWebViewClient()
        webView.webViewClient = webViewClient

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                Log.d(tag, "onPermissionRequest=" + request.resources.contentToString())
                val permissions = request.resources
                val permissionsToRequest = mutableListOf<String>()

                for (permission in permissions) {
                    if (permission == PermissionRequest.RESOURCE_AUDIO_CAPTURE &&
                        ContextCompat.checkSelfPermission(
                            this@ActivityBrowser,
                            android.Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionsToRequest.add(android.Manifest.permission.RECORD_AUDIO)
                    }
                    if (permission == PermissionRequest.RESOURCE_VIDEO_CAPTURE &&
                        ContextCompat.checkSelfPermission(
                            this@ActivityBrowser,
                            android.Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionsToRequest.add(android.Manifest.permission.CAMERA)
                    }
                }

                if (permissionsToRequest.isNotEmpty()) {
                    ActivityCompat.requestPermissions(
                        this@ActivityBrowser,
                        permissionsToRequest.toTypedArray(),
                        permissionRequestCode
                    )
                    pendingPermissionRequest = request
                } else {
                    request.grant(permissions)
                }
            }

            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
                titleTextView.text = title
                titleTextView.isSelected = true
            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.postDelayed({ progressBar.isVisible = false }, 0)
                } else {
                    progressBar.isVisible = true
                }
            }

            override fun onJsAlert(
                view: WebView,
                url: String,
                message: String,
                result: JsResult,
            ): Boolean {
                return true
            }
        }

        webView.setDownloadListener { url, _, _, _, _ ->
            val snackbar = Snackbar.make(webView, "确认下载文件?", Snackbar.LENGTH_LONG)
                .setAction("是") {
                    try {
                        val downloadIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        downloadIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                        val activities: List<ResolveInfo> = context.packageManager.queryIntentActivities(downloadIntent, PackageManager.MATCH_DEFAULT_ONLY)

                        if (activities.isNotEmpty()) {
                            val chooserIntent = Intent.createChooser(downloadIntent, "选择应用下载")
                            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(chooserIntent)
                        } else {
                            toast("没有找到可以处理此下载请求的应用。")
                        }
                    } catch (e: ActivityNotFoundException) {
                        toast("没有找到可以处理此下载请求的活动。")
                    }
                }

            snackbar.show()
        }

        val url = intent.getStringExtra("url")
        if (url.isNullOrEmpty()) {
            webView.loadUrl("https://www.bing.com/")
        } else {
            webView.loadUrl(url)
        }

        webView.setOnLongClickListener(
            WebViewLongClickListener(
                this, ImageSaver(this)
            )
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionRequestCode) {
            var allPermissionsGranted = true

            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }

            pendingPermissionRequest?.let { request ->
                if (allPermissionsGranted) {
                    request.grant(request.resources)
                } else {
                    request.deny()
                }
            }

            pendingPermissionRequest = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(keyMyView, myView?.let { saveViewState(it) })
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        myView = savedInstanceState.getParcelable(keyMyView)
        myView?.let { restoreViewState(it) }
    }

    private fun saveViewState(view: View): Parcelable {
        val state = Bundle()
        val viewState = SparseArray<Parcelable>()
        view.saveHierarchyState(viewState)
        state.putSparseParcelableArray(keyMyView, viewState)
        return state
    }

    private fun restoreViewState(view: View) {
        val state = view.tag as? Bundle
        if (state != null) {
            val viewState = state.getSparseParcelableArray<Parcelable>(keyMyView)
            if (viewState != null) {
                view.restoreHierarchyState(viewState)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_browser, menu)
        if (menu.javaClass.simpleName.equals("MenuBuilder", ignoreCase = true)) {
            try {
                val method: Method = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible",
                    Boolean::class.javaPrimitiveType
                )
                method.isAccessible = true
                method.invoke(menu, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.html_copy_link -> {
                copyCurrentPageLink()
                return true
            }

            R.id.html_open_browser -> openInExternalBrowser()

            R.id.html_back -> {
                if (webView.canGoBack()) webView.goBack()
            }

            R.id.html_stop -> webView.stopLoading()

            R.id.html_refresh -> webView.reload()

            R.id.html_clear_all -> {
                webView.clearCache(true)
                webView.clearFormData()
                webView.clearHistory()
                webView.clearMatches()
                deleteDatabase("WebView.db")
                deleteDatabase("WebViewCache.db")
                cacheDir
                toast("已清除所有数据")
            }

            R.id.html_forward -> {
                if (webView.canGoForward()) webView.goForward()
            }
        }
        return true
    }

    private fun copyCurrentPageLink() {
        webView.let {
            val currentUrl = it.url
            if (!currentUrl.isNullOrEmpty()) {
                val clipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("URL", currentUrl)
                clipboardManager.setPrimaryClip(clip)
                toast("链接已复制")
            } else {
                toast("无法获取当前链接")
            }
        }
    }

    private fun openInExternalBrowser() {
        webView.let { it ->
            val currentUrl = it.url
            if (!currentUrl.isNullOrEmpty()) {
                Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl)).also { startActivity(it) }

            } else {
                toast("无法获取当前链接")
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        webView.let {
            it.stopLoading()
            val parent = it.parent as? ViewGroup
            parent?.removeView(it)
            it.removeAllViews()
            it.destroy()
        }
        super.onDestroy()
    }

    private inner class MyWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val uri: Uri = request.url
            val url = uri.toString()

            return if (url.startsWith("http://") || url.startsWith("https://")) {
                false // 继续让 WebView 加载 HTTP/HTTPS 开头的 URL
            } else {
                try {
                    val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    if (intent != null) {
                        when {
                            url.startsWith("zhihu://") ||
                                    url.startsWith("mqq://") ||
                                    url.startsWith("newsapp://") ||
                                    url.startsWith("mobilenotes://") ||
                                    url.startsWith("sohunews://") ||
                                    url.startsWith("dingtalk://") ||
                                    url.startsWith("taobao://") ||
                                    url.startsWith("qqmusic://") ||
                                    url.startsWith("qqmail://") ||
                                    url.startsWith("weiyun://") ||
                                    url.startsWith("sosomap://") ||
                                    url.startsWith("weixin://") ||
                                    url.startsWith("wechat://") -> {
                                showOpenAppSnackBar(intent)
                                true
                            }
                            url.startsWith("sms://") -> {
                                try {
                                    // 获取短信号码
                                    val phoneNumber = intent.data?.schemeSpecificPart
                                    Intent(
                                        Intent.ACTION_SENDTO,
                                        Uri.parse("smsto:$phoneNumber")
                                    ).also { startActivity(it) }
                                } catch (e: Exception) {
                                    Log.e(tag, "Error handling SMS URL: ${e.message}")
                                    // 处理异常情况
                                }
                                true // 返回 true 表示已经处理了该 URL
                            }
                            url.startsWith("itms-apps://") -> {
                                try {
                                    // 在这里执行打开App Store的逻辑
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e(tag, "Error handling App Store URL: ${e.message}")
                                    // 处理异常情况
                                }
                                true // 返回 true 表示已经处理了该 URL
                            }
                            url.startsWith("tel://") -> {
                                try {
                                    // 获取电话号码
                                    val phoneNumber = intent.data?.schemeSpecificPart
                                    // 在这里执行拨打电话的逻辑

                                    Intent(
                                        Intent.ACTION_DIAL,
                                        Uri.parse("tel:$phoneNumber")
                                    ).also { startActivity(it) }

                                } catch (e: Exception) {
                                    Log.e(tag, "Error handling Tel URL: ${e.message}")
                                }
                                true
                            }
                            else -> {
                                false
                            }
                        }
                    } else {
                        false
                    }
                } catch (e: URISyntaxException) {
                    Log.e(tag, "URISyntaxException: ${e.message}")
                    false
                } catch (e: ActivityNotFoundException) {
                    // 处理异常，例如没有可处理该 Intent 的应用
                    Log.e(tag, "Activity not found to handle Intent: $url")
                    handleWebViewError()
                    true // 返回 true 表示已经处理了该 URL
                }
            }
        }

        private var snackbar: Snackbar? = null
        private var isActionClicked = false

        @SuppressLint("QueryPermissionsNeeded")
        private fun showOpenAppSnackBar(intent: Intent) {
            // 如果已经有 Snackbar 在显示，并且 Action 已经点击，则直接返回，不再显示新的 Snackbar
            if (snackbar != null && (snackbar!!.isShownOrQueued || isActionClicked)) {
                return
            }
            // 使用PackageManager检查是否有应用能够处理该Intent
            if (intent.resolveActivity(packageManager) != null) {
                try {
                    val packageManager = packageManager
                    val packageName = intent.resolveActivity(packageManager)?.packageName
                    val applicationInfo =
                        packageName?.let { packageManager.getApplicationInfo(it, 0) }
                    val appName =
                        applicationInfo?.let { packageManager.getApplicationLabel(it) } as String

                    // 创建新的 Snackbar 实例并保存到成员变量中
                    snackbar = Snackbar.make(webView, "是否允许打开 $appName 应用？", Snackbar.LENGTH_INDEFINITE)
                        .setAction("允许") { _ ->
                            isActionClicked = true // 设置标志位为 true，表示用户已经点击了 Action

                            try {
                                startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                // 处理启动Activity时应用未找到的异常
                                e.printStackTrace()
                                // 可以显示一个提示或者其他处理逻辑
                            } catch (e: Exception) {
                                // 处理其他异常
                                e.printStackTrace()
                            }
                        }
                    // 显示 Snackbar
                    snackbar!!.show()
                } catch (e: Exception) {
                    // 处理Snackbar显示时的异常
                    e.printStackTrace()
                }
            } else {
                // 如果没有应用可以处理Intent，可以显示一个提示
                toast("没有应用可以处理该操作")
            }
        }

        private fun handleWebViewError() {
            // 处理 WebView 加载错误
            Log.d(
                tag,
                "onReceivedError=error=" + "Unknown URL scheme" + ",errorCode=" + "net::ERR_UNKNOWN_URL_SCHEME"
            )
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError,
        ) {
            super.onReceivedError(view, request, error)
            handleWebViewError(error)
        }

        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest,
            errorResponse: WebResourceResponse,
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            handleWebViewError(errorResponse)
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            super.onReceivedSslError(view, handler, error)
            handleSslError(error)
        }

        override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest) {
            super.onReceivedClientCertRequest(view, request)
            // 处理客户端证书请求
        }

        private fun handleWebViewError(error: WebResourceError) {
            // 处理通用的 WebView 加载错误
            Log.d(
                tag,
                "onReceivedError=error=" + error.description + ",errorCode=" + error.errorCode
            )
//            showSnackbarWithStyle(webView,"WebView Error: " + error.description);
        }

        private fun handleWebViewError(errorResponse: WebResourceResponse) {
            // 处理 HTTP 错误
            Log.d(tag, "onReceivedHttpError=statusCode=" + errorResponse.statusCode)
//            showSnackbarWithStyle(webView,"HTTP Error: " + errorResponse.statusCode);
        }

        private fun handleSslError(error: SslError) {
            // 处理 SSL 错误
            Log.d(tag, "onReceivedSslError=primaryError=" + error.getPrimaryError())
            when (error.getPrimaryError()) {
                SslError.SSL_INVALID, SslError.SSL_UNTRUSTED ->  // 可以选择继续加载页面
                    // handler.proceed();
//                    toast( "SSL Error: " + error.getPrimaryError())
                    return

                else ->  // 其他情况，取消加载
                    // handler.cancel();
//                    toast("SSL Error: " + error.getPrimaryError())
                    return
            }
        }
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }
}