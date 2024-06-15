package mapleleaf.materialdesign.engine.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.widget.TextView
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.shell.AsyncSuShellUnit
import mapleleaf.materialdesign.engine.utils.toast

open class DialogProgressBar(private var context: Activity, private var uniqueId: String? = null) {
    private var alert: DialogHelper.DialogWrap? = null
    private var textView: TextView? = null

    companion object {
        private val dialogs = LinkedHashMap<String, DialogHelper.DialogWrap>()
    }

    init {
        hideDialog()
    }

    class DefaultHandler(private var alertDialog: DialogHelper.DialogWrap?) :
        Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            try {
                if (alertDialog == null) {
                    return
                }
                if (msg.what == 10) {
                    alertDialog!!.dismiss()
                    alertDialog!!.hide()
                    if (msg.obj == true) {
                        toast("执行成功")
                    } else {
                        toast("执行失败")
                    }
                } else if (msg.what == -1) {
                    toast("执行失败")
                } else if (msg.what == 0 && msg.obj == false) {
                    alertDialog!!.dismiss()
                    alertDialog!!.hide()
                    toast("执行失败")
                }
            } catch (ex: Exception) {
            }
        }
    }

    @SuppressLint("InflateParams")
    public fun execShell(cmd: String, handler: Handler? = null) {
        hideDialog()

        val layoutInflater = LayoutInflater.from(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
        val textView: TextView = (dialogView.findViewById(R.id.dialog_text))
        textView.text = context.getString(R.string.execute_wait)
        alert = DialogHelper.customDialog(context, dialogView)
        // AlertDialog.Builder(context).setView(dialog).setCancelable(false).create()
        if (handler == null) {
            AsyncSuShellUnit(DefaultHandler(alert)).exec(cmd).waitFor()
        } else {
            AsyncSuShellUnit(handler).exec(cmd).waitFor()
        }
    }

    public fun execShell(sb: StringBuilder, handler: Handler? = null) {
        execShell(sb.toString(), handler)
    }

    public fun isDialogShow(): Boolean {
        return this.alert != null
    }

    public fun hideDialog() {
        try {
            if (alert != null) {
                alert!!.dismiss()
                alert!!.hide()
                alert = null
            }
        } catch (ex: Exception) {
        }

        uniqueId?.run {
            if (dialogs.containsKey(this)) {
                dialogs.remove(this)
            }
        }
    }

    @SuppressLint("InflateParams")
    fun showDialog(text: String = "正在加载，请稍等..."): DialogProgressBar {
        if (textView != null && alert != null) {
            textView!!.text = text
        } else {
            hideDialog()
            val layoutInflater = LayoutInflater.from(context)
            val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
            textView = (dialogView.findViewById(R.id.dialog_text)!!)
            textView!!.text = text
            alert = DialogHelper.customDialog(context, dialogView, false)
            // AlertDialog.Builder(context).setView(dialog).setCancelable(false).create()
        }

        uniqueId?.run {
            if (dialogs.containsKey(this)) {
                dialogs.remove(this)
            }
            if (alert != null) {
                dialogs[this] = alert!!
            }
        }

        return this
    }
}
