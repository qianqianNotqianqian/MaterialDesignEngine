package mapleleaf.materialdesign.engine.ui.activities

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.model.ProcessInfo
import mapleleaf.materialdesign.engine.shell.ProcessUtils
import mapleleaf.materialdesign.engine.ui.adapter.AdapterProcess
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.utils.toast
import java.util.Timer
import java.util.TimerTask

class ActivityProcess : UniversalActivityBase(R.layout.activity_process) {

    private val processUtils = ProcessUtils()
    private var supported: Boolean = false
    private val handle = lifecycleScope

    override fun initializeComponents(savedInstanceState: Bundle?) {
        supported = processUtils.supported(this)
        val processUnsupported = findViewById<TextView>(R.id.process_unsupported)
        val processView = findViewById<LinearLayout>(R.id.process_view)
        val processList = findViewById<ListView>(R.id.process_list)
        val processSearch = findViewById<AppCompatEditText>(R.id.process_search)
        val processSortMode = findViewById<Spinner>(R.id.process_sort_mode)
        val processFilter = findViewById<Spinner>(R.id.process_filter)

        val baseColor = ContextCompat.getColor(MaterialDesignEngine.context, R.color.background_color)
        val primaryColor = ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
        val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)

        findViewById<MaterialCardView>(R.id.materialCardView).apply {
            strokeColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.3f)
            setCardBackgroundColor(blendedColor)
        }
        findViewById<MaterialCardView>(R.id.materialCardView2).apply {
            strokeColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.3f)
            setCardBackgroundColor(blendedColor)
        }
        findViewById<MaterialCardView>(R.id.materialCardView3).apply {
            strokeColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.3f)
            setCardBackgroundColor(blendedColor)
        }

        if (supported) {
            processUnsupported.isVisible = false
            processView.isVisible = true
        } else {
            processUnsupported.isVisible = true
            processView.isVisible = false
        }

        lifecycleScope.launch(Dispatchers.Main) { // 在主线程中启动携程
            if (supported) {
                val adapter = withContext(Dispatchers.IO) {
                    AdapterProcess(this@ActivityProcess)
                }
                processList.adapter = adapter

                processList.setOnItemClickListener { _, _, position, _ ->
                    openProcessDetail((processList.adapter as AdapterProcess).getItem(position))
                }
            }

            // 搜索关键字
            processSearch.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    (processList.adapter as AdapterProcess?)?.updateKeywords(v.text.toString())
                    return@setOnEditorActionListener true
                }
                false
            }

            // 排序方式
            processSortMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    (processList.adapter as AdapterProcess?)?.updateSortMode(
                        when (position) {
                            0 -> AdapterProcess.SORT_MODE_CPU
                            1 -> AdapterProcess.SORT_MODE_RES
                            2 -> AdapterProcess.SORT_MODE_PID
                            else -> AdapterProcess.SORT_MODE_DEFAULT
                        }
                    )
                }
            }

            processFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    (processList.adapter as AdapterProcess?)?.updateFilterMode(
                        when (position) {
                            0 -> AdapterProcess.FILTER_ANDROID_USER
                            1 -> AdapterProcess.FILTER_ANDROID_SYSTEM
                            2 -> AdapterProcess.FILTER_ANDROID
                            3 -> AdapterProcess.FILTER_OTHER
                            4 -> AdapterProcess.FILTER_ALL
                            else -> AdapterProcess.FILTER_ALL
                        }
                    )
                }
            }
        }
        pm = packageManager
        setToolbarTitle(getString(R.string.toolbar_title_activity_processes))
    }

    // 更新任务列表
    private fun updateData() {
        val processList = findViewById<ListView>(R.id.process_list)
        val data = processUtils.allProcess
        handle.launch {
            val adapter = processList.adapter as? AdapterProcess
            adapter?.setList(data)
            adapter?.notifyDataSetChanged()
        }
    }


    private fun resume() {
        if (supported && timer == null) {
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    updateData()
                }
            }, 0, 3000)
        }
    }

    private fun pause() {
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
    }

    private var timer: Timer? = null
    override fun onResume() {
        super.onResume()
        resume()
    }

    override fun onPause() {
        pause()
        super.onPause()
    }

    private val regexPackageName = Regex(".*\\..*")
    private fun isAndroidProcess(processInfo: ProcessInfo): Boolean {
        return (processInfo.command.contains("app_process") && processInfo.name.matches(
            regexPackageName
        ))
    }

    private var pm: PackageManager? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun loadIcon(imageView: ImageView, item: ProcessInfo) {
        lifecycleScope.launch(Dispatchers.IO) {
            var icon: Drawable? = null
            try {
                val name = if (item.name.contains(":")) item.name.substring(
                    0,
                    item.name.indexOf(":")
                ) else item.name
                val installInfo = pm!!.getPackageInfo(name, 0)
                icon = installInfo.applicationInfo.loadIcon(pm)
            } catch (ex: Exception) {
                // 处理异常
            } finally {
                withContext(Dispatchers.Main) {
                    if (icon != null) {
                        imageView.setImageDrawable(icon)
                    } else {
                        imageView.setImageDrawable(getDrawable(R.drawable.process_android))
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun openProcessDetail(processInfo: ProcessInfo) {
        lifecycleScope.launch {
            val detail = processUtils.getProcessDetail(processInfo.pid)
            if (detail != null) {
                val dialogView = LayoutInflater.from(this@ActivityProcess)
                    .inflate(R.layout.dialog_process_detail, null)

                if (pm == null) {
                    pm = packageManager
                }

                val name = if (detail.name.contains(":")) detail.name.substring(
                    0,
                    detail.name.indexOf(":")
                ) else detail.name
                try {
                    val app = pm!!.getApplicationInfo(name, 0)
                    detail.friendlyName = "" + app.loadLabel(pm!!)
                } catch (ex: Exception) {
                    detail.friendlyName = name
                }
                val dialog = DialogHelper.customDialog(this@ActivityProcess, dialogView)

                val materialCardView =
                    dialogView.findViewById<MaterialCardView>(R.id.materialCardView)
                val baseColor =
                    ContextCompat.getColor(MaterialDesignEngine.context, R.color.background_color)
                val primaryColor =
                    ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
                val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)
                materialCardView.setCardBackgroundColor(blendedColor)

                dialogView.run {
                    findViewById<TextView>(R.id.ProcessFriendlyName).text = detail.friendlyName
                    findViewById<TextView>(R.id.ProcessName).text = detail.name
                    findViewById<TextView>(R.id.ProcessCommand).text = detail.command
                    findViewById<TextView>(R.id.ProcessCmdline).text = detail.cmdline
                    findViewById<TextView>(R.id.ProcessPID).text = detail.pid.toString()
                    findViewById<TextView>(R.id.ProcessCPU).text = detail.getCpu().toString() + "%"
                    findViewById<TextView>(R.id.ProcessCpuSet).text = "" + detail.cpuSet.toString()
                    findViewById<TextView>(R.id.ProcessCGroup).text = "" + detail.cGroup
                    findViewById<TextView>(R.id.ProcessOOMADJ).text = "" + detail.oomAdj
                    findViewById<TextView>(R.id.ProcessOOMScoreAdj).text = "" + detail.oomScoreAdj
                    findViewById<TextView>(R.id.ProcessState).text = detail.getState()
                    findViewById<TextView>(R.id.ProcessMEM).text =
                        if (detail.res > 8192) "${(detail.res / 1024).toInt()}MB" else "${detail.res}KB"
                    findViewById<TextView>(R.id.ProcessSWAP).text =
                        if (detail.swap > 8192) "${(detail.swap / 1024).toInt()}MB" else "${detail.swap}KB"
                    findViewById<TextView>(R.id.ProcessUSER).text = processInfo.user
                    if (isAndroidProcess(processInfo)) {
                        loadIcon(findViewById(R.id.ProcessIcon), processInfo)
                        val btn = findViewById<Button>(R.id.ProcessStopApp)
                        btn.setOnClickListener {
                            processUtils.killProcess(processInfo)
                            dialog.dismiss()
                        }
                        btn.isVisible = true
                    }
                    findViewById<View>(R.id.ProcessKill).setOnClickListener {
                        processUtils.killProcess(detail.pid)
                        dialog.dismiss()
                    }
                }
            } else {
                toast("无法获取详情，该进程可能已经退出!")
            }
        }
    }

}
