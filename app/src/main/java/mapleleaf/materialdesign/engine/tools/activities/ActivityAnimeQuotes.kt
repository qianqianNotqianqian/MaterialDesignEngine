package mapleleaf.materialdesign.engine.tools.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.utils.SearchTextWatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class ActivityAnimeQuotes : UniversalActivityBase() {

    private lateinit var textView: TextView
    private lateinit var editText: EditText
    private lateinit var buttonClear: MaterialButton
    private lateinit var msgMaterialCardView: MaterialCardView
    private var searchTextWatcher: SearchTextWatcher? = null

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_anime_quotes
    }

    override fun initializeComponents(savedInstanceState: Bundle?) {
        textView = findViewById(R.id.text_view)
        editText = findViewById(R.id.edit_text)
        buttonClear = findViewById(R.id.button_clear)
        msgMaterialCardView = findViewById(R.id.msgMaterialCardView)

        buttonClear.setOnClickListener {
            editText.text.clear()
        }
        editText.removeTextChangedListener(searchTextWatcher)

        searchTextWatcher = SearchTextWatcher {
            val searchText = editText.text.toString()
            fetchText(searchText) { responseText ->
                runOnUiThread {
                    if (responseText != null) {
                        textView.text = responseText
                        msgMaterialCardView.visibility =
                            if (searchText.isEmpty()) View.GONE else View.VISIBLE
                    } else {
                        textView.text = "请求失败或无响应数据"
                    }
                }
            }
        }

        editText.addTextChangedListener(searchTextWatcher)

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 不需要在这里做任何操作
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString() ?: ""
                buttonClear.visibility = if (searchText.isEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
                // 不需要在这里做任何操作
            }
        })
        msgMaterialCardView.visibility = if (editText.text.isEmpty()) View.GONE else View.VISIBLE
        setToolbarTitle(getString(R.string.toolbar_title_activity_anime_quotes))
    }

    private fun fetchText(query: String, callback: (String?) -> Unit) {
        val client = OkHttpClient()
        val url = "https://www.hhlqilongzhu.cn/api/wenan_sou.php?msg=${query}"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    callback(response.body?.string())
                } else {
                    callback(null)
                }
            }
        })
    }
}