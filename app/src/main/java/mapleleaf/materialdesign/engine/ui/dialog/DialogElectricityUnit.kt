package mapleleaf.materialdesign.engine.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.store.ChargeSpeedStore
import mapleleaf.materialdesign.engine.store.SpfConfig
import java.util.Timer
import java.util.TimerTask

class DialogElectricityUnit {

    @SuppressLint("InflateParams", "SetTextI18n")
    fun showDialog(context: Context) {
        val globalSPF = context.getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE)

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val currentNow = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val defaultUnit = if (Build.MANUFACTURER.toUpperCase() == "XIAOMI") {
            SpfConfig.GLOBAL_SPF_CURRENT_NOW_UNIT_DEFAULT
        } else {
            val batteryStatus =
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            if (batteryStatus == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                if (currentNow > 20000) {
                    -1000
                } else if (currentNow < -20000) {
                    1000
                } else if (currentNow > 0) {
                    -1
                } else {
                    1
                }
            } else if (batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
                if (currentNow > 20000) {
                    1000
                } else if (currentNow < -20000) {
                    -1000
                } else if (currentNow > 0) {
                    1
                } else {
                    -1
                }
            } else {
                SpfConfig.GLOBAL_SPF_CURRENT_NOW_UNIT_DEFAULT
            }
        }
        var unit = globalSPF.getInt(SpfConfig.GLOBAL_SPF_CURRENT_NOW_UNIT, defaultUnit)
        var alertDialog: DialogHelper.DialogWrap? = null
        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_electricity_unit, null)
        val electricityAdjUnit = dialogView.findViewById<TextView>(R.id.electricity_adj_unit)
        val electricityAdjSample = dialogView.findViewById<TextView>(R.id.electricity_adj_sample)

        dialogView.findViewById<ImageButton>(R.id.electricity_adj_minus).setOnClickListener {
            if (unit == -1) {
                unit = -10
            } else if (unit == 1) {
                unit = -1
            } else if (unit > 0) {
                unit /= 10
            } else if (unit > -1000 * 1000 * 100) {
                unit *= 10
            }
            electricityAdjUnit.text = unit.toString()
            val currentMA = currentNow / unit
            electricityAdjSample.text = (if (currentMA >= 0) "+" else "") + currentMA + "mA"
        }
        dialogView.findViewById<ImageButton>(R.id.electricity_adj_plus).setOnClickListener {
            if (unit == -1) {
                unit = 1
            } else if (unit < 0) {
                unit /= 10
            } else if (unit < 1000 * 1000 * 100) {
                unit *= 10
            }
            electricityAdjUnit.text = unit.toString()
            val currentMA = currentNow / unit
            electricityAdjSample.text = (if (currentMA >= 0) "+" else "") + currentMA + "mA"
        }
        dialogView.findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            globalSPF.edit().putInt(SpfConfig.GLOBAL_SPF_CURRENT_NOW_UNIT, unit).apply()
            alertDialog?.dismiss()
        }
        electricityAdjUnit.text = unit.toString()
        val handler = Handler(Looper.getMainLooper())
        val timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                        try {
                            val currentMA = currentNow / unit
                            electricityAdjSample.text =
                                (if (currentMA >= 0) "+" else "") + currentMA + "mA"
                        } catch (ex: Exception) {
                            Log.d("DialogElectricityUnit", "无法获取电流: $ex")
                        }
                    }
                }
            }, 10, 1000)
        }

        alertDialog = DialogHelper.customDialog(context, dialogView).setOnDismissListener {
            ChargeSpeedStore(context).clearAll()
            timer.cancel()
        }
    }
}
