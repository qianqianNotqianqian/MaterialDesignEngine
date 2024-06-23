package mapleleaf.materialdesign.engine.ui.fragments

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import mapleleaf.materialdesign.engine.shell.SwapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.databinding.FragmentSystemOverviewBinding
import mapleleaf.materialdesign.engine.shell.GpuUtils
import mapleleaf.materialdesign.engine.shell.MemoryUtils
import mapleleaf.materialdesign.engine.model.CpuCoreInfo
import mapleleaf.materialdesign.engine.shell.CpuFrequencyUtils
import mapleleaf.materialdesign.engine.shell.CpuLoadUtils
import mapleleaf.materialdesign.engine.shell.KeepShellPublic
import mapleleaf.materialdesign.engine.store.SpfConfig
import mapleleaf.materialdesign.engine.ui.adapter.AdapterCpuCores
import mapleleaf.materialdesign.engine.ui.dialog.DialogElectricityUnit
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.utils.openUrlByBrowser
import mapleleaf.materialdesign.engine.utils.toast
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Timer
import java.util.TimerTask

class FragmentOverViewSystem : Fragment() {
    private var binding: FragmentSystemOverviewBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSystemOverviewBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private var cpuFrequencyUtil = CpuFrequencyUtils()
    private lateinit var globalSPF: SharedPreferences
    private var timer: Timer? = null

    private lateinit var spf: SharedPreferences
    private var lifecycleCoroutineScope = lifecycleScope
    private var cpuLoadUtils = CpuLoadUtils()
    private val memoryUtils = MemoryUtils()

    private suspend fun forceKSWAPD(mode: Int): String {
        return withContext(Dispatchers.Default) {
            SwapUtils(requireContext()).forceKswapd(mode)
        }
    }

    private suspend fun dropCaches() {
        return withContext(Dispatchers.Default) {
            KeepShellPublic.doCmdSync(
                "sync\n" +
                        "echo 3 > /proc/sys/vm/drop_caches\n" +
                        "echo 1 > /proc/sys/vm/compact_memory"
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityManager = requireContext().getSystemService(ACTIVITY_SERVICE) as ActivityManager
        batteryManager =
            requireContext().getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        globalSPF =
            requireContext().getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE)

        spf = requireContext().getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE)

        binding!!.homeClearRam.setOnClickListener {
            binding!!.homeRaminfoText.text = getString(R.string.please_wait)
            lifecycleScope.launch {
                dropCaches()
                toast("缓存已清理...")
            }
        }

        binding!!.homeClearSwap.setOnClickListener {
            binding!!.homeZramsizeText.text = getText(R.string.please_wait)
            lifecycleScope.launch {
                toast("开始回收少量内存(长按回收更多~)")
                val result = forceKSWAPD(1)
                toast(result)
            }
        }

        binding!!.homeClearSwap.setOnLongClickListener {
            binding!!.homeZramsizeText.text = getText(R.string.please_wait)
            lifecycleScope.launch {
                val result = forceKSWAPD(2)
                toast(result)
            }
            true
        }

        binding!!.homeHelp.setOnClickListener {
            openUrlByBrowser(requireContext(), "http://vtools.omarea.com/")
        }

        binding!!.homeBatteryEdit.setOnClickListener {
            DialogElectricityUnit().showDialog(requireContext())
        }

        // 点击CPU核心 查看详细参数
        binding!!.cpuCoreList.setOnItemClickListener { _, _, position, _ ->
            cpuFrequencyUtil.getCoregGovernorParams(position)?.run {
                val msg = StringBuilder()
                for (param in this) {
                    msg.append("\n")
                    msg.append(param.key)
                    msg.append("：")
                    msg.append(param.value)
                    msg.append("\n")
                }
                DialogHelper.alert(requireActivity(), "调度器参数", msg.toString())
            }
        }

        binding!!.homeDeviceName.text = when (Build.VERSION.SDK_INT) {
            34 -> "Android 14"
            33 -> "Android 13L"
            32 -> "Android 13"
            31 -> "Android 12"
            30 -> "Android 11"
            29 -> "Android 10"
            28 -> "Android 9"
            27 -> "Android 8.1"
            26 -> "Android 8.0"
            25 -> "Android 7.0"
            24 -> "Android 7.0"
            23 -> "Android 6.0"
            22 -> "Android 5.1"
            21 -> "Android 5.0"
            else -> "SDK(" + Build.VERSION.SDK_INT + ")"
        }
        (Build.MANUFACTURER + " " + Build.MODEL + " (SDK" + Build.VERSION.SDK_INT + ")").trim()
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        if (isDetached) {
            return
        }

        maxFreqs.clear()
        minFreqs.clear()
        stopTimer()
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    updateInfo()
                }
            }, 0, 1500)
        }
    }

    private var coreCount = -1
    private lateinit var batteryManager: BatteryManager
    private lateinit var activityManager: ActivityManager

    private var minFreqs = HashMap<Int, String>()
    private var maxFreqs = HashMap<Int, String>()
    fun format1(value: Double): String {
        var bd = BigDecimal(value)
        bd = bd.setScale(1, RoundingMode.HALF_UP)
        return bd.toString()
    }

    private var batteryCurrentNow = 0L

    @SuppressLint("SetTextI18n")
    private fun updateRamInfo() {
        try {
            val info = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(info)
            val totalMem = (info.totalMem / 1024 / 1024f).toInt()
            val availMem = (info.availMem / 1024 / 1024f).toInt()

            val swapInfo = KeepShellPublic.doCmdSync("free -m | grep Swap")
            var swapTotal = 0
            var swapUse = 0
            if (swapInfo.contains("Swap")) {
                try {
                    val swapInformation =
                        swapInfo.substring(swapInfo.indexOf(" "), swapInfo.lastIndexOf(" ")).trim()
                    if (Regex("[\\d]+[\\s]{1,}[\\d]{1,}").matches(swapInformation)) {
                        swapTotal =
                            swapInformation.substring(0, swapInformation.indexOf(" ")).trim()
                                .toInt()
                        swapUse =
                            swapInformation.substring(swapInformation.indexOf(" ")).trim().toInt()
                    }
                } catch (ex: java.lang.Exception) {
                }
                // home_swapstate.text = swapInfo.substring(swapInfo.indexOf(" "), swapInfo.lastIndexOf(" ")).trim()
            }

            lifecycleCoroutineScope.launch {
                if (binding != null) {
                    binding!!.homeRaminfoText.text =
                        "${((totalMem - availMem) * 100 / totalMem)}% (${totalMem / 1024 + 1}GB)"
                    binding!!.homeRaminfo.setData(totalMem.toFloat(), availMem.toFloat())
                    binding!!.homeSwapstateChat.setData(
                        swapTotal.toFloat(),
                        (swapTotal - swapUse).toFloat()
                    )
                    binding!!.homeZramsizeText.text = (
                            if (swapTotal > 99) {
                                "${(swapUse * 100.0 / swapTotal).toInt()}% (${format1(swapTotal / 1024.0)}GB)"
                            } else {
                                "${(swapUse * 100.0 / swapTotal).toInt()}% (${swapTotal}MB)"
                            }
                            )
                }

            }
        } catch (ex: Exception) {
        }
    }

    /**
     * dp转换成px
     */
    private fun dp2px(dpValue: Float): Int {
        val scale = requireContext().resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    private fun elapsedRealtimeStr(): String {
        val timer = SystemClock.elapsedRealtime() / 1000
        return String.format("%02d:%02d:%02d", timer / 3600, timer % 3600 / 60, timer % 60)
    }

    private var updateTick = 0

    @SuppressLint("SetTextI18n")
    private fun updateInfo() {
        if (coreCount < 1) {
            coreCount = cpuFrequencyUtil.coreCount
            lifecycleCoroutineScope.launch {
                try {
                    binding!!.cpuCoreCount.text = "$coreCount 核心"
                } catch (ex: Exception) {
                }
            }
        }
        val cores = ArrayList<CpuCoreInfo>()
        for (coreIndex in 0 until coreCount) {
            val core = CpuCoreInfo(coreIndex)

            core.currentFreq = cpuFrequencyUtil.getCurrentFrequency("cpu$coreIndex")
            if (!maxFreqs.containsKey(coreIndex) || (core.currentFreq != "" && maxFreqs[coreIndex].isNullOrEmpty())) {
                maxFreqs[coreIndex] = cpuFrequencyUtil.getCurrentMaxFrequency("cpu$coreIndex")
            }
            core.maxFreq = maxFreqs[coreIndex]

            if (!minFreqs.containsKey(coreIndex) || (core.currentFreq != "" && minFreqs[coreIndex].isNullOrEmpty())) {
                minFreqs.put(coreIndex, cpuFrequencyUtil.getCurrentMinFrequency("cpu$coreIndex"))
            }
            core.minFreq = minFreqs[coreIndex]
            cores.add(core)
        }
        val loads = cpuLoadUtils.cpuLoad
        for (core in cores) {
            if (loads.containsKey(core.coreIndex)) {
                core.loadRatio = loads[core.coreIndex]!!
            }
        }

        val gpuFreq = GpuUtils.getGpuFreq() + "Mhz"
        val gpuLoad = GpuUtils.getGpuLoad()

        // 电池电流
        batteryCurrentNow =
            batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        // 电量
        val batteryCapacity =
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        // 电池温度
        val intent = context?.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val rawTemperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val celsiusTemperature = rawTemperature?.toFloat()?.div(10.0)

        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val batteryVoltage = voltage?.div(1000.0)
        val batteryInfo = "$batteryCapacity%    ${batteryVoltage}V"
        updateRamInfo()
        val memInfo = memoryUtils.memoryInfo

        lifecycleCoroutineScope.launch {
            try {
                binding!!.homeSwapCached.text = "" + (memInfo.swapCached / 1024) + "MB"
                binding!!.homeBuffers.text = "" + (memInfo.buffers / 1024) + "MB"
                binding!!.homeDirty.text = "" + (memInfo.dirty / 1024) + "MB"

                binding!!.homeRunningTime.text = elapsedRealtimeStr()
                if (batteryCurrentNow != Long.MIN_VALUE && batteryCurrentNow != Long.MAX_VALUE) {

                    binding!!.homeBatteryNow.text = (batteryCurrentNow / globalSPF.getInt(
                        SpfConfig.GLOBAL_SPF_CURRENT_NOW_UNIT,
                        SpfConfig.GLOBAL_SPF_CURRENT_NOW_UNIT_DEFAULT
                    )).toString() + "mA"

                } else {
                    binding!!.homeBatteryNow.text = "--"
                }

                binding!!.homeBatteryCapacity.text = batteryInfo
                binding!!.homeBatteryTemperature.text = "${celsiusTemperature}°C"

                binding!!.homeGpuFreq.text = gpuFreq
                binding!!.homeGpuLoad.text = "负载：$gpuLoad%"
                if (gpuLoad > -1) {
                    binding!!.homeGpuChat.setData(100.toFloat(), (100 - gpuLoad).toFloat())
                }
                if (loads.containsKey(-1)) {
                    binding!!.cpuCoreTotalLoad.text = "负载：" + loads[-1]!!.toInt().toString() + "%"
                    binding!!.homeCpuChat.setData(
                        100.toFloat(),
                        (100 - loads[-1]!!.toInt()).toFloat()
                    )
                }
                if (binding!!.cpuCoreList.adapter == null) {
                    val layoutParams = binding!!.cpuCoreList.layoutParams
                    if (cores.size < 6) {
                        layoutParams.height = dp2px(105 * 2F)
                        binding!!.cpuCoreList.numColumns = 2
                    } else if (cores.size > 12) {
                        layoutParams.height = dp2px(105 * 4F)
                    } else if (cores.size > 8) {
                        layoutParams.height = dp2px(105 * 3F)
                    } else {
                        layoutParams.height = dp2px(105 * 2F)
                    }
                    binding!!.cpuCoreList.layoutParams = layoutParams
                    binding!!.cpuCoreList.adapter = AdapterCpuCores(requireContext(), cores)
                } else {
                    (binding!!.cpuCoreList.adapter as AdapterCpuCores).setData(cores)
                }

            } catch (ex: Exception) {

            }
        }
        updateTick++
        if (updateTick > 5) {
            updateTick = 0
            minFreqs.clear()
            maxFreqs.clear()
        }
    }

    private fun stopTimer() {
        if (this.timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    override fun onPause() {
        stopTimer()
        super.onPause()
    }
}
