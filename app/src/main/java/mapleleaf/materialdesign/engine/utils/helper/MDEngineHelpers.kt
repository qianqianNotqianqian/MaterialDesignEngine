package mapleleaf.materialdesign.engine.utils.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.ui.activities.ActivityAbout
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper

object MDEngineHelpers {

    fun showAbout(activity: FragmentActivity) {
        val fragmentManager: FragmentManager = activity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val prev: Fragment? = fragmentManager.findFragmentByTag("dialog_about")
        if (prev != null) {
            fragmentTransaction.remove(prev)
        }
        fragmentTransaction.addToBackStack(null)

        AboutDialog().show(fragmentTransaction, "dialog_about")
    }

    class AboutDialog : DialogFragment() {

        @SuppressLint("InflateParams", "UseGetLayoutInflater")
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity: Activity? = activity
            var appName = "MDEngine"
            if (activity != null) {
                val pm: PackageManager = activity.packageManager
                try {
                    val pInfo: PackageInfo = pm.getPackageInfo(activity.packageName, 0)
                    val version: String = pInfo.versionName
                    val versionCode: Long = PackageInfoCompat.getLongVersionCode(pInfo)
                    appName = "MDEngine $version\nVerCode $versionCode"

                } catch (e: PackageManager.NameNotFoundException) {
                    Log.e(
                        "应用版本获取",
                        "捕获到 PackageManager.NameNotFoundException 异常：${e.message}"
                    )
                }
            }

            val dialogView: View =
                LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_about_info, null)
            val dialog = DialogHelper.customDialog(requireContext(), dialogView).dialog
            val messageTextView: TextView = dialogView.findViewById(R.id.confirm_message)
            val titleTextView: TextView = dialogView.findViewById(R.id.confirm_title)

            val htmlText: String = getString(R.string.menu_about_context)
            messageTextView.text = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
            messageTextView.movementMethod = LinkMovementMethod.getInstance()
            titleTextView.text = appName

            dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
                dialog.dismiss()
            }
            dialogView.findViewById<View>(R.id.btn_more).setOnClickListener {
                startActivity(Intent(requireActivity(), ActivityAbout::class.java))
                dialog.dismiss()
            }
            return dialog
        }
    }
}