package mapleleaf.materialdesign.engine.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.CompoundButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.interfaces.BottomDialogSlideEventLifecycleCallback
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.util.TextInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.popup.FloatMonitor
import mapleleaf.materialdesign.engine.popup.FloatMonitorMini
import mapleleaf.materialdesign.engine.popup.FloatTaskManager
import mapleleaf.materialdesign.engine.ui.activities.ActivityAbout
import mapleleaf.materialdesign.engine.ui.activities.ActivityMenu
import mapleleaf.materialdesign.engine.utils.toast

class DialogMonitor(var context: Activity) {
    @SuppressLint("InflateParams")
    fun show() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_float_monitor, null)
        val dialog = DialogHelper.customDialog(context, dialogView)
        dialogView.findViewById<TextView>(R.id.confirm_title).text = "选择监视器"
        dialogView.findViewById<TextView>(R.id.confirm_message).text = "监控系统占用情况及其温度等详细信息"

        dialogView.findViewById<CompoundButton>(R.id.monitor_perf).run {
            isChecked = FloatMonitor.show == true
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    FloatMonitor(context).showPopupWindow()
                } else {
                    FloatMonitor(context).hidePopupWindow()
                }
            }
        }
        dialogView.findViewById<CompoundButton>(R.id.monitor_proc).run {
            isChecked = FloatTaskManager.show == true
            // 合并滑动事件监听器和点击事件监听器
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val floatTaskManager = FloatTaskManager(context)
                    if (floatTaskManager.supported) {
                        FloatTaskManager(context).showPopupWindow()
                    } else {
                        toast(context.getString(R.string.monitor_process_unsupported))
                        // 如果不支持，则将开关状态改为关闭
                        this.isChecked = false
                    }
                } else {
                    FloatTaskManager(context).hidePopupWindow()
                }
            }
        }

        dialogView.findViewById<CompoundButton>(R.id.monitor_game).run {
            isChecked = FloatMonitorMini.show == true
            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    FloatMonitorMini(context).showPopupWindow()
                } else {
                    FloatMonitorMini(context).hidePopupWindow()
                }
            }
        }
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
    }
}
