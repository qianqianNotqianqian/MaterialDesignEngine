package mapleleaf.materialdesign.engine.ui.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.utils.toast
import java.security.MessageDigest

class ActivityText2MD5 : UniversalActivityBase(R.layout.activity_text_md5) {

//    override fun getLayoutResourceId() = R.layout.activity_text_md5

    override fun initializeComponents(savedInstanceState: Bundle?) {
        val inputEditText = findViewById<EditText>(R.id.inputText_text)
        val outputEditText = findViewById<EditText>(R.id.inputText_md5)
        val convertButton = findViewById<Button>(R.id.convertToMD5Button)

        convertButton.setOnClickListener {
            val text = inputEditText.text.toString()
            if (text.isEmpty()) {
                toast("请输入文字再转换！")
            } else {
                val md5 = calculateMD5(text)
                outputEditText.setText(md5)
            }
        }

        setToolbarTitle(getString(R.string.toolbar_title_activity_text_md5))

    }

    private fun calculateMD5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val byteArray = md.digest(input.toByteArray())

        val hexString = StringBuilder()
        for (byte in byteArray) {
            val hex = Integer.toHexString(0xFF and byte.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}