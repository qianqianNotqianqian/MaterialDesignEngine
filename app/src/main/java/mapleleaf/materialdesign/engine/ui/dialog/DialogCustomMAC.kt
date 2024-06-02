package mapleleaf.materialdesign.engine.ui.dialog

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.shared.RawText
import mapleleaf.materialdesign.engine.shell.KeepShellPublic
import mapleleaf.materialdesign.engine.store.SpfConfig
import mapleleaf.materialdesign.engine.utils.toast

/**
 * Created by Hello on 2018/01/17.
 */

class DialogCustomMAC(private var context: Context) {
    private var spf: SharedPreferences? = null

    init {
        spf = context.getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE)
    }

    fun modifyMAC(mode: Int) {
        val layoutInflater = LayoutInflater.from(context)
        val dialog = layoutInflater.inflate(R.layout.dialog_system_modify_mac, null)
        val macInput: EditText = dialog.findViewById(R.id.dialog_system_modify_mac_input)
        val autoChange: CheckBox = dialog.findViewById(R.id.dialog_system_modify_mac_autochange)
        macInput.setText(spf!!.getString(SpfConfig.GLOBAL_SPF_MAC, "ec:d0:9f:af:95:01"))

        autoChange.isChecked =
            spf!!.getInt(SpfConfig.GLOBAL_SPF_MAC_AUTOCHANGE_MODE, 0).equals(mode)
        autoChange.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                spf!!.edit().putInt(SpfConfig.GLOBAL_SPF_MAC_AUTOCHANGE_MODE, mode).apply()
            } else {
                spf!!.edit().remove(SpfConfig.GLOBAL_SPF_MAC_AUTOCHANGE_MODE).apply()
            }
        }

        DialogHelper.confirm(context, "自定义WIFI MAC", "", dialog, {
            val mac = macInput.text.trim().replace(Regex("-"), ":").toLowerCase()
            if (!Regex(
                    "[\\w\\d]{2}:[\\w\\d]{2}:[\\w\\d]{2}:[\\w\\d]{2}:[\\w\\d]{2}:[\\w\\d]{2}$",
                    RegexOption.IGNORE_CASE
                ).matches(mac)
            ) {
                toast("输入的MAC地址无效，格式应如 ec:d0:9f:af:95:01")
                return@confirm
            }

            val raw = when (mode) {
                SpfConfig.GLOBAL_SPF_MAC_AUTOCHANGE_MODE_1 -> {
                    RawText.getRawText(context, R.raw.change_mac_1)
                }

                SpfConfig.GLOBAL_SPF_MAC_AUTOCHANGE_MODE_2 -> {
                    RawText.getRawText(context, R.raw.change_mac_2)
                }

                else -> {
                    RawText.getRawText(context, R.raw.change_mac_1)
                }
            }
            val shell = "mac=\"$mac\"\n$raw"
            val r = KeepShellPublic.doCmdSync(shell)
            if (r == "error") {
                toast("修改失败！")
            } else {
                toast("MAC已修改")
                spf!!.edit().putString(SpfConfig.GLOBAL_SPF_MAC, mac).apply()
            }
        })
    }
}
