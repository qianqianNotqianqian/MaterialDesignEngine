package mapleleaf.materialdesign.engine.modify

import android.app.Activity
import mapleleaf.materialdesign.engine.ui.dialog.DialogProgressBar

/**
 * Created by Hello on 2018/02/20.
 */

open class ModifyBase(private var context: Activity) : DialogProgressBar(context) {
    var title: String? = null
    var desc: String? = null
    var command: String? = null

    open fun run() {
        if (command != null) {
            execShell(command!!)
        }
    }
}
