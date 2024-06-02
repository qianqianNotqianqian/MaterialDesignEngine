package mapleleaf.materialdesign.engine.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.UiModeManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.utils.FastBlurUtility

class DialogHelper {
    class DialogButton(
        val text: String,
        val onClick: Runnable? = null,
        val dismiss: Boolean = true,
    )

    class DialogWrap(private val d: AlertDialog) {
        val context = dialog.context
        private var mCancelable = true
        val isCancelable: Boolean
            get() {
                return mCancelable
            }

        fun setCustomCancelable(cancelable: Boolean): DialogWrap {
            mCancelable = cancelable
            d.setCancelable(cancelable)

            return this
        }

        fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener): DialogWrap {
            d.setOnDismissListener(onDismissListener)

            return this
        }

        val dialog: AlertDialog
            get() {
                return d
            }

        fun dismiss() {
            try {
                d.dismiss()
            } catch (ex: Exception) {
            }
        }

        fun hide() {
            try {
                d.hide()
            } catch (ex: Exception) {
            }
        }

        val isShowing: Boolean
            get() {
                return d.isShowing
            }
    }

    companion object {
        // 是否禁用模糊背景
        private var disableBlurBg = false

        private fun animDialog(dialog: AlertDialog?): DialogWrap? {
            if (dialog != null && !dialog.isShowing) {
                dialog.window?.run {
                    setWindowAnimations(R.style.windowAnim)
                }
                dialog.show()
            }
            return if (dialog != null) DialogWrap(dialog) else null
        }

        fun animDialog(builder: AlertDialog.Builder): DialogWrap {
            val dialog = builder.create()
            animDialog(dialog)
            return DialogWrap(dialog)
        }

        fun helpInfo(context: Context, message: String, onDismiss: Runnable? = null): DialogWrap {
            return helpInfo(context, context.getString(R.string.help_title), message, onDismiss)
        }

        @SuppressLint("InflateParams")
        fun helpInfo(
            context: Context,
            title: String,
            message: String,
            onDismiss: Runnable? = null,
        ): DialogWrap {
            val layoutInflater = LayoutInflater.from(context)
            val dialogView = layoutInflater.inflate(R.layout.dialog_help_info, null)

            (dialogView.findViewById<TextView>(R.id.confirm_title)!!).run {
                if (title.isNotEmpty()) {
                    text = title
                    visibility = View.VISIBLE
                } else {
                    visibility = View.GONE
                }
            }

            (dialogView.findViewById<TextView>(R.id.confirm_message)!!).run {
                if (message.isNotEmpty()) {
                    text = message
                    visibility = View.VISIBLE
                } else {
                    visibility = View.GONE
                }
            }

            val d = customDialog(context, dialogView, onDismiss == null)
            d.setCustomCancelable(false)
            (dialogView.findViewById<View>(R.id.btn_confirm)!!).run {
                if (onDismiss != null) {
                    d.setOnDismissListener {
                        onDismiss.run()
                    }
                }
                setOnClickListener {
                    d.dismiss()
                }
            }

            return d
        }

        fun helpInfo(
            context: Context,
            title: String = "",
            message: String = "",
            contentView: View,
            onConfirm: Runnable? = null,
        ): DialogWrap {
            val dialogView =
                getCustomDialogView(context, R.layout.dialog_help_info, title, message, contentView)

            val dialog = customDialog(context, dialogView)
            dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
                dialog.dismiss()
                onConfirm?.run()
            }

            return dialog
        }

        fun confirm(
            context: Context,
            title: String = "",
            message: String = "",
            onConfirm: Runnable? = null,
            onCancel: Runnable? = null,
        ): DialogWrap {
            return openContinueAlert(
                context,
                R.layout.dialog_confirm,
                title,
                message,
                onConfirm,
                onCancel
            )
        }

        fun warning(
            context: Context,
            title: String = "",
            message: String = "",
            onConfirm: Runnable? = null,
            onCancel: Runnable? = null,
        ): DialogWrap {
            return openContinueAlert(
                context,
                R.layout.dialog_warning,
                title,
                message,
                onConfirm,
                onCancel
            )
        }

        private fun getCustomDialogView(
            context: Context,
            layout: Int,
            title: String = "",
            message: String = "",
            contentView: View? = null,
        ): View {

            val view = LayoutInflater.from(context).inflate(layout, null)
            view.findViewById<TextView?>(R.id.confirm_title)?.run {
                if (title.isEmpty()) {
                    visibility = View.GONE
                } else {
                    text = title
                }
            }

            view.findViewById<TextView?>(R.id.confirm_message)?.run {
                if (message.isEmpty()) {
                    visibility = View.GONE
                } else {
                    text = message
                }
            }

            if (contentView != null) {
                view.findViewById<FrameLayout?>(R.id.confirm_custom_view)?.addView(contentView)
            }

            return view
        }

        fun confirm(
            context: Context,
            title: String = "",
            message: String = "",
            contentView: View? = null,
            onConfirm: Runnable? = null,
            onCancel: Runnable? = null,
        ): DialogWrap {
            val dialogView =
                getCustomDialogView(context, R.layout.dialog_confirm, title, message, contentView)

            val dialog = customDialog(context, dialogView)
            dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
                onCancel?.run()
            }
            dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
                dialog.dismiss()
                onConfirm?.run()
            }

            return dialog
        }

        fun confirm(
            context: Context,
            title: String = "",
            message: String = "",
            onConfirm: DialogButton? = null,
            onCancel: DialogButton? = null,
        ): DialogWrap {
            return confirm(context, title, message, null, onConfirm, onCancel)
        }

        fun confirm(
            context: Context,
            title: String = "",
            message: String = "",
            contentView: View? = null,
            onConfirm: DialogButton? = null,
            onCancel: DialogButton? = null,
        ): DialogWrap {
            val dialogView =
                getCustomDialogView(context, R.layout.dialog_confirm, title, message, contentView)

            val dialog = customDialog(context, dialogView)

            val btnConfirm = dialogView.findViewById<TextView?>(R.id.btn_confirm)
            if (onConfirm != null) {
                btnConfirm?.text = onConfirm.text
            }
            btnConfirm?.setOnClickListener {
                if (onConfirm != null) {
                    if (onConfirm.dismiss) {
                        dialog.dismiss()
                    }
                    onConfirm.onClick?.run()
                } else {
                    dialog.dismiss()
                }
            }


            val btnCancel = dialogView.findViewById<TextView?>(R.id.btn_cancel)
            if (onCancel != null) {
                btnCancel?.text = onCancel.text
            }
            btnCancel.setOnClickListener {
                if (onCancel != null) {
                    if (onCancel.dismiss) {
                        dialog.dismiss()
                    }
                    onCancel.onClick?.run()
                } else {
                    dialog.dismiss()
                }
            }

            return dialog
        }

        fun confirm(
            context: Context,
            contentView: View? = null,
            onConfirm: DialogButton? = null,
            onCancel: DialogButton? = null,
        ): DialogWrap {
            return confirm(context, "", "", contentView, onConfirm, onCancel)
        }

        private fun getWindowBackground(
            context: Context,
            defaultColor: Int = Color.TRANSPARENT,
        ): Int {
            // val attrsArray = intArrayOf(android.R.attr.windowBackground)
            val attrsArray = intArrayOf(android.R.attr.background)
            val typedArray = context.obtainStyledAttributes(attrsArray)
            val color = typedArray.getColor(0, defaultColor)
            typedArray.recycle()
            return color
        }

        // 设置点击空白区域关闭弹窗
        private fun setOutsideTouchDismiss(view: View, dialogWrap: DialogWrap): DialogWrap {
            val dialog = dialogWrap.dialog
            val rootView = dialog.window?.decorView
            rootView?.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (event != null && event.action == MotionEvent.ACTION_UP) {
                        val x = event.x.toInt()
                        val y = event.y.toInt()
                        val rect = Rect()
                        view.getGlobalVisibleRect(rect)
                        if (!rect.contains(x, y)) {
                            // TODO: 从何获取呢...
                            val mCancelable = dialogWrap.isCancelable // false
                            if (mCancelable) {
                                dialogWrap.dismiss()
                            }
                        }
                        return true
                    }
                    return false
                }
            })

            return dialogWrap
        }

        private fun getStatusBarColor(context: Context): Int {
            val defaultColor = Color.TRANSPARENT
            val attrsArray = intArrayOf(android.R.attr.statusBarColor)
            val typedArray = context.obtainStyledAttributes(attrsArray)
            val color = typedArray.getColor(0, defaultColor)
            typedArray.recycle()
            return color
        }

        private fun openContinueAlert(
            context: Context,
            layout: Int,
            title: String = "",
            message: String = "",
            onConfirm: Runnable? = null,
            onCancel: Runnable? = null,
        ): DialogWrap {
            val dialogView = getCustomDialogView(context, layout, title, message, null)

            val dialog = customDialog(context, dialogView)
            dialogView.findViewById<View?>(R.id.btn_cancel)?.setOnClickListener {
                dialog.dismiss()
                onCancel?.run()
            }
            dialogView.findViewById<View?>(R.id.btn_confirm)?.setOnClickListener {
                dialog.dismiss()
                onConfirm?.run()
            }

            return dialog
        }

        fun confirmBlur(
            context: Activity,
            title: String = "",
            message: String = "",
            onConfirm: Runnable? = null,
            onCancel: Runnable? = null,
        ): DialogWrap {
            return openContinueAlert(
                context,
                R.layout.dialog_confirm,
                title,
                message,
                onConfirm,
                onCancel
            )
        }

        fun alert(
            context: Context,
            title: String = "",
            message: String = "",
            onConfirm: Runnable? = null,
        ): DialogWrap {
            return openContinueAlert(
                context,
                R.layout.dialog_alert,
                title,
                message,
                onConfirm,
                null
            )
        }

        fun alert(
            context: Context,
            title: String = "",
            message: String = "",
            contentView: View,
            onConfirm: Runnable? = null,
        ): DialogWrap {
            val dialogView =
                getCustomDialogView(context, R.layout.dialog_alert, title, message, contentView)

            val dialog = customDialog(context, dialogView)
            dialogView.findViewById<View>(R.id.btn_confirm).setOnClickListener {
                dialog.dismiss()
                onConfirm?.run()
            }

            return dialog
        }

        fun customDialog(context: Context, view: View, cancelable: Boolean = true): DialogWrap {
            val useBlur = (
                    context is Activity &&
                            context.window.attributes.flags and WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER == 0
                    )

            val dialog = (if (useBlur) {
                AlertDialog.Builder(context, R.style.custom_alert_dialog)
            } else {
                AlertDialog.Builder(context)
            }).setView(view).setCancelable(cancelable).create()

            if (context is Activity) {
                dialog.show()
                dialog.window?.run {
                    setWindowBlurBg(this, context)
                    decorView.run {
                        systemUiVisibility =
                            context.window.decorView.systemUiVisibility // View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                }
            } else {
                dialog.window?.run {
                    setWindowAnimations(R.style.windowAnim2)
                }
                dialog.show()
                dialog.window?.run {
                    setBackgroundDrawableResource(android.R.color.transparent)
                }
            }

            return setOutsideTouchDismiss(view, DialogWrap(dialog).setCustomCancelable(cancelable))
        }

        private fun isNightMode(context: Context): Boolean {
            val nightMode = AppCompatDelegate.getDefaultNightMode()
            return when (nightMode) {
                AppCompatDelegate.MODE_NIGHT_YES -> {
                    true
                }

                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> {
                    val uiModeManager =
                        context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
                    uiModeManager.nightMode == UiModeManager.MODE_NIGHT_YES
                }

                else -> {
                    false
                }
            }
        }

        fun setWindowBlurBg(window: Window, activity: Activity) {
            // 是否使用了动态壁纸
            val wallpaperMode =
                activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER != 0

            window.run {
                // TODO:处理模糊背景
                // BlurBackground(activity).setScreenBgLight(dialog)

                // val attrs = attributes
                // attrs.alpha = 0.1f
                // attributes =attrs
                // decorView.setPadding(0, 0, 0, 0)

                val blurBitmap = if (disableBlurBg || wallpaperMode) {
                    null
                } else {
                    FastBlurUtility.getBlurBackgroundDrawer(activity)
                }

                // window.setDimAmount(0f)
                if (blurBitmap != null) {
                    setBackgroundDrawable(BitmapDrawable(activity.resources, blurBitmap))
                } else {
                    // setBackgroundDrawableResource(android.R.color.transparent)
                    try {
                        val bg = getWindowBackground(activity)
                        if (bg == Color.TRANSPARENT) {

                            if (isFloating) {
                                val d = ColorDrawable(bg)
                                setBackgroundDrawable(d)
                                setDimAmount(0.9f)
                                return
                            } else {
                                if (wallpaperMode || isNightMode(context)) {
                                    val d = ColorDrawable(Color.argb(255, 18, 18, 18))
                                    setBackgroundDrawable(d)
                                } else {
                                    val d = ColorDrawable(Color.argb(255, 245, 245, 245))
                                    setBackgroundDrawable(d)
                                }
                            }

                        } else {
                            val d = ColorDrawable(bg)
                            setBackgroundDrawable(d)
                        }
                    } catch (ex: java.lang.Exception) {
                        val d = ColorDrawable(Color.argb(255, 245, 245, 245))
                        setBackgroundDrawable(d)
                    }
                }
            }
        }
    }
}
