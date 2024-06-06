package mapleleaf.materialdesign.engine.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import mapleleaf.materialdesign.engine.ui.fragments.FragmentMarqueeCommon
import mapleleaf.materialdesign.engine.ui.fragments.FragmentMarqueeRecycleView
import mapleleaf.materialdesign.engine.ui.adapter.AdapterFragmentPagerItem
import com.xuexiang.rxutil2.rxjava.RxJavaUtils
import com.xuexiang.xui.widget.textview.MarqueeTextView
import com.xuexiang.xui.widget.textview.marqueen.ComplexItemEntity
import com.xuexiang.xui.widget.textview.marqueen.DisplayEntity
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.utils.ComplexViewMF
import mapleleaf.materialdesign.engine.utils.toast
import mapleleaf.materialdesign.engine.view.XUIMarqueeFactory
import mapleleaf.materialdesign.engine.view.XUIMarqueeView

class ActivityMarqueeView: UniversalActivityBase() {

    private lateinit var marqueeViewXUI1: XUIMarqueeView
    private lateinit var marqueeViewXUI2: XUIMarqueeView
    private lateinit var marqueeViewXUI3: XUIMarqueeView
    private lateinit var marqueeViewXUI4: XUIMarqueeView
    private lateinit var marqueeViewXUI5: XUIMarqueeView
    private lateinit var tvMarquee: MarqueeTextView
    private lateinit var splitBody: AppCompatImageView

    override fun getLayoutResourceId() = R.layout.activity_marquee_view

    override fun initializeComponents(savedInstanceState: Bundle?) {

        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val viewPager: ViewPager = findViewById(R.id.view_pager)

        marqueeViewXUI1 = findViewById(R.id.marqueeViewXUI1)
        marqueeViewXUI2 = findViewById(R.id.marqueeViewXUI2)
        marqueeViewXUI3 = findViewById(R.id.marqueeViewXUI3)
        marqueeViewXUI4 = findViewById(R.id.marqueeViewXUI4)
        marqueeViewXUI5 = findViewById(R.id.marqueeViewXUI5)
        tvMarquee = findViewById(R.id.tv_marquee)
        splitBody = findViewById(R.id.split_body)
        splitBody.isVisible = false

        val appBarLayout = findViewById<AppBarLayout>(R.id.appBarLayout)
        val baseColor = ContextCompat.getColor(context, R.color.background_color)
        val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
        val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)
        appBarLayout.setBackgroundColor(blendedColor)

        initMarqueeView()

        val adapter = AdapterFragmentPagerItem.Builder(this, supportFragmentManager)
            .add("Common",
                FragmentMarqueeCommon()
            )
            .add("RecyclerView",
                FragmentMarqueeRecycleView()
            )
            .build()
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1
        tabLayout.setupWithViewPager(viewPager)
        getToolbar().setBackgroundColor(getColor(R.color.transparent))

        setToolbarTitle(getString(R.string.toolbar_title_activity_marquee_view))

    }

    private fun initMarqueeView() {
        val datas: List<String> = mutableListOf(
            "《赋得古原草送别》",
            "离离原上草，一岁一枯荣。",
            "野火烧不尽，春风吹又生。",
            "远芳侵古道，晴翠接荒城。",
            "又送王孙去，萋萋满别情。"
        )
        val marqueeFactoryXUI1: XUIMarqueeFactory<TextView, String> = object : XUIMarqueeFactory<TextView, String>(
            context) {
            override fun generateMarqueeItemView(data: String): TextView {
                // 根据 data 生成一个 TextView
                val textView = TextView(context)
                textView.text = data
                return textView
            }
        }

        marqueeViewXUI1.setMarqueeFactory(marqueeFactoryXUI1)
        marqueeViewXUI1.startFlipping()
        marqueeFactoryXUI1.setOnItemClickListener { _, holder -> toast(holder.data) }
        marqueeFactoryXUI1.setData(datas)

        val marqueeFactoryXUI2: XUIMarqueeFactory<TextView, String> = object : XUIMarqueeFactory<TextView, String>(this) {
            override fun generateMarqueeItemView(data: String?): TextView {
                val textView = TextView(context)
                textView.text = data
                return textView
            }
        }
        marqueeFactoryXUI2.setOnItemClickListener { _, holder -> toast(holder.data) }
        marqueeFactoryXUI2.setData(datas)
        marqueeViewXUI2.setMarqueeFactory(marqueeFactoryXUI2)
        marqueeViewXUI2.setAnimDuration(15000)
        marqueeViewXUI2.setInterval(16000)
        marqueeViewXUI2.startFlipping()

        val marqueeFactoryXUI3 = object : XUIMarqueeFactory<TextView, String>(this) {
            override fun generateMarqueeItemView(data: String?): TextView {
                val textView = TextView(context)
                textView.text = data
                return textView
            }
        }
        marqueeFactoryXUI3.setOnItemClickListener { view, holder -> toast(holder.data) }
        marqueeFactoryXUI3.setData(datas)
        marqueeViewXUI3.setMarqueeFactory(marqueeFactoryXUI3)
        marqueeViewXUI3.setAnimInAndOut(R.anim.marquee_left_in, R.anim.marquee_right_out)
        marqueeViewXUI3.setAnimDuration(8000)
        marqueeViewXUI3.setInterval(8500)
        marqueeViewXUI3.startFlipping()

        val marqueeFactoryXUI4 = object : XUIMarqueeFactory<TextView, String>(this) {
            override fun generateMarqueeItemView(data: String?): TextView {
                val textView = TextView(context)
                textView.text = data
                return textView
            }
        }
        marqueeFactoryXUI4.setOnItemClickListener { view, holder -> toast(holder.data) }
        marqueeFactoryXUI4.setData(datas)
        marqueeViewXUI4.setAnimInAndOut(R.anim.marquee_top_in, R.anim.marquee_bottom_out)
        marqueeViewXUI4.setMarqueeFactory(marqueeFactoryXUI4)
        marqueeViewXUI4.startFlipping()

        val complexDatas: MutableList<ComplexItemEntity> = ArrayList()
        for (i in 0 until 5) {
            complexDatas.add(ComplexItemEntity("标题 $i", "副标题 $i", "时间 $i"))
        }
        val marqueeFactoryXUI5: XUIMarqueeFactory<RelativeLayout, ComplexItemEntity> =
            ComplexViewMF(context)
        marqueeFactoryXUI5.setOnItemClickListener { _, holder -> toast(holder.data.toString()) }
        marqueeFactoryXUI5.setData(complexDatas)
        marqueeViewXUI5.setAnimInAndOut(R.anim.marquee_top_in, R.anim.marquee_bottom_out)
        marqueeViewXUI5.setMarqueeFactory(marqueeFactoryXUI5)
        marqueeViewXUI5.startFlipping()

        tvMarquee.setOnMarqueeListener(object : MarqueeTextView.OnMarqueeListener {
            override fun onStartMarquee(displayMsg: DisplayEntity, index: Int): DisplayEntity? {
                return if ("离离原上草，一岁一枯荣。" == displayMsg.toString()) {
                    null
                } else {
                    toast("开始滚动：$displayMsg")
                    displayMsg
                }
            }

            override fun onMarqueeFinished(displayDatas: List<DisplayEntity>): List<DisplayEntity> {
                toast("一轮滚动完毕！")
                return displayDatas
            }
        })
        tvMarquee.startSimpleRoll(datas)

        RxJavaUtils.delay(5) { tvMarquee.removeDisplayString(datas[3]) }

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