package mapleleaf.materialdesign.engine.ui.activities

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.ui.fragments.FragmentUniversalFunctions

class ActivityFunctions : UniversalActivityBase() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var loading: AppCompatImageView
    private lateinit var splitBody: AppCompatImageView

    override fun getLayoutResourceId() = R.layout.activity_functions

    override fun initializeComponents(savedInstanceState: Bundle?) {

        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
        appBarLayout = findViewById(R.id.appBarLayout)
        splitBody = findViewById(R.id.split_body)
        splitBody.isVisible = false
        setToolbarTitle(getString(R.string.toolbar_title_activity_functions))

        getToolbar().setBackgroundColor(getColor(R.color.transparent))
        loading = findViewById(R.id.loading)
        val animatedVectorDrawable = AppCompatResources.getDrawable(
            this,
            R.drawable.progress_loading_manager
        ) as? AnimatedVectorDrawable
        loading.setImageDrawable(animatedVectorDrawable)
        animatedVectorDrawable?.start()

        viewPager.offscreenPageLimit = 4  // 设置页面保留数量

        CoroutineScope(Dispatchers.Main).launch {
            val fragmentList = listOf(
                FragmentUniversalFunctions.newInstanceWithRandomUrls(arrayOf("file:///android_asset/html/coderunner/index.html")),
                FragmentUniversalFunctions.newInstanceWithRandomUrls(arrayOf("file:///android_asset/html/ikunblock/index.html")),
                FragmentUniversalFunctions.newInstanceWithRandomUrls(arrayOf("file:///android_asset/html/ikunmusic/index.html")),
                FragmentUniversalFunctions.newInstanceWithRandomUrls(arrayOf("file:///android_asset/html/pulled/index.html")),
                FragmentUniversalFunctions.newInstanceWithRandomUrls(arrayOf("file:///android_asset/html/rps/index.html"))
            )

            val fragmentTitleList = listOf(
                "代码运行", "别踩坤快", "坤赖之音",
                "拉粑粑吗", "斗三拳头"
            )

            val adapter = ViewPagerAdapter(this@ActivityFunctions, fragmentList, fragmentTitleList)
            viewPager.adapter = adapter
            viewPager.setPageTransformer(true, FadePageTransformer())

            tabLayout.setupWithViewPager(viewPager)

            animatedVectorDrawable?.stop()
            loading.isVisible = false
        }

        val baseColor = ContextCompat.getColor(MaterialDesignEngine.context, R.color.background_color)
        val primaryColor = ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
        val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)
        appBarLayout.setBackgroundColor(blendedColor)
    }

    class FadePageTransformer : ViewPager.PageTransformer {
        override fun transformPage(view: View, position: Float) {
            view.alpha = when {
                position <= -1 -> 0f
                position >= 1 -> 0f
                position < 0 -> 1 + position
                else -> 1 - position
            }
        }
    }

    class ViewPagerAdapter(
        fragmentActivity: FragmentActivity,
        private val fragmentList: List<Fragment>,
        private val fragmentTitleList: List<String>,
    ) : FragmentPagerAdapter(
        fragmentActivity.supportFragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

        override fun getCount(): Int = fragmentList.size

        override fun getItem(position: Int): Fragment = fragmentList[position]

        override fun getPageTitle(position: Int): CharSequence = fragmentTitleList[position]
    }
}
