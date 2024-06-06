package mapleleaf.materialdesign.engine.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.ui.fragments.FragmentComponentActivities
import mapleleaf.materialdesign.engine.ui.fragments.FragmentComponentProviders
import mapleleaf.materialdesign.engine.ui.fragments.FragmentComponentReceivers
import mapleleaf.materialdesign.engine.ui.fragments.FragmentComponentServices
import mapleleaf.materialdesign.engine.ui.fragments.FragmentComponentWidgetInfo
import mapleleaf.materialdesign.engine.utils.toast

class ActivityAppComponents : UniversalActivityBase() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var nullList: AppCompatImageView
    private lateinit var appBarLayout: AppBarLayout
    private var progressBar: ProgressBar? = null
    private lateinit var splitBody: AppCompatImageView

    override fun getLayoutResourceId() = R.layout.activity_app_components

    override fun initializeComponents(savedInstanceState: Bundle?) {

        progressBar = findViewById(R.id.progressBar)
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
        nullList = findViewById(R.id.null_list)
        appBarLayout = findViewById(R.id.appBarLayout)
        splitBody = findViewById(R.id.split_body)
        splitBody.isVisible = false
        getToolbar().setBackgroundColor(getColor(R.color.transparent))

        val baseColor = ContextCompat.getColor(MaterialDesignEngine.context, R.color.background_color)
        val primaryColor = ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
        val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)
        appBarLayout.setBackgroundColor(blendedColor)

        val packageName = intent.getStringExtra("packageName")
        if (packageName == null) {
            nullList.isVisible = true
            tabLayout.isVisible = false
            toast("缺少必要参数:packageName")
        } else {
            viewPager.offscreenPageLimit = 4
            progressBar?.isIndeterminate = true
            progressBar?.isVisible  = true

            lifecycleScope.launch(Dispatchers.Main) {
                val fragmentList = listOf(
                    FragmentComponentActivities().apply {
                        arguments = Bundle().apply {
                            putString("packageName", packageName)
                        }
                    },
                    FragmentComponentProviders().apply {
                        arguments = Bundle().apply {
                            putString("packageName", packageName)
                        }
                    },
                    FragmentComponentReceivers().apply {
                        arguments = Bundle().apply {
                            putString("packageName", packageName)
                        }
                    },
                    FragmentComponentServices().apply {
                        arguments = Bundle().apply {
                            putString("packageName", packageName)
                        }
                    },
                    FragmentComponentWidgetInfo().apply {
                        arguments = Bundle().apply {
                            putString("packageName", packageName)
                        }
                    }
                )

                val fragmentTitleList = listOf(
                    "活动", "内容提供", "广播接收", "服务", "小部件"
                )

                val adapter = ViewPagerAdapter(
                    this@ActivityAppComponents,
                    fragmentList,
                    fragmentTitleList
                )
                viewPager.adapter = adapter
                tabLayout.setupWithViewPager(viewPager)
                viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int,
                    ) {
                        // Do nothing
                    }

                    override fun onPageSelected(position: Int) {
                        val fragment = adapter.getItem(position) as? ContentLoadable
                        fragment?.loadContent()
                        invalidateOptionsMenu()
                    }

                    override fun onPageScrollStateChanged(state: Int) {
                        // Do nothing
                    }
                })
                tabLayout.getTabAt(0)?.select()
                progressBar?.isVisible = false
            }

            packageName.let {
                val appInfo = packageManager.getApplicationInfo(it, 0)
                val label = packageManager.getApplicationLabel(appInfo).toString()
                setToolbarTitle(label)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_clear_search, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val selectedFragment = (viewPager.adapter as? ViewPagerAdapter)?.getItem(viewPager.currentItem)
        val shouldShowClearMenu = when (selectedFragment) {
            is FragmentComponentActivities -> selectedFragment.isSearchBoxNotEmpty()
            is FragmentComponentProviders -> selectedFragment.isSearchBoxNotEmpty()
            is FragmentComponentReceivers -> selectedFragment.isSearchBoxNotEmpty()
            is FragmentComponentServices -> selectedFragment.isSearchBoxNotEmpty()
            is FragmentComponentWidgetInfo -> selectedFragment.isSearchBoxNotEmpty()
            else -> false
        }
        menu?.findItem(R.id.action_clear_search)?.isVisible = shouldShowClearMenu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_search -> {
                val selectedFragment = (viewPager.adapter as? ViewPagerAdapter)?.getItem(viewPager.currentItem)
                when (selectedFragment) {
                    is FragmentComponentActivities -> selectedFragment.clearSearchBox()
                    is FragmentComponentProviders -> selectedFragment.clearSearchBox()
                    is FragmentComponentReceivers -> selectedFragment.clearSearchBox()
                    is FragmentComponentServices -> selectedFragment.clearSearchBox()
                    is FragmentComponentWidgetInfo -> selectedFragment.clearSearchBox()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    interface ContentLoadable {
        fun loadContent()
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