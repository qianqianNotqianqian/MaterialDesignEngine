package mapleleaf.materialdesign.engine.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.google.android.material.card.MaterialCardView
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.modify.DexCompileModify
import mapleleaf.materialdesign.engine.store.SpfConfig
import mapleleaf.materialdesign.engine.ui.dialog.DialogCustomMAC
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.ui.dialog.DialogSettingModifyDPI
import mapleleaf.materialdesign.engine.ui.dialog.DialogSettingModifyDevice
import mapleleaf.materialdesign.engine.ui.dialog.DialogSettingWIFI


class ActivitySystemModify : UniversalActivityBase(R.layout.activity_system_modify) {
    private fun createItem(
        title: String,
        desc: String,
        runnable: Runnable?,
        wran: Boolean = true,
    ): HashMap<String, Any> {
        val item = HashMap<String, Any>()
        item["Title"] = title
        item["Desc"] = desc
        item["Wran"] = wran
        if (runnable != null)
            item["Action"] = runnable
        return item
    }

    private lateinit var systemModifyListView: ListView

    override fun initializeComponents(savedInstanceState: Bundle?) {

        systemModifyListView = findViewById(R.id.system_modify_listview)

        setSupportActionBar(getToolbar())
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }
        getToolbar().setNavigationOnClickListener {
            this.finish()
        }

        setToolbarTitle(getString(R.string.toolbar_title_activity_sundry))

        customizeCardView(findViewById(R.id.materialCardView))
        initItem(this.systemModifyListView)
    }

    private fun initItem(view: View) {
        val activity = this
        val context = this
        val listItem = ArrayList<HashMap<String, Any>>().apply {

            add(
                createItem(
                    getString(R.string.system_modify_wifi),
                    getString(R.string.system_modify_wifi_desc),
                    { DialogSettingWIFI(context).show() },
                    false
                )
            )

            add(
                createItem(
                    getString(R.string.system_modify_dpi),
                    getString(R.string.system_modify_dpi_desc),
                    {
                        DialogSettingModifyDPI(context).modifyDPI(
                            activity.windowManager.defaultDisplay, context
                        )
                    },
                    false
                )
            )
            add(
                createItem(
                    getString(R.string.system_modify_deviceinfo), getString(
                        R.string.system_modify_deviceinfo_desc
                    ),
                    { DialogSettingModifyDevice(context).modifyDeviceInfo() },
                    false
                )
            )
            add(
                createItem(
                    getString(R.string.system_modify_mac),
                    getString(R.string.system_modify_mac_desc),
                    { DialogCustomMAC(context).modifyMAC(SpfConfig.GLOBAL_SPF_MAC_AUTOCHANGE_MODE_1) },
                    false
                )
            )
            add(
                createItem(
                    getString(R.string.system_modify_mac_2),
                    getString(R.string.system_modify_mac_desc_2),
                    { DialogCustomMAC(context).modifyMAC(SpfConfig.GLOBAL_SPF_MAC_AUTOCHANGE_MODE_2) },
                    false
                )
            )
            add(
                createItem(
                    getString(R.string.system_modify_force_dex_compile),
                    getString(R.string.system_modify_force_dex_compile_desc),
                    { DexCompileModify(context).run() },
                    false
                )
            )

        }

        val mSimpleAdapter = SimpleAdapter(
            view.context, listItem,
            R.layout.list_item_sys_modify_menu,
            arrayOf("Title", "Desc"),
            intArrayOf(R.id.Title, R.id.Desc)
        )
        systemModifyListView.adapter = mSimpleAdapter
        systemModifyListView.onItemClickListener = onActionClick
    }

    private var onActionClick = AdapterView.OnItemClickListener { parent, _, position, _ ->
        val item = parent.adapter.getItem(position) as HashMap<*, *>
        if (item["Wran"] == false) {
            (item["Action"] as Runnable).run()
        } else {
            DialogHelper.confirm(this,
                item["Title"].toString(),
                item["Desc"].toString(), {
                    (item["Action"] as Runnable).run()
                })
        }
    }

    override fun onResume() {
        super.onResume()
    }
}
