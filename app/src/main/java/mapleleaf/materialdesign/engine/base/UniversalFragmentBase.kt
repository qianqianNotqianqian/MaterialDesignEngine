package mapleleaf.materialdesign.engine.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.utils.getStatusBarHeight

abstract class UniversalFragmentBase(@LayoutRes private val layoutRes: Int) : Fragment() {

    private var toolbar: Toolbar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(layoutRes, container, false)
        initToolbar(view)
        return view
    }

    private fun initToolbar(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        toolbar?.post {
            val layoutParams = toolbar!!.layoutParams as? ConstraintLayout.LayoutParams
            layoutParams?.topMargin = getStatusBarHeight(requireActivity().window, requireContext())
            toolbar!!.layoutParams = layoutParams
        }
    }

    protected fun setToolbarTitle(title: String) {
        toolbar?.title = title
    }

    protected fun setToolbarSubTitle(subTitle: String) {
        toolbar?.subtitle = subTitle
    }
}
