package mapleleaf.materialdesign.engine.utils.clickListener

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.widget.TextView
import com.kongzue.dialogx.dialogs.MessageDialog
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.asynctask.ImageSaver
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.utils.toast

class WebViewLongClickListener(private val context: Context, private val imageSaver: ImageSaver) :
    View.OnLongClickListener {
    override fun onLongClick(view: View): Boolean {
        val hitTestResult = (view as WebView).hitTestResult

        if (hitTestResult.type == WebView.HitTestResult.IMAGE_TYPE ||
            hitTestResult.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
        ) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm, null)
            val dialog = DialogHelper.customDialog(context, dialogView)

            dialogView.findViewById<TextView>(R.id.confirm_message).text = "是否保存图片到本地？"

            dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
                dialog.dismiss()
                val saveImgUrl = hitTestResult.extra
                if (!imageSaver.isRunning) {
                    imageSaver.saveImage(saveImgUrl)
                } else {
                    toast("已经在运行")
                }
            }
            dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
            }
            return true
        }
        return false
    }
}
