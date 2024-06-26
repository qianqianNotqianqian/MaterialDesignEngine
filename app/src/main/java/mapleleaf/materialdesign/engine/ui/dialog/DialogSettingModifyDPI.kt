package mapleleaf.materialdesign.engine.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Display
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.shared.MagiskExtend
import mapleleaf.materialdesign.engine.shell.KeepShellPublic
import mapleleaf.materialdesign.engine.store.SpfConfig
import mapleleaf.materialdesign.engine.utils.CommonCommands
import mapleleaf.materialdesign.engine.utils.toast
import java.util.Timer
import java.util.TimerTask

/**
 * Created by Hello on 2017/12/03.
 */

class DialogSettingModifyDPI(var context: Activity) {
    private val BACKUP_SCREEN_DPI: String = "screen_dpi"
    private val BACKUP_SCREEN_RATIO: String = "screen_ratio"
    private val BACKUP_SCREEN_WIDTH: String = "screen_width"
    private val DEFAULT_RATIO: Float = 16 / 9f
    private val DEFAULT_DPI: Int = 320
    private val DEFAULT_WIDTH: Int = 720

    @SuppressLint("ApplySharedPref")
    private fun backupDisplay(point: Point, dm: DisplayMetrics, context: Context) {
        val spf = context.getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE);
        if (!spf.contains(BACKUP_SCREEN_RATIO)) {
            spf.edit().putFloat(BACKUP_SCREEN_RATIO, point.y / point.x.toFloat()).commit()
        }
        if (!spf.contains(BACKUP_SCREEN_DPI) || !spf.contains(BACKUP_SCREEN_WIDTH)) {
            spf.edit().putInt(BACKUP_SCREEN_DPI, dm.densityDpi).commit()
            spf.edit().putInt(BACKUP_SCREEN_WIDTH, point.x).commit()
        }
    }

    private fun getHeightScaleValue(width: Int): Int {
        val spf = context.getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE);
        return (width * spf.getFloat(BACKUP_SCREEN_RATIO, DEFAULT_RATIO)).toInt()
    }

    private fun getDpiScaleValue(width: Int): Int {
        val spf = context.getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE);
        return (spf.getInt(BACKUP_SCREEN_DPI, DEFAULT_DPI) * width / spf.getInt(
            BACKUP_SCREEN_WIDTH,
            DEFAULT_WIDTH
        ))
    }

    @SuppressLint("InflateParams")
    fun modifyDPI(display: Display, context: Activity) {
        val layoutInflater = LayoutInflater.from(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_system_modify_dpi, null)
        val dpiInput: EditText = dialogView.findViewById(R.id.dialog_system_modify_input_dpi)
        val widthInput: EditText = dialogView.findViewById(R.id.dialog_system_modify_dpi_width)
        val heightInput: EditText = dialogView.findViewById(R.id.dialog_system_modify_dpi_height)
        val quickChange: CheckBox = dialogView.findViewById(R.id.dialog_system_modify_dpi_quickchange)

        val dm = DisplayMetrics()
        display.getMetrics(dm)
        val point = Point()
        display.getRealSize(point)

        backupDisplay(point, dm, context);

        dpiInput.setText(dm.densityDpi.toString())
        widthInput.setText(point.x.toString())
        heightInput.setText(point.y.toString())

        quickChange.isChecked = true

        val rate = dm.heightPixels / 1.0 / dm.widthPixels
        dialogView.findViewById<Button>(R.id.dialog_dpi_720).setOnClickListener {
            val width = 720
            widthInput.setText(width.toString())
            val height = getHeightScaleValue(width)
            heightInput.setText(height.toString())
            dpiInput.setText((dm.densityDpi.toFloat() * width / point.x).toInt().toString())
        }
        dialogView.findViewById<Button>(R.id.dialog_dpi_1080).setOnClickListener {
            val width = 1080
            widthInput.setText(width.toString())
            heightInput.setText(getHeightScaleValue(width).toString())
            dpiInput.setText(getDpiScaleValue(width).toString())
        }
        dialogView.findViewById<Button>(R.id.dialog_dpi_2k).setOnClickListener {
            val width = 1440
            widthInput.setText(width.toString())
            heightInput.setText(getHeightScaleValue(width).toString())
            dpiInput.setText(getDpiScaleValue(width).toString())
        }
        dialogView.findViewById<Button>(R.id.dialog_dpi_4k).setOnClickListener {
            val width = 2160
            widthInput.setText(width.toString())
            heightInput.setText(getHeightScaleValue(width).toString())
            dpiInput.setText(getDpiScaleValue(width).toString())
        }

        val dialogInstance = DialogHelper.confirm(context, "DPI、分辨率", "", dialogView, {
            val dpi = if (dpiInput.text.isNotEmpty()) (dpiInput.text.toString().toInt()) else (0)
            val width =
                if (widthInput.text.isNotEmpty()) (widthInput.text.toString().toInt()) else (0)
            val height =
                if (heightInput.text.isNotEmpty()) (heightInput.text.toString().toInt()) else (0)
            val qc = quickChange.isChecked

            val cmd = StringBuilder()
            if (width >= 320 && height >= 480) {
                cmd.append("wm size ${width}x$height")
                cmd.append("\n")
            }
            if (dpi >= 96) {
                if (qc) {
                    cmd.append("wm density $dpi")
                    cmd.append("\n")
                } else {
                    if (MagiskExtend.moduleInstalled()) {
                        KeepShellPublic.doCmdSync("wm density reset");
                        MagiskExtend.setSystemProp("ro.sf.lcd_density", dpi.toString());
                        MagiskExtend.setSystemProp("vendor.display.lcd_density", dpi.toString());
                        toast("已通过Magisk更改参数，请重启手机~")
                    } else {
                        cmd.append(CommonCommands.MountSystemRW)
                        cmd.append("wm density reset\n")
                        cmd.append("sed '/ro.sf.lcd_density=/'d /system/build.prop > /data/build.prop\n")
                        cmd.append("sed '\$aro.sf.lcd_density=$dpi' /data/build.prop > /data/build2.prop\n")
                        cmd.append("cp /system/build.prop /system/build.prop.dpi_bak\n")
                        cmd.append("cp /data/build2.prop /system/build.prop\n")
                        cmd.append("rm /data/build.prop\n")
                        cmd.append("rm /data/build2.prop\n")
                        cmd.append("chmod 0755 /system/build.prop\n")
                        cmd.append("sync\n")
                        cmd.append("reboot\n")
                    }
                }
            }
            if (cmd.isNotEmpty())
                KeepShellPublic.doCmdSync(cmd.toString())

            if (qc) {
                autoResetConfirm()
            }
        }, {

        })

        dialogView.findViewById<Button>(R.id.dialog_dpi_reset).setOnClickListener {
            if (dialogInstance.isShowing) {
                try {
                    dialogInstance.dismiss()
                } catch (ex: java.lang.Exception) {
                }
            }
            resetDisplay()
        }
    }

    private fun autoResetConfirm() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_system_modify_dpi_confirm, null)
            val timeoutView = dialogView.findViewById<TextView>(R.id.dpi_modify_timeout)
            val dialog = DialogHelper.customDialog(context, dialogView)

            var timeOut = 30
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    timeOut -= 1
                    if (timeOut < 1) {
                        cancel()
                        if (dialog.isShowing) {
                            handler.post {
                                try {
                                    dialog.dismiss()
                                } catch (ex: Exception) {
                                }
                            }
                            resetDisplay()
                            pointerLocationOff()
                        }
                    } else {
                        handler.post {
                            try {
                                timeoutView.text = timeOut.toString()
                            } catch (ex: Exception) {
                            }
                        }
                    }
                }
            }, 1000, 1000)

            timeoutView.text = timeOut.toString()
            dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
                resetDisplay()
                pointerLocationOff()
            }
            dialogView.findViewById<Button>(R.id.btn_confirm).setOnClickListener {
                dialog.dismiss()
                pointerLocationOff()
            }
            KeepShellPublic.doCmdSync("settings put system pointer_location 1")
        }, 2000)
    }

    private fun pointerLocationOff() {
        KeepShellPublic.doCmdSync("settings put system pointer_location 0")
    }

    private fun resetDisplay() {
        val cmd = StringBuilder()
        cmd.append("wm size reset\n")
        cmd.append("wm density reset\n")
        cmd.append("wm overscan reset\n")
        KeepShellPublic.doCmdSync(cmd.toString())
    }
}
