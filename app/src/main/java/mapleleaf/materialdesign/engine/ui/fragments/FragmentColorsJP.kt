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
import java.io.IOException
import java.nio.charset.StandardCharsets

class FragmentColorsJP : UniversalFragmentBase() {

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
                    loadJSONFromAsset("nippon_colors.json")
                }
                parseColorsJson(jsonContent)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("japanColor", "错误：${e.message}")
                swipeRefreshLayout.isRefreshing = false
            } catch (e: JSONException) {
                e.printStackTrace()
                Log.d("japanColor", "无法解析JSON：${e.message}")
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun loadJSONFromAsset(fileName: String): String {
        val assetManager: AssetManager = requireContext().assets
        return assetManager.open(fileName).use { inputStream ->
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            String(buffer, StandardCharsets.UTF_8)
        }
    }

    private suspend fun parseColorsJson(json: String): List<ColorItem> {
        val colorItems = mutableListOf<ColorItem>()
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val colorObj = jsonArray.getJSONObject(i)
            val name = colorObj.getString("name")
            val cname = colorObj.getString("cname")
            val colorString = colorObj.getString("color")
            val color = Color.parseColor("#$colorString")
            val colorItem = ColorItem(name, cname, color, i.toLong())
            colorItems.add(colorItem)
            withContext(Dispatchers.Main) {
                adapter.updateColorItems(colorItems)
                swipeRefreshLayout.isRefreshing = false
            }
        }
        return colorItems
    }

    data class ColorItem(
        val name: String,
        val cname: String,
        val color: Int,
        val id: Long,
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
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.color_item, parent, false)
            return ColorViewHolder(view)
        }

        override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
            val colorItem = colorItems[position]
            val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
            layoutParams.topMargin = if (position == 0) 18 else 0
            holder.itemView.layoutParams = layoutParams
            holder.colorConstraintLayout.setBackgroundColor(colorItem.color)
            holder.nameTextView.text = colorItem.cname
            holder.pinyinTextView.text = colorItem.name
            holder.hexTextView.text = String.format("#%06X", 0xFFFFFF and colorItem.color)

            val textColor = if (calculateBrightness(colorItem.color) < 128) {
                Color.WHITE
            } else {
                Color.BLACK
            }

            holder.nameTextView.setTextColor(textColor)
            holder.pinyinTextView.setTextColor(textColor)
            holder.hexTextView.setTextColor(textColor)
            holder.colorView.setBackgroundColor(textColor)
            holder.image.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.japan_color)
            )
            holder.image.setColorFilter(textColor)

            holder.colorMaterialCardView.setOnClickListener {
                showColorDetailsDialog(context, colorItem)
            }
        }

        override fun onViewAttachedToWindow(holder: ColorViewHolder) {
            super.onViewAttachedToWindow(holder)
            setFadeAnimation(holder.itemView)
        }

        @SuppressLint("SetTextI18n", "InflateParams")
        private fun showColorDetailsDialog(context: Context, colorItem: ColorItem) {

            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.color_details_dialog, null)
            val dialog = DialogHelper.customDialog(context, dialogView)

            val r = Color.red(colorItem.color)
            val g = Color.green(colorItem.color)
            val b = Color.blue(colorItem.color)

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
                setBackgroundColor(Color.rgb(r, g, b))
            }

            colorNameTextView.text = colorItem.cname
            colorPinyinTextView.text = colorItem.name
            colorHexTextView.text = String.format("#%06X", 0xFFFFFF and colorItem.color)
            colorRGBTextView.text = "$r,$g,$b"

            val c = 1 - r / 255.0
            val m = 1 - g / 255.0
            val y = 1 - b / 255.0
            val k = minOf(c, m, y)
            val c1 = (c - k) / (1 - k)
            val m1 = (m - k) / (1 - k)
            val y1 = (y - k) / (1 - k)

            val cInt = ((c1 * 100).toInt())
            val mInt = ((m1 * 100).toInt())
            val yInt = ((y1 * 100).toInt())
            val kInt = ((k * 100).toInt())

            colorCYMKTextView.text = "$cInt,$mInt,$yInt,$kInt"

            val hsv = FloatArray(3)
            Color.RGBToHSV(r, g, b, hsv)
            colorHSVTextView.text = "${String.format("%.6f", hsv[0])},${
                String.format(
                    "%.2f",
                    hsv[1] * 100
                )
            }%,${String.format("%.2f", hsv[2] * 100)}%"

            dialogView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
                dialog.dismiss()
            }
            dialogView.findViewById<View>(R.id.btn_copy_item).setOnClickListener {
                dialog.dismiss()
                copyColorDetailsToClipboard(colorItem)
            }
        }

        private fun copyColorDetailsToClipboard(colorItem: ColorItem) {
            val r = Color.red(colorItem.color)
            val g = Color.green(colorItem.color)
            val b = Color.blue(colorItem.color)

            val rgbText = "$r,$g,$b"

            val c = 1 - r / 255.0
            val m = 1 - g / 255.0
            val y = 1 - b / 255.0
            val k = minOf(c, m, y)
            val c1 = (c - k) / (1 - k)
            val m1 = (m - k) / (1 - k)
            val y1 = (y - k) / (1 - k)

            val cmykText =
                "${(c1 * 100).toInt()},${(m1 * 100).toInt()},${(y1 * 100).toInt()},${(k * 100).toInt()}"

            val hsv = FloatArray(3)
            Color.RGBToHSV(r, g, b, hsv)
            val hsvText = "${String.format("%.6f", hsv[0])},${
                String.format(
                    "%.2f",
                    hsv[1] * 100
                )
            }%,${String.format("%.2f", hsv[2] * 100)}%"

            val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                "color_details",
                "Name: ${colorItem.cname}\n" +
                        "japan: ${colorItem.name}\n" +
                        "Hex: ${String.format("#%06X", 0xFFFFFF and colorItem.color)}\n" +
                        "RGB: $rgbText\n" +
                        "CMYK: $cmykText\n" +
                        "HSV: $hsvText"
            )
            clipboardManager.setPrimaryClip(clip)
            toast("颜色详情已复制到剪贴板")
        }

        private fun calculateBrightness(color: Int): Double {
            return (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color))
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

        private fun setFadeAnimation(view: View) {
            val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            animator.duration = 320
            animator.start()
        }
    }
}