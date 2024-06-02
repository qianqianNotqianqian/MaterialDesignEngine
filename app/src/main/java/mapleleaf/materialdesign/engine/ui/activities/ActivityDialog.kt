package mapleleaf.materialdesign.engine.ui.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.google.android.material.card.MaterialCardView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogx.dialogs.MessageDialog
import com.kongzue.dialogx.util.TextInfo
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.utils.toast
import me.zhanghai.android.fastscroll.FastScrollNestedScrollView
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ActivityDialog : UniversalActivityBase() {
    private val items = arrayOf("Item - 1", "Item - 2", "Item - 3")
    private var singleSelectResult: String = items[0]

    override fun getLayoutResourceId() = R.layout.activity_dialog_show

    override fun initializeComponents(savedInstanceState: Bundle?) {
        setToolbarTitle(getString(R.string.toolbar_title_activity_md_dialog))
        val scrollingView = findViewById<FastScrollNestedScrollView>(R.id.nestedScrollView2)
        FastScrollerBuilder(scrollingView).build()
        DialogX.init(this)
        val materialCardView = findViewById<MaterialCardView>(R.id.materialCardView)
        val materialCardView2 = findViewById<MaterialCardView>(R.id.materialCardView2)
        val baseColor =
            ContextCompat.getColor(MaterialDesignEngine.context, R.color.background_color)
        val primaryColor =
            ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
        val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.15f)
        materialCardView.setCardBackgroundColor(blendedColor)
        materialCardView2.setCardBackgroundColor(blendedColor)
    }

    fun messageDialog(view: View) {

        val primaryColor = ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
        val baseColor = ContextCompat.getColor(MaterialDesignEngine.context, R.color.text_color)
        MessageDialog.show("提示对话框", "这是一个提示对话框！", "明明就是", "不像", "其他")
            .setBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.8f))
            .setOkButton { _, _ ->
                toast("明明就是")
                false
            }
            .setCancelButton { _, _ ->
                toast("真不像？")
                false
            }
            .setOkTextInfo(
                TextInfo().setFontColor(Color.parseColor("#00FF26")).setBold(false)
            )
            .setOtherTextInfo(
                TextInfo().setFontColor(Color.parseColor("#00FF26")).setBold(false)
            )
            .setCancelTextInfo(
                TextInfo().setFontColor(Color.parseColor("#EB5545")).setBold(true)
            )
            .setButtonOrientation(LinearLayout.HORIZONTAL)
    }

    fun threeButtonDialog(view: View) {
        val primaryColor = ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
        val baseColor = ContextCompat.getColor(MaterialDesignEngine.context, R.color.text_color)
        MessageDialog.show("提示对话框", "这是一个提示对话框！", "右", null, "左")
            .setBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.8f))
            .setOkButton { _, _ ->
                toast("右？")
                false
            }
            .setOtherButton { _, _ ->
                toast("左？")
                false
            }
            .setOkTextInfo(
                TextInfo().setFontColor(Color.parseColor("#00FF26")).setBold(false)
            )
            .setOtherTextInfo(
                TextInfo().setFontColor(Color.parseColor("#00FF26")).setBold(false)
            )
            .setCancelTextInfo(
                TextInfo().setFontColor(Color.parseColor("#EB5545")).setBold(true)
            )
            .setButtonOrientation(LinearLayout.HORIZONTAL)
    }

    fun listDialog(view: View) {

        AlertDialog.Builder(this)
            .setItems(items) { _, which ->
                toast("点击了列表对话框的 ${items[which]}")
            }
            .show()
    }

    fun radioDialog(view: View) {
        AlertDialog.Builder(this)
            .setTitle("单选对话框")
            .setSingleChoiceItems(items, 0) { dialog, which ->
                singleSelectResult = items[which]
            }
            .setPositiveButton("确定") { dialog, which ->
                toast(singleSelectResult)
            }
            .show()
    }

    fun multipleChoiceDialog(view: View) {
        val bool = booleanArrayOf(false, false, false, false)
        AlertDialog.Builder(this)
            .setTitle("多选对话框")
            .setMultiChoiceItems(items, bool) { _, _, _ ->
                // Not handling click event in this implementation
            }
            .setPositiveButton("确定") { _, _ ->
                val result = StringBuilder("选中了")
                for (i in bool.indices) {
                    if (bool[i]) {
                        result.append(items[i])
                    }
                }
                toast(result.toString())
            }
            .show()
    }

    /**
     * 圆环进度条对话框
     */
    fun progressBarDialog2(view: View) {
        val inflate = LayoutInflater.from(this).inflate(R.layout.progress_bar, null)
        // 设置一直滚动的效果
        var i = 0
        while (i <= 100) {
            if (i == 0) {
                i = 100
            }
            (inflate.findViewById<View>(R.id.progressBar) as ProgressBar).progress = i
            i++
        }
        val dialog = AlertDialog.Builder(this)
            .setView(inflate).create()
        dialog.show()
        // 设置对话框宽高，要先show()再设置才回生效
        val attributes = dialog.window!!.attributes
        attributes.width = 700
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        attributes.gravity = Gravity.CENTER
        dialog.window!!.setAttributes(attributes)
    }

    /**
     * 时间对话框
     */
    fun timePickerDialog(view: View) {
        TimePickerDialog(
            this, { timePicker: TimePicker?, hourOfDay: Int, minute: Int ->
                toast("选中了" + hourOfDay + "时" + minute + "分")
            },
            0,
            0,
            true
        ).show()
    }

    /**
     * 日期选择对话框
     */
    fun datePickerDialog(view: View) {
        DatePickerDialog(
            this, { datePicker: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                toast("选中了" + year + "年" + (month + 1) + "月" + dayOfMonth + "日")
            },
            2022,
            6,
            25
        ).show()
    }

    fun customizeDialog(view: View) {
        val dialog = AlertDialog.Builder(this).setView(R.layout.dialog_radius).create()
        dialog.show()
        // 设置对话框显示的位置
        dialog.window!!.setGravity(Gravity.BOTTOM)
        // 将背景设置为透明
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // 在xml设置的宽度、高度不起作用时，可使用如下方法设置宽高
        dialog.window!!.setLayout(500, 500)
    }

    // Material Design -----------------------------------------------------------------------------

    /**
     * 提示对话框
     */
    fun alertMaterialDialog(view: View) {
        MaterialAlertDialogBuilder(this).setTitle("提示对话框")
            .setMessage(R.string.confirm_exit)
            .setNegativeButton("不退了") { dialog: DialogInterface?, which: Int ->
                toast("真不退了？")
            }
            .setPositiveButton("退！！！") { dialog: DialogInterface?, which: Int ->
                toast("退就退！！！")
                finish()
            }
            .show()
    }

    /**
     * 左右按钮对话框
     */
    fun leftRightMaterialDialog(view: View) {
        MaterialAlertDialogBuilder(this)
            .setMessage("左右按钮对话框！")
            .setNeutralButton("左") { dialog: DialogInterface?, which: Int ->
                toast("左")
            }
            .setPositiveButton("右") { dialog: DialogInterface?, which: Int ->
                toast("右")
            }
            .show()
    }

    /**
     * 列表对话框
     */
    fun listMaterialDialog(view: View) {
        MaterialAlertDialogBuilder(this)
            .setItems(
                items
            ) { dialog: DialogInterface?, which: Int ->
                toast("点击了列表对话框的 " + items[which])
            }
            .show()
    }

    /**
     * 单选对话框
     */
    fun radioMaterialDialog(view: View) {
        MaterialAlertDialogBuilder(this)
            .setTitle("单选对话框")
            .setSingleChoiceItems(
                items, 0
            ) { dialog: DialogInterface?, which: Int ->
                singleSelectResult = items[which]
            }
            .setPositiveButton(
                "确定"
            ) { dialog: DialogInterface?, which: Int ->
                toast(singleSelectResult)
            }
            .show()
    }

    /**
     * 多选对话框
     */
    fun multipleChoiceMaterialDialog(view: View) {
        val bool = booleanArrayOf(false, false, false, false)
        MaterialAlertDialogBuilder(this)
            .setTitle("多选对话框")
            .setMultiChoiceItems(items, bool) { _: DialogInterface?, _: Int, _: Boolean -> }
            .setPositiveButton("确定") { _: DialogInterface?, _: Int ->
                val result = java.lang.StringBuilder("选中了")
                for (i in bool.indices) {
                    if (bool[i]) {
                        result.append(items[i])
                    }
                }
                toast(result.toString())
            }.show()
    }

    /**
     * 时间选择对话框
     */
    fun timePickerMaterialDialog(view: View) {
        // 获取当前时间
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // 创建时间选择对话框，并设置初始时间为当前时间
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(currentHour)
            .setMinute(currentMinute)
            .setTitleText("选择时间")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute
            val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)

            // 显示提示
            toast("选中的时间为 " + (if (timePicker.hour < 12) "上午" else "下午") + " " + timeString)
        }

        timePicker.show(supportFragmentManager, "1")
    }

    /**
     * 日期选择对话框
     */
    fun datePickerMaterialDialog(view: View) {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("选择日期") // 设置标题文本
        val datePicker = builder.build()
        datePicker.addOnPositiveButtonClickListener { selection: Long ->
            // 在这里获取选中的日期
            val selectedDate =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date(selection))
            // 显示选中的日期，使用 Snackbar 显示消息
            val message = "选择日期: $selectedDate"
            toast(message)
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }
}
