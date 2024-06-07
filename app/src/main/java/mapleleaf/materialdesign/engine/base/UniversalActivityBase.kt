package mapleleaf.materialdesign.engine.base

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.utils.StatusBarColorExtractor
import mapleleaf.materialdesign.engine.utils.ThemeModeState
import mapleleaf.materialdesign.engine.utils.getStatusBarHeight

abstract class UniversalActivityBase(@LayoutRes layoutRes: Int) : AppCompatActivity(layoutRes) {

    private var toolbar: Toolbar? = null

    override fun onStart() {
        StatusBarColorExtractor.extractToolbarColor(this, getToolbar())
        ThemeModeState.switchTheme(this)
//        if (DarkThemeUtil.isDarkTheme(this)) {
//            setStatusBarIconColor(this, false)
//        }
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(getLayoutResourceId())
        initToolbar()
        setToolbarTopMargin()
        initializeComponents(savedInstanceState)
        setOverflowIconColor(R.color.text_color)
    }

    private fun setToolbarTopMargin() {
        val layoutParams = toolbar?.layoutParams as? ConstraintLayout.LayoutParams
        if (layoutParams != null) {
            layoutParams.topMargin = getStatusBarHeight(window, this)
            toolbar?.layoutParams = layoutParams
        } else {
            Log.e(
                "setToolbarTopMargin",
                "工具栏为 null 或 LayoutParams 不是 ConstraintLayout.LayoutParams 类型: ${toolbar?.layoutParams?.javaClass?.name}"
            )
        }
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        val baseColor =
            ContextCompat.getColor(MaterialDesignEngine.context, R.color.background_color)
        val primaryColor =
            ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
        val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)
        toolbar!!.setBackgroundColor(blendedColor)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayUseLogoEnabled(true)
        }
    }

//    @LayoutRes
//    protected abstract fun getLayoutResourceId(): Int

    protected abstract fun initializeComponents(savedInstanceState: Bundle?)

    protected fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    protected fun setToolbarSubtitle(subtitle: String) {
        supportActionBar?.subtitle = subtitle
    }

    private fun setOverflowIconColor(@ColorRes colorRes: Int) {
        val overflowIcon: Drawable? = ContextCompat.getDrawable(this, R.drawable.ic_overflow)
        overflowIcon?.let {
            val color = ContextCompat.getColor(this, colorRes)
            val tintedIcon = DrawableCompat.wrap(it)
            DrawableCompat.setTint(tintedIcon, color)
            toolbar?.overflowIcon = tintedIcon
        }
    }

    protected fun hideToolbar() {
        supportActionBar?.hide()
    }

    protected fun showToolbar() {
        supportActionBar?.show()
    }

    protected fun getToolbar(): Toolbar {
        return toolbar!!
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}
