package mapleleaf.materialdesign.engine.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.kongzue.baseframework.BaseFrameworkSettings.log
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.interfaces.BottomDialogSlideEventLifecycleCallback
import com.kongzue.dialogx.interfaces.OnBindView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalFragmentBase
import mapleleaf.materialdesign.engine.ui.dialog.DialogHelper
import mapleleaf.materialdesign.engine.utils.toast
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets

class FragmentColorsZH : UniversalFragmentBase() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterColors
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override val layoutResId: Int
        get() = R.layout.fragment_colors

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)

        recyclerView = rootView.findViewById(R.id.recyclerView)
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout)

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = AdapterColors(requireContext())
        recyclerView.adapter = adapter
        FastScrollerBuilder(recyclerView).build()

        loadColorJSON()

        val colorRed = ContextCompat.getColor(requireContext(), R.color.red1)
        val colorGreen = ContextCompat.getColor(requireContext(), R.color.lawngreen)
        val colorBlue = ContextCompat.getColor(requireContext(), R.color.blue)
        val colorOrange = ContextCompat.getColor(requireContext(), R.color.orange2)
        val progressColors =
            ContextCompat.getColor(requireContext(), R.color.swipe_refresh_layout_progress)
        swipeRefreshLayout.setColorSchemeColors(colorRed, colorGreen, colorBlue, colorOrange)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(progressColors)

        swipeRefreshLayout.setOnRefreshListener {
            loadColorJSON()
        }
    }

    private fun loadColorJSON() {
        lifecycleScope.launch {
            try {
                val jsonContent = withContext(Dispatchers.IO) {
                    loadJSONFromAsset("chinese_colors.json")
                }
                parseColorsJson(jsonContent)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("chineseColor", "错误：${e.message}")
                swipeRefreshLayout.isRefreshing = false
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.d("chineseColor", "无法解析JSON：${e.message}")
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun loadJSONFromAsset(fileName: String): String {
        val assetManager: AssetManager = requireContext().assets
        return assetManager.open(fileName).use { inputStream ->
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            String(buffer, StandardCharsets.UTF_8)
        }
    }

    private suspend fun parseColorsJson(json: String): List<ColorItem> {
        val colorItems = mutableListOf<ColorItem>()
        val obj = JSONObject(json)
        val colorsArray = obj.getJSONArray("colors")
        for (i in 0 until colorsArray.length()) {
            val colorObj = colorsArray.getJSONObject(i)
            val cmyk = jsonArrayToIntArray(colorObj.getJSONArray("CMYK"))
            val rgb = jsonArrayToIntArray(colorObj.getJSONArray("RGB"))
            val hex = colorObj.getString("hex")
            val name = colorObj.getString("name")
            val pinyin = colorObj.getString("pinyin")
            val colorItem = ColorItem(cmyk, rgb, hex, name, pinyin)
            colorItems.add(colorItem)
            withContext(Dispatchers.Main) {
                adapter.updateColorItems(colorItems)
                swipeRefreshLayout.isRefreshing = false
            }
        }
        return colorItems
    }

    private fun jsonArrayToIntArray(jsonArray: JSONArray): IntArray {
        val array = IntArray(jsonArray.length())
        for (i in 0 until jsonArray.length()) {
            array[i] = jsonArray.getInt(i)
        }
        return array
    }

    class ColorItem(
        var cmyk: IntArray,
        var rgb: IntArray,
        var hex: String,
        var name: String,
        var pinyin: String,
    )

    class AdapterColors(private val context: Context) :
        RecyclerView.Adapter<AdapterColors.ColorViewHolder>() {

        private val colorItems = mutableListOf<ColorItem>()

        @SuppressLint("NotifyDataSetChanged")
        fun updateColorItems(newColorItems: List<ColorItem>) {
//            val diffResult = DiffUtil.calculateDiff(DiffCallback(colorItems, newColorItems))
//            colorItems.clear()
//            colorItems.addAll(newColorItems)
//            diffResult.dispatchUpdatesTo(this)

            colorItems.clear()
            colorItems.addAll(newColorItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.color_item, parent, false)
            return ColorViewHolder(view)
        }

        override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
            val colorItem = colorItems[position]
            val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
            layoutParams.topMargin = if (position == 0) 18 else 0
            holder.itemView.layoutParams = layoutParams

            val rgb = colorItem.rgb
            holder.colorConstraintLayout.setBackgroundColor(Color.rgb(rgb[0], rgb[1], rgb[2]))

            holder.nameTextView.text = colorItem.name
            holder.pinyinTextView.text = colorItem.pinyin
            holder.hexTextView.text = colorItem.hex.uppercase()

            val textColor = if (calculateBrightness(rgb) < 128) {
                Color.WHITE
            } else {
                Color.BLACK
            }

            holder.nameTextView.setTextColor(textColor)
            holder.pinyinTextView.setTextColor(textColor)
            holder.hexTextView.setTextColor(textColor)
            holder.colorView.setBackgroundColor(textColor)
            holder.image.setColorFilter(textColor)

            holder.colorMaterialCardView.setOnClickListener {
                showColorDetailsDialog(context, colorItem)
            }
        }

        override fun onViewAttachedToWindow(holder: ColorViewHolder) {
            super.onViewAttachedToWindow(holder)
            setFadeAnimation(holder.itemView)
        }

        private fun calculateBrightness(rgb: IntArray): Double {
            return (0.299 * rgb[0] + 0.587 * rgb[1] + 0.114 * rgb[2])
        }

        @SuppressLint("SetTextI18n", "InflateParams")
        private fun showColorDetailsDialog(context: Context, colorItem: ColorItem) {

            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.color_details_dialog, null)
            val dialog = DialogHelper.customDialog(context, dialogView)

            val rgb = colorItem.rgb
            val colorHexTextView = dialogView.findViewById<TextView>(R.id.color_hex_text_view)
            val colorRGBTextView = dialogView.findViewById<TextView>(R.id.color_rgb_text_view)
            val colorCYMKTextView =
                dialogView.findViewById<TextView>(R.id.color_cymk_text_view)
            val colorHSVTextView = dialogView.findViewById<TextView>(R.id.color_hsv_text_view)
            val colorNameTextView =
                dialogView.findViewById<TextView>(R.id.color_name_text_view)
            val colorPinyinTextView =
                dialogView.findViewById<TextView>(R.id.color_pinyin_text_view)

            dialogView.findViewById<ConstraintLayout>(R.id.constraint_layout).apply {
                setBackgroundColor(Color.rgb(rgb[0], rgb[1], rgb[2]))
            }

            colorNameTextView.text = colorItem.name
            colorPinyinTextView.text = colorItem.pinyin
            colorHexTextView.text = colorItem.hex.uppercase()
            colorRGBTextView.text = colorItem.rgb.joinToString(",")
            colorCYMKTextView.text = colorItem.cmyk.joinToString(",")

            val hsv = FloatArray(3)
            Color.RGBToHSV(colorItem.rgb[0], colorItem.rgb[1], colorItem.rgb[2], hsv)
            val hue = String.format("%.6f", hsv[0].toDouble())
            val saturation = String.format("%.2f", (hsv[1] * 100)) + "%"
            val value = String.format("%.2f", (hsv[2] * 100)) + "%"
            colorHSVTextView.text = "$hue,$saturation,$value"

            dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
            }
            dialogView.findViewById<View>(R.id.btn_copy_item).setOnClickListener {
                dialog.dismiss()
                copyColorDetailsToClipboard(colorItem)
            }
        }

        private fun copyColorDetailsToClipboard(colorItem: ColorItem) {
            val hsv = FloatArray(3)
            Color.RGBToHSV(colorItem.rgb[0], colorItem.rgb[1], colorItem.rgb[2], hsv)
            val hue = String.format("%.6f", hsv[0].toDouble())
            val saturation = String.format("%.2f", (hsv[1] * 100)) + "%"
            val value = String.format("%.2f", (hsv[2] * 100)) + "%"

            val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                "color_details",
                "Name: ${colorItem.name}\n" +
                        "Pinyin: ${colorItem.pinyin}\n" +
                        "Hex: ${colorItem.hex.uppercase()}\n" +
                        "RGB: ${colorItem.rgb.joinToString(",")}\n" +
                        "CMYK: ${colorItem.cmyk.joinToString(",")}\n" +
                        "HSV: $hue,$saturation,$value"
            )
            clipboardManager.setPrimaryClip(clip)
            toast("颜色详情已复制到剪贴板")
        }

        override fun getItemCount(): Int {
            return colorItems.size
        }

        class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
            val pinyinTextView: TextView = itemView.findViewById(R.id.pinyinTextView)
            val hexTextView: TextView = itemView.findViewById(R.id.hexTextView)
            val colorConstraintLayout: ConstraintLayout =
                itemView.findViewById(R.id.colorConstraintLayout)
            val colorMaterialCardView: MaterialCardView =
                itemView.findViewById(R.id.color_material_cardView)
            val colorView: View = itemView.findViewById(R.id.colorView)
            val image: AppCompatImageView = itemView.findViewById(R.id.color_image)
        }

//        fun setFadeAnimation(view: View, context: Context) {
//            val animation = AnimationUtils.loadAnimation(context, R.anim.dialog_fade_in)
//            view.startAnimation(animation)
//        }

        private fun setFadeAnimation(view: View) {
            val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            animator.duration = 320
            animator.start()
        }
    }
}