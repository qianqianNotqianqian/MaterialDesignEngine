package mapleleaf.materialdesign.engine.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.PermissionChecker
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.shell.KeepShellPublic
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.utils.CommonCommands
import kotlin.system.exitProcess

/**
 * 检查获取root权限
 * Created by helloklf on 2017/6/3.
 */

class CheckRootStatus(
    var context: Context,
    private val next: Runnable? = null,
    private var disableSeLinux: Boolean = false,
    private val skip: Runnable? = null,
) {

    private var myHandler: Handler = Handler(Looper.getMainLooper())
    var thread: Thread? = null

    @SuppressLint("InflateParams")
    public fun forceGetRoot() {
        if (lastCheckResult) {
            if (next != null) {
                myHandler.post(next)
            }
        } else {
            var completed = false
            thread = Thread {
                setRootStatus(KeepShellPublic.checkRoot())

                if (completed) {
                    return@Thread
                }

                completed = true

                if (lastCheckResult) {
                    if (disableSeLinux) {
                        KeepShellPublic.doCmdSync(CommonCommands.DisableSELinux)
                    }
                    if (next != null) {
                        myHandler.post(next)
                    }
                } else {
                    myHandler.post {
                        KeepShellPublic.tryExit()

                        val dialogView = LayoutInflater.from(context)
                            .inflate(R.layout.dialog_root_rejected, null)
                        DialogHelper.customDialog(context, dialogView).apply {
                            dialogView.findViewById<View>(R.id.btn_retry).setOnClickListener {
                                dismiss()

                                KeepShellPublic.tryExit()
                                if (thread != null && thread!!.isAlive && !thread!!.isInterrupted) {
                                    thread!!.interrupt()
                                    thread = null
                                }
                                forceGetRoot()
                            }
                            dialogView.findViewById<View>(R.id.btn_skip).setOnClickListener {
                                dismiss()
                                skip?.run {
                                    myHandler.post(skip)
                                }
                            }
                        }

                    }
                }
            }
            thread!!.start()
            Thread {
                Thread.sleep(1000 * 10)

                if (!completed) {
                    KeepShellPublic.tryExit()
                    myHandler.post {
                        val dialogView =
                            LayoutInflater.from(context).inflate(R.layout.dialog_root_timeout, null)
                        DialogHelper.customDialog(context, dialogView).apply {
                            dialogView.findViewById<View>(R.id.btn_retry).setOnClickListener {
                                if (thread != null && thread!!.isAlive && !thread!!.isInterrupted) {
                                    thread!!.interrupt()
                                    thread = null
                                }
                                forceGetRoot()
                            }
                            dialogView.findViewById<View>(R.id.btn_exit).setOnClickListener {
                                dismiss()

                                exitProcess(0)
//                                android.os.Process.killProcess(android.os.Process.myPid())
                            }
                        }
                    }
                }
            }.start()
        }
    }

    companion object {
        private var rootStatus = false

        private fun checkPermission(context: Context, permission: String): Boolean =
            PermissionChecker.checkSelfPermission(
                context,
                permission
            ) == PermissionChecker.PERMISSION_GRANTED

        @SuppressLint("ObsoleteSdkInt")
        @RequiresApi(Build.VERSION_CODES.R)
        fun grantPermission(context: Context) {
            val cmd = StringBuilder()
            // 必需的权限
            val requiredPermission = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CHANGE_CONFIGURATION,
                Manifest.permission.WRITE_SECURE_SETTINGS,
                Manifest.permission.SYSTEM_ALERT_WINDOW,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            )
            requiredPermission.forEach {
                if (it == Manifest.permission.MANAGE_EXTERNAL_STORAGE) {
                    if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
                        cmd.append("appops set --uid ${context.packageName} MANAGE_EXTERNAL_STORAGE allow\n")
                    }
                } else if (it == Manifest.permission.SYSTEM_ALERT_WINDOW) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!Settings.canDrawOverlays(context)) {
                            // 未允许悬浮窗
                            try {
//                                启动Activity让用户授权
//                                 Toast.makeText(context, "Scene未获得显示悬浮窗权限", Toast.LENGTH_SHORT).show()
//                                 val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName));
//                                 context.startActivity(intent);
                            } catch (ex: Exception) {
                                Log.d("text", "text")
                            }
                        }
                    } else {
                        if (!checkPermission(context, it)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val option = it.substring("android.permission.".length)
                                cmd.append("appops set ${context.packageName} $option allow\n")
                            }
                            cmd.append("pm grant ${context.packageName} $it\n")
                        }
                    }
                } else {
                    if (!checkPermission(context, it)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val option = it.substring("android.permission.".length)
                            cmd.append("appops set ${context.packageName} $option allow\n")
                        }
                        cmd.append("pm grant ${context.packageName} $it\n")
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!checkPermission(
                        context,
                        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    )
                ) {
                    cmd.append("dumpsys deviceidle whitelist +${context.packageName};\n")
                }
            }

            KeepShellPublic.doCmdSync(cmd.toString())
        }

        // 最后的ROOT检测结果
        val lastCheckResult: Boolean
            get() {
                return rootStatus
            }

        private fun setRootStatus(root: Boolean) {
            rootStatus = root
            MaterialDesignEngine.setBoolean("root", root)
        }
    }
}
