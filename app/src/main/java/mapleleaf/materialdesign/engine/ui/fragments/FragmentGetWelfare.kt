package mapleleaf.materialdesign.engine.ui.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalFragmentBase
import mapleleaf.materialdesign.engine.utils.helper.CustomWebView
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class FragmentGetWelfare : UniversalFragmentBase() {

    private var webView: WebView? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val randomUrls = arrayOf(
        "https://moe.jitsu.top/r18",
        "https://sex.nyan.xyz/api/v2/img",
        "https://image.anosu.top/pixiv/direct?r18=1&keyword=azurlane",
        "https://image.anosu.top/pixiv/direct?r18=1&keyword=bluearchive",
        "https://image.anosu.top/pixiv/direct?r18=1&keyword=genshinimpact",
        "https://image.anosu.top/pixiv/direct?r18=1&keyword=arknights",
        "https://image.anosu.top/pixiv/direct?r18=1&keyword=honkai",
        "https://image.anosu.top/pixiv/direct?r18=1&keyword=fate",
        "https://image.anosu.top/pixiv/direct?r18=1&keyword=frontline",
        "https://image.anosu.top/pixiv/direct?r18=1&keyword=princess",
        "https://image.anosu.top/pixiv/direct?r18=1&keyword=idolmaster",
        "https://image.anosu.top/pixiv/direct?r18=1&keyword=hololive",
        "https://image.anosu.top/pixiv/direct?r18=1&keyword=touhou",
        "https://api.r10086.com/樱道随机图片api接口.php?图片系列=P站系列1",
        "https://api.r10086.com/樱道随机图片api接口.php?图片系列=P站系列2",
        "https://api.r10086.com/樱道随机图片api接口.php?图片系列=P站系列3",
        "https://api.r10086.com/樱道随机图片api接口.php?图片系列=P站系列4",
        "https://api.r10086.com/樱道随机图片api接口.php?图片系列=萝莉",
        "https://api.asxe.vip/whitesilk.php",
        "https://api.suyanw.cn/api/tbmjx.php"
    )
    override val layoutResId: Int
        get() = R.layout.fragment_get_web_image

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)

        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                progressBar = rootView.findViewById(R.id.progressBar)
                webView = rootView.findViewById(R.id.webView)
                swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout)
            }

            val toolbar = rootView.findViewById<Toolbar>(R.id.toolbar)
            val baseColor =
                ContextCompat.getColor(MaterialDesignEngine.context, R.color.background_color)
            val primaryColor =
                ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
            val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)
            toolbar.setBackgroundColor(blendedColor)

            FastScrollerBuilder(webView!!).build()
            withContext(Dispatchers.Main) {
                val colorRed = ContextCompat.getColor(requireContext(), R.color.red1)
                val colorGreen = ContextCompat.getColor(requireContext(), R.color.lawngreen)
                val colorBlue = ContextCompat.getColor(requireContext(), R.color.blue)
                val colorOrange = ContextCompat.getColor(requireContext(), R.color.orange2)
                val progressColors =
                    ContextCompat.getColor(requireContext(), R.color.swipe_refresh_layout_progress)
                swipeRefreshLayout.setColorSchemeColors(
                    colorRed,
                    colorGreen,
                    colorBlue,
                    colorOrange
                )
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(progressColors)
            }
            CustomWebView.setupWebView(
                requireActivity(),
                webView!!,
                randomUrls,
                progressBar,
                swipeRefreshLayout
            )
        }
        setToolbarTitle(getString(R.string.toolbar_title_fragment_sex))
        setToolbarSubTitle(getString(R.string.toolbar_subtitle_fragment_sex))

    }
}
