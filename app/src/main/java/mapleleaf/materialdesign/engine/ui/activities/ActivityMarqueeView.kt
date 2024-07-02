package mapleleaf.materialdesign.engine.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.ui.adapter.AdapterFragmentPagerItem
import mapleleaf.materialdesign.engine.ui.fragments.FragmentMarqueeCommon
import mapleleaf.materialdesign.engine.ui.fragments.FragmentMarqueeRecycleView

class ActivityMarqueeView : UniversalActivityBase(R.layout.activity_marquee_view) {

    override fun initializeComponents(savedInstanceState: Bundle?) {

        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val viewPager: ViewPager = findViewById(R.id.view_pager)

        setAppBarBackgroundColor(findViewById(R.id.appBarLayout))
        
        val adapter = AdapterFragmentPagerItem.Builder(this, supportFragmentManager)
            .add(
                "Common",
                FragmentMarqueeCommon()
            )
            .add(
                "RecyclerView",
                FragmentMarqueeRecycleView()
            )
            .build()
        viewPager.adapter = adapter

        tabLayout.setupWithViewPager(viewPager)
        getToolbar().setBackgroundColor(getColor(R.color.transparent))

        setToolbarTitle(getString(R.string.toolbar_title_activity_marquee_view))

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_menu_app -> {
                startActivity(Intent(this, ActivityMarqueeAbout::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}