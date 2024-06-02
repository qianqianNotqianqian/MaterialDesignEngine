package mapleleaf.materialdesign.engine.ui.dialog

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.store.AppPreferences
import mapleleaf.materialdesign.engine.ui.fragments.FragmentBaseDialog
import mapleleaf.materialdesign.engine.utils.openUrlByBrowser

class DialogDonate : FragmentBaseDialog() {

    private lateinit var appPreferences: AppPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        appPreferences = AppPreferences(requireContext())
        return MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_support_development)
            .setTitle(R.string.about_donation)
            .setMessage(R.string.donate_message)
            .setNegativeButton(R.string.donate_negative_action_text) { _, _ ->
                appPreferences.showDonate = false
            }
            .setPositiveButton(R.string.donate_action_text) { _, _ ->
                openUrlByBrowser(requireContext(), getString(R.string.donate_link))
            }
            .create()
    }

    companion object {

        const val TAG = "DonateDialog"

        fun newInstance(): DialogDonate {
            return DialogDonate()
        }
    }
}
