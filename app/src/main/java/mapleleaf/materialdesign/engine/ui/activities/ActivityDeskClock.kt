package mapleleaf.materialdesign.engine.ui.activities

import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.google.android.material.card.MaterialCardView
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ActivityDeskClock : UniversalActivityBase(R.layout.activity_desk_clock) {

    override fun initializeComponents(savedInstanceState: Bundle?) {
        // 获取TextView实例
        val dateTextView = findViewById<TextView>(R.id.date)
        // 获取当前日期和星期
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd EEEE", Locale.getDefault())
        val currentDateAndDayOfWeek = dateFormat.format(calendar.time)
        // 将当前日期和星期设置到TextView中
        dateTextView.text = currentDateAndDayOfWeek

        customizeCardView(findViewById(R.id.main_clock))
        setToolbarTitle(getString(R.string.toolbar_title_activity_clock))
    }
}
