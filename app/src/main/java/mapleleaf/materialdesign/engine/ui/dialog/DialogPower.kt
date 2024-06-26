package mapleleaf.materialdesign.engine.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.widget.TextView
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.shell.KeepShellPublic

class DialogPower(var context: Activity) {
    @SuppressLint("InflateParams")
    fun showPowerMenu() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_power_operation, null)
        val dialog = DialogHelper.customDialog(context, dialogView)
        dialogView.findViewById<TextView>(R.id.confirm_title).text = "电源菜单"
        dialogView.findViewById<TextView>(R.id.confirm_message).text = "选择电源控制方式"

        dialogView.findViewById<View>(R.id.power_shutdown).setOnClickListener {
            dialog.dismiss()
            KeepShellPublic.doCmdSync(context.getString(R.string.power_shutdown_cmd))
        }
        dialogView.findViewById<View>(R.id.power_reboot).setOnClickListener {
            dialog.dismiss()
            KeepShellPublic.doCmdSync(context.getString(R.string.power_reboot_cmd))
        }
        dialogView.findViewById<View>(R.id.power_hot_reboot).setOnClickListener {
            dialog.dismiss()
            KeepShellPublic.doCmdSync(context.getString(R.string.power_hot_reboot_cmd))
        }

        dialogView.findViewById<View>(R.id.power_recovery).setOnClickListener {
            dialog.dismiss()
            KeepShellPublic.doCmdSync(context.getString(R.string.power_recovery_cmd))
        }
        dialogView.findViewById<View>(R.id.power_fastboot).setOnClickListener {
            dialog.dismiss()
            KeepShellPublic.doCmdSync(context.getString(R.string.power_fastboot_cmd))
        }
        dialogView.findViewById<View>(R.id.power_emergency_detail).setOnClickListener {
            dialog.dismiss()
            showEmergencyPowerMenu()
        }
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun showEmergencyPowerMenu() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_power_operation_warn, null)
        val dialog = DialogHelper.customDialog(context, dialogView)
        dialogView.findViewById<TextView>(R.id.confirm_title).text = "重启到9008模式"
        dialogView.findViewById<TextView>(R.id.confirm_message).text =
            "确认要重启到9008模式（DEL模式）？该功能可能极度危险，因为它可能造成设备无限重启，请谨慎使用"
        dialogView.findViewById<View>(R.id.power_emergency).setOnClickListener {
            dialog.dismiss()
            KeepShellPublic.doCmdSync(context.getString(R.string.power_emergency_cmd))
        }
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
    }
}
