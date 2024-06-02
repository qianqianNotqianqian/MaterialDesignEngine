package mapleleaf.materialdesign.engine.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.UserManager
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.model.AppInfo
import mapleleaf.materialdesign.engine.shared.MagiskExtend
import mapleleaf.materialdesign.engine.shell.AsynSuShellUnit
import mapleleaf.materialdesign.engine.shell.KeepShell
import mapleleaf.materialdesign.engine.utils.CommonCommands
import mapleleaf.materialdesign.engine.utils.RootChecker
import mapleleaf.materialdesign.engine.utils.toast
import java.io.File
import java.io.IOException
import java.util.Locale

/**
 * Created by helloklf on 2017/12/04.
 */
open class DialogAppOptions(
    protected var context: Activity,
    protected var apps: ArrayList<AppInfo>,
    protected var handler: Handler,
) {
    private var allowPigz = false
    private var userdataPath = ""
    val rootChecker = RootChecker()

    companion object {
        private const val PROGRESS_UPDATE = 1
        private const val TASK_COMPLETE = 2
    }

    init {
        userdataPath = context.filesDir.absolutePath
        userdataPath = userdataPath.substring(0, userdataPath.indexOf(context.packageName) - 1)
    }

    fun selectUserAppOptions() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_app_options_user, null)

        val dialog = DialogHelper.customDialog(context, dialogView)
        dialogView.findViewById<View>(R.id.app_options_single_only).visibility = View.GONE
        dialogView.findViewById<View>(R.id.app_options_clear).setOnClickListener {
            dialog.dismiss()
            if (rootChecker.isDeviceRooted()) {
                clearAll()
            } else {
                toast("设备未获取Root权限")
            }
        }

        dialogView.findViewById<View>(R.id.app_options_uninstall).setOnClickListener {
            dialog.dismiss()
            if (rootChecker.isDeviceRooted()) {
                uninstallAll()
            } else {
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<View>(R.id.app_options_dex2oat).setOnClickListener {
            dialog.dismiss()
            if (rootChecker.isDeviceRooted()) {
                buildAll()
            } else {
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<TextView>(R.id.app_options_title).text = "请选择操作"

        dialogView.findViewById<View>(R.id.app_options_app_freeze).setOnClickListener {
            dialog.dismiss()
            if (rootChecker.isDeviceRooted()) {
                modifyStateAll()
            } else {
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<View>(R.id.app_options_app_share).setOnClickListener {
            dialog.dismiss()
            shareAllApp(apps)
        }
        dialogView.findViewById<View>(R.id.app_options_extra_package).setOnClickListener {
            dialog.dismiss()
            if (rootChecker.isDeviceRooted()) {
                extraAllPackage(apps)
            } else {
                saveApkToLocal(apps)
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
    }

    fun selectSystemAppOptions() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_app_options_system, null)

        val dialog = DialogHelper.customDialog(context, dialogView)
        dialogView.findViewById<View>(R.id.app_options_single_only).visibility = View.GONE
        dialogView.findViewById<View>(R.id.app_options_clear).setOnClickListener {
            dialog.dismiss()
            if (rootChecker.isDeviceRooted()) {
                clearAll()
            } else {
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<View>(R.id.app_options_uninstall_user).setOnClickListener {
            dialog.dismiss()
            if (rootChecker.isDeviceRooted()) {
                uninstallAllSystem(false) // TODO:xxx
            } else {
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<View>(R.id.app_options_dex2oat).setOnClickListener {
            dialog.dismiss()
            if (rootChecker.isDeviceRooted()) {
                buildAll()
            } else {
                toast("设备未获取Root权限")
            }
        }

        dialogView.findViewById<View>(R.id.app_options_delete).setOnClickListener {
            dialog.dismiss()
            if (rootChecker.isDeviceRooted()) {
                deleteAll()
            } else {
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<View>(R.id.app_options_uninstall).visibility = View.GONE

        dialogView.findViewById<TextView>(R.id.app_options_title).setText("请选择操作")

        dialogView.findViewById<View>(R.id.app_options_app_freeze).setOnClickListener {
            dialog.dismiss()
            if (rootChecker.isDeviceRooted()) {
                modifyStateAll()
            } else {
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<View>(R.id.app_options_app_share).setOnClickListener {
            dialog.dismiss()
            shareAllApp(apps)
        }
        dialogView.findViewById<View>(R.id.app_options_extra_package).setOnClickListener {
            dialog.dismiss()
            if (rootChecker.isDeviceRooted()) {
                extraAllPackage(apps)
            } else {
                saveApkToLocal(apps)
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun shareAllApp(apps: ArrayList<AppInfo>) {
        // 创建 Intent.ACTION_SEND_MULTIPLE Intent
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        shareIntent.type = "application/vnd.android.package-archive"

        // 创建一个包含所有 APK 文件 URI 的列表
        val apkUris = ArrayList<Uri>()
        for (app in apps) {
            try {
                val appInfo = context.packageManager.getApplicationInfo(app.packageName, 0)
                val apkFile = File(appInfo.sourceDir)
                val apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.shareAPK",
                    apkFile
                )
                apkUris.add(apkUri)
            } catch (e: PackageManager.NameNotFoundException) {
                toast("未找到某些应用程序")
            }
        }

        // 将 APK 文件 URI 列表设置为 Intent 的 EXTRA_STREAM
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, apkUris)

        // 启动分享 Intent
        try {
            context.startActivity(Intent.createChooser(shareIntent, "分享选中的应用"))
        } catch (e: ActivityNotFoundException) {
            toast("未找到可以分享的应用程序")
        }
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun extraAllPackage(apps: ArrayList<AppInfo>) {
        val layoutInflater = LayoutInflater.from(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
        val textView: TextView = (dialogView.findViewById(R.id.dialog_text))
        val progressBar: ProgressBar = (dialogView.findViewById(R.id.dialog_app_details_progress))
        progressBar.visibility = View.VISIBLE
        val alert = DialogHelper.customDialog(context, dialogView)

        Thread {
            val sb = StringBuilder()
            for (app in apps) {
                val appName = app.appName
                textView.post {
                    textView.text = "正在提取 $appName 的安装包"
                }
                sb.append("echo '正在提取 ${app.appName}'\n")

                try {
                    val appInfo = context.packageManager.getApplicationInfo(app.packageName, 0)
                    val apkFile = File(appInfo.sourceDir)

                    // 获取应用的Android/data目录
                    val dataDir = context.getExternalFilesDir(null)

                    // 获取应用的名称和版本号
                    val appName = app.appName.replace(" ", "_")
                    val versionName = app.versionName

                    // 构建新的文件名
                    val newFileName = "$appName-$versionName.apk"

                    // 将APK文件复制到Android/data目录下并重命名
                    val destFile = File(dataDir, newFileName)
                    apkFile.copyTo(destFile, true)

                } catch (e: PackageManager.NameNotFoundException) {
                    sb.append("echo '未找到该应用程序'\n")
                } catch (e: Exception) {
                    sb.append("echo '提取APK文件失败'\n")
                }
            }

            sb.append("echo '[operation completed]'\n")

            // Execute shell commands
            AsynSuShellUnit(ProgressHandlerExtra(dialogView, alert, handler)).exec(sb.toString())
                .waitFor()
        }.start()
    }

    private fun saveApkToLocal(apps: ArrayList<AppInfo>) {
        GlobalScope.launch(Dispatchers.Main) {
            val layoutInflater = LayoutInflater.from(context)
            val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
            val textView: TextView = (dialogView.findViewById(R.id.dialog_text))
            val progressBar: ProgressBar =
                (dialogView.findViewById(R.id.dialog_app_details_progress))
            progressBar.visibility = View.VISIBLE

            val alert = DialogHelper.customDialog(context, dialogView)

            withContext(Dispatchers.IO) {
                try {
                    for (app in apps) {
                        val packageInfo: PackageInfo =
                            context.packageManager.getPackageInfo(app.packageName, 0)
                        val apkPath = packageInfo.applicationInfo.sourceDir
                        val sourceFile = File(apkPath)
                        val destinationFile =
                            File(context.getExternalFilesDir(null), "${app.packageName}.apk")

                        // 显示加载对话框
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.VISIBLE
                            textView.text = "正在保存安装包..."
                        }

                        // 复制 APK 文件
                        sourceFile.copyTo(destinationFile, true)

                        // 隐藏加载对话框
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.GONE
                            alert.dismiss()
                            toast("安装包已保存到 ${destinationFile.absolutePath}")
                        }
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        toast("无法获取应用信息")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        toast("保存安装包失败")
                    }
                }
            }
        }
    }

    private fun isMagisk(): Boolean {
        val keepShell = KeepShell(false)
        val result =
            keepShell.doCmdSync("su -v").toUpperCase(Locale.getDefault()).contains("MAGISKSU")
        keepShell.tryExit()
        return result
    }

    private fun isTmpfs(dir: String): Boolean {
        val keepShell = KeepShell(false)
        val result =
            keepShell.doCmdSync("df | grep tmpfs | grep \"$dir\"").toUpperCase(Locale.getDefault())
                .trim().isNotEmpty()
        keepShell.tryExit()
        return result
    }

    @SuppressLint("InflateParams")
    private fun execShell(sb: StringBuilder) {
        val layoutInflater = LayoutInflater.from(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
        val textView: TextView = (dialogView.findViewById(R.id.dialog_text))
        textView.text = "正在获取权限"
        val alert = DialogHelper.customDialog(context, dialogView)
        AsynSuShellUnit(ProgressHandler(dialogView, alert, handler)).exec(sb.toString()).waitFor()
    }

    open class ProgressHandler(
        dialog: View,
        protected var alert: DialogHelper.DialogWrap,
        protected var handler: Handler,
    ) : Handler(Looper.getMainLooper()) {
        private var textView: TextView = dialog.findViewById(R.id.dialog_text)
        var progressBar: ProgressBar = dialog.findViewById(R.id.dialog_app_details_progress)
        private var error = java.lang.StringBuilder()

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.obj != null) {
                if (msg.what == 0) {
                    textView.text = "正在执行操作..."
                } else if (msg.what == 5) {
                    error.append(msg.obj)
                    error.append("\n")
                } else if (msg.what == 10) {
                    if (msg.obj == true) {
                        textView.text = "操作完成！"
                    } else {
                        textView.text = "出现异常！"
                    }
                    handler.postDelayed({
                        alert.dismiss()
                        alert.hide()
                    }, 2000)
                } else {
                    val obj = msg.obj.toString()
                    if (obj.contains("[operation completed]")) {
                        progressBar.progress = 100
                        textView.text = "操作完成！"
                        handler.postDelayed({
                            try {
                                alert.dismiss()
                                alert.hide()
                            } catch (ex: Exception) {
                            }
                            if (error.isNotBlank()) {
                                val context: Context = alert.context
                                DialogHelper.alert(context, "出现了一些错误", error.toString())
                            }
                        }, 1200)
                        handler.handleMessage(handler.obtainMessage(2))
                    } else if (Regex("^\\[.*]\$").matches(obj)) {
                        progressBar.progress = msg.what
                        val txt = obj
                            .replace("[copy ", "[复制 ")
                            .replace("[uninstall ", "[卸载 ")
                            .replace("[install ", "[安装 ")
                            .replace("[restore ", "[还原 ")
                            .replace("[backup ", "[备份 ")
                            .replace("[unhide ", "[显示 ")
                            .replace("[hide ", "[隐藏 ")
                            .replace("[delete ", "[删除 ")
                            .replace("[disable ", "[禁用 ")
                            .replace("[enable ", "[启用 ")
                            .replace("[trim caches ", "[清除缓存 ")
                            .replace("[clear ", "[清除数据 ")
                            .replace("[skip ", "[跳过 ")
                            .replace("[link ", "[链接 ")
                            .replace("[compile ", "[编译 ")
                        textView.text = txt
                    }
                }
            }
        }

        init {
            textView.text = "正在获取权限"
        }
    }

    open class ProgressHandlerExtra(
        dialog: View,
        protected var alert: DialogHelper.DialogWrap,
        protected var handler: Handler,
    ) : Handler(Looper.getMainLooper()) {
        private var textView: TextView = (dialog.findViewById(R.id.dialog_text) as TextView)
        var progressBar: ProgressBar =
            (dialog.findViewById(R.id.dialog_app_details_progress) as ProgressBar)
        private var error = java.lang.StringBuilder()

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.obj != null) {
                if (msg.what == 0) {
                    textView.text = "正在执行操作..."
                } else if (msg.what == 5) {
                    error.append(msg.obj)
                    error.append("\n")
                } else if (msg.what == 10) {
                    if (msg.obj == true) {
                        textView.text = "操作完成！"
                    } else {
                        textView.text = "出现异常！"
                    }
                    handler.postDelayed({
                        alert.dismiss()
                        alert.hide()
                    }, 2000)
                } else {
                    val obj = msg.obj.toString()
                    if (obj.contains("[operation completed]")) {
                        progressBar.progress = 100
                        textView.text = "操作完成！"
                        handler.postDelayed({
                            try {
                                alert.dismiss()
                                alert.hide()
                            } catch (ex: Exception) {
                            }
                            if (error.isNotBlank()) {
                                val context: Context = alert.context
                                DialogHelper.alert(context, "出现了一些错误", error.toString())
                            }
                        }, 1200)
                        handler.handleMessage(handler.obtainMessage(2))
                    }
                }
            }
        }
    }

    private fun confirm(title: String, msg: String, next: Runnable?) {
        DialogHelper.confirmBlur(context, title, msg, next)
    }

    /**
     * 禁用所选的应用
     */
    @SuppressLint("SetTextI18n")
    protected fun modifyStateAll() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_app_status, null)
        val dialog = DialogHelper.customDialog(context, dialogView)
        dialogView.findViewById<TextView>(R.id.confirm_message).text =
            "选中了 ${apps.size} 个应用，你希望把它们的状态改成？"

        val switchSuspend = dialogView.findViewById<CompoundButton>(R.id.disable_suspend)
        val switchFreeze = dialogView.findViewById<CompoundButton>(R.id.disable_freeze)
        val switchHide = dialogView.findViewById<CompoundButton>(R.id.disable_hide)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            switchSuspend.isEnabled = false
            switchSuspend.isEnabled = true
        }
        switchSuspend.isChecked = apps.filter { it.suspended }.size == apps.size
        switchFreeze.isChecked = apps.filter { !it.enabled }.size == apps.size

        dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()

            val suspend = switchSuspend.isChecked
            val freeze = switchFreeze.isChecked
            val hide = switchHide.isChecked
            _modifyStateAll(suspend, freeze, hide)
        }
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun _modifyStateAll(suspend: Boolean, freeze: Boolean, hide: Boolean) {
        val androidP = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        val sb = StringBuilder()
        for (item in apps) {
            val packageName = item.packageName.toString()
            if (suspend) {
                if (!item.suspended) {
                    sb.append("echo '[suspend ${item.appName}]'\n")
                    sb.append("pm suspend $packageName\n")
                }
            } else if (androidP) {
                if (item.suspended) {
                    sb.append("echo '[unsuspend ${item.appName}]'\n")
                    sb.append("am kill $packageName 2>/dev/null\n")
                    sb.append("pm unsuspend $packageName\n")
                }
            }

            if (freeze) {
                if (item.enabled) {
                    sb.append("echo '[disable ${item.appName}]'\n")
                    sb.append("pm disable ${packageName}\n")
                }
            } else {
                if (!item.enabled) {
                    sb.append("echo '[enable ${item.appName}]'\n")
                    sb.append("pm enable ${packageName}\n")
                }
            }

            if (hide) {
                sb.append("echo '[hide ${item.appName}]'\n")
                sb.append("pm hide ${packageName}\n")
            }

        }

        sb.append("echo '[operation completed]'\n")
        execShell(sb)
    }

    /**
     * 删除选中的应用
     */
    protected fun deleteAll() {
        confirm(
            "删除应用",
            "已选择 ${apps.size} 个应用，删除系统应用可能导致功能不正常，甚至无法开机，确定要继续删除？"
        ) {
            if (isMagisk() && !MagiskExtend.moduleInstalled() && (isTmpfs("/system/app") || isTmpfs(
                    "/system/priv-app"
                ))
            ) {
                DialogHelper.confirm(
                    context,
                    "Magisk 副作用警告",
                    "检测到你正在使用Magisk作为ROOT权限管理器，并且/system/app和/system/priv-app目录已被某些模块修改，这可能导致这些目录被Magisk劫持并且无法写入！！",
                    DialogHelper.DialogButton(context.getString(R.string.btn_continue), {
                        _deleteAll()
                    })
                )
            } else {
                _deleteAll()
            }
        }
    }

    private fun _deleteAll() {
        val sb = StringBuilder()
        sb.append(CommonCommands.MountSystemRW)
        var useMagisk = false
        for (item in apps) {
            val packageName = item.packageName.toString()
            // 先禁用再删除，避免老弹停止运行
            sb.append("echo '[disable ${item.appName}]'\n")
            sb.append("pm disable $packageName\n")

            sb.append("echo '[delete ${item.appName}]'\n")
            if (MagiskExtend.moduleInstalled()) {
                MagiskExtend.deleteSystemPath(item.path.toString())
                useMagisk = true
            } else {
                val dir = item.dir.toString()

                sb.append("rm -rf $dir/oat\n")
                sb.append("rm -rf $dir/lib\n")
                sb.append("rm -rf '${item.path}'\n")
            }
        }

        sb.append("echo '[operation completed]'\n")
        execShell(sb)
        if (useMagisk) {
            DialogHelper.helpInfo(context, "已通过Magisk完成操作，请重启手机~", "")
        }
    }

    /**
     * 清除数据
     */
    @SuppressLint("SetTextI18n")
    protected fun clearAll() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_app_clear_data, null)
        dialogView.findViewById<TextView>(R.id.confirm_message).text =
            "确定清除 ${apps.size} 个应用的数据？"
        val dialog = DialogHelper.customDialog(context, dialogView)
        val userOnly = dialogView.findViewById<CompoundButton>(R.id.clear_user_only)

        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()
            _clearAll(userOnly.isChecked)
        }
    }

    private fun _clearAll(userOnly: Boolean) {
        val um = context.getSystemService(Context.USER_SERVICE) as UserManager?
        val userHandle = android.os.Process.myUserHandle()
        var uid = 0L
        if (um != null) {
            uid = um.getSerialNumberForUser(userHandle)
        } else {
            toast("获取用户ID失败！")
            return
        }

        val sb = StringBuilder()
        for (item in apps) {
            val packageName = item.packageName.toString()
            sb.append("echo '[clear ${item.appName}]'\n")

            if (userOnly) {
                sb.append("pm clear --user $uid $packageName\n")
            } else {
                sb.append("pm clear $packageName\n")
            }
        }

        sb.append("echo '[operation completed]'\n")
        execShell(sb)
    }

    /**
     * 卸载选中
     */
    @SuppressLint("SetTextI18n")
    protected fun uninstallAll() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_app_uninstall, null)
        dialogView.findViewById<TextView>(R.id.confirm_message).text =
            "确定卸载选中的 ${apps.size} 个应用？"

        val dialog = DialogHelper.customDialog(context, dialogView)
        val userOnly = dialogView.findViewById<CompoundButton>(R.id.uninstall_user_only)
        val keepData = dialogView.findViewById<CompoundButton>(R.id.uninstall_keep_data)

        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()

            _uninstallAll(userOnly.isChecked, keepData.isChecked)
        }
    }

    /**
     * 卸载选中
     */
    @SuppressLint("SetTextI18n")
    protected fun uninstallAllSystem(updated: Boolean) {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_app_uninstall, null)
        dialogView.findViewById<TextView>(R.id.confirm_message).text =
            "确定卸载选中的 ${apps.size} 个系统应用？"

        val dialog = DialogHelper.customDialog(context, dialogView)
        val userOnly = dialogView.findViewById<CompoundButton>(R.id.uninstall_user_only)
        val keepData = dialogView.findViewById<CompoundButton>(R.id.uninstall_keep_data)

        userOnly.isEnabled = false
        if (updated) {
            userOnly.isEnabled = false
            keepData.isEnabled = false

            userOnly.isChecked = false
            keepData.isChecked = false
        } else {
            userOnly.isEnabled = false
            userOnly.isChecked = true
        }

        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()

            _uninstallAll(userOnly.isChecked, keepData.isChecked)
        }
    }

    private fun _uninstallAll(userOnly: Boolean, keepData: Boolean) {
        if (userOnly) {
            val um = context.getSystemService(Context.USER_SERVICE) as UserManager?
            val userHandle = android.os.Process.myUserHandle()
            if (um != null) {
                val uid = um.getSerialNumberForUser(userHandle)
                _uninstallAllOnlyUser(uid, keepData)
            } else {
                toast("获取用户ID失败！")
            }
        } else {
            val sb = StringBuilder()

            for (item in apps) {
                val packageName = item.packageName.toString()
                sb.append("echo '[uninstall ${item.appName}]'\n")

                if (keepData) {
                    sb.append("pm uninstall -k $packageName\n")
                } else {
                    sb.append("pm uninstall $packageName\n")
                }
            }

            sb.append("echo '[operation completed]'\n")
            execShell(sb)
        }
    }

    private fun _uninstallAllOnlyUser(uid: Long, keepData: Boolean) {
        val sb = StringBuilder()
        for (item in apps) {
            val packageName = item.packageName.toString()
            sb.append("echo '[uninstall ${item.appName}]'\n")

            if (keepData) {
                sb.append("pm uninstall -k --user $uid $packageName\n")
            } else {
                sb.append("pm uninstall --user $uid $packageName\n")
            }
        }

        sb.append("echo '[operation completed]'\n")
        execShell(sb)
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    protected fun buildAll() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_app_dex2oat, null)
        dialogView.findViewById<TextView>(R.id.confirm_message).text =
            "dex2oat编译可提升(低端机)运行应用时的响应速度，但会显著增加存储空间占用。\n\n确定为选中的 ${apps.size} 个应用进行dex2oat编译吗？"
        val switchEverything = dialogView.findViewById<CompoundButton>(R.id.dex2oat_everything)
        val switchForce = dialogView.findViewById<CompoundButton>(R.id.dex2oat_force)

        val dialog = DialogHelper.customDialog(context, dialogView)
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()
            if (switchEverything.isChecked) {
                buildAll("everything", switchForce.isChecked)
            } else {
                buildAll("speed", switchForce.isChecked)
            }
        }

    }

    private fun buildAll(mode: String, forced: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            toast("该功能只支持Android N（7.0）以上的系统！")
            return
        }
        val sb = StringBuilder()
        for (item in apps) {
            val packageName = item.packageName.toString()
            sb.append("echo '[compile ${item.appName}]'\n")

            if (forced) {
                sb.append("cmd package compile -f -m $mode $packageName\n\n")
            } else {
                sb.append("cmd package compile -m $mode $packageName\n\n")
            }
        }

        sb.append("echo '[operation completed]'\n\n")
        execShell(sb)
    }
}
