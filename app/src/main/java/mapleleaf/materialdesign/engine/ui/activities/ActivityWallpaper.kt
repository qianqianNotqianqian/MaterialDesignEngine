package mapleleaf.materialdesign.engine.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.ui.fragments.FragmentGetBombShell
import mapleleaf.materialdesign.engine.ui.fragments.FragmentGetEveryDay
import mapleleaf.materialdesign.engine.ui.fragments.FragmentGetRandomCosplay
import mapleleaf.materialdesign.engine.ui.fragments.FragmentGetWallpaper
import mapleleaf.materialdesign.engine.ui.fragments.FragmentGetWebImage
import mapleleaf.materialdesign.engine.ui.fragments.FragmentGetWelfare
import mapleleaf.materialdesign.engine.utils.TabIcon
import mapleleaf.materialdesign.engine.utils.toast

class ActivityWallpaper : UniversalActivityBase(R.layout.activity_wallpaper) {

    private lateinit var nullList: AppCompatImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var tabLayout: TabLayout
    private lateinit var include: View

    override fun initializeComponents(savedInstanceState: Bundle?) {

        nullList = findViewById(R.id.null_list)
        progressBar = findViewById(R.id.progressBar)
        tabLayout = findViewById(R.id.tab_layout)
        include = findViewById(R.id.include)

        val intent = intent
        val userAnswer = intent.getDoubleExtra("isAnswerCorrect", -1.0)
        if (userAnswer == -1.0) {
            nullList.isVisible = true
            include.isVisible = true
            progressBar.isVisible = false
            tabLayout.isVisible = false
            toast("缺少必要参数:isAnswerCorrect")
        } else {

            val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
            val viewPager = findViewById<ViewPager>(R.id.view_pager)

            viewPager.offscreenPageLimit = 5

            TabIcon(
                tabLayout,
                viewPager,
                this@ActivityWallpaper,
                supportFragmentManager,
                R.layout.list_item_tab
            ).run {
                newTabSpec(
                    "图片",
                    AppCompatResources.getDrawable(
                        this@ActivityWallpaper,
                        R.drawable.ic_image
                    )!!,
                    FragmentGetWallpaper()
                )
                newTabSpec(
                    "妹纸",
                    AppCompatResources.getDrawable(
                        this@ActivityWallpaper,
                        R.drawable.ic_image
                    )!!,
                    FragmentGetBombShell()
                )
                newTabSpec(
                    "Cos",
                    AppCompatResources.getDrawable(
                        this@ActivityWallpaper,
                        R.drawable.ic_pixiv
                    )!!,
                    FragmentGetRandomCosplay()
                )
                newTabSpec(
                    "每日",
                    AppCompatResources.getDrawable(
                        this@ActivityWallpaper,
                        R.drawable.ic_image
                    )!!,
                    FragmentGetEveryDay()
                )
                newTabSpec(
                    "网页",
                    AppCompatResources.getDrawable(
                        this@ActivityWallpaper,
                        R.drawable.ic_image
                    )!!,
                    FragmentGetWebImage()
                )
                newTabSpec(
                    "好康",
                    AppCompatResources.getDrawable(
                        this@ActivityWallpaper,
                        R.drawable.ic_pixiv
                    )!!,
                    FragmentGetWelfare()
                )
                viewPager.adapter = this.adapter
                tabLayout.getTabAt(0)?.select()
            }
        }
    }
}