package mapleleaf.materialdesign.engine.ui.activities

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.Slider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.utils.SearchTextWatcher
import java.io.FileOutputStream
import java.util.EnumMap

class ActivityQRCode : UniversalActivityBase(R.layout.activity_qrcode) {

    private lateinit var editTextContent: EditText
    private lateinit var imageViewQRCode: ImageView
    private lateinit var clearInputImageView: ImageView
    private lateinit var sliderMargin: Slider
    private var searchTextWatcher: SearchTextWatcher? = null
    private var qrCodeMargin: Int = 0

//    override fun getLayoutResourceId() = R.layout.activity_qrcode

    override fun initializeComponents(savedInstanceState: Bundle?) {
        editTextContent = findViewById(R.id.edit_text_content)
        imageViewQRCode = findViewById(R.id.image_view_qr_code)
        clearInputImageView = findViewById(R.id.clear_input)

        sliderMargin = findViewById(R.id.slider_margin)
        sliderMargin.value = qrCodeMargin.toFloat()
        sliderMargin.valueFrom = 0f
        sliderMargin.valueTo = 8f

        lifecycleScope.launch(Dispatchers.Main) {
            generateQRCode()
        }

        searchTextWatcher = SearchTextWatcher {
            lifecycleScope.launch(Dispatchers.Main) {
                generateQRCode()
            }
        }
        editTextContent.addTextChangedListener(searchTextWatcher)

        clearInputImageView.setOnClickListener {
            editTextContent.text.clear()
        }

        sliderMargin.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // 在开始拖动时不进行操作
            }

            override fun onStopTrackingTouch(slider: Slider) {
                qrCodeMargin = slider.value.toInt()
                lifecycleScope.launch(Dispatchers.Main) {
                    generateQRCode()
                }
            }
        })

    }

    private fun setupSearchBox(searchTextWatcher: SearchTextWatcher) {

    }

    private suspend fun generateQRCode() {
        val content = editTextContent.text.toString().trim()
        if (content.isNotEmpty()) {
            val bitmap = withContext(Dispatchers.Default) {
                QRCodeGenerator.generateQRCode(content, qrCodeMargin)
            }
            imageViewQRCode.setImageBitmap(bitmap)

            // 保存 QR 码
            saveQRCode(bitmap)
        }
    }

    private suspend fun saveQRCode(bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            try {
                FileOutputStream(filesDir.resolve("QRCode.png")).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    object QRCodeGenerator {
        suspend fun generateQRCode(content: String, margin: Int): Bitmap =
            withContext(Dispatchers.Default) {
                val utf8Content = encodeAsUtf8(content)
                val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
                hints[EncodeHintType.MARGIN] = margin
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(utf8Content, BarcodeFormat.QR_CODE, 512, 512, hints)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
                bitmap
            }

        private fun encodeAsUtf8(input: String): String {
            return try {
                String(input.toByteArray(Charsets.UTF_8), Charsets.ISO_8859_1)
            } catch (e: java.io.UnsupportedEncodingException) {
                input
            }
        }
    }
}