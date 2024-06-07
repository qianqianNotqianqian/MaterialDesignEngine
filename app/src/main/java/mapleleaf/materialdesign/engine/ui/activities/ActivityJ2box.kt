package mapleleaf.materialdesign.engine.ui.activities

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.view.CollisionView

class ActivityJ2box : UniversalActivityBase(R.layout.activity_j2box), SensorEventListener {
    private val imgs = intArrayOf(
        R.mipmap.share_fb,
        R.mipmap.share_kongjian,
        R.mipmap.share_pyq,
        R.mipmap.share_qq,
        R.mipmap.share_tw,
        R.mipmap.share_wechat,
        R.mipmap.share_weibo
    )
    private lateinit var sensorManager: SensorManager
    private lateinit var defaultSensor: Sensor
    private lateinit var collisionView: CollisionView

//    override fun getLayoutResourceId() = R.layout.activity_j2box

    override fun initializeComponents(savedInstanceState: Bundle?) {
        collisionView = findViewById(R.id.collisionView)
        initView()

        getToolbar().setBackgroundColor(getColor(R.color.transparent))
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        defaultSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
    }

    private fun initView() {
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER
        for (img in imgs) {
            val imageView = ImageView(this)
            imageView.setImageResource(img)
            collisionView.addView(imageView, layoutParams)
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, defaultSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1] * 2.0f
            collisionView.onSensorChanged(-x, y)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Ignored
    }
}
