package mapleleaf.materialdesign.engine.ui.fragments

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

open class FragmentBaseDialog : DialogFragment() {

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (_: IllegalStateException) {
        }
    }
}
