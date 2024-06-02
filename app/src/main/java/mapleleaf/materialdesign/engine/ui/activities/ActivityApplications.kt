package mapleleaf.materialdesign.engine.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.ui.dialog.DialogDonate
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.ui.fragments.FragmentApplicationSystem
import mapleleaf.materialdesign.engine.ui.fragments.FragmentApplicationUser
import mapleleaf.materialdesign.engine.utils.SearchTextWatcher
import mapleleaf.materialdesign.engine.utils.TabIcon
import mapleleaf.materialdesign.engine.utils.toast

class ActivityApplications : UniversalActivityBase() {

    private var myHandler: Handler = UpdateHandler {
        reloadList()
    }
    private val fragmentApplicationUser = FragmentApplicationUser(myHandler)
    private val fragmentApplicationSystem = FragmentApplicationSystem(myHandler)
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var materialCardView: MaterialCardView
    private lateinit var materialCardViewEdit: MaterialCardView
    private var openActivityCount = 0
    private val maxOpenCount = 20
    private lateinit var buttonClear: AppCompatImageButton

    @LayoutRes
    override fun getLayoutResourceId() = R.layout.activity_applications

    override fun initializeComponents(savedInstanceState: Bundle?) {
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        val appsSearchBox = findViewById<EditText>(R.id.apps_search_box)

        buttonClear = findViewById(R.id.buttonClear)
        materialCardView = findViewById(R.id.materialCardView)
        materialCardViewEdit = findViewById(R.id.materialCardViewEdit)

        CoroutineScope(Dispatchers.Main).launch {
            TabIcon(
                tabLayout,
                viewPager,
                this@ActivityApplications,
                supportFragmentManager,
                R.layout.list_item_tab
            ).run {
                newTabSpec(
                    "用户",
                    AppCompatResources.getDrawable(
                        this@ActivityApplications,
                        R.drawable.ic_tab_user_app
                    )!!,
                    fragmentApplicationUser
                )
                newTabSpec(
                    "系统",
                    AppCompatResources.getDrawable(
                        this@ActivityApplications,
                        R.drawable.ic_tab_system_app
                    )!!,
                    fragmentApplicationSystem
                )
                viewPager.adapter = this.adapter
                viewPager.offscreenPageLimit = 2
            }
        }
        sharedPreferences = getSharedPreferences("activity_open_count", Context.MODE_PRIVATE)
        openActivityCount = sharedPreferences.getInt("open_count", 0)
        openActivityCount++

        appsSearchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchApp(appsSearchBox.text)
            }
            true
        }

        val searchTextWatcher = SearchTextWatcher {
            searchApp(appsSearchBox.text)
        }
        appsSearchBox.addTextChangedListener(searchTextWatcher)

        appsSearchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 不需要在这里做任何操作
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString() ?: ""
                buttonClear.isVisible = searchText.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
                // 不需要在这里做任何操作
            }
        })

        buttonClear.setOnClickListener {
            appsSearchBox.text.clear()
        }
        if (Build.VERSION.SDK_INT >= 30 && !Environment.isExternalStorageManager()) {
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivity(intent)
            } catch (ex: Exception) {
                toast("无法申请存储管理权限~")
            }
        }

        sharedPreferences.edit().putInt("open_count", openActivityCount).apply()

        if (openActivityCount >= maxOpenCount) {
            showDonateSnackbar()
            openActivityCount = 0
            sharedPreferences.edit().putInt("open_count", openActivityCount).apply()
        }

        val baseColor = ContextCompat.getColor(context, R.color.background)
        val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
        materialCardView.setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.15f))
        materialCardViewEdit.setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.15f))
    }

    private fun showDonateSnackbar() {
        val coordinator = findViewById<CoordinatorLayout>(R.id.coordinator)
        val behavior = BaseTransientBottomBar.Behavior().apply {
            setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY)
        }
        Snackbar.make(coordinator, R.string.donate_snackbar_text, Snackbar.LENGTH_SHORT)
            .setBehavior(behavior)
            .setAction(getString(R.string.donate_snackbar_action_text).uppercase()) {
                val dialog = DialogDonate.newInstance()
                dialog.show(supportFragmentManager, DialogDonate.TAG)
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_applist, menu)
        if (menu != null) {
            if (menu.javaClass.simpleName.equals("MenuBuilder", ignoreCase = true)) {
                try {
                    val method = menu.javaClass.getDeclaredMethod(
                        "setOptionalIconsVisible",
                        java.lang.Boolean.TYPE
                    )
                    method.isAccessible = true
                    method.invoke(menu, true)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.menu_instructions -> {
                showInstructionsDialog()
                return true
            }

            R.id.menu_all_app -> {
                val intent = Intent(this, ActivityAllApplications::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("InflateParams")
    private fun showInstructionsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_help_info, null)
        val dialog = DialogHelper.customDialog(this, dialogView)
        dialogView.findViewById<TextView>(R.id.confirm_message).setText(R.string.usage_instructions)
        dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()
        }
    }

    class UpdateHandler(private var updateList: Runnable) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 2) {
                updateList.run()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setToolbarTitle(getString(R.string.toolbar_title_activity_application_manager))
    }

    private fun searchApp(text: Editable) {
        text.toString().run {
            fragmentApplicationUser.searchText = this
            fragmentApplicationSystem.searchText = this
        }
    }

    private fun reloadList() {
        try {
            fragmentApplicationUser.reloadList()
        } catch (ex: Exception) {
            // 记录异常信息到日志
            Log.e("ReloadList", "Error reloading list in fragmentAppUser: ${ex.message}", ex)
        }
        try {
            fragmentApplicationSystem.reloadList()
        } catch (ex: Exception) {
            // 记录异常信息到日志
            Log.e("ReloadList", "Error reloading list in fragmentAppSystem: ${ex.message}", ex)
        }
    }

}