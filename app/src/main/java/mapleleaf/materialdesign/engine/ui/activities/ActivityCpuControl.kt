package mapleleaf.materialdesign.engine.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.databinding.ActivityCpuControlBinding
import mapleleaf.materialdesign.engine.library.shell.GpuUtils
import mapleleaf.materialdesign.engine.model.CpuClusterStatus
import mapleleaf.materialdesign.engine.model.CpuStatus
import mapleleaf.materialdesign.engine.model.SelectItem
import mapleleaf.materialdesign.engine.shell.CpuFrequencyUtils
import mapleleaf.materialdesign.engine.shell.KernelProp
import mapleleaf.materialdesign.engine.shell.ThermalControlUtils
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.ui.dialog.DialogItemChooser
import mapleleaf.materialdesign.engine.ui.dialog.DialogItemChooser2
import mapleleaf.materialdesign.engine.utils.ThemeMode
import mapleleaf.materialdesign.engine.utils.getStatusBarHeight
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.locks.ReentrantLock

class ActivityCpuControl : UniversalActivityBase(R.layout.activity_cpu_control) {
    // 应用到指定的配置模式
    private var cpuModeName: String? = null
    private lateinit var themeMode: ThemeMode
    private var clusterCount = 0
    private var handler = Handler(Looper.getMainLooper())
    private var coreCount = 0
    private var cores = arrayListOf<CheckBox>()
    private var exynosHMP = false
    private var supportedGPU = false
    private var adrenoGPU = false
    private var adrenoFreqs = arrayOf("")
    private var adrenoGovernors = arrayOf("")
    private var adrenoPLevels = arrayOf("")
    private var inited = false
    private var statusOnBoot: CpuStatus? = null

    private val clusterFreqs: HashMap<Int, Array<String>> = HashMap()
    private val clusterGovernors: HashMap<Int, Array<String>> = HashMap()

    private val thermalControlUtils = ThermalControlUtils()
    private val cpuFrequencyUtil = CpuFrequencyUtils()
    private var qualcommThermalSupported: Boolean = false

    private lateinit var binding: ActivityCpuControlBinding
    private var toolbar: Toolbar? = null

//    override fun getLayoutResourceId() = R.layout.activity_cpu_control

    override fun initializeComponents(savedInstanceState: Bundle?) {
        binding = ActivityCpuControlBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbar = findViewById(R.id.toolbar)
        this.onViewCreated()
        val baseColor = ContextCompat.getColor(context, R.color.background_color)
        val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
        val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)
        toolbar?.setBackgroundColor(blendedColor)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayUseLogoEnabled(true)
        }

        themeMode = ThemeMode()
//        setToolbarTopMargin()
        setToolbarTitle(getString(R.string.toolbar_title_activity_cpu_control))
    }

    private fun setToolbarTopMargin() {
        val layoutParams = toolbar?.layoutParams as? ConstraintLayout.LayoutParams
        if (layoutParams != null) {
            layoutParams.topMargin = getStatusBarHeight(window, this)
            toolbar?.layoutParams = layoutParams
        } else {
            Log.e(
                "setToolbarTopMargin",
                "Error: toolbar is null or LayoutParams are not of type ConstraintLayout.LayoutParams. Actual type: ${toolbar?.layoutParams?.javaClass?.name}"
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initData() {
        clusterCount = cpuFrequencyUtil.clusterInfo.size
        for (cluster in 0 until clusterCount) {
            clusterFreqs[cluster] = cpuFrequencyUtil.getAvailableFrequencies(cluster)
            clusterGovernors[cluster] = cpuFrequencyUtil.getAvailableGovernors(cluster)
        }

        coreCount = cpuFrequencyUtil.coreCount

        val exynosCpuHotPlugSupport = cpuFrequencyUtil.exynosCpuhotplugSupport()
        exynosHMP = cpuFrequencyUtil.exynosHMP()

        supportedGPU = GpuUtils.supported()
        adrenoGPU = GpuUtils.isAdrenoGPU()
        qualcommThermalSupported = thermalControlUtils.isSupported

        if (supportedGPU) {
            adrenoGovernors = GpuUtils.getGovernors()
            adrenoFreqs = GpuUtils.getAvailableFreqs()
            adrenoPLevels = GpuUtils.getAdrenoGPUPowerLevels()
        }

        handler.post {
            try {
                if (exynosHMP || exynosCpuHotPlugSupport) {
                    binding.cpuExynos.isVisible = true
                    binding.exynosCpuHotplug.isEnabled = exynosCpuHotPlugSupport
                    binding.exynosHmpUp.isEnabled = exynosHMP
                    binding.exynosHmpDown.isEnabled = exynosHMP
                    binding.exynosHmpBooster.isEnabled = exynosHMP
                } else {
                    binding.cpuExynos.isVisible = false
                }

                if (supportedGPU) {
                    binding.gpuParams.isVisible = true
                    if (adrenoGPU) {
                        binding.adrenoGpuPower.isVisible = true
                    } else {
                        binding.adrenoGpuPower.isVisible = false
                    }
                } else {
                    binding.gpuParams.isVisible = false
                    binding.adrenoGpuPower.isVisible = false
                }

                for (i in 0 until coreCount) {
                    val checkBox = CheckBox(context)
                    checkBox.text = "CPU$i"
                    checkBox.setTextColor(ContextCompat.getColor(context, R.color.text_color))
                    cores.add(checkBox)
                    val params = GridLayout.LayoutParams()
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT
                    params.width = GridLayout.LayoutParams.MATCH_PARENT
                    binding.cpuCores.addView(checkBox, params)
                }

                bindEvent()
                inited = true
            } catch (ex: Exception) {

            }
        }
    }

    /*
    * 获得近似值
    */
    private fun getApproximation(arr: Array<String>, value: String): String {
        try {
            when {
                arr.contains(value) -> {
                    return value
                }

                else -> {
                    var approximation = if (arr.isNotEmpty()) arr[0] else ""
                    for (item in arr) {
                        if (item.toInt() <= value.toInt()) {
                            approximation = item
                        } else {
                            break
                        }
                    }

                    return approximation
                }
            }
        } catch (ex: Exception) {
            return value
        }
    }

    @SuppressLint("InflateParams")
    private fun bindEvent() {
        try {
            binding.thermalCoreControl.setOnClickListener {
                thermalControlUtils.setCoreControlState((it as CheckBox).isChecked)
            }
            binding.thermalVdd.setOnClickListener {
                thermalControlUtils.setVDDRestrictionState((it as CheckBox).isChecked)
            }
            binding.thermalParamters.setOnClickListener {
                thermalControlUtils.setTheramlState((it as CheckBox).isChecked)
            }

            for (cluster in 0 until clusterCount) {
                handler.post {
                    bindClusterConfig(cluster)
                }
            }

            bindGPUConfig()

            for (i in 0 until cores.size) {
                cores[i].setOnClickListener {
                    cpuFrequencyUtil.setCoreOnlineState(i, (it as CheckBox).isChecked)
                }
            }

            bindExynosConfig()
            bindCpuSetConfig()

        } catch (ex: Exception) {
        }
    }

    interface PickerCallback {
        fun onSelected(result: String)
    }

    interface PickerCallback2 {
        fun onSelected(result: BooleanArray)
    }

    private fun openMultiplePicker(
        dialogTitle: String,
        options: ArrayList<SelectItem>,
        selectedIndex: Int,
        pickerCallback: PickerCallback,
    ) {
        val selected = (ArrayList<SelectItem>().apply {
            if (selectedIndex > -1) {
                add(options[selectedIndex])
            }
        })
        DialogItemChooser2(
            themeMode.isDarkMode,
            options,
            selected,
            false,
            object : DialogItemChooser2.Callback {
                override fun onConfirm(selected: List<SelectItem>, status: BooleanArray) {
                    if (selected.isNotEmpty()) {
                        pickerCallback.onSelected("" + selected.first().value)
                    }
                }
            }).setTitle(dialogTitle).show(supportFragmentManager, "cpu-control")
    }

    private fun openMultiplePicker(
        dialogTitle: String,
        options: ArrayList<SelectItem>,
        pickerCallback: PickerCallback2,
    ) {
        DialogItemChooser(themeMode.isDarkMode, options, true, object : DialogItemChooser.Callback {
            override fun onConfirm(selected: List<SelectItem>, status: BooleanArray) {
                if (status.isNotEmpty()) {
                    pickerCallback.onSelected(status)
                }
            }
        }).setTitle(dialogTitle).show(supportFragmentManager, "cpu-control")
    }

    private fun string2SelectItem(items: Array<String>): ArrayList<SelectItem> {
        return ArrayList(items.map {
            SelectItem().apply {
                title = it
                value = it
            }
        })
    }

    private fun bindGPUConfig() {
        if (supportedGPU) {
            binding.gpuMinFreq.setOnClickListener {
                openMultiplePicker("选择GPU最小频率",
                    parseGPUFreqList(adrenoFreqs),
                    adrenoFreqs.indexOf(status.adrenoMinFreq),
                    object : PickerCallback {
                        override fun onSelected(result: String) {
                            if (GpuUtils.getMinFreq() != result) {
                                GpuUtils.setMinFreq(result)
                                status.adrenoMinFreq = result
                                setText(it as TextView?, subGPUFreqStr(result))
                            }
                        }
                    })
            }
            binding.gpuMaxFreq.setOnClickListener {
                openMultiplePicker("选择GPU最大频率",
                    parseGPUFreqList(adrenoFreqs),
                    adrenoFreqs.indexOf(status.adrenoMaxFreq),
                    object : PickerCallback {
                        override fun onSelected(result: String) {
                            if (GpuUtils.getMaxFreq() != result) {
                                GpuUtils.setMaxFreq(result)
                                status.adrenoMaxFreq = result
                                setText(it as TextView?, subGPUFreqStr(result))
                            }
                        }
                    })
            }
            binding.gpuGovernor.setOnClickListener {
                openMultiplePicker("选择GPU调度",
                    string2SelectItem(adrenoGovernors),
                    adrenoGovernors.indexOf(status.adrenoGovernor),
                    object : PickerCallback {
                        override fun onSelected(result: String) {
                            if (GpuUtils.getGovernor() != result) {
                                GpuUtils.setGovernor(result)
                                status.adrenoGovernor = result
                                setText(it as TextView?, result)
                            }
                        }
                    })
            }
            if (adrenoGPU) {
                binding.adrenoGpuMinPl.setOnClickListener {
                    openMultiplePicker("选择GPU最小功耗级别",
                        string2SelectItem(adrenoPLevels),
                        adrenoPLevels.indexOf(status.adrenoMinPL),
                        object : PickerCallback {
                            override fun onSelected(result: String) {
                                if (GpuUtils.getAdrenoGPUMinPowerLevel() != result) {
                                    GpuUtils.setAdrenoGPUMinPowerLevel(result)
                                    status.adrenoMinPL = result
                                    setText(it as TextView?, result)
                                }
                            }
                        })
                }
                binding.adrenoGpuMaxPl.setOnClickListener {
                    openMultiplePicker("选择GPU最大功耗级别",
                        string2SelectItem(adrenoPLevels),
                        adrenoPLevels.indexOf(status.adrenoMaxPL),
                        object : PickerCallback {
                            override fun onSelected(result: String) {
                                if (GpuUtils.getAdrenoGPUMaxPowerLevel() != result) {
                                    GpuUtils.setAdrenoGPUMaxPowerLevel(result)
                                    status.adrenoMaxPL = result
                                    setText(it as TextView?, result)
                                }
                            }
                        })
                }
                binding.adrenoGpuDefaultPl.setOnClickListener {
                    openMultiplePicker("选择GPU默认功耗级别",
                        string2SelectItem(adrenoPLevels),
                        adrenoPLevels.indexOf(status.adrenoDefaultPL),
                        object : PickerCallback {
                            override fun onSelected(result: String) {
                                if (GpuUtils.getAdrenoGPUDefaultPowerLevel() != result) {
                                    GpuUtils.setAdrenoGPUDefaultPowerLevel(result)
                                    status.adrenoDefaultPL = result
                                    updateUI()
                                }
                            }
                        })
                }
            }
        }
    }

    private fun bindExynosConfig() {
        binding.exynosCpuHotplug.setOnClickListener {
            cpuFrequencyUtil.exynosHotplug = (it as CheckBox).isChecked
        }
        binding.exynosHmpBooster.setOnClickListener {
            cpuFrequencyUtil.exynosBooster = (it as CheckBox).isChecked
        }
        binding.exynosHmpUp.setOnSeekBarChangeListener(
            OnSeekBarChangeListener(
                true,
                cpuFrequencyUtil
            )
        )
        binding.exynosHmpDown.setOnSeekBarChangeListener(
            OnSeekBarChangeListener(
                false,
                cpuFrequencyUtil
            )
        )
    }

    private fun bindCpuSetConfig(currentState: String, callback: PickerCallback2) {
        if (currentState.isNotEmpty()) {
            val coreState = parsetCpuset(currentState)
            openMultiplePicker("选择要使用的核心",
                getCoreList(coreState),
                object : PickerCallback2 {
                    override fun onSelected(result: BooleanArray) {
                        callback.onSelected(result)
                        updateUI()
                    }
                })
        }
    }

    private fun bindCpuSetConfig() {
        binding.cpusetBg.setOnClickListener {
            bindCpuSetConfig(status.cpusetBackground, object : PickerCallback2 {
                override fun onSelected(result: BooleanArray) {
                    status.cpusetBackground = parsetCpuset(result)
                    KernelProp.setProp("/dev/cpuset/background/cpus", status.cpusetBackground)
                }
            })
        }
        binding.cpusetSystemBg.setOnClickListener {
            bindCpuSetConfig(status.cpusetSysBackground, object : PickerCallback2 {
                override fun onSelected(result: BooleanArray) {
                    status.cpusetSysBackground = parsetCpuset(result)
                    KernelProp.setProp(
                        "/dev/cpuset/system-background/cpus",
                        status.cpusetSysBackground
                    )
                }
            })
        }
        binding.cpusetForeground.setOnClickListener {
            bindCpuSetConfig(status.cpusetForeground, object : PickerCallback2 {
                override fun onSelected(result: BooleanArray) {
                    status.cpusetForeground = parsetCpuset(result)
                    KernelProp.setProp("/dev/cpuset/foreground/cpus", status.cpusetForeground)
                }
            })
        }
        binding.cpusetTopApp.setOnClickListener {
            bindCpuSetConfig(status.cpusetTopApp, object : PickerCallback2 {
                override fun onSelected(result: BooleanArray) {
                    status.cpusetTopApp = parsetCpuset(result)
                    KernelProp.setProp("/dev/cpuset/top-app/cpus", status.cpusetTopApp)
                }
            })
        }
    }

    private fun getClusterFreqs(cluster: Int): Array<String> {
        val freqs = clusterFreqs[cluster]
        if (freqs == null || freqs.size < 2) {
            clusterFreqs[cluster] = cpuFrequencyUtil.getAvailableFrequencies(cluster)
        }
        return clusterFreqs[cluster]!!
    }

    private fun getClusterGovernors(cluster: Int): Array<String> {
        val freqs = clusterGovernors[cluster]
        if (freqs == null || freqs.size < 2) {
            clusterFreqs[cluster] = cpuFrequencyUtil.getAvailableGovernors(cluster)
        }
        return clusterGovernors[cluster]!!
    }

    @SuppressLint("SetTextI18n")
    private fun bindClusterConfig(cluster: Int) {
        val view = View.inflate(context, R.layout.fragment_cpu_cluster, null)
        binding.cpuClusterList.addView(view)
        view.findViewById<TextView>(R.id.cluster_title).text = "CPU - Cluster $cluster"
        view.findViewById<TextView>(R.id.cluster_title)
            .setTextColor(ContextCompat.getColor(context, R.color.text_color))
        view.tag = "cluster_$cluster"

        val clusterMinFreq = view.findViewById<TextView>(R.id.cluster_min_freq)
        val clusterMaxFreq = view.findViewById<TextView>(R.id.cluster_max_freq)
        val clusterGovernor = view.findViewById<TextView>(R.id.cluster_governor)
        val clusterGovernorParams = view.findViewById<TextView>(R.id.cluster_governor_params)

        clusterMinFreq.setOnClickListener {
            val freqs = getClusterFreqs(cluster)
            if (freqs.isNotEmpty()) {
                openMultiplePicker("选择最小频率",
                    parseFreqList(freqs),
                    freqs.indexOf(
                        getApproximation(
                            freqs,
                            status.cpuClusterStatuses[cluster].min_freq
                        )
                    ),
                    object : PickerCallback {
                        override fun onSelected(result: String) {
                            if (cpuFrequencyUtil.getCurrentMinFrequency(cluster) != result) {
                                cpuFrequencyUtil.setMinFrequency(result, cluster)
                                status.cpuClusterStatuses[cluster].min_freq = result
                                setText(it as TextView?, subFreqStr(result))
                            }
                        }
                    })
            } else {
            }
        }

        clusterMaxFreq.setOnClickListener {
            val freqs = getClusterFreqs(cluster)
            if (freqs.isNotEmpty()) {
                openMultiplePicker("选择最大频率",
                    parseFreqList(freqs),
                    freqs.indexOf(
                        getApproximation(
                            freqs,
                            status.cpuClusterStatuses[cluster].max_freq
                        )
                    ),
                    object : PickerCallback {
                        override fun onSelected(result: String) {
                            if (cpuFrequencyUtil.getCurrentMinFrequency(cluster) != result) {
                                cpuFrequencyUtil.setMaxFrequency(result, cluster)
                                status.cpuClusterStatuses[cluster].max_freq = result
                                setText(it as TextView?, subFreqStr(result))
                            }
                        }
                    })
            } else {
            }
        }

        clusterGovernor.setOnClickListener {
            val governors = getClusterGovernors(cluster)
            if (governors.isNotEmpty()) {
                openMultiplePicker("选择调度模式",
                    string2SelectItem(governors),
                    governors.indexOf(status.cpuClusterStatuses[cluster].governor),
                    object : PickerCallback {
                        override fun onSelected(result: String) {
                            if (cpuFrequencyUtil.getCurrentScalingGovernor(cluster) != result) {
                                cpuFrequencyUtil.setGovernor(result, cluster)
                                status.cpuClusterStatuses[cluster].governor = result
                                setText(it as TextView?, result)
                            }
                        }
                    })
            } else {
            }
            return@setOnClickListener
        }

        clusterGovernorParams.setOnClickListener {
            status.cpuClusterStatuses[cluster].governor_params =
                cpuFrequencyUtil.getCurrentScalingGovernorParams(cluster)

            if (status.cpuClusterStatuses[cluster].governor_params != null) {
                val msg = StringBuilder()
                for (param in status.cpuClusterStatuses[cluster].governor_params) {
                    msg.append("\n")
                    msg.append(param.key)
                    msg.append("：")
                    msg.append(param.value)
                    msg.append("\n")
                }
                DialogHelper.alert(this, "调度器参数", msg.toString())
            }
        }
    }

    private fun parsetCpuset(booleanArray: BooleanArray): String {
        val stringBuilder = StringBuilder()
        for (index in booleanArray.indices) {
            if (booleanArray.get(index)) {
                if (stringBuilder.isNotEmpty()) {
                    stringBuilder.append(",")
                }
                stringBuilder.append(index)
            }
        }
        return stringBuilder.toString()
    }

    private fun parsetCpuset(value: String): BooleanArray {
        val cores = ArrayList<Boolean>()
        for (coreIndex in 0 until coreCount) {
            cores.add(false)
        }
        if (value.isEmpty() || value == "error") {

        } else {
            val valueGroups = value.split(",")
            for (valueGroup in valueGroups) {
                if (valueGroup.contains("-")) {
                    try {
                        val range = valueGroup.split("-")
                        val min = range[0].toInt()
                        val max = range[1].toInt()
                        for (coreIndex in min..max) {
                            if (coreIndex < cores.size) {
                                cores[coreIndex] = true
                            }
                        }
                    } catch (ex: Exception) {
                    }
                } else {
                    try {
                        val coreIndex = valueGroup.toInt()
                        if (coreIndex < cores.size) {
                            cores[coreIndex] = true
                        }
                    } catch (ex: Exception) {
                    }
                }
            }
        }
        return cores.toBooleanArray()
    }

    private fun getCoreList(coreState: BooleanArray): ArrayList<SelectItem> {
        val cores = ArrayList<SelectItem>()
        for (coreIndex in 0 until coreCount) {
            cores.add(SelectItem().apply {
                title = "Cpu$coreIndex"
                if (coreIndex < coreState.size) {
                    selected = coreState[coreIndex]
                }
            })
        }
        return cores
    }

    class OnSeekBarChangeListener(
        private var up: Boolean,
        private var cpuFrequencyUtils: CpuFrequencyUtils,
    ) : SeekBar.OnSeekBarChangeListener {
        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if (seekBar != null) {
                if (up)
                    cpuFrequencyUtils.exynosHmpUP = seekBar.progress
                else
                    cpuFrequencyUtils.exynosHmpDown = seekBar.progress
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        @SuppressLint("ApplySharedPref")
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        }
    }

    private var status = CpuStatus()

    private fun updateState() {
        try {
            for (cluster in 0 until clusterCount) {
                if (status.cpuClusterStatuses.size < cluster + 1) {
                    status.cpuClusterStatuses.add(CpuClusterStatus())
                }
                val config = status.cpuClusterStatuses.get(cluster)
                config.min_freq = cpuFrequencyUtil.getCurrentMinFrequency(cluster)
                config.max_freq = cpuFrequencyUtil.getCurrentMaxFrequency(cluster)
                config.governor = cpuFrequencyUtil.getCurrentScalingGovernor(cluster)
                // TODO: 要不要加载 config.governor_params = cpuFrequencyUtil.getCurrentScalingGovernorParams(cluster)
            }

            if (qualcommThermalSupported) {
                status.coreControl = thermalControlUtils.coreControlState
                status.vdd = thermalControlUtils.vddRestrictionState
                status.msmThermal = thermalControlUtils.theramlState
            }

            status.exynosHmpUP = cpuFrequencyUtil.exynosHmpUP
            status.exynosHmpDown = cpuFrequencyUtil.exynosHmpDown
            status.exynosHmpBooster = cpuFrequencyUtil.exynosBooster
            status.exynosHotplug = cpuFrequencyUtil.exynosHotplug

            if (supportedGPU) {
                if (adrenoGPU) {
                    status.adrenoDefaultPL = GpuUtils.getAdrenoGPUDefaultPowerLevel()
                    status.adrenoMinPL = GpuUtils.getAdrenoGPUMinPowerLevel()
                    status.adrenoMaxPL = GpuUtils.getAdrenoGPUMaxPowerLevel()
                }
                status.adrenoMinFreq = getApproximation(adrenoFreqs, GpuUtils.getMinFreq())
                status.adrenoMaxFreq = getApproximation(adrenoFreqs, GpuUtils.getMaxFreq())
                status.adrenoGovernor = GpuUtils.getGovernor()
            }

            status.coreOnline = arrayListOf<Boolean>()
            try {
                mLock.lockInterruptibly()
                for (i in 0 until coreCount) {
                    status.coreOnline.add(cpuFrequencyUtil.getCoreOnlineState(i))
                }
            } catch (ex: Exception) {
            } finally {
                mLock.unlock()
            }
            status.cpusetBackground = KernelProp.getProp("/dev/cpuset/background/cpus")
            status.cpusetSysBackground = KernelProp.getProp("/dev/cpuset/system-background/cpus")
            status.cpusetForeground = KernelProp.getProp("/dev/cpuset/foreground/cpus")
            status.cpusetRestricted = KernelProp.getProp("/dev/cpuset/restricted/cpus")
            status.cpusetTopApp = KernelProp.getProp("/dev/cpuset/top-app/cpus")

            handler.post {
                updateUI()
            }
        } catch (ex: Exception) {
        }
    }

    private val mLock = ReentrantLock()
    private fun subFreqStr(freq: String): String {
        return if (freq.length > 3) {
            freq.substring(0, freq.length - 3) + " Mhz"
        } else {
            freq
        }
    }

    private fun subGPUFreqStr(freq: String): String {
        if (freq.isEmpty()) {
            return ""
        }
        return if (freq.length > 6) {
            freq.substring(0, freq.length - 6) + " Mhz"
        } else {
            freq
        }
    }

    private fun parseFreqList(arr: Array<String>): ArrayList<SelectItem> {
        val arrMhz = ArrayList<SelectItem>()
        for (item in arr) {
            arrMhz.add(SelectItem().apply {
                title = subFreqStr(item)
                value = item
            })
        }
        return arrMhz
    }


    private fun parseGPUFreqList(arr: Array<String>): ArrayList<SelectItem> {
        val arrMhz = ArrayList<SelectItem>()
        for (item in arr) {
            arrMhz.add(
                SelectItem().apply {
                    title = subGPUFreqStr(item)
                    value = item
                }
            )
        }
        return arrMhz
    }

    private fun setText(view: TextView?, text: String) {
        if (view != null && view.text != text) {
            view.text = text
        }
    }

    private fun updateUI() {
        try {
            for (cluster in 0 until clusterCount) {
                if (status.cpuClusterStatuses.size > cluster) {
                    val clusterView =
                        binding.cpuClusterList.findViewWithTag<View>("cluster_" + cluster)
                    val clusterMinFreq =
                        clusterView.findViewById<TextView>(R.id.cluster_min_freq)
                    val clusterMaxFreq =
                        clusterView.findViewById<TextView>(R.id.cluster_max_freq)
                    val clusterGovernor =
                        clusterView.findViewById<TextView>(R.id.cluster_governor)
                    val status = status.cpuClusterStatuses[cluster]!!
                    setText(clusterMinFreq, subFreqStr(status.min_freq))
                    setText(clusterMaxFreq, subFreqStr(status.max_freq))
                    setText(clusterGovernor, status.governor)
                }
            }

            if (qualcommThermalSupported) {
                binding.qualcommThermal.isVisible = true
                if (status.coreControl.isEmpty()) {
                    binding.thermalCoreControl.isEnabled = false
                }
                binding.thermalCoreControl.isChecked = status.coreControl == "1"

                if (status.vdd.isEmpty()) {
                    binding.thermalVdd.isEnabled = false
                }
                binding.thermalVdd.isChecked = status.vdd == "1"


                if (status.msmThermal.isEmpty()) {
                    binding.thermalParamters.isEnabled = false
                }
                binding.thermalParamters.isChecked = status.msmThermal == "Y"
            } else {
                binding.qualcommThermal.isVisible = false
            }

            binding.exynosHmpDown.progress = status.exynosHmpDown
            binding.exynosHmpDownText.text = status.exynosHmpDown.toString()
            binding.exynosHmpUp.progress = status.exynosHmpUP
            binding.exynosHmpUpText.text = status.exynosHmpUP.toString()
            binding.exynosCpuHotplug.isChecked = status.exynosHotplug
            binding.exynosHmpBooster.isChecked = status.exynosHmpBooster

            if (supportedGPU) {
                if (adrenoGPU) {
                    binding.adrenoGpuDefaultPl.text = status.adrenoDefaultPL
                    binding.adrenoGpuMinPl.text = status.adrenoMinPL
                    binding.adrenoGpuMaxPl.text = status.adrenoMaxPL
                }
                binding.gpuMinFreq.text = subGPUFreqStr(status.adrenoMinFreq)
                binding.gpuMaxFreq.text = subGPUFreqStr(status.adrenoMaxFreq)
                binding.gpuGovernor.text = status.adrenoGovernor
            }

            for (i in 0 until coreCount) {
                cores[i].isChecked = status.coreOnline[i]
            }

            binding.cpusetBg.text = status.cpusetBackground
            binding.cpusetSystemBg.text = status.cpusetSysBackground
            binding.cpusetForeground.text = status.cpusetForeground
            binding.cpusetTopApp.text = status.cpusetTopApp
        } catch (ex: Exception) {
        }
    }

    private fun onViewCreated() {
        if (intent.hasExtra("cpuModeName")) {
            cpuModeName = intent.getStringExtra("cpuModeName")
        }

        Thread {
            initData()
        }.start()
    }

    private var timer: Timer? = null
    override fun onResume() {
        super.onResume()

        if (timer == null) {
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    if (!inited) {
                        return
                    }
                    updateState()
                }
            }, 1000, 1000)
        }
    }

    override fun onPause() {
        super.onPause()
        stopStatusUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopStatusUpdate()
    }

    private fun stopStatusUpdate() {
        try {
            if (timer != null) {
                timer!!.cancel()
                timer = null
            }
        } catch (ex: Exception) {

        }
    }
}