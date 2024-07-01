package mapleleaf.materialdesign.engine.ui.activities

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.card.MaterialCardView
import dalvik.system.DexFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class ActivityApplicationDex : UniversalActivityBase(R.layout.activity_application_dex) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterDexMethodInfo
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var loading: AppCompatImageView
    private var animatedVectorDrawable: AnimatedVectorDrawable? = null

//    override fun getLayoutResourceId() = R.layout.activity_application_dex

    override fun initializeComponents(savedInstanceState: Bundle?) {
        recyclerView = findViewById(R.id.recyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        val colorRed = ContextCompat.getColor(this, R.color.red1)
        val colorGreen = ContextCompat.getColor(this, R.color.lawngreen)
        val colorBlue = ContextCompat.getColor(this, R.color.blue)
        val colorOrange = ContextCompat.getColor(this, R.color.orange2)
        val progressColors = ContextCompat.getColor(this, R.color.swipe_refresh_layout_progress)
        swipeRefreshLayout.setColorSchemeColors(colorRed, colorGreen, colorBlue, colorOrange)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(progressColors)

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = AdapterDexMethodInfo()
        recyclerView.adapter = adapter
        FastScrollerBuilder(recyclerView).useMd2Style().build()

        loading = findViewById(R.id.loading)
        animatedVectorDrawable = AppCompatResources.getDrawable(
            this,
            R.drawable.progress_loading_manager
        ) as AnimatedVectorDrawable
        loading.setImageDrawable(animatedVectorDrawable)
        animatedVectorDrawable?.start()

        val packageName = intent.getStringExtra("packageName")
        if (packageName != null) {
            loadDexMethods(packageName)
            packageName.let {
                val appInfo = packageManager.getApplicationInfo(it, 0)
                val label = packageManager.getApplicationLabel(appInfo).toString()
                setToolbarTitle(label)
            }
        } else {
            loadDexMethods()
        }

        swipeRefreshLayout.setOnRefreshListener {
            if (packageName != null) {
                loadDexMethods(packageName)
            } else {
                loadDexMethods()
            }
        }
    }

    private fun loadDexMethods(packageName: String) {
        lifecycleScope.launch {
            val dexClasses = getDexClasses(packageName)
            withContext(Dispatchers.Main) {
                adapter.setDexMethodsList(dexClasses)
                animatedVectorDrawable?.stop()
                loading.isVisible = false
                swipeRefreshLayout.isRefreshing = false
                setToolbarSubtitle(
                    getString(
                        R.string.toolbar_subtitle_activity_application_dex,
                        dexClasses.size
                    )
                )
            }
        }
    }

    private fun loadDexMethods() {
        lifecycleScope.launch {
            val dexClasses = getDexClasses()
            withContext(Dispatchers.Main) {
                adapter.setDexMethodsList(dexClasses)
                animatedVectorDrawable?.stop()
                loading.isVisible = false
                swipeRefreshLayout.isRefreshing = false
                setToolbarSubtitle(
                    getString(
                        R.string.toolbar_subtitle_activity_application_dex,
                        dexClasses.size
                    )
                )
            }
        }
    }

    private suspend fun getDexClasses(packageName: String): List<String> =
        withContext(Dispatchers.IO) {
            val dexClasses = ArrayList<String>()
            try {
                val packageManager = packageManager
                val applicationInfo: ApplicationInfo =
                    packageManager.getApplicationInfo(packageName, 0)
                val apkPath: String = applicationInfo.sourceDir
                val dexFiles = getDexFiles(apkPath)
                for (dexFilePath in dexFiles) {
                    val dexFile = DexFile(dexFilePath)
                    val entries = dexFile.entries()
                    while (entries.hasMoreElements()) {
                        val className = entries.nextElement()
                        if (!className.contains("$")) {
                            dexClasses.add(className)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("getDexClasses", "Error in getDexClasses: ${e.message}")
                e.printStackTrace()
            }

            dexClasses
        }

    private suspend fun getDexClasses(): List<String> = withContext(Dispatchers.IO) {
        val dexClasses = ArrayList<String>()
        val applicationInfo: ApplicationInfo = applicationInfo
        val apkPath: String = applicationInfo.sourceDir
        try {
            val dexFiles = getDexFiles(apkPath)
            for (dexFilePath in dexFiles) {
                val dexFile = DexFile(dexFilePath)
                val entries = dexFile.entries()
                while (entries.hasMoreElements()) {
                    val className = entries.nextElement()
                    if (!className.contains("$")) { // Only add top-level classes
                        dexClasses.add(className)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("getDexClasses", "Error in getDexClasses: ${e.message}")
            e.printStackTrace()
        }

        dexClasses
    }

    private fun getDexFiles(apkPath: String): List<String> {
        val dexFiles = ArrayList<String>()
        try {
            val dexFile = DexFile(apkPath)
            dexFiles.add(apkPath)
            val dexElements = getDexElements(dexFile)
            for (dexElement in dexElements) {
                val dexPath = dexElement.toString()
                if (dexPath.isNotEmpty()) {
                    dexFiles.add(dexPath)
                }
            }
        } catch (e: Exception) {
            Log.d("getDexFiles", "Error in getDexFiles: ${e.message}")
            e.printStackTrace()
        }
        return dexFiles
    }

    private fun getDexElements(dexFile: DexFile): Array<Any> {
        try {
            val dexFileClass = dexFile.javaClass
            val field = dexFileClass.getDeclaredField("dexElements")
            field.isAccessible = true
            val fieldValue = field.get(dexFile)
            if (fieldValue is Array<*>) {
                return fieldValue as Array<Any>
            }
        } catch (e: Exception) {
            Log.d("getDexElements", "Error in getDexElements: ${e.message}")
            e.printStackTrace()
        }
        return emptyArray()
    }

    class AdapterDexMethodInfo :
        RecyclerView.Adapter<AdapterDexMethodInfo.ApplicationDexViewHolder>() {

        private var dexClasses: List<String> = ArrayList()

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): ApplicationDexViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_dex_method, parent, false)
            return ApplicationDexViewHolder(view)
        }

        override fun onBindViewHolder(holder: ApplicationDexViewHolder, position: Int) {
            holder.bind(dexClasses[position])
            val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
            layoutParams.topMargin = if (position == 0) 18 else 0
            holder.itemView.layoutParams = layoutParams
        }

        override fun onViewAttachedToWindow(holder: ApplicationDexViewHolder) {
            super.onViewAttachedToWindow(holder)
            setFadeAnimation(holder.itemView)
        }

        override fun getItemCount(): Int {
            return dexClasses.size
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setDexMethodsList(dexClasses: List<String>) {
            this.dexClasses = dexClasses
            notifyDataSetChanged()
        }

        class ApplicationDexViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val dexClassNameTextView: TextView = itemView.findViewById(R.id.dexMethods)
            private val appDetailsMaterialCardView =
                itemView.findViewById<MaterialCardView>(R.id.appDetailsMaterialCardView)

            fun bind(className: String) {
                val simpleClassName = className.substringAfterLast('.')
                dexClassNameTextView.text = simpleClassName
                val baseColor =
                    ContextCompat.getColor(MaterialDesignEngine.context, R.color.background_color)
                val primaryColor =
                    ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)

                appDetailsMaterialCardView.apply {
                    strokeColor = ColorUtils.blendARGB(baseColor, primaryColor, 0.3f)
                    setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.2f))
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
