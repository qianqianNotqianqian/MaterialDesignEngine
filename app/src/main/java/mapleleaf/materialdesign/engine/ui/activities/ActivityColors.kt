package mapleleaf.materialdesign.engine.ui.activities

import android.os.Bundle
import android.widget.ProgressBar
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
import mapleleaf.materialdesign.engine.ui.fragments.FragmentColorsJP
import mapleleaf.materialdesign.engine.ui.fragments.FragmentColorsZH

class ActivityColors : UniversalActivityBase(R.layout.activity_colors) {

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var appBarLayout: AppBarLayout

    override fun initializeComponents(savedInstanceState: Bundle?) {

        progressBar = findViewById(R.id.progressBar)
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
        appBarLayout = findViewById(R.id.appBarLayout)
        progressBar.isIndeterminate = true
        progressBar.isVisible = true

        getToolbar().setBackgroundColor(getColor(R.color.transparent))

        CoroutineScope(Dispatchers.Main).launch {
            val fragmentList = listOf(
                FragmentColorsZH(),
                FragmentColorsJP()
            )

            val fragmentTitleList = listOf(
                "中国传统色", "日本传统色"
            )

            val adapter = ViewPagerAdapter(
                this@ActivityColors,
                fragmentList,
                fragmentTitleList
            )
            viewPager.adapter = adapter

            tabLayout.setupWithViewPager(viewPager)

            progressBar.isVisible = false
        }
        val baseColor =
            ContextCompat.getColor(MaterialDesignEngine.context, R.color.background_color)
        val primaryColor =
            ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
        val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)
        appBarLayout.setBackgroundColor(blendedColor)

        setToolbarTitle(getString(R.string.toolbar_title_activity_colors))
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
