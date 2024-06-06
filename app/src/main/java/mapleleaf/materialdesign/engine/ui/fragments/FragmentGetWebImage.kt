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

class FragmentGetWebImage : UniversalFragmentBase() {

    private lateinit var progressBar: ProgressBar
    private var webView: WebView? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val randomUrls = arrayOf(
        "https://img.moehu.org/pics.php?id=xjj"
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
        setToolbarTitle(getString(R.string.toolbar_title_fragment_look_girl))
        setToolbarSubTitle(getString(R.string.toolbar_subtitle_fragment_look_girl))

    }
}
