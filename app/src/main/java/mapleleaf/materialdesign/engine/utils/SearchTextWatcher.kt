package mapleleaf.materialdesign.engine.utils

import android.text.Editable
import android.text.TextWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 监听文本变化的简单SearchTextWatcher
 * Created by Hello on 2018/03/04.
 */


//class SearchTextWatcher2(private var onChange: Runnable) : TextWatcher {
//    private val myHandler = Handler(Looper.getMainLooper())
//    private var lastInput = 0L
//    private val delayMillis = 300L

//    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//        myHandler.removeCallbacksAndMessages(null) // 取消之前的延迟任务

//        s?.run {
//            lastInput = System.currentTimeMillis()
//            myHandler.postDelayed({
//                val current = System.currentTimeMillis()
//                if (current - lastInput >= delayMillis) {
//                    onChange.run()
//                }
//            }, delayMillis)
//        }
//    }

//    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//    }

//    override fun afterTextChanged(s: Editable?) {
//    }
//}

class SearchTextWatcher(private val onChange: suspend () -> Unit) : TextWatcher {
    private var searchJob: Job? = null
    private val delayMillis = 0L

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        searchJob?.cancel() // 取消之前的任务

        searchJob = CoroutineScope(Dispatchers.Main).launch {
            delay(delayMillis) // 延迟指定时间

            // 等待延迟后执行任务
            onChange()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
    }
}