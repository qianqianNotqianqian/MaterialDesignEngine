package mapleleaf.materialdesign.engine.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase

class ActivityMarqueeAbout : UniversalActivityBase() {

    private lateinit var webView: WebView
    private lateinit var settings: WebSettings

    companion object {
        fun getVersionName(context: Context): String {
            return try {
                val packageManager = context.packageManager
                val packInfo = packageManager.getPackageInfo(context.packageName, 0)
                packInfo.versionName ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.marquee_activity_about
    }

    override fun initializeComponents(savedInstanceState: Bundle?) {
        initView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView() {
        webView = findViewById(R.id.webView)
        setToolbarTitle("关于(V${getVersionName(this)})")

        settings = webView.settings
        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            settings.loadsImagesAutomatically = true
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            settings.loadsImagesAutomatically = false
        }

        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.domStorageEnabled = true
        settings.saveFormData = true
        settings.setSupportMultipleWindows(true)
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        webView.setHorizontalScrollbarOverlay(true)
        webView.isHorizontalScrollBarEnabled = false
        webView.overScrollMode = View.OVER_SCROLL_NEVER
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.requestFocus()

        webView.loadUrl("file:///android_asset/about.html")
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }
}
