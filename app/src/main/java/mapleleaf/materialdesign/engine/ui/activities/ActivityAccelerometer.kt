package mapleleaf.materialdesign.engine.ui.activities

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.card.MaterialCardView
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import me.zhanghai.android.fastscroll.FastScrollNestedScrollView
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import java.util.Locale

class ActivityAccelerometer : UniversalActivityBase(R.layout.activity_accelerometer),
    SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometerSensor: Sensor
    private lateinit var sensorNameTextView: TextView
    private lateinit var sensorNameTextView2: TextView
    private lateinit var sensorVendorTextView: TextView
    private lateinit var sensorVersionTextView: TextView
    private lateinit var sensorResolutionTextView: TextView
    private lateinit var sensorPowerTextView: TextView
    private lateinit var sensorMaxRangeTextView: TextView
    private lateinit var sensorDynamicTextView: TextView
    private lateinit var sensorWakeUpTextView: TextView
    private lateinit var sensorReportingModeTextView: TextView
    private lateinit var sensorXTextView: TextView
    private lateinit var sensorYTextView: TextView
    private lateinit var sensorZTextView: TextView
    private lateinit var sensorChart: LineChart
    private var setX: LineDataSet? = null
    private var setY: LineDataSet? = null
    private var setZ: LineDataSet? = null
    private var index = 0

//    override fun getLayoutResourceId() = R.layout.activity_accelerometer

    override fun initializeComponents(savedInstanceState: Bundle?) {

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        sensorNameTextView = findViewById(R.id.sensor_name_textview)
        sensorNameTextView2 = findViewById(R.id.sensor_name_textview2)
        sensorVendorTextView = findViewById(R.id.sensor_vendor_textview)
        sensorVersionTextView = findViewById(R.id.sensor_version_textview)
        sensorResolutionTextView = findViewById(R.id.sensor_resolution_textview)
        sensorPowerTextView = findViewById(R.id.sensor_power_textview)
        sensorMaxRangeTextView = findViewById(R.id.sensor_max_range_textview)
        sensorDynamicTextView = findViewById(R.id.sensor_dynamic_textview)
        sensorWakeUpTextView = findViewById(R.id.sensor_wake_up_textview)
        sensorReportingModeTextView = findViewById(R.id.sensor_reporting_mode_textview)
        sensorChart = findViewById(R.id.sensor_chart)

        sensorChart.description.isEnabled = false
        sensorChart.legend.isEnabled = false
        sensorChart.xAxis.isEnabled = false
        sensorChart.axisRight.isEnabled = false
        sensorChart.setTouchEnabled(false)
        sensorChart.isDragEnabled = true
        sensorChart.setScaleEnabled(false)
        sensorChart.axisLeft.textColor = ContextCompat.getColor(context, R.color.text_color)

        displaySensorInfo()

        sensorXTextView = findViewById(R.id.sensor_x)
        sensorYTextView = findViewById(R.id.sensor_y)
        sensorZTextView = findViewById(R.id.sensor_z)

        val baseColor = ContextCompat.getColor(context, R.color.background)
        val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)

        findViewById<MaterialCardView>(R.id.materialCardView).apply {
            strokeColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.3f)
            setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.15f))
        }
        findViewById<MaterialCardView>(R.id.materialCardView2).apply {
            strokeColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.3f)
            setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.15f))
        }

        val nestedScrollView = findViewById<FastScrollNestedScrollView>(R.id.nestedScrollView2)
        FastScrollerBuilder(nestedScrollView).build()
        setToolbarTitle(getString(R.string.toolbar_title_activity_accelerometer))
    }

    private fun displaySensorInfo() {
        val resolutionText =
            getString(
                R.string.accelerometer_sensor_resolution,
                accelerometerSensor.resolution.toString()
            )
        val powerText =
            getString(R.string.accelerometer_sensor_power, accelerometerSensor.power.toString())
        val maxRangeText =
            getString(
                R.string.accelerometer_sensor_max_range,
                accelerometerSensor.maximumRange.toString()
            )
        sensorNameTextView.text = accelerometerSensor.name
        sensorNameTextView2.text = accelerometerSensor.name
        sensorVendorTextView.text = accelerometerSensor.vendor
        sensorVersionTextView.text = accelerometerSensor.version.toString()
        sensorResolutionTextView.text = resolutionText
        sensorPowerTextView.text = powerText
        sensorMaxRangeTextView.text = maxRangeText
        sensorDynamicTextView.text = accelerometerSensor.isDynamicSensor.toString()
        sensorWakeUpTextView.text = accelerometerSensor.isWakeUpSensor.toString()
        sensorReportingModeTextView.text =
            if (accelerometerSensor.reportingMode == Sensor.REPORTING_MODE_CONTINUOUS) getString(R.string.reporting_mode1) else getString(
                R.string.reporting_mode2
            )
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val values = event.values
            val x = values[0]
            val y = values[1]
            val z = values[2]
            val formattedX = String.format(Locale.US, "%+.3f m/s²", x)
            val formattedY = String.format(Locale.US, "%+.3f m/s²", y)
            val formattedZ = String.format(Locale.US, "%+.3f m/s²", z)

            sensorXTextView.text =
                resources.getString(R.string.accelerometer_formatted_x, formattedX)
            sensorYTextView.text =
                resources.getString(R.string.accelerometer_formatted_y, formattedY)
            sensorZTextView.text =
                resources.getString(R.string.accelerometer_formatted_z, formattedZ)

            var data = sensorChart.data
            if (data == null) {
                data = LineData()
                sensorChart.data = data
            }
            if (setX == null) {
                setX = LineDataSet(null, "X")
                setX?.color = Color.RED
                setX?.setDrawCircles(false)
                data.addDataSet(setX)
            }
            if (setY == null) {
                setY = LineDataSet(null, "Y")
                setY?.color = Color.GREEN
                setY?.setDrawCircles(false)
                data.addDataSet(setY)
            }
            if (setZ == null) {
                setZ = LineDataSet(null, "Z")
                setZ?.color = Color.BLUE
                setZ?.setDrawCircles(false)
                data.addDataSet(setZ)
            }
            data.addEntry(Entry(index.toFloat(), x), 0)
            data.addEntry(Entry(index.toFloat(), y), 1)
            data.addEntry(Entry(index.toFloat(), z), 2)
            sensorChart.notifyDataSetChanged()
            sensorChart.setVisibleXRangeMaximum(50f)
            sensorChart.moveViewToX(index.toFloat())
            index++
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}