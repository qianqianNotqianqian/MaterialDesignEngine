package mapleleaf.materialdesign.engine.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.interfaces.BottomDialogSlideEventLifecycleCallback
import com.kongzue.dialogx.interfaces.OnBindView
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.popup.FloatMonitor
import mapleleaf.materialdesign.engine.popup.FloatMonitorMini
import mapleleaf.materialdesign.engine.popup.FloatTaskManager
import mapleleaf.materialdesign.engine.shell.KeepShellPublic
import mapleleaf.materialdesign.engine.utils.toast

class DialogPower(var context: Activity) {
    @SuppressLint("InflateParams")
    fun showPowerMenu() {
        MessageDialog.show(null,null, null, "取消")
            .setDialogLifecycleCallback(object : BottomDialogSlideEventLifecycleCallback<MessageDialog>() {
                override fun onShow(dialog: MessageDialog) {
                    super.onShow(dialog)
                    dialog.dialogImpl.txtDialogTip.setPadding(0, 6, 0, 0)
                }
            })
            .setCustomView(object : OnBindView<MessageDialog>(R.layout.dialog_power_operation) {
                override fun onBind(dialog: MessageDialog, view: View) {
                    view.findViewById<View>(R.id.power_shutdown).setOnClickListener {
                        dialog.dismiss()
                        KeepShellPublic.doCmdSync(context.getString(R.string.power_shutdown_cmd))
                    }
                    view.findViewById<View>(R.id.power_reboot).setOnClickListener {
                        dialog.dismiss()
                        KeepShellPublic.doCmdSync(context.getString(R.string.power_reboot_cmd))
                    }
                    view.findViewById<View>(R.id.power_hot_reboot).setOnClickListener {
                        dialog.dismiss()
                        KeepShellPublic.doCmdSync(context.getString(R.string.power_hot_reboot_cmd))
                    }

                    view.findViewById<View>(R.id.power_recovery).setOnClickListener {
                        dialog.dismiss()
                        KeepShellPublic.doCmdSync(context.getString(R.string.power_recovery_cmd))
                    }
                    view.findViewById<View>(R.id.power_fastboot).setOnClickListener {
                        dialog.dismiss()
                        KeepShellPublic.doCmdSync(context.getString(R.string.power_fastboot_cmd))
                    }
                    view.findViewById<View>(R.id.power_emergency_detail).setOnClickListener {
                        dialog.dismiss()
                        showEmergencyPowerMenu()
                    }
                }
            })
            .setCancelButton { dialog, v ->
                false
            }

    }

    private fun showEmergencyPowerMenu() {
        MessageDialog.show("重启到9008模式", "确认要重启到9008模式（DEL模式）？该功能可能极度危险，因为它可能造成设备无限重启，请谨慎使用", null, "取消")
            .setDialogLifecycleCallback(object : BottomDialogSlideEventLifecycleCallback<MessageDialog>() {
                override fun onShow(dialog: MessageDialog) {
                    super.onShow(dialog)
                    dialog.dialogImpl.txtDialogTip.setPadding(0, 6, 0, 0)
                }
            })
            .setCustomView(object : OnBindView<MessageDialog>(R.layout.dialog_power_operation_warn) {
                override fun onBind(dialog: MessageDialog, view: View) {
                    view.findViewById<View>(R.id.power_emergency).setOnClickListener {
                        dialog.dismiss()
                        KeepShellPublic.doCmdSync(context.getString(R.string.power_emergency_cmd))
                    }
                }
            })
            .setCancelButton { dialog, v ->
                false
            }
    }
}
