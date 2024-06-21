package mapleleaf.materialdesign.engine.ui.fragments

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalFragmentBase
import mapleleaf.materialdesign.engine.model.SunfushengMarqueeCustomModel
import mapleleaf.materialdesign.engine.utils.toast
import mapleleaf.materialdesign.engine.view.SunfushengMarqueeView

class FragmentMarqueeCommon : UniversalFragmentBase(R.layout.marquee_fragment_tab) {

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)
        val sunfushengMarqueeView = rootView.findViewById<SunfushengMarqueeView<*>>(R.id.marqueeView)
        val sunfushengMarqueeView1 = rootView.findViewById<SunfushengMarqueeView<*>>(R.id.marqueeView1)
        val sunfushengMarqueeView2 = rootView.findViewById<SunfushengMarqueeView<*>>(R.id.marqueeView2)
        val sunfushengMarqueeView3 = rootView.findViewById<SunfushengMarqueeView<*>>(R.id.marqueeView3)
        val sunfushengMarqueeView4 = rootView.findViewById<SunfushengMarqueeView<*>>(R.id.marqueeView4)

        val list = ArrayList<CharSequence>()
        val ss1 = SpannableString("1、MarqueeView开源项目")
        ss1.setSpan(ForegroundColorSpan(context?.let { ContextCompat.getColor(it, R.color.red) }!!), 2, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        list.add(ss1)
        val ss2 = SpannableString("2、GitHub：sunfusheng")
        ss2.setSpan(ForegroundColorSpan(context?.let { ContextCompat.getColor(it, R.color.blue) }!!), 9, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        list.add(ss2)
        val ss3 = SpannableString("3、个人博客：sunfusheng.com")
        ss3.setSpan(URLSpan("http://sunfusheng.com/"), 7, 21, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        list.add(ss3)
        list.add("4、新浪微博：@孙福生微博")

        sunfushengMarqueeView.startWithList(list as List<Nothing>?)
        sunfushengMarqueeView.setOnItemClickListener { _, textView -> toast(textView.text.toString()) }

        sunfushengMarqueeView1.startWithText(getString(R.string.marquee_texts), R.anim.anim_top_in, R.anim.anim_bottom_out)
        sunfushengMarqueeView1.setOnItemClickListener { position, textView -> toast("$position. ${textView.text}") }

        sunfushengMarqueeView2.startWithText(getString(R.string.marquee_text))

        sunfushengMarqueeView3.startWithText(getString(R.string.marquee_texts))
        sunfushengMarqueeView3.setOnItemClickListener { position, textView ->
            val model = sunfushengMarqueeView3.messages[position] as CharSequence
            toast(model.toString())
        }

        val models = ArrayList<SunfushengMarqueeCustomModel>()
        models.add(SunfushengMarqueeCustomModel(10000, "增加了新功能：", "设置自定义的Model数据类型"))
        models.add(SunfushengMarqueeCustomModel(10001, "GitHub：sunfusheng", "新浪微博：@孙福生微博"))
        models.add(SunfushengMarqueeCustomModel(10002, "MarqueeView开源项目", "个人博客：sunfusheng.com"))
        sunfushengMarqueeView4.startWithList(models as List<Nothing>?)
        sunfushengMarqueeView4.setOnItemClickListener { position, _ ->
            val model = sunfushengMarqueeView4.messages[position] as SunfushengMarqueeCustomModel
            toast("ID:" + model.id)
        }

    }
}
