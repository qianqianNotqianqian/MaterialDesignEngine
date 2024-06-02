package mapleleaf.materialdesign.engine.utils.clickListener

import android.content.Context
import android.view.View
import android.webkit.WebView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.PopTip.tip
import com.kongzue.dialogx.interfaces.DialogXRunnable
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.asynctask.ImageSaver
import mapleleaf.materialdesign.engine.utils.toast

class WebViewLongClickListener(private val context: Context, private val imageSaver: ImageSaver) :
    View.OnLongClickListener {
    override fun onLongClick(view: View): Boolean {
        val hitTestResult = (view as WebView).hitTestResult

        if (hitTestResult.type == WebView.HitTestResult.IMAGE_TYPE ||
            hitTestResult.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
        ) {
            MessageDialog.show("提示", "是否保存图片到本地？", "确定", "取消")
                .setOkButton { _, _ ->
                    val saveImgUrl = hitTestResult.extra
                    if (!imageSaver.isRunning) {
                        imageSaver.saveImage(saveImgUrl)
                    } else {
                        toast("已经在运行")
                    }
                    false
                }
                .setCancelButton { _, v ->
                    false
                }

            return true
        }
        return false
    }
}
