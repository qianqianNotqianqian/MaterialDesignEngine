package mapleleaf.materialdesign.engine.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.FileProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.model.AppInfo
import mapleleaf.materialdesign.engine.shell.AsynSuShellUnit
import mapleleaf.materialdesign.engine.ui.activities.ActivityAppComponents
import mapleleaf.materialdesign.engine.ui.activities.ActivityApplicationDetails
import mapleleaf.materialdesign.engine.utils.toast
import java.io.File
import java.io.IOException

/**
 * Created by Hello on 2018/01/26.
 */
class DialogSingleAppOptions(context: Activity, var app: AppInfo, handler: Handler) :
    DialogAppOptions(context, arrayListOf(app), handler) {

    fun showSingleAppOptions() {
        when (app.appType) {
            AppInfo.AppType.USER -> showUserAppOptions()
            AppInfo.AppType.SYSTEM -> showSystemAppOptions()
            else -> {
                toast("UNSupport！")
            }
        }
    }

    private fun loadAppIcon(app: AppInfo): Drawable? {
        if (app.icon != null) {
            return app.icon
        } else {
            var icon: Drawable? = null
            try {
                val installInfo =
                    context.packageManager.getPackageInfo(app.packageName.toString(), 0)
                icon = installInfo.applicationInfo.loadIcon(context.packageManager)
                return icon
            } catch (ex: Exception) {
            } finally {
            }
            return null
        }
    }

    /**
     * 显示用户应用选项
     */

    @SuppressLint("InflateParams")
    private fun showUserAppOptions() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_app_options_user, null)
        val dialog = DialogHelper.customDialog(context, dialogView)
        dialogView.findViewById<TextView>(R.id.app_target_sdk).text =
            context.getString(R.string.applications_app_target_sdk, app.targetSdkVersion.toString())
        dialogView.findViewById<TextView>(R.id.app_min_sdk).text =
            context.getString(R.string.applications_app_min_sdk, app.minSdkVersion.toString())
        dialogView.findViewById<TextView>(R.id.app_version_name).text =
            context.getString(R.string.applications_app_version_name, app.versionName)
        dialogView.findViewById<TextView>(R.id.app_version_code).text =
            context.getString(R.string.applications_app_version_code, app.versionCode.toString())
        dialogView.findViewById<ImageView>(R.id.app_logo).setImageDrawable(loadAppIcon(app))

        dialogView.findViewById<View>(R.id.app_options_single_only).visibility = View.VISIBLE
        dialogView.findViewById<View>(R.id.app_options_copay_package).setOnClickListener {
            dialog.dismiss()
            copyPackageName()
        }
        dialogView.findViewById<TextView>(R.id.app_package_name).text = app.packageName
        dialogView.findViewById<View>(R.id.app_options_copay_path).setOnClickListener {
            dialog.dismiss()
            copyInstallPath()
        }
        dialogView.findViewById<TextView>(R.id.app_install_path).text = app.path
        dialogView.findViewById<View>(R.id.app_options_open_detail).setOnClickListener {
            openDetails()
        }
        dialogView.findViewById<View>(R.id.app_options_app_store).setOnClickListener {
            showInMarketAndLaunch()
        }
        dialogView.findViewById<View>(R.id.app_options_component_management).setOnClickListener {
            val intent = Intent(context, ActivityAppComponents::class.java)
            intent.putExtra("packageName", app.packageName)
            context.startActivity(intent)
        }
        dialogView.findViewById<View>(R.id.app_options_clear).setOnClickListener {
            if (rootChecker.isDeviceRooted()) {
                clearAll()
            } else {
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<View>(R.id.app_options_uninstall).setOnClickListener {
            if (rootChecker.isDeviceRooted()) {
                uninstallAll()
            } else {
                val packageUri = Uri.parse(app.packageName)
                val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
                context.startActivity(uninstallIntent)
                toast("设备未获取Root权限")
            }
        }

        dialogView.findViewById<View>(R.id.app_options_dex2oat).setOnClickListener {
            if (rootChecker.isDeviceRooted()) {
                buildAll()
            } else {
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<TextView>(R.id.app_options_title).text = app.appName

        dialogView.findViewById<View>(R.id.app_options_app_freeze).setOnClickListener {
            if (rootChecker.isDeviceRooted()) {
                modifyStateAll()
            } else {
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<View>(R.id.app_options_app_share).setOnClickListener {
            dialog.dismiss()
            shareSingleApp()
        }
        dialogView.findViewById<View>(R.id.app_options_extra_package).setOnClickListener {
            dialog.dismiss()
            if (rootChecker.isDeviceRooted()) {
                extraPackage(app)
            } else {
                saveApkToLocal(app)
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
    }

    /**
     * 显示系统应用选项
     */

    private fun showSystemAppOptions() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_app_options_system, null)

        val dialog = DialogHelper.customDialog(context, dialogView)
        dialogView.findViewById<TextView>(R.id.app_target_sdk).text =
            context.getString(R.string.applications_app_target_sdk, app.targetSdkVersion.toString())
        dialogView.findViewById<TextView>(R.id.app_min_sdk).text =
            context.getString(R.string.applications_app_min_sdk, app.minSdkVersion.toString())
        dialogView.findViewById<TextView>(R.id.app_version_name).text =
            context.getString(R.string.applications_app_version_name, app.versionName)
        dialogView.findViewById<TextView>(R.id.app_version_code).text =
            context.getString(R.string.applications_app_version_code, app.versionCode.toString())
        dialogView.findViewById<ImageView>(R.id.app_logo).setImageDrawable(loadAppIcon(app))

        dialogView.findViewById<View>(R.id.app_options_single_only).visibility = View.VISIBLE

        dialogView.findViewById<View>(R.id.app_options_copay_package).setOnClickListener {
            dialog.dismiss()
            copyPackageName()
        }
        dialogView.findViewById<TextView>(R.id.app_package_name).setText(app.packageName)
        dialogView.findViewById<View>(R.id.app_options_copay_path).setOnClickListener {
            dialog.dismiss()
            copyInstallPath()
        }
        dialogView.findViewById<TextView>(R.id.app_install_path).setText(app.path)
        dialogView.findViewById<View>(R.id.app_options_open_detail).setOnClickListener {
            openDetails()
        }
        dialogView.findViewById<View>(R.id.app_options_app_store).setOnClickListener {
            showInMarketAndLaunch()
        }
        dialogView.findViewById<View>(R.id.app_options_clear).setOnClickListener {
            if (rootChecker.isDeviceRooted()) {
                clearAll()
            } else {
                toast("设备未获取Root权限")
            }
        }
        if (app.updated) {
            dialogView.findViewById<View>(R.id.app_options_uninstall_user).visibility = View.GONE
        } else {
            dialogView.findViewById<View>(R.id.app_options_uninstall_user).setOnClickListener {
                uninstallAllSystem(app.updated)
            }
        }
        dialogView.findViewById<View>(R.id.app_options_dex2oat).setOnClickListener {
            if (rootChecker.isDeviceRooted()) {
                buildAll()
            } else {
                toast("设备未获取Root权限")
            }
        }
        if (app.updated) {
            dialogView.findViewById<View>(R.id.app_options_delete).visibility = View.GONE
            dialogView.findViewById<View>(R.id.app_options_uninstall).setOnClickListener {
                if (rootChecker.isDeviceRooted()) {
                    uninstallAllSystem(app.updated)
                } else {
                    toast("设备未获取Root权限")
                }
            }
        } else {
            dialogView.findViewById<View>(R.id.app_options_delete).setOnClickListener {
                if (rootChecker.isDeviceRooted()) {
                    deleteAll()
                } else {
                    toast("设备未获取Root权限")
                }
            }
            dialogView.findViewById<View>(R.id.app_options_uninstall).visibility = View.GONE
        }

        dialogView.findViewById<TextView>(R.id.app_options_title).setText(app.appName)

        dialogView.findViewById<View>(R.id.app_options_app_freeze).setOnClickListener {
            if (rootChecker.isDeviceRooted()) {
                modifyStateAll()
            } else {
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<View>(R.id.app_options_app_share).setOnClickListener {
            dialog.dismiss()
            shareSingleApp()
        }
        dialogView.findViewById<View>(R.id.app_options_extra_package).setOnClickListener {
            dialog.dismiss()
            if (rootChecker.isDeviceRooted()) {
                extraPackage(app)
            } else {
                saveApkToLocal(app)
                toast("设备未获取Root权限")
            }
        }
        dialogView.findViewById<View>(R.id.app_options_component_management).setOnClickListener {
            val intent = Intent(context, ActivityAppComponents::class.java)
            intent.putExtra("packageName", app.packageName)
            context.startActivity(intent)
        }
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun extraPackage(app: AppInfo) {
        val layoutInflater = LayoutInflater.from(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
        val textView: TextView = (dialogView.findViewById(R.id.dialog_text))
        val progressBar: ProgressBar = (dialogView.findViewById(R.id.dialog_app_details_progress))
        progressBar.visibility = View.VISIBLE

        val alert = DialogHelper.customDialog(context, dialogView)

        Thread {
            try {
                val sb = StringBuilder()
                textView.post {
                    textView.text = "正在提取 ${app.appName}的安装包"
                }
                sb.append("echo '正在提取 ${app.appName}'\n")

                val appInfo = context.packageManager.getApplicationInfo(app.packageName, 0)
                val apkFile = File(appInfo.sourceDir)

                // 获取应用的Android/data目录
                val dataDir = context.getExternalFilesDir(null)

                // 获取应用的名称和版本号
                val appName = app.appName.replace(" ", "_") // 将应用名称中的空格替换为下划线，以便作为文件名
                val versionName = app.versionName

                // 构建新的文件名
                val newFileName = "$appName-$versionName.apk"

                // 将APK文件复制到Android/data目录下并重命名
                val destFile = File(dataDir, newFileName)
                apkFile.copyTo(destFile, true)

                sb.append("echo '已提取 ${app.appName} 的 APK 文件'\n")

                sb.append("echo '[operation completed]'\n")

                // Execute shell commands
                AsynSuShellUnit(
                    ProgressHandlerExtra(
                        dialogView,
                        alert,
                        handler
                    )
                ).exec(sb.toString()).waitFor()
            } catch (e: PackageManager.NameNotFoundException) {
                textView.post {
                    toast("未找到该应用程序")
                }
            } catch (e: Exception) {
                textView.post {
                    toast("提取APK文件失败")
                }
            }
        }.start()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun saveApkToLocal(app: AppInfo) {
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
//                        toast("安装包已保存到 ${destinationFile.absolutePath}")
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

    private fun shareSingleApp() {
        try {
            val appInfo = context.packageManager.getApplicationInfo(app.packageName, 0)
            val apkFile = File(appInfo.sourceDir)
            val apkUri =
                FileProvider.getUriForFile(context, "${context.packageName}.shareAPK", apkFile)

            // 创建分享的 Intent
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/vnd.android.package-archive"
            shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri)

            // 启动分享 Intent
            try {
                context.startActivity(Intent.createChooser(shareIntent, "分享" + app.appName))
            } catch (e: ActivityNotFoundException) {
                toast("未找到可以分享的应用程序")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            toast("未找到该应用程序")
        }
    }

    private fun openDetails() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_app_details, null)
        val dialog = DialogHelper.customDialog(context, dialogView)
        dialogView.findViewById<View>(R.id.app_options_detail_on_system).setOnClickListener {
            dialog.dismiss()
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.data = Uri.fromParts("package", app.packageName, null)
            context.startActivity(intent)
        }
        dialogView.findViewById<View>(R.id.app_options_detail_on_custom).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(context, ActivityApplicationDetails::class.java)
            intent.putExtra("packageName", app.packageName)
            context.startActivity(intent)
        }
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun copyPackageName() {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.text = app.packageName
        toast("已复制：${app.packageName}")
    }

    private fun copyInstallPath() {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.text = app.path
        toast("已复制：${app.path}")
    }

    private fun showInMarketAndLaunch() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_app_launcher, null)

        val dialog = DialogHelper.customDialog(context, dialogView)
        dialogView.findViewById<View>(R.id.app_options_launch).setOnClickListener {
            dialog.dismiss()
            // 获取应用的启动 Intent
            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
            // 检查启动 Intent 是否为空
            if (launchIntent != null) {
                // 启动应用
                context.startActivity(launchIntent)
            } else {
                toast("无法打开应用，因为该应用被禁用或者没有提供可启动的活动")
            }
        }
        dialogView.findViewById<View>(R.id.app_options_show_market).setOnClickListener {
            dialog.dismiss()
            val query = "market://search?q=" + app.packageName // 使用market://search来搜索应用
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(query))
            try {
                context.startActivity(intent) // 尝试启动市场应用
            } catch (e: ActivityNotFoundException) {
                // 市场应用没有找到，尝试使用其他方式
                val playStoreQuery =
                    "https://play.google.com/store/search?q=" + app.packageName // 使用网页链接搜索应用
                val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreQuery))
                context.startActivity(playStoreIntent) // 启动网页搜索
            }
        }
        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
    }
}
