package mapleleaf.materialdesign.engine.ui.activities.dialogs

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.kongzue.baseframework.BaseApp.dip2px
import com.kongzue.baseframework.BaseFrameworkSettings.log
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.dialogs.BottomMenu
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.dialogs.FullScreenDialog
import com.kongzue.dialogx.dialogs.GuideDialog
import com.kongzue.dialogx.dialogs.InputDialog
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.dialogs.MessageMenu
import com.kongzue.dialogx.dialogs.PopMenu
import com.kongzue.dialogx.dialogs.PopNotification
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.PopTip.tip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import com.kongzue.dialogx.interfaces.BaseDialog
import com.kongzue.dialogx.interfaces.BottomDialogSlideEventLifecycleCallback
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.kongzue.dialogx.interfaces.MenuItemTextInfoInterceptor
import com.kongzue.dialogx.interfaces.OnBindView
import com.kongzue.dialogx.interfaces.OnIconChangeCallBack
import com.kongzue.dialogx.interfaces.OnMenuItemSelectListener
import com.kongzue.dialogx.style.IOSStyle
import com.kongzue.dialogx.style.KongzueStyle
import com.kongzue.dialogx.style.MIUIStyle
import com.kongzue.dialogx.style.MaterialStyle
import com.kongzue.dialogx.util.TextInfo
import com.kongzue.dialogxdemo.fragment.CustomFragment
import com.kongzue.dialogxmaterialyou.style.MaterialYouStyle
import com.xuexiang.xui.utils.XToastUtils.toast
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.util.Random

class ActivityKongzueDialog: UniversalActivityBase() {

    private val singleSelectMenuText = arrayOf("拒绝", "询问", "始终允许", "仅在使用中允许")
    private val multiSelectMenuText = arrayOf("上海", "北京", "广州", "深圳")

    private lateinit var handler: Handler
    private lateinit var grpStyle: MaterialButtonToggleGroup
    private lateinit var rdoMaterial: MaterialButton
    private lateinit var rdoIos: MaterialButton
    private lateinit var rdoKongzue: MaterialButton
    private lateinit var rdoMiui: MaterialButton
    private lateinit var rdoMaterialYou: MaterialButton

    private lateinit var grpTheme: MaterialButtonToggleGroup
    private lateinit var rdoAuto: MaterialButton
    private lateinit var rdoLight: MaterialButton
    private lateinit var rdoDark: MaterialButton

    private lateinit var grpMode: MaterialButtonToggleGroup
    private lateinit var rdoModeView: MaterialButton
    private lateinit var rdoModeWindow: MaterialButton
    private lateinit var rdoModeDialogFragment: MaterialButton
    private lateinit var rdoModeFloatingActivity: MaterialButton

    private lateinit var btnMessageDialog: MaterialButton
    private lateinit var btnSelectDialog: MaterialButton
    private lateinit var btnInputDialog: MaterialButton
    private lateinit var btnSelectMessageMenu: MaterialButton
    private lateinit var btnMultiSelectMessageMenu: MaterialButton
    private lateinit var btnWaitDialog: MaterialButton
    private lateinit var btnWaitAndTipDialog: MaterialButton
    private lateinit var btnTipSuccess: MaterialButton
    private lateinit var btnTipWarning: MaterialButton
    private lateinit var btnTipError: MaterialButton
    private lateinit var btnTipProgress: MaterialButton

    private lateinit var btnPopTip: MaterialButton
    private lateinit var btnPopTipBigMessage: MaterialButton
    private lateinit var btnPopTipSuccess: MaterialButton
    private lateinit var btnPopTipWarning: MaterialButton
    private lateinit var btnPopTipError: MaterialButton

    private lateinit var btnPopnotification: MaterialButton
    private lateinit var btnPopnotificationBigMessage: MaterialButton
    private lateinit var btnPopnotificationOverlay: MaterialButton

    private lateinit var btnBottomDialog: MaterialButton
    private lateinit var btnBottomMenu: MaterialButton
    private lateinit var btnBottomReply: MaterialButton
    private lateinit var btnBottomSelectMenu: MaterialButton
    private lateinit var btnBottomMultiSelectMenu: MaterialButton

    private lateinit var btnCustomMessageDialog: MaterialButton
    private lateinit var btnCustomInputDialog: MaterialButton
    private lateinit var btnCustomBottomMenu: MaterialButton
    private lateinit var btnCustomDialog: MaterialButton
    private lateinit var btnCustomDialogAlign: MaterialButton

    private lateinit var btnFullScreenDialogWebPage: MaterialButton
    private lateinit var btnFullScreenDialogLogin: MaterialButton
    private lateinit var btnFullScreenDialogFragment: MaterialButton

    private lateinit var btnContextMenu: MaterialButton

    private lateinit var btnSelectMenu: TextView
    private lateinit var btnShowGuide: MaterialButton
    private lateinit var btnShowGuideBaseView: MaterialButton
    private lateinit var btnShowGuideBaseViewRectangle: MaterialButton

    private lateinit var btnListDialog: MaterialButton

    var waitId = 0
    private lateinit var btnReplyCommit: TextView
    private lateinit var editReplyCommit: EditText
    private lateinit var btnCancel: TextView
    private lateinit var btnSubmit: TextView

    private lateinit var boxUserName: RelativeLayout
    private lateinit var editUserName: EditText
    private lateinit var boxPassword: RelativeLayout
    private lateinit var editPassword: EditText

    private lateinit var btnLicense: TextView
    private lateinit var btnClose: TextView

    private lateinit var webView: WebView

    var selectMenuIndex = 0
    private var selectMenuIndexArray: IntArray? = null
    private lateinit var multiSelectMenuResultCache: String

    override fun getLayoutResourceId() = R.layout.activity_dialog_show

    override fun initializeComponents(savedInstanceState: Bundle?) {

        grpStyle = findViewById(R.id.grp_style)
        rdoMaterial = findViewById(R.id.rdo_material)
        rdoIos = findViewById(R.id.rdo_ios)
        rdoKongzue = findViewById(R.id.rdo_kongzue)
        rdoMiui = findViewById(R.id.rdo_miui)
        rdoMaterialYou = findViewById(R.id.rdo_material_you)
        grpTheme = findViewById(R.id.grp_theme)
        rdoAuto = findViewById(R.id.rdo_auto)
        rdoLight = findViewById(R.id.rdo_light)
        rdoDark = findViewById(R.id.rdo_dark)
        grpMode = findViewById(R.id.grp_mode)
        rdoModeView = findViewById(R.id.rdo_mode_view)
        rdoModeWindow = findViewById(R.id.rdo_mode_window)
        rdoModeDialogFragment = findViewById(R.id.rdo_mode_dialogFragment)
        rdoModeFloatingActivity = findViewById(R.id.rdo_mode_floatingActivity)
        btnMessageDialog = findViewById(R.id.btn_messageDialog)
        btnSelectDialog = findViewById(R.id.btn_selectDialog)
        btnInputDialog = findViewById(R.id.btn_inputDialog)
        btnSelectMessageMenu = findViewById(R.id.btn_select_menu)
        btnMultiSelectMessageMenu = findViewById(R.id.btn_multiSelect_menu)
        btnWaitDialog = findViewById(R.id.btn_waitDialog)
        btnWaitAndTipDialog = findViewById(R.id.btn_waitAndTipDialog)
        btnTipSuccess = findViewById(R.id.btn_tipSuccess)
        btnTipWarning = findViewById(R.id.btn_tipWarning)
        btnTipError = findViewById(R.id.btn_tipError)
        btnTipProgress = findViewById(R.id.btn_tipProgress)
        btnPopTip = findViewById(R.id.btn_poptip)
        btnPopTipBigMessage = findViewById(R.id.btn_poptip_bigMessage)
        btnPopTipSuccess = findViewById(R.id.btn_poptip_success)
        btnPopTipWarning = findViewById(R.id.btn_poptip_warning)
        btnPopTipError = findViewById(R.id.btn_poptip_error)
        btnPopnotification = findViewById(R.id.btn_popnotification)
        btnPopnotificationBigMessage = findViewById(R.id.btn_popnotification_bigMessage)
        btnPopnotificationOverlay = findViewById(R.id.btn_popnotification_overlay)
        btnBottomDialog = findViewById(R.id.btn_bottom_dialog)
        btnBottomMenu = findViewById(R.id.btn_bottom_menu)
        btnBottomReply = findViewById(R.id.btn_bottom_reply)
        btnBottomSelectMenu = findViewById(R.id.btn_bottom_select_menu)
        btnBottomMultiSelectMenu = findViewById(R.id.btn_bottom_multiSelect_menu)
        btnCustomMessageDialog = findViewById(R.id.btn_customMessageDialog)
        btnCustomInputDialog = findViewById(R.id.btn_customInputDialog)
        btnCustomBottomMenu = findViewById(R.id.btn_customBottomMenu)
        btnCustomDialog = findViewById(R.id.btn_customDialog)
        btnCustomDialogAlign = findViewById(R.id.btn_customDialogAlign)
        btnFullScreenDialogWebPage = findViewById(R.id.btn_fullScreenDialog_webPage)
        btnFullScreenDialogLogin = findViewById(R.id.btn_fullScreenDialog_login)
        btnFullScreenDialogFragment = findViewById(R.id.btn_fullScreenDialog_fragment)
        btnContextMenu = findViewById(R.id.btn_contextMenu)
        btnSelectMenu = findViewById(R.id.btn_selectMenu)
        btnShowGuide = findViewById(R.id.btn_showGuide)
        btnShowGuideBaseView = findViewById(R.id.btn_showGuideBaseView)
        btnShowGuideBaseViewRectangle = findViewById(R.id.btn_showGuideBaseViewRectangle)
        btnListDialog = findViewById(R.id.btn_listDialog)

        val nestedScrollView = findViewById<NestedScrollView>(R.id.nestedScrollView)
        FastScrollerBuilder(nestedScrollView).build()

        grpMode.addOnButtonCheckedListener { _, checkedId, _ ->
            BaseDialog.cleanAll()
            when (checkedId) {
                R.id.rdo_mode_view -> DialogX.implIMPLMode = DialogX.IMPL_MODE.VIEW
                R.id.rdo_mode_window -> DialogX.implIMPLMode = DialogX.IMPL_MODE.WINDOW
                R.id.rdo_mode_dialogFragment -> DialogX.implIMPLMode = DialogX.IMPL_MODE.DIALOG_FRAGMENT
                R.id.rdo_mode_floatingActivity -> DialogX.implIMPLMode = DialogX.IMPL_MODE.FLOATING_ACTIVITY
            }
        }

        grpTheme.addOnButtonCheckedListener { group, checkedId, isChecked ->
            when (checkedId) {
                R.id.rdo_auto -> DialogX.globalTheme = DialogX.THEME.AUTO
                R.id.rdo_light -> DialogX.globalTheme = DialogX.THEME.LIGHT
                R.id.rdo_dark -> DialogX.globalTheme = DialogX.THEME.DARK
            }
        }

        grpStyle.addOnButtonCheckedListener { group, checkedId, isChecked ->
            DialogX.cancelButtonText = "取消"
            DialogX.titleTextInfo = null
            DialogX.buttonTextInfo = null
            when (checkedId) {
                R.id.rdo_material -> {
                    DialogX.globalStyle = MaterialStyle.style()
                    DialogX.cancelButtonText = ""
                }
                R.id.rdo_kongzue -> DialogX.globalStyle = KongzueStyle.style()
                R.id.rdo_ios -> DialogX.globalStyle = IOSStyle.style()
                R.id.rdo_miui -> DialogX.globalStyle = MIUIStyle.style()
                R.id.rdo_material_you -> DialogX.globalStyle = MaterialYouStyle.style()
            }
        }

        btnContextMenu.setOnClickListener {
            PopMenu.show("添加", "编辑", "删除", "分享")
                .disableMenu("编辑", "删除")
                .setIconResIds(R.mipmap.img_dialogx_demo_add, R.mipmap.img_dialogx_demo_edit, R.mipmap.img_dialogx_demo_delete, R.mipmap.img_dialogx_demo_share)
                .setOnMenuItemClickListener { dialog, text, index ->
                    if (index == 0) {
                        dialog.setMenuList(arrayOf("产品A", "产品B", "产品C"))
                        true
                    } else {
                        false
                    }
                }
        }

        btnSelectMenu.setOnClickListener { view ->
            PopMenu.show(view, arrayOf("选项1", "选项2", "选项3"))
                .setOnMenuItemClickListener { dialog, text, index ->
                    btnSelectMenu.text = text
                    false
                }
        }

        btnFullScreenDialogFragment.setOnClickListener {
            val customFragment = CustomFragment()
                .setAddButtonClickListener { v12 -> btnFullScreenDialogFragment.callOnClick() }
            FullScreenDialog.build(object : OnBindView<FullScreenDialog>(customFragment) {
                override fun onBind(dialog: FullScreenDialog, v: View) {
                }
            })
                .hideActivityContentView(true)
                .show()
        }

        btnMessageDialog.setOnClickListener {
            MessageDialog.show("标题", "这里是正文内容。", "确定")
                .setTitleIcon(R.mipmap.img_demo_avatar)
                .setOkButton { baseDialog, v ->
                    PopTip.show("点击确定按钮")
                    false
                }
        }

        btnSelectDialog.setOnClickListener {
            val messageDialog = MessageDialog("多选对话框", "移除App会将它从主屏幕移除并保留其所有数据。", "删除App", "取消", "移至App资源库")
                .setButtonOrientation(LinearLayout.VERTICAL)
            if (!rdoMiui.isChecked) {
                messageDialog.setOkTextInfo(TextInfo().setFontColor(Color.parseColor("#EB5545")).setBold(true))
            }
            messageDialog.show()
        }

        btnInputDialog.setOnClickListener {
            InputDialog("标题", "正文内容", "确定", "取消", "正在输入的文字")
                .setInputText("Hello World")
                .setOkButton { _, _, inputStr ->
                    PopTip.show("输入的内容：$inputStr")
                    false
                }
                .show()
        }

        btnSelectMessageMenu.setOnClickListener {
            MessageMenu.show(singleSelectMenuText)
                .setShowSelectedBackgroundTips(rdoMiui.isChecked)
                .setMessage("这里是权限确认的文本说明，这是一个演示菜单")
                .setTitle("获得权限标题")
                .setOnMenuItemClickListener(object : OnMenuItemSelectListener<MessageMenu>() {
                    override fun onOneItemSelect(dialog: MessageMenu, text: CharSequence, index: Int, select: Boolean) {
                        selectMenuIndex = index
                    }
                })
                .setCancelButton("确定") { _, _ ->
                    PopTip.show("已选择：" + singleSelectMenuText[selectMenuIndex])
                    false
                }
                .setSelection(selectMenuIndex)
        }

        btnMultiSelectMessageMenu.setOnClickListener {
            MessageMenu.show(multiSelectMenuText)
                .setMessage("这里是选择城市的模拟范例，这是一个演示菜单")
                .setTitle("请选择城市")
                .setOnMenuItemClickListener(object : OnMenuItemSelectListener<MessageMenu>() {
                    override fun onMultiItemSelect(dialog: MessageMenu, text: Array<out CharSequence>, index: IntArray) {
                        multiSelectMenuResultCache = ""
                        for (c in text) {
                            multiSelectMenuResultCache += " $c"
                        }
                        selectMenuIndexArray = index
                    }
                })
                .setOkButton("确定") { _, _ ->
                    PopTip.show("已选择：$multiSelectMenuResultCache")
                    false
                }
                .setSelection(selectMenuIndexArray)
        }

        btnWaitDialog.setOnClickListener {
            WaitDialog.show("Please Wait!")
                .setOnBackPressedListener {
                    PopTip.show("按下返回")
                    false
                }
            handler.postDelayed({ WaitDialog.dismiss() }, 1500)
        }

        var closeFlag: Boolean

        btnWaitAndTipDialog.setOnClickListener {
            closeFlag = false
            WaitDialog.show("Please Wait!").setOnBackPressedListener {
                PopTip.show("按下返回", "关闭").setButton { _, _ ->
                    closeFlag = true
                    WaitDialog.dismiss()
                    false
                }
                false
            }
            if (!closeFlag) {
                handler.postDelayed({
                    if (!closeFlag) {
                        TipDialog.show("完成！", WaitDialog.TYPE.SUCCESS)
                    }
                }, 1500 + Random().nextInt(1000).toLong())
            }
        }

        btnTipSuccess.setOnClickListener {
            TipDialog.show("Success!", WaitDialog.TYPE.SUCCESS)
        }

        btnTipWarning.setOnClickListener {
            TipDialog.show("Warning!", WaitDialog.TYPE.WARNING)
        }

        btnTipError.setOnClickListener {
            TipDialog.show("Error!", WaitDialog.TYPE.ERROR)
        }

        btnTipProgress.setOnClickListener {
            waitId = 0
            WaitDialog.show("假装连接...").setOnBackPressedListener { dialog ->
                MessageDialog.show("正在进行", "是否取消？", "是", "否").setOkButton { baseDialog, v1 ->
                    waitId = -1
                    WaitDialog.dismiss()
                    false
                }
                false
            }
            handler.postDelayed({
                if (waitId != 0) {
                    return@postDelayed
                }
                val cycleRunner = object : Runnable {
                    var progress = 0f
                    override fun run() {
                        if (waitId != 0) {
                            handler.removeCallbacks(this)
                            return
                        }
                        progress += 0.1f
                        if (progress < 1f) {
                            WaitDialog.show("假装加载" + ((progress * 100).toInt()) + "%", progress)
                            handler.postDelayed(this, 1000)
                        } else {
                            TipDialog.show("加载完成", WaitDialog.TYPE.SUCCESS)
                            handler.removeCallbacks(this)
                        }
                    }
                }
                handler.post(cycleRunner)
            }, 3000)
        }

        btnBottomDialog.setOnClickListener {
            val s = if (rdoMaterial.isChecked) "你可以向下滑动来关闭这个对话框" else "你可以点击空白区域或返回键来关闭这个对话框"
            BottomDialog("标题", "这里是对话框内容。\n$s。\n底部对话框也支持自定义布局扩展使用方式。",
                object : OnBindView<BottomDialog>(R.layout.layout_custom_view) {
                    override fun onBind(dialog: BottomDialog, v: View) {
                        if (dialog.dialogImpl.imgTab != null) {
                            (dialog.dialogImpl.imgTab.parent as? ViewGroup)?.removeView(dialog.dialogImpl.imgTab)
                        }
                        v.setOnClickListener {
                            dialog.dismiss()
                            PopTip.show("Click Custom View")
                        }
                    }
                })
                .setDialogLifecycleCallback(object : DialogLifecycleCallback<BottomDialog>() {
                    override fun onShow(dialog: BottomDialog) {
                        log("#onShow")
                    }

                    override fun onDismiss(dialog: BottomDialog) {
                        log("#onDismiss")
                    }
                })
                .show()
        }

        btnBottomMenu.setOnClickListener {
            if (rdoMaterial.isChecked) {
                // Material 可滑动展开 BottomMenu 演示
                BottomMenu.build()
                    .setBottomDialogMaxHeight(0.6f)
                    .setMenuList(arrayOf("添加", "查看", "编辑", "删除", "分享", "评论", "下载", "收藏", "赞！", "不喜欢", "所属专辑", "复制链接", "类似推荐", "添加", "查看", "编辑", "删除", "分享", "评论", "下载", "收藏", "赞！", "不喜欢", "所属专辑", "复制链接", "类似推荐"))
                    .setOnIconChangeCallBack(object : OnIconChangeCallBack<BottomMenu>() {
                        override fun getIcon(bottomMenu: BottomMenu, index: Int, menuText: String): Int {
                            return when (menuText) {
                                "添加" -> R.mipmap.img_dialogx_demo_add
                                "查看" -> R.mipmap.img_dialogx_demo_view
                                "编辑" -> R.mipmap.img_dialogx_demo_edit
                                "删除" -> R.mipmap.img_dialogx_demo_delete
                                "分享" -> R.mipmap.img_dialogx_demo_share
                                "评论" -> R.mipmap.img_dialogx_demo_comment
                                "下载" -> R.mipmap.img_dialogx_demo_download
                                "收藏" -> R.mipmap.img_dialogx_demo_favorite
                                "赞！" -> R.mipmap.img_dialogx_demo_good
                                "不喜欢" -> R.mipmap.img_dialogx_demo_dislike
                                "所属专辑" -> R.mipmap.img_dialogx_demo_album
                                "复制链接" -> R.mipmap.img_dialogx_demo_link
                                "类似推荐" -> R.mipmap.img_dialogx_demo_recommend
                                else -> 0
                            }
                        }
                    })
                    .setOnMenuItemClickListener { dialog, text, index ->
                        PopTip.show(text)
                        false
                    }
                    .show()
            } else {
                BottomMenu.show("新标签页中打开", "稍后阅读", "复制链接网址")
                    .setMessage("http://www.kongzue.com/DialogX")
                    .setMenuItemTextInfoInterceptor(object : MenuItemTextInfoInterceptor<BottomMenu>() {
                        override fun menuItemTextInfo(dialog: BottomMenu, index: Int, menuText: String): TextInfo? {
                            return if (index == 2) {
                                TextInfo().setFontColor(Color.RED).setBold(true)
                            } else null
                        }
                    })
                    .setOnMenuItemClickListener { _, text, _ ->
                        PopTip.show(text)
                        false
                    }
            }
        }

        btnBottomReply.setOnClickListener {
            BottomDialog.show(object : OnBindView<BottomDialog>(if (rdoDark.isChecked) R.layout.layout_custom_reply_dark else R.layout.layout_custom_reply) {
                override fun onBind(dialog: BottomDialog, v: View) {
                    btnReplyCommit = v.findViewById(R.id.btn_reply_commit)
                    editReplyCommit = v.findViewById(R.id.edit_reply_commit)
                    btnReplyCommit.setOnClickListener {
                        dialog.dismiss()
                        PopTip.show("提交内容：\n${editReplyCommit.text}")
                    }
                    editReplyCommit.postDelayed({ showIME(editReplyCommit) }, 300)
                }
            })
                .setAllowInterceptTouch(false)
        }

        btnCustomMessageDialog.setOnClickListener {
            MessageDialog.show("这里是标题", "此对话框演示的是自定义对话框内部布局的效果", "确定", "取消")
                .setDialogLifecycleCallback(object : BottomDialogSlideEventLifecycleCallback<MessageDialog>() {
                    override fun onShow(dialog: MessageDialog) {
                        super.onShow(dialog)
                        dialog.dialogImpl.txtDialogTip.setPadding(0, dip2px(20f), 0, 0)
                    }
                })
                .setCustomView(object : OnBindView<MessageDialog>(R.layout.layout_custom_view) {
                    override fun onBind(dialog: MessageDialog, v: View) {
                        // Custom view binding logic here
                    }
                })
        }

        btnCustomInputDialog.setOnClickListener {
            InputDialog.show("这里是标题", "此对话框演示的是自定义对话框内部布局的效果", "确定", "取消")
                .setCustomView(object : OnBindView<MessageDialog>(R.layout.layout_custom_view) {
                    override fun onBind(dialog: MessageDialog, v: View) {
                        // Custom view binding logic here
                    }
                })
        }

        btnCustomBottomMenu.setOnClickListener {
            BottomMenu.show("新标签页中打开", "稍后阅读", "复制链接网址")
                .setMessage("http://www.kongzue.com/DialogX")
                .setOnMenuItemClickListener { dialog, text, index ->
                    PopTip.show(text)
                    false
                }
                //.setDialogXAnimImpl(alphaDialogAnimation)
                .setCustomView(object : OnBindView<BottomDialog>(R.layout.layout_custom_view) {
                    override fun onBind(dialog: BottomDialog, v: View) {
                        // Custom view binding logic here
                    }
                })
        }

        btnShowGuide.setOnClickListener {
            GuideDialog.show(R.mipmap.img_guide_tip)
        }

        btnShowGuideBaseView.setOnClickListener {
            GuideDialog.show(btnFullScreenDialogLogin, R.mipmap.img_tip_login)
                .setBaseViewMarginTop(-dip2px(30f))
        }

        btnShowGuideBaseViewRectangle.setOnClickListener {
            GuideDialog.show(btnCustomDialogAlign,
                GuideDialog.STAGE_LIGHT_TYPE.RECTANGLE,
                R.mipmap.img_tip_login_clicktest)
                .setStageLightFilletRadius(dip2px(5f).toFloat())
                .setBaseViewMarginTop(-dip2px(30f))
                .setOnBackgroundMaskClickListener { dialog, v17 ->
                    toast("点击了外围遮罩")
                    false
                }
                .setOnStageLightPathClickListener { dialog, v18 ->
                    toast("点击了原按钮")
                    btnCustomDialogAlign.callOnClick()
                    false
                }
        }

        btnListDialog.setOnClickListener {
            DialogX.showDialogList(
                MessageDialog.build()
                    .setTitle("提示")
                    .setMessage("这是一组消息对话框队列")
                    .setOkButton("开始")
                    .setCancelButton("取消")
                    .setCancelButton { dialog, v19 ->
                        dialog.cleanDialogList()
                        false
                    },
                PopTip.build().setMessage("每个对话框会依次显示"),
                PopNotification.build().setTitle("通知提示").setMessage("直到上一个对话框消失"),
                InputDialog.build().setTitle("请注意").setMessage("你必须使用 .build() 方法构建，并保证不要自己执行 .show() 方法").setInputText("输入文字").setOkButton("知道了"),
                TipDialog.build().setMessageContent("准备结束...").setTipType(WaitDialog.TYPE.SUCCESS),
                BottomDialog.build()
                    .setTitle("结束")
                    .setMessage("下滑以结束旅程，祝你编码愉快！")
                    .setCustomView(object : OnBindView<BottomDialog>(R.layout.layout_custom_dialog) {
                        override fun onBind(dialog: BottomDialog, v: View) {
                            val btnOk: ImageView = v.findViewById(R.id.btn_ok)
                            btnOk.setOnClickListener { v110 -> dialog.dismiss() }
                        }
                    })
            )
        }

        btnFullScreenDialogLogin.setOnClickListener {
            object : View.OnClickListener {
                // 采用异步加载布局防止卡顿测试
                var onBindView: OnBindView<FullScreenDialog>? = null
                override fun onClick(v: View) {
                    onBindView = object : OnBindView<FullScreenDialog>(R.layout.layout_full_login, true) {
                        override fun onBind(dialog: FullScreenDialog, v: View) {
                            btnCancel = v.findViewById(R.id.btn_cancel)
                            btnSubmit = v.findViewById(R.id.btn_submit)
                            boxUserName = v.findViewById(R.id.box_userName)
                            editUserName = v.findViewById(R.id.edit_userName)
                            boxPassword = v.findViewById(R.id.box_password)
                            editPassword = v.findViewById(R.id.edit_password)
                            btnLicense = v.findViewById(R.id.btn_license)

//                            initFullScreenLoginDemo(dialog)
                        }
                    }
                    FullScreenDialog.show(onBindView)
                }
            }.onClick(btnFullScreenDialogLogin) // 触发点击事件
        }

        btnFullScreenDialogWebPage.setOnClickListener {
            FullScreenDialog.show(object : OnBindView<FullScreenDialog>(R.layout.layout_full_webview) {
                @SuppressLint("SetJavaScriptEnabled")
                override fun onBind(dialog: FullScreenDialog, v: View) {
                    btnClose = v.findViewById(R.id.btn_close)
                    webView = v.findViewById(R.id.webView)

                    btnClose.setOnClickListener { v111 -> dialog.dismiss() }

                    val webSettings: WebSettings = webView.settings
                    webSettings.javaScriptEnabled = true
                    webSettings.loadWithOverviewMode = true
                    webSettings.useWideViewPort = true
                    webSettings.setSupportZoom(false)
                    webSettings.allowFileAccess = true
                    webSettings.javaScriptCanOpenWindowsAutomatically = true
                    webSettings.loadsImagesAutomatically = true
                    webSettings.defaultTextEncodingName = "utf-8"

                    webView.webViewClient = object : WebViewClient() {
                        @Deprecated("Deprecated in Java")
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Log.d("webView", "shouldOverrideUrlLoading: $e")
                            }
                            return true
                        }

                        override fun onPageFinished(view: WebView, url: String) {
                            super.onPageFinished(view, url)
                        }
                    }

                    webView.loadUrl("https://github.com/kongzue/DialogX")
                }
            }).setBottomNonSafetyAreaBySelf(false)
        }

        btnCustomDialog.setOnClickListener {
            CustomDialog.show(object : OnBindView<CustomDialog>(R.layout.layout_custom_dialog) {
                override fun onBind(dialog: CustomDialog, v: View) {
                    val btnOk: ImageView = v.findViewById(R.id.btn_ok)
                    btnOk.setOnClickListener { dialog.dismiss() }
                }
            })
                //.setAlign(CustomDialog.ALIGN.LEFT)
                //.setAnimResId(R.anim.anim_right_in, R.anim.anim_right_out)
                .setMaskColor(ContextCompat.getColor(context, com.kongzue.dialogx.R.color.black30))
        }

        btnCustomDialogAlign.setOnClickListener {
            CustomDialog.show(object : OnBindView<CustomDialog>(R.layout.layout_custom_dialog_align) {
                override fun onBind(dialog: CustomDialog, v: View) {
                    val btnSelectPositive = v.findViewById<TextView>(R.id.btn_selectPositive)
                    btnSelectPositive.setOnClickListener {
                        PopTip.show("我知道了")
                        dialog.dismiss()
                    }
                }
            })
                .setCancelable(false)
                .setMaskColor(ContextCompat.getColor(context, com.kongzue.dialogx.R.color.black30))
                .setEnterAnimResId(R.anim.anim_custom_pop_enter)
                .setExitAnimResId(R.anim.anim_custom_pop_exit)
                .setAlignBaseViewGravity(btnCustomDialogAlign, Gravity.TOP or Gravity.CENTER_HORIZONTAL)
                .setBaseViewMarginBottom(-dip2px(45f))
                .show()
        }

        btnPopTip.setOnClickListener { PopTip.show("这是一个提示") }

        btnPopTipBigMessage.setOnClickListener {
            if (rdoIos.isChecked) {
                PopTip.show(R.mipmap.img_air_pods_pro, "AirPods Pro 已连接")
                    .setTintIcon(false)
                    .showLong()
            } else {
                PopTip.show(R.mipmap.img_mail_line_white, "邮件已发送", "撤回")
                    .setButton { popTip, v114 ->
                        toast("邮件已撤回")
                        false
                    }
                    .setTintIcon(true)
                    .showLong()
            }
        }

        btnPopTipSuccess.setOnClickListener { PopTip.show("操作已完成").iconSuccess() }

        btnPopTipWarning.setOnClickListener {
            PopTip.show("存储空间不足")
                .setButton("立即清理") { baseDialog, v115 ->
                    toast("点击了立即清理")
                    false
                }
                .iconWarning()
        }

        btnPopTipError.setOnClickListener { PopTip.show("无法连接网络").iconError() }
        btnPopnotification.setOnClickListener {
            notificationIndex++
            PopNotification.build()
                .setMessage("这是一条消息 $notificationIndex")
                .setOnPopNotificationClickListener { dialog, v118 ->
                    tip("点击了通知" + dialog.dialogKey())
                    true
                }
                .show()
        }
        btnPopnotificationOverlay.setOnClickListener {
            DialogX.globalHoverWindow = true
            // 悬浮窗权限检查
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    toast("使用 DialogX.globalHoverWindow 必须开启悬浮窗权限")
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    startActivity(intent)
                    return@setOnClickListener
                }
            }

            val icon = BitmapFactory.decodeResource(resources, R.mipmap.img_demo_avatar)
            notificationIndex++
            toast("会在1秒后显示悬浮窗！")

            // 跳转到桌面
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

            // 等待一秒后显示
            Handler(Looper.getMainLooper()).postDelayed({
                PopNotification.build()
                    .setDialogImplMode(DialogX.IMPL_MODE.WINDOW)
                    .setTitle("这是一条消息 $notificationIndex")
                    .setIcon(icon)
                    .setButton("回复") { baseDialog, v120 ->
                        val intent1 = Intent(context, ActivityKongzueDialog::class.java)
                        intent1.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent1)
                        false
                    }
                    .showLong()
            }, 1000)
        }

        btnPopnotificationBigMessage.setOnClickListener {
            val icon = BitmapFactory.decodeResource(resources, R.mipmap.img_demo_avatar)
            notificationIndex++
            PopNotification.show("这是一条消息 $notificationIndex", "吃了没？\uD83E\uDD6A")
                .setIcon(icon)
                .setButton("回复") { baseDialog, v119 ->
                    toast("点击回复按钮")
                    false
                }
                .showLong()
        }

        btnBottomSelectMenu.setOnClickListener {
            BottomMenu.show(singleSelectMenuText)
                .setShowSelectedBackgroundTips(rdoMiui.isChecked())
                .setMessage("这里是权限确认的文本说明，这是一个演示菜单")
                .setTitle("获得权限标题")
                .setOnMenuItemClickListener(object : OnMenuItemSelectListener<BottomMenu>() {
                    override fun onOneItemSelect(dialog: BottomMenu, text: CharSequence, index: Int, select: Boolean) {
                        selectMenuIndex = index
                    }
                })
                .setCancelButton("确定") { _, _ ->
                    PopTip.show("已选择：${singleSelectMenuText[selectMenuIndex]}")
                    false
                }
                .setSelection(selectMenuIndex)
        }

        btnBottomMultiSelectMenu.setOnClickListener {
            BottomMenu.show(multiSelectMenuText)
                .setMessage("这里是选择城市的模拟范例，这是一个演示菜单")
                .setTitle("请选择城市")
                .setOnMenuItemClickListener(object : OnMenuItemSelectListener<BottomMenu>() {
                    override fun onMultiItemSelect(dialog: BottomMenu, text: Array<out CharSequence>, index: IntArray) {
                        multiSelectMenuResultCache = ""
                        for (c in text) {
                            multiSelectMenuResultCache += " $c"
                        }
                        selectMenuIndexArray = index
                    }
                })
                .setOkButton("确定") { _, _ ->
                    PopTip.show("已选择：$multiSelectMenuResultCache")
                    false
                }
                .setSelection(selectMenuIndexArray)
        }
        handler = Handler(Looper.getMainLooper())
        setToolbarTitle(getString(R.string.toolbar_title_activity_kongzue_dialog))
    }
    private var notificationIndex = 0
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        log("#MainActivity.onBackPressed")
        super.onBackPressed()
    }

    fun showIME(editText: EditText?) {
        editText ?: return
        editText.requestFocus()
        editText.isFocusableInTouchMode = true
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.RESULT_UNCHANGED_SHOWN)
    }

}