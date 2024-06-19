package mapleleaf.materialdesign.engine.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.permissions.CheckRootStatus
import mapleleaf.materialdesign.engine.store.SpfConfig

class ActivityCheckPermission : UniversalActivityBase(R.layout.activity_check_permission) {
    companion object {
        var finished = false
    }

    private lateinit var startStateText: TextView
    private lateinit var globalSPF: SharedPreferences

//    override fun getLayoutResourceId() = R.layout.activity_check_permission

    override fun initializeComponents(savedInstanceState: Bundle?) {
        globalSPF = getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE)
        checkRoot()
        startStateText = findViewById(R.id.start_state_text)
        hideToolbar()
        getToolbar().setBackgroundColor(getColor(R.color.transparent))
    }

    /**
     * 开始检查必需权限
     */
    private fun checkRoot() {
        val disableSeLinux = globalSPF.getBoolean(SpfConfig.GLOBAL_SPF_DISABLE_ENFORCE, false)
        CheckRootStatus(this, {
            if (globalSPF.getBoolean(SpfConfig.GLOBAL_SPF_CONTRACT, false)) {
                CheckFileWrite(this).run()
            } else {
                startToFinish()
            }
        }, disableSeLinux, CheckPermission(this)).forceGetRoot()
    }

    private class CheckFileWrite(private val context: ActivityCheckPermission) : Runnable {
        override fun run() {
            context.startStateText.text = "检查并获取必需权限……"
            context.hasRoot = true
            context.startToFinish()
        }

    }

    private class CheckPermission(private val context: ActivityCheckPermission) : Runnable {
        override fun run() {
            context.startToFinish()
        }
    }

    private var hasRoot = false

    /**
     * 启动完成
     */
    private fun startToFinish() {
        startStateText.text = "启动完成！"
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            ActivityCompat.startActivity(
                this,
                Intent(this@ActivityCheckPermission, ActivitySystemOverview::class.java),
                ActivityOptionsCompat.makeCustomAnimation(
                    this,
                    R.anim.activity_fade_in_up,
                    R.anim.activity_fade_out_up
                ).toBundle()
            )
            finished = true
            finish()
        }, 600)
    }

}