package mapleleaf.materialdesign.engine.modify

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.shell.PropsUtils
import mapleleaf.materialdesign.engine.services.CompileService
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.utils.CommonCommands
import mapleleaf.materialdesign.engine.utils.toast

/**
 * Created by Hello on 2018/02/20.
 */

class DexCompileModify(private var context: UniversalActivityBase) : ModifyBase(context) {

    @SuppressLint("ObsoleteSdkInt")
    private fun isSupport(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            toast("系统版本过低，至少需要Android 7.0！")
            return false
        }
        return true
    }

    private fun triggerCompile(action: String) {
        if (CompileService.compiling) {
            toast("有一个后台编译过程正在进行，不能重复开启")
        } else {
            try {
                val service = Intent(context, CompileService::class.java)
                service.action = action
                context.startService(service)
                toast("开始后台编译，请查看通知了解进度")
            } catch (ex: java.lang.Exception) {
                toast("启动后台过程失败")
            }
        }
    }

    //增加进度显示，而且不再出现因为编译应用自身而退出
    private fun run2() {
        if (!isSupport()) {
            return
        }
        if (CompileService.compiling) {
            toast("有一个后台编译过程正在进行~")
            return
        }

        val dialogView = context.layoutInflater.inflate(R.layout.dialog_system_modify_compile, null)
        val dialog = DialogHelper.customDialog(context, dialogView)
        dialogView.findViewById<View>(R.id.mode_speed_profile).setOnClickListener {
            dialog.dismiss()
            triggerCompile(context.getString(R.string.engine_speed_profile_compile))
        }
        dialogView.findViewById<View>(R.id.mode_speed).setOnClickListener {
            dialog.dismiss()
            triggerCompile(context.getString(R.string.engine_speed_compile))
        }
        dialogView.findViewById<View>(R.id.mode_everything).setOnClickListener {
            dialog.dismiss()
            triggerCompile(context.getString(R.string.engine_everything_compile))
        }
        dialogView.findViewById<View>(R.id.mode_reset).setOnClickListener {
            dialog.dismiss()
            triggerCompile(context.getString(R.string.engine_reset_compile))
        }
        dialogView.findViewById<View>(R.id.faq).setOnClickListener {
            dialog.dismiss()
            toast("此页面，在国内(CN)可能需要“虚拟专用网络”才能正常访问")

            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(context.getString(R.string.system_modify_dex2oat_help))
                )
            )
        }
    }

    override fun run() {
        run2()
    }

    private fun modifyConfigOld() {
        val arr = arrayOf(
            "verify",
            "speed",
            "恢复默认"
        )
        val installMode = PropsUtils.getProp("dalvik.vm.dex2oat-filter")
        var index = 0
        when (installMode) {
            "interpret-only" -> index = 0
            "speed" -> index = 1
        }
        DialogHelper.animDialog(AlertDialog.Builder(context)
            .setTitle("请选择Dex2oat配置")
            .setSingleChoiceItems(arr, index) { _, which ->
                index = which
            }
            .setNegativeButton("确定") { _, _ ->
                val stringBuilder = StringBuilder()

                //移除已添加的配置
                stringBuilder.append("sed '/^dalvik.vm.image-dex2oat-filter=/'d /system/build.prop > /data/build.prop;")
                stringBuilder.append("sed -i '/^dalvik.vm.dex2oat-filter=/'d /data/build.prop;")

                when (index) {
                    0 -> {
                        stringBuilder.append("sed -i '\$adalvik.vm.image-dex2oat-filter=interpret-only' /data/build.prop;")
                        stringBuilder.append("sed -i '\$adalvik.vm.dex2oat-filter=interpret-only' /data/build.prop;")
                    }

                    1 -> {
                        stringBuilder.append("sed -i '\$adalvik.vm.image-dex2oat-filter=speed' /data/build.prop;")
                        stringBuilder.append("sed -i '\$adalvik.vm.dex2oat-filter=speed' /data/build.prop;")
                    }
                }

                stringBuilder.append(CommonCommands.MountSystemRW)
                stringBuilder.append("cp /system/build.prop /system/build.prop.${System.currentTimeMillis()}\n")
                stringBuilder.append("cp /data/build.prop /system/build.prop\n")
                stringBuilder.append("rm /data/build.prop\n")
                stringBuilder.append("chmod 0755 /system/build.prop\n")

                execShell(stringBuilder)
                toast("配置已修改，但需要重启才能生效！")
            }
            .setNeutralButton("查看说明") { _, _ ->
                DialogHelper.animDialog(
                    AlertDialog.Builder(context).setTitle("说明")
                        .setMessage("interpret-only模式安装应用更快。speed模式安装应用将会很慢，但是运行速度更快。")
                )
            })
    }

}
