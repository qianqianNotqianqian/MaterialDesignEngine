package mapleleaf.materialdesign.engine.ui.dialog

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import android.util.Base64
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.shared.MagiskExtend
import mapleleaf.materialdesign.engine.shell.KeepShellPublic
import mapleleaf.materialdesign.engine.shell.PropsUtils
import mapleleaf.materialdesign.engine.shell.RootFile
import mapleleaf.materialdesign.engine.utils.CommonCommands
import mapleleaf.materialdesign.engine.utils.toast

/**
 * Created by Hello on 2017/12/03.
 */

class DialogSettingModifyDevice(var context: UniversalActivityBase) {

    val BACKUP_SUCCESS = "persist.vtools.device.backuped"
    val BACKUP_BRAND = "persist.vtools.brand"
    val BACKUP_MODEL = "persist.vtools.model"
    val BACKUP_PRODUCT = "persist.vtools.product"
    val BACKUP_DEVICE = "persist.vtools.device"
    val BACKUP_MANUFACTURER = "persist.vtools.manufacturer"

    private fun getBackupProp(prop: String, default: String): String {
        val value = PropsUtils.getProp(prop)
        if (value == "null" || value == "") {
            return default
        }

        return value
    }

    val brand_prop = "ro.product.brand"
    val name_prop = "ro.product.name"
    val model_prop = "ro.product.model"
    val manufacturer_prop = "ro.product.manufacturer"
    val device_prop = "ro.product.device"

    @SuppressLint("InflateParams")
    fun modifyDeviceInfo() {

        val layoutInflater = LayoutInflater.from(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_system_modify_device, null)
        val dialog = DialogHelper.customDialog(context, dialogView)

        editModel = dialogView.findViewById(R.id.dialog_system_modify_model)!!
        editBrand = dialogView.findViewById(R.id.dialog_system_modify_brand)!!
        editProductName = dialogView.findViewById(R.id.dialog_system_modify_name)!!
        editDevice = dialogView.findViewById(R.id.dialog_system_modify_device)!!
        editManufacturer = dialogView.findViewById(R.id.dialog_system_modify_manufacturer)!!

        dialogView.findViewById<Button>(R.id.dialog_system_modify_default).setOnClickListener {
            setDefault()
        }
        dialogView.findViewById<Button>(R.id.btn_help).setOnClickListener {
            DialogHelper.alert(
                context,
                "使用帮助",
                context.getString(R.string.dialog_system_modify_device_desc)
            )
        }
        dialogView.findViewById<Button>(R.id.btn_restart).setOnClickListener {
            val model = editModel.text.trim()
            val brand = editBrand.text.trim()
            val product = editProductName.text.trim()
            val device = editDevice.text.trim()
            val manufacturer = editManufacturer.text.trim()
            if (model.isNotEmpty() || brand.isNotEmpty() || product.isNotEmpty() || device.isNotEmpty() || manufacturer.isNotEmpty()) {
                backupDefault()
                if (MagiskExtend.moduleInstalled()) {
                    if (brand.isNotEmpty())
                        MagiskExtend.setSystemProp(brand_prop, brand.toString())
                    if (product.isNotEmpty())
                        MagiskExtend.setSystemProp(name_prop, product.toString())
                    if (model.isNotEmpty())
                        MagiskExtend.setSystemProp(model_prop, model.toString())
                    if (manufacturer.isNotEmpty())
                        MagiskExtend.setSystemProp(manufacturer_prop, manufacturer.toString())
                    if (device.isNotEmpty())
                        MagiskExtend.setSystemProp(device_prop, device.toString())
                    // 小米 - 改model参数以后device_features要处理下
                    if (RootFile.fileExists("/system/etc/device_features/${Build.PRODUCT}.xml")) {
                        if (model != Build.PRODUCT) {
                            MagiskExtend.replaceSystemFile(
                                "/system/etc/device_features/${product}.xml",
                                "/system/etc/device_features/${Build.PRODUCT}.xml"
                            )
                        }
                    }
                    toast("已通过Magisk更改参数，请重启手机~")
                } else {
                    val sb = StringBuilder()
                    sb.append(CommonCommands.MountSystemRW)
                    sb.append("cp /system/build.prop /data/build.prop;chmod 0755 /data/build.prop;")

                    if (brand.isNotEmpty())
                        sb.append("busybox sed -i 's/^$brand_prop=.*/$brand_prop=$brand/' /data/build.prop;")
                    if (product.isNotEmpty())
                        sb.append("busybox sed -i 's/^$name_prop=.*/$name_prop=$product/' /data/build.prop;")
                    if (model.isNotEmpty())
                        sb.append("busybox sed -i 's/^$model_prop=.*/$model_prop=$model/' /data/build.prop;")
                    if (manufacturer.isNotEmpty())
                        sb.append("busybox sed -i 's/^$manufacturer_prop=.*/$manufacturer_prop=$manufacturer/' /data/build.prop;")
                    if (device.isNotEmpty())
                        sb.append("busybox sed -i 's/^$device_prop=.*/$device_prop=$device/' /data/build.prop;")

                    sb.append("cp /system/build.prop /system/build.bak.prop\n")
                    sb.append("cp /data/build.prop /system/build.prop\n")
                    sb.append("rm /data/build.prop\n")
                    sb.append("chmod 0755 /system/build.prop\n")

                    // 小米 - 改model参数以后device_features要处理下
                    if (RootFile.fileExists("/system/etc/device_features/${Build.PRODUCT}.xml")) {
                        if (model != Build.PRODUCT) {
                            KeepShellPublic.doCmdSync("cp \"/system/etc/device_features/${Build.PRODUCT}.xml\" \"/system/etc/device_features/${product}.xml\"")
                        }
                    }

                    sb.append("sync\n")
                    sb.append("reboot\n")

                    KeepShellPublic.doCmdSync(sb.toString())
                }
            } else {
                toast("什么也没有修改！")
            }
        }

        loadCurrent()

        try {
            val cm = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val data = cm.primaryClip
            val item = data?.run { getItemAt(0) }
            val content = item?.text
            if (!content.isNullOrEmpty()) {
                val copyData = String(Base64.decode(content.toString().trim(), Base64.DEFAULT))
                if (Regex("^.*@.*@.*@.*@.*\$").matches(copyData)) {
                    DialogHelper.confirm(context,
                        "可用的模板",
                        "检测到已复制的机型信息：\n\n$copyData\n\n是否立即使用？",
                        {
                            splitCodeStr(copyData)
                        })
                }
            }
        } catch (ex: Exception) {
        }
    }

    private fun splitCodeStr(codeStr: String) {
        if (Regex("^.*@.*@.*@.*@.*\$").matches(codeStr)) {
            val deviceInfos = codeStr.split("@")
            editModel.setText(deviceInfos[0])
            editBrand.setText(deviceInfos[1])
            editManufacturer.setText(deviceInfos[2])
            editProductName.setText(deviceInfos[3])
            editDevice.setText(deviceInfos[4])
        }
    }

    private lateinit var editModel: EditText
    private lateinit var editBrand: EditText
    private lateinit var editProductName: EditText
    private lateinit var editDevice: EditText
    private lateinit var editManufacturer: EditText

    private fun loadCurrent() {
        if (getBackupProp(BACKUP_SUCCESS, "false") != "true") {
            return
        } else {
            editBrand.setText(Build.BRAND)
            editModel.setText(Build.MODEL)
            editProductName.setText(Build.PRODUCT)
            editDevice.setText(Build.DEVICE)
            editManufacturer.setText(Build.MANUFACTURER)
        }
    }

    private fun setDefault() {
        if (getBackupProp(BACKUP_SUCCESS, "false") != "true") {
            editBrand.setText(Build.BRAND)
            editModel.setText(Build.MODEL)
            editProductName.setText(Build.PRODUCT)
            editDevice.setText(Build.DEVICE)
            editManufacturer.setText(Build.MANUFACTURER)
        } else {
            editBrand.setText(getBackupProp(BACKUP_BRAND, Build.BRAND))
            editModel.setText(getBackupProp(BACKUP_MODEL, Build.MODEL))
            editProductName.setText(getBackupProp(BACKUP_PRODUCT, Build.PRODUCT))
            editDevice.setText(getBackupProp(BACKUP_DEVICE, Build.DEVICE))
            editManufacturer.setText(
                getBackupProp(
                    BACKUP_MANUFACTURER,
                    Build.MANUFACTURER
                )
            )
        }
    }

    @SuppressLint("ApplySharedPref")
    private fun backupDefault() {
        if (getBackupProp(BACKUP_SUCCESS, "false") != "true") {
            PropsUtils.setPorp(BACKUP_BRAND, Build.BRAND)
            PropsUtils.setPorp(BACKUP_MODEL, Build.MODEL)
            PropsUtils.setPorp(BACKUP_PRODUCT, Build.PRODUCT)
            PropsUtils.setPorp(BACKUP_DEVICE, Build.DEVICE)
            PropsUtils.setPorp(BACKUP_MANUFACTURER, Build.MANUFACTURER)
            PropsUtils.setPorp(BACKUP_SUCCESS, "true")
        }
    }
}
