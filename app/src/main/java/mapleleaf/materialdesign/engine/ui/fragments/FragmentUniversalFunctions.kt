package mapleleaf.materialdesign.engine.ui.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.launch
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalFragmentBase
import mapleleaf.materialdesign.engine.utils.helper.CustomWebView
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class FragmentUniversalFunctions : UniversalFragmentBase(R.layout.fragment_functions) {
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var webView: WebView? = null
    private var randomUrls: Array<String>? = null

    companion object {
        fun newInstanceWithRandomUrls(randomUrls: Array<String>): FragmentUniversalFunctions {
            val fragment = FragmentUniversalFunctions()
            val args = Bundle()
            args.putStringArray("randomUrls", randomUrls)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)

        progressBar = rootView.findViewById(R.id.functions_progressBar)
        webView = rootView.findViewById(R.id.webView)
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout)

        val colorRed = ContextCompat.getColor(requireContext(), R.color.red1)
        val colorGreen = ContextCompat.getColor(requireContext(), R.color.lawngreen)
        val colorBlue = ContextCompat.getColor(requireContext(), R.color.blue)
        val colorOrange = ContextCompat.getColor(requireContext(), R.color.orange2)
        val progressColors =
            ContextCompat.getColor(requireContext(), R.color.swipe_refresh_layout_progress)
        swipeRefreshLayout.setColorSchemeColors(colorRed, colorGreen, colorBlue, colorOrange)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(progressColors)

        FastScrollerBuilder(webView!!).useMd2Style().build()
        randomUrls = arguments?.getStringArray("randomUrls") ?: emptyArray()

        lifecycleScope.launch {
            randomUrls?.let {
                CustomWebView.setupWebView(
                    requireActivity(),
                    webView!!,
                    it,
                    progressBar,
                    swipeRefreshLayout
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        CustomWebView.onDestroy(webView!!)
    }
}
