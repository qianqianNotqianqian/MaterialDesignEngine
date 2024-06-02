package mapleleaf.materialdesign.engine.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.permissions.CheckRootStatus
import mapleleaf.materialdesign.engine.ui.fragments.FragmentOverViewDonate
import mapleleaf.materialdesign.engine.ui.fragments.FragmentOverViewNotRoot
import mapleleaf.materialdesign.engine.ui.fragments.FragmentOverViewSystem
import mapleleaf.materialdesign.engine.utils.TabIcon
import mapleleaf.materialdesign.engine.utils.getStatusBarHeight

class ActivitySystemOverview : UniversalActivityBase() {

    override fun getLayoutResourceId() = R.layout.activity_system_overview

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun initializeComponents(savedInstanceState: Bundle?) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        setSupportActionBar(toolbar)
        // 显示返回按钮
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        toolbar.setNavigationOnClickListener {
            this.onBackPressed()
        }

        TabIcon(
            tabLayout,
            viewPager,
            this@ActivitySystemOverview,
            supportFragmentManager,
            R.layout.list_item_tab
        ).run {
            newTabSpec(
                getString(R.string.tab_system_overview),
                getDrawable(R.drawable.ic_home)!!,
                if (CheckRootStatus.lastCheckResult) {
                    FragmentOverViewSystem()
                } else {
                    FragmentOverViewNotRoot()
                }
            )
            newTabSpec(
                getString(R.string.tab_donate),
                getDrawable(R.drawable.ic_donate)!!,
                FragmentOverViewDonate()
            )
            viewPager.adapter = this.adapter
            viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
            tabLayout.getTabAt(0)?.select()

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    // 根据选中的 Tab 设置标题
                    tab?.let {
                        val title =
                            if (it.position == 0) getString(R.string.tab_system_overview) else getString(
                                R.string.tab_donate
                            )
                        setToolbarSubtitle(title)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }

        setToolbarSubtitle(getString(R.string.tab_system_overview))
        setToolbarTitle(getString(R.string.toolbar_title_activity_system_overview))

        val layoutParams = tabLayout?.layoutParams as? ConstraintLayout.LayoutParams
        layoutParams?.topMargin = getStatusBarHeight(window, this)
        tabLayout?.layoutParams = layoutParams
    }
}