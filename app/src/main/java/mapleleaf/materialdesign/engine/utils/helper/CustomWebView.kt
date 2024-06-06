package mapleleaf.materialdesign.engine.utils.helper

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.net.http.SslError
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import mapleleaf.materialdesign.engine.asynctask.ImageSaver
import mapleleaf.materialdesign.engine.utils.clickListener.WebViewLongClickListener
import mapleleaf.materialdesign.engine.utils.toast
import java.lang.reflect.Method
import java.util.Random

object CustomWebView {

    private const val TAG = "CustomWebView"
    private val random = Random()

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebView(
        context: Context,
        webView: WebView,
        urls: Array<String>,
        progressBar: ProgressBar,
        swipeRefreshLayout: SwipeRefreshLayout,
    ) {
        val webSettings = webView.settings
        webSettings.builtInZoomControls = false//**
        webSettings.displayZoomControls = false
        webSettings.supportMultipleWindows()
        webSettings.javaScriptEnabled = true
        // 允许javascript出错
        try {
            val method: Method = Class.forName("android.webkit.WebView")
                .getMethod("setWebContentsDebuggingEnabled", Boolean::class.javaPrimitiveType)
            method.isAccessible = true
            method.invoke(null, true)
        } catch (e: Exception) {
            // do nothing
        }
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.loadsImagesAutomatically = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.setSupportZoom(false)//**
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE

        webView.loadUrl(urls[random.nextInt(urls.size)])
        // 设置 WebViewClient
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean {
                //解决重定向问题
                if (!TextUtils.isEmpty(request.url.path)) {
                    view.hitTestResult
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                //解决重定向问题
                if (!TextUtils.isEmpty(url)) {
                    view.hitTestResult
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError,
            ) {
                super.onReceivedError(view, request, error)
                Log.d(
                    TAG,
                    "onReceivedError=error=" + error.description + ",errorCode=" + error.errorCode + ",failingUrl=" + request.url
                )
            }

            @Deprecated("Deprecated in Java")
            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String,
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.d(
                    TAG,
                    "onReceivedError=description=$description,errorCode=$errorCode,failingUrl=$failingUrl"
                )
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError,
            ) {
                super.onReceivedSslError(view, handler, error)
                when (error.primaryError) {
                    SslError.SSL_INVALID, SslError.SSL_UNTRUSTED -> // 证书有问题
                        handler.proceed()
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                progressBar.isVisible = false
            }
        }

        // 设置 WebChromeClient
        webView.webChromeClient = object : WebChromeClient() {

            override fun onPermissionRequest(request: PermissionRequest) {
                Log.d(TAG, "onPermissionRequest=" + request.resources.contentToString())
            }

            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
            }

            //扩展支持alert事件
            override fun onJsAlert(
                view: WebView,
                url: String,
                message: String,
                result: JsResult,
            ): Boolean {
                return true
            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.isVisible = false
                    swipeRefreshLayout.isRefreshing = false
                } else {
                    progressBar.isVisible = true
                    swipeRefreshLayout.isRefreshing = true
                }
            }
        }
        webView.setOnLongClickListener(
            WebViewLongClickListener(
                context, ImageSaver(context)
            )
        )
        // 设置下载监听器
        webView.setDownloadListener { url, _, _, _, _ ->
            try {
                val isIDMInstalled = isPackageInstalled(context)
                if (isIDMInstalled) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.`package` = "idm.internet.download.manager.plus"
                    intent.data = Uri.parse(url)
                    context.startActivity(intent)
                } else {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    val packageManager: PackageManager = context.packageManager
                    val resolveInfo: ResolveInfo? =
                        packageManager.resolveActivity(
                            browserIntent,
                            PackageManager.MATCH_DEFAULT_ONLY
                        )
                    if (resolveInfo != null) {
                        context.startActivity(browserIntent)
                    } else {
                        toast("未找到处理此请求的活动。")
                    }
                }
            } catch (e: ActivityNotFoundException) {
                toast("无法找到任何能够处理该请求的Activity。")
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            webView.loadUrl(urls[random.nextInt(urls.size)])
        }
    }

    private fun isPackageInstalled(context: Context): Boolean {
        val pm: PackageManager = context.packageManager
        return try {
            pm.getPackageInfo("idm.internet.download.manager.plus", PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun onKeyDown(webView: WebView, keyCode: Int): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack()
            }
            return true
        }
        return false
    }

    fun onDestroy(webView: WebView) {
        webView.let {
            it.stopLoading()
            val parent = it.parent as? ViewGroup
            parent?.removeView(it)
            it.removeAllViews()
            it.destroy()
        }
    }
}
