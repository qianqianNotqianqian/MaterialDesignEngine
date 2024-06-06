package mapleleaf.materialdesign.engine.ui.activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class ActivitySystemSensors : UniversalActivityBase() {

    private lateinit var sensorManager: SensorManager
    lateinit var adapterSensor: AdapterSensor
    private lateinit var recyclerView: RecyclerView
    private lateinit var loading: AppCompatImageView
    private var animatedVectorDrawable: AnimatedVectorDrawable? = null

    override fun getLayoutResourceId() = R.layout.activity_sensor_info

    @SuppressLint("SetTextI18n")
    override fun initializeComponents(savedInstanceState: Bundle?) {

        setToolbarTitle(getString(R.string.toolbar_title_activity_system_sensors))
        adapterSensor = AdapterSensor(this)

        recyclerView = findViewById(R.id.recyclerView)
        loading = findViewById(R.id.loading)
        animatedVectorDrawable = AppCompatResources.getDrawable(
            this,
            R.drawable.progress_loading_manager
        ) as AnimatedVectorDrawable
        loading.setImageDrawable(animatedVectorDrawable)
        animatedVectorDrawable!!.start()

        recyclerView.adapter = adapterSensor
        recyclerView.layoutManager = LinearLayoutManager(this)
        FastScrollerBuilder(recyclerView).build()
        sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        CoroutineScope(Dispatchers.Main).launch {
            loadSensors()
        }
    }

    private fun loadSensors() {
        lifecycleScope.launch(Dispatchers.Main) {
            val sensors = withContext(Dispatchers.Default) {
                sensorManager.getSensorList(Sensor.TYPE_ALL)
            }
            animatedVectorDrawable!!.stop()
            loading.isVisible = false
            adapterSensor.updateSensors(sensors)
            val sensorCount = adapterSensor.itemCount
            setToolbarSubtitle("$sensorCount 个")
        }
    }

    class AdapterSensor(private val context: Context) :
        RecyclerView.Adapter<AdapterSensor.SensorViewHolder>() {

        private val sensorTypeMap: SparseIntArray = SparseIntArray()
        private var sensors = emptyList<Sensor>()

        init {
            // 添加传感器类型到中文名称的映射
            sensorTypeMap.put(Sensor.TYPE_ACCELEROMETER, R.string.sensor_type_accelerometer)
            sensorTypeMap.put(
                Sensor.TYPE_ACCELEROMETER_UNCALIBRATED,
                R.string.sensor_type_accelerometer_uncalibrated
            )
            sensorTypeMap.put(Sensor.TYPE_GRAVITY, R.string.sensor_type_gravity)
            sensorTypeMap.put(Sensor.TYPE_STATIONARY_DETECT, R.string.sensor_type_stationry_detect)
            sensorTypeMap.put(
                Sensor.TYPE_GYROSCOPE_UNCALIBRATED,
                R.string.sensor_type_gyroscope_uncalibrated
            )
            sensorTypeMap.put(Sensor.TYPE_GYROSCOPE, R.string.sensor_type_gyroscope)
            sensorTypeMap.put(Sensor.TYPE_LIGHT, R.string.sensor_type_light)
            sensorTypeMap.put(Sensor.TYPE_MAGNETIC_FIELD, R.string.sensor_type_magnetic_field)
            sensorTypeMap.put(Sensor.TYPE_ORIENTATION, R.string.sensor_type_orientation)
            sensorTypeMap.put(Sensor.TYPE_PROXIMITY, R.string.sensor_type_proximity)
            sensorTypeMap.put(Sensor.TYPE_ROTATION_VECTOR, R.string.sensor_type_rotation_vector)
            sensorTypeMap.put(
                Sensor.TYPE_GAME_ROTATION_VECTOR,
                R.string.sensor_type_game_rotation_vector
            )
            sensorTypeMap.put(
                Sensor.TYPE_LINEAR_ACCELERATION,
                R.string.sensor_type_linear_acceleration
            )
            sensorTypeMap.put(Sensor.TYPE_STEP_COUNTER, R.string.sensor_type_step_counter)
            sensorTypeMap.put(Sensor.TYPE_STEP_DETECTOR, R.string.sensor_type_step_detector)
            sensorTypeMap.put(
                Sensor.TYPE_SIGNIFICANT_MOTION,
                R.string.sensor_type_significant_motion
            )
            sensorTypeMap.put(
                Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,
                R.string.sensor_type_field_uncalibrated
            )
            sensorTypeMap.put(
                Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
                R.string.sensor_type_geomagnetic_rotation_vector
            )
            sensorTypeMap.put(Sensor.TYPE_MOTION_DETECT, R.string.sensor_type_motion_detect)
            sensorTypeMap.put(Sensor.TYPE_GRAVITY, R.string.sensor_type_gravity)
            sensors = emptyList()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val itemView = inflater.inflate(R.layout.item_sensor_info, parent, false)
            return SensorViewHolder(itemView)
        }

        override fun getItemCount(): Int = sensors.size

        override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
            val sensor = sensors[position]
            val chineseName = getSensorChineseName(sensor.type) ?: sensor.name
            val englishName = sensor.name
            holder.bind(
                chineseName,
                englishName,
                sensor.vendor,
                sensor.isWakeUpSensor,
                sensor.power
            )
            val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
            layoutParams.topMargin = if (position == 0) 18 else 0
            holder.itemView.layoutParams = layoutParams
        }

        override fun onViewAttachedToWindow(holder: SensorViewHolder) {
            super.onViewAttachedToWindow(holder)
            setFadeAnimation(holder.itemView)
        }

        @SuppressLint("NotifyDataSetChanged")
        fun updateSensors(newSensors: List<Sensor>) {
//            val diffCallback = DiffCallback(sensors, newSensors)
//            val diffResult = DiffUtil.calculateDiff(diffCallback)
//            sensors = newSensors
//            diffResult.dispatchUpdatesTo(this)
            sensors = newSensors
            notifyDataSetChanged()
        }

        private fun getSensorChineseName(sensorType: Int): String? {
            val resId = sensorTypeMap.get(sensorType)
            return if (resId != 0) {
                context.getString(resId)
            } else {
                null
            }
        }

        fun getSensorAtPosition(position: Int): Sensor? {
            return if (position in sensors.indices) {
                sensors[position]
            } else {
                null
            }
        }

        class SensorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
            private val textViewChineseName: TextView =
                itemView.findViewById(R.id.textViewChineseName)
            private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
            private val textViewVendor: TextView = itemView.findViewById(R.id.textViewVendor)
            private val textViewWakeUp: TextView = itemView.findViewById(R.id.textViewWakeUp)
            private val textViewPower: TextView = itemView.findViewById(R.id.textViewPower)
            private val deviceSensorMaterialCardView: MaterialCardView =
                itemView.findViewById(R.id.deviceSensorMaterialCardView)

            init {
                val baseColor =
                    ContextCompat.getColor(MaterialDesignEngine.context, R.color.background_color)
                val primaryColor =
                    ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
                val blendedColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.2f)
                deviceSensorMaterialCardView.setBackgroundColor(blendedColor)
                deviceSensorMaterialCardView.setOnClickListener(this)
            }


            @SuppressLint("SetTextI18n")
            fun bind(
                chineseName: String,
                englishName: String,
                vendor: String,
                isWakeUpSensor: Boolean,
                power: Float,
            ) {
                textViewChineseName.text = chineseName
                textViewName.text = "名称: $englishName"
                textViewVendor.text = "厂商: $vendor"
                textViewWakeUp.text = "唤醒型传感器: ${if (isWakeUpSensor) "是" else "否"}"
                textViewPower.text = "耗电量: %.3f mA".format(power)
            }

            override fun onClick(v: View) {
                // 获取点击的位置
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val activity = v.context as? ActivitySystemSensors
                    activity?.let {
                        val sensor = activity.adapterSensor.getSensorAtPosition(position)
                        sensor?.let {
                        }
                    }
                }
            }
        }

        private fun setFadeAnimation(view: View) {
            val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            animator.duration = 320
            animator.start()
        }
    }
}
