package mapleleaf.materialdesign.engine.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.map.DeviceNameMapper
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import me.zhanghai.android.fastscroll.FastScrollNestedScrollView
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.io.BufferedReader
import java.io.InputStreamReader

class ActivitySystemInfo : UniversalActivityBase(R.layout.activity_device_info) {

    private lateinit var deviceNameTextView: TextView
    private lateinit var deviceModelTextView: TextView
    private lateinit var deviceManufacturerTextView: TextView
    private lateinit var deviceTextView: TextView
    private lateinit var deviceBoardTextView: TextView
    private lateinit var devicePlatformTextView: TextView
    private lateinit var deviceBrandTextView: TextView
    private lateinit var deviceNameTextView2: TextView
    private lateinit var deviceBrandTextView2: TextView
    private lateinit var deviceIdTextView: TextView
    private lateinit var serialNumberTextView: TextView
    private lateinit var signalTypeTextView: TextView
    private lateinit var carrierTextView: TextView
    private lateinit var networkTypeTextView: TextView
    private lateinit var fingerprintTextView: TextView
    private lateinit var wlanMacTextView: TextView
    private lateinit var bluetoothMacTextView: TextView
    private lateinit var usbDebuggingTextView: TextView
    private val PERMISSION_REQUEST_CODE = 1
    private var loadDeviceInfoJob: Job? = null

//    override fun getLayoutResourceId() = R.layout.activity_device_info

    private fun showPermissionDeniedMessage() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_get_permission, null)
        val dialog = DialogHelper.customDialog(this, dialogView)
        dialogView.findViewById<TextView>(R.id.confirm_title).text = "需要权限"
        val messageTextView = dialogView.findViewById<TextView>(R.id.confirm_message)
        messageTextView.text = "请授予权限以访问设备信息"

        dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()
            openAppSettings()
        }
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", this@ActivitySystemInfo.packageName, null)
        }.also { startActivity(it) }

    }

    override fun initializeComponents(savedInstanceState: Bundle?) {
        setToolbarTitle(getString(R.string.toolbar_title_activity_device_info))
        deviceNameTextView = findViewById(R.id.device_name)
        deviceModelTextView = findViewById(R.id.device_model)
        deviceManufacturerTextView = findViewById(R.id.device_manufacturer)
        deviceTextView = findViewById(R.id.device)
        deviceBoardTextView = findViewById(R.id.device_board)
        devicePlatformTextView = findViewById(R.id.device_platform)
        deviceBrandTextView = findViewById(R.id.device_brand)
        deviceNameTextView2 = findViewById(R.id.device_name2)
        deviceBrandTextView2 = findViewById(R.id.device_brand2)
        deviceIdTextView = findViewById(R.id.device_id)
        serialNumberTextView = findViewById(R.id.serial_number)
        fingerprintTextView = findViewById(R.id.device_fingerprint)

        signalTypeTextView = findViewById<TextView?>(R.id.signal_type).apply {
            setTextIsSelectable(false)
            setOnClickListener { checkPermission() }
        }
        carrierTextView = findViewById<TextView?>(R.id.carrier).apply {
            setTextIsSelectable(false)
            setOnClickListener { checkPermission() }
        }
        networkTypeTextView = findViewById(R.id.network_type)
        wlanMacTextView = findViewById(R.id.wlan_mac)
        bluetoothMacTextView = findViewById(R.id.bluetooth_mac)
        usbDebuggingTextView = findViewById(R.id.usb_debugging)

        CoroutineScope(Dispatchers.Main).launch {
            loadDeviceInfo()
        }

        val scrollingView = findViewById<FastScrollNestedScrollView>(R.id.nestedScrollView2)
        FastScrollerBuilder(scrollingView).build()

        val materialCardView = findViewById<MaterialCardView>(R.id.materialCardView)
        val materialCardView2 = findViewById<MaterialCardView>(R.id.materialCardView2)
        val baseColor =
            ContextCompat.getColor(MaterialDesignEngine.context, R.color.background_color)
        val primaryColor =
            ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
        val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)
        materialCardView.setCardBackgroundColor(blendedColor)
        materialCardView2.setCardBackgroundColor(blendedColor)
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            handlePermissionGranted()
        } else {
            // 未授权，请求权限
            requestPermissions(
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun handlePermissionGranted() {
        val telephonyManager =
            getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        // 获取并显示运营商信息
        val carrier = telephonyManager.networkOperatorName
        carrierTextView.text = carrier
        carrierTextView.setTextIsSelectable(true)

        // 获取并显示信号类型
        val signalType =
            when (telephonyManager.phoneType) {
                TelephonyManager.PHONE_TYPE_GSM -> "GSM"
                TelephonyManager.PHONE_TYPE_CDMA -> "CDMA"
                else -> "Unknown"
            }
        signalTypeTextView.text = signalType
        signalTypeTextView.setTextIsSelectable(true)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            handlePermissionGranted()
        } else {
            showPermissionDeniedMessage()
        }
    }

    private suspend fun loadDeviceInfo() {
        val deviceInfo = loadDeviceInfoInBackground()
        updateUI(deviceInfo)
    }

    private suspend fun delayLoad(textView: TextView, text: String) {
        withContext(Dispatchers.Main) {
            val delayMillis = 500L
            textView.postDelayed({
                textView.text = text
            }, delayMillis)
        }
    }

    private fun getBluetoothMacAddress(): String {
        return try {
            val command = "settings get secure bluetooth_address"
            runRootCommand(command)
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun getWlanMacAddress(): String {
        return try {
            val command = "settings get global wifi_mac_address"
            runRootCommand(command)
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun runRootCommand(command: String): String {
        return try {
            val processBuilder = ProcessBuilder("su", "-c", command)
            val process = processBuilder.start()
            val inputStream = process.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val output = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                output.append(line)
            }
            process.waitFor()
            output.toString().trim()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun getSerialNumber(): String {
        return try {
            val process = Runtime.getRuntime().exec("su -c getprop ro.serialno")
            val inputStream = process.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val output = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                output.append(line)
            }
            process.waitFor()
            output.toString().trim()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadDeviceInfoJob?.cancel()
        removeDelayedTasks()
    }

    private fun removeDelayedTasks() {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.removeCallbacksAndMessages(null)
    }

    private fun updateUI(deviceInfo: DeviceInfo) {
        deviceModelTextView.text = deviceInfo.model
        deviceManufacturerTextView.text = deviceInfo.manufacturer
        deviceTextView.text = deviceInfo.device
        deviceBoardTextView.text = deviceInfo.board
        devicePlatformTextView.text = deviceInfo.platform
        deviceBrandTextView.text = deviceInfo.brand
        deviceNameTextView2.text = deviceInfo.name
        deviceBrandTextView2.text = deviceInfo.brand
    }

    @SuppressLint("HardwareIds")
    private suspend fun loadDeviceInfoInBackground(): DeviceInfo {
        return withContext(Dispatchers.IO) {
            val deviceCode = Build.DEVICE
            val deviceName = DeviceNameMapper.getDeviceName(deviceCode)

            delayLoad(deviceNameTextView, deviceName)

            val androidId = Settings.Secure.getString(
                this@ActivitySystemInfo.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            delayLoad(deviceIdTextView, androidId)

            val deviceFingerprint = Build.FINGERPRINT
            delayLoad(fingerprintTextView, deviceFingerprint)

            val serialNumber = getSerialNumber()
            delayLoad(serialNumberTextView, serialNumber)

            val telephonyManager =
                this@ActivitySystemInfo.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            val signalType =
                if (ContextCompat.checkSelfPermission(
                        this@ActivitySystemInfo,
                        Manifest.permission.READ_PHONE_STATE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    when (telephonyManager.phoneType) {
                        TelephonyManager.PHONE_TYPE_GSM -> "GSM"
                        TelephonyManager.PHONE_TYPE_CDMA -> "CDMA"
                        TelephonyManager.PHONE_TYPE_NONE -> "NONE"
                        else -> "Unknown"
                    }
                } else {
                    "权限未授予"

                }
            delayLoad(signalTypeTextView, signalType)

            val carrier = if (ContextCompat.checkSelfPermission(
                    this@ActivitySystemInfo,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val telephonyManagerCe =
                    this@ActivitySystemInfo.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
                telephonyManagerCe.networkOperatorName
            } else {
                "权限未授予"
            }
            delayLoad(carrierTextView, carrier)

            val networkType =
                if (ContextCompat.checkSelfPermission(
                        this@ActivitySystemInfo,
                        Manifest.permission.ACCESS_NETWORK_STATE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val connectivityManager =
                        getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager
                    val network = connectivityManager?.activeNetwork
                    val networkCapabilities = connectivityManager?.getNetworkCapabilities(network)
                    when {
                        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "无线网络"
                        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "移动数据"
                        else -> "Unknown"
                    }

                } else {
                    "权限未授予"
                }
            delayLoad(networkTypeTextView, networkType)

            val wlanMac = getWlanMacAddress()
            delayLoad(wlanMacTextView, wlanMac)

            val bluetoothMac = getBluetoothMacAddress()
            delayLoad(bluetoothMacTextView, bluetoothMac)

            val usbDebuggingStatus = if (Settings.Global.getInt(
                    this@ActivitySystemInfo.contentResolver,
                    Settings.Global.ADB_ENABLED, 0
                ) == 1
            ) {
                "启用"
            } else {
                "禁用"
            }
            delayLoad(usbDebuggingTextView, usbDebuggingStatus)

            DeviceInfo(
                deviceName,
                Build.MODEL,
                Build.MANUFACTURER,
                Build.DEVICE,
                Build.BOARD,
                Build.HARDWARE,
                Build.BRAND,
                androidId,
                deviceFingerprint,
                serialNumber,
                signalType,
                carrier,
                networkType,
                wlanMac,
                bluetoothMac,
                usbDebuggingStatus
            )
        }
    }

    data class DeviceInfo(
        val name: String,
        val model: String,
        val manufacturer: String,
        val device: String,
        val board: String,
        val platform: String,
        val brand: String,
        val androidId: String,
        val fingerprint: String,
        val serialNumber: String,
        val signalType: String,
        val carrier: String,
        val networkType: String,
        val wlanMac: String,
        val bluetoothMac: String,
        val usbDebuggingStatus: String,
    )
}
