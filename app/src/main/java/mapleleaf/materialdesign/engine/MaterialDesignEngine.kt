package mapleleaf.materialdesign.engine

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.kongzue.baseframework.BaseApp
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.style.MIUIStyle
import com.kongzue.dialogxmaterialyou.style.MaterialYouStyle
import mapleleaf.materialdesign.engine.store.SpfConfig

class MaterialDesignEngine : BaseApp<MaterialDesignEngine>() {
    companion object {
        lateinit var context: Application
        private var config: SharedPreferences? = null

        private fun getGlobalConfig(): SharedPreferences {
            if (config == null) {
                config = context.getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE)
            }
            return config!!
        }

        fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return getGlobalConfig().getBoolean(key, defaultValue)
        }

        fun setBoolean(key: String, value: Boolean) {
            getGlobalConfig().edit().putBoolean(key, value).apply()
        }

    }

    override fun init() {
        context = this
        DialogX.init(this)
        DialogX.useHaptic = true
        DialogX.globalStyle = MaterialYouStyle()
        DialogX.globalTheme = DialogX.THEME.AUTO
        DialogX.onlyOnePopTip = false
    }
}
