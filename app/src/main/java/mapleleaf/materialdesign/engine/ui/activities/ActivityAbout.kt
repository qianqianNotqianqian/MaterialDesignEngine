package mapleleaf.materialdesign.engine.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.content.pm.PackageInfoCompat
import com.drakeet.about.AbsAboutActivity
import com.drakeet.about.Card
import com.drakeet.about.Category
import com.drakeet.about.Contributor
import com.drakeet.about.License
import com.drakeet.about.provided.PicassoImageLoader
import com.google.android.material.appbar.CollapsingToolbarLayout
import mapleleaf.materialdesign.engine.MaterialDesignEngine.Companion.context
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.utils.ThemeConfig
import mapleleaf.materialdesign.engine.utils.toast

class ActivityAbout : AbsAboutActivity() {

    override fun onCreateHeader(
        icon: ImageView,
        slogan: TextView,
        version: TextView,
        collapsingToolbar: CollapsingToolbarLayout,
    ) {
        icon.setImageResource(R.mipmap.ic_launcher_sa)

        val textColor = ContextCompat.getColor(context, R.color.text_color)
        slogan.apply {
            setText(R.string.app_name)
            setTextColor(textColor)
        }

        version.apply {
            val versionName = getVersionName()
            val versionCode = getVersionCode()
            setTextColor(textColor)
            text = getString(R.string.version_text, versionName, versionCode.toString())
            gravity = Gravity.CENTER
        }

        toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.text_color))
        collapsingToolbar.setCollapsedTitleTextColor(
            ContextCompat.getColor(
                context,
                R.color.text_color
            )
        )
        setImageLoader(PicassoImageLoader())
    }

    private fun getVersionName(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "Unknown"
        }
    }

    private fun getVersionCode(): Long {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            PackageInfoCompat.getLongVersionCode(packageInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            -1
        }
    }

    override fun onItemsCreated(items: MutableList<Any>) {
        items.apply {
            add(
                Category(getString(R.string.menu_about))
            )
            add(
                Card("MDEngine 是一款提供给Android玩机爱好者的实用工具应用。\n\n搞机千万条，安全第一条。\n搞机不规范，亲人两行泪。")
            )
            add(
                Category(getString(R.string.menu_action_feedback))
            )
            add(
                Card("Website (官网):\nhttps://github.com/qianqianNotqianqian/MaterialDesignEngine")
            )
            add(
                Card("Download (下载地址):\nhttps://github.com/qianqianNotqianqian/MaterialDesignEngine")
            )
            add(
                Card("GitHub (开源仓库):\nhttps://github.com/qianqianNotqianqian/MaterialDesignEngine")
            )
            add(
                Card("Telegram Channel (TG频道):\nhttps://t.me/qianqiancxy_channel")
            )
            add(
                Card("Telegram Group (TG群组):\nhttps://t.me/qianqiancxy")
            )
            add(
                Card("QQ Channel (QQ频道):\nhttps://pd.qq.com/s/ggj9svlrl")
            )
            add(
                Card("QQ Group (QQ群):\n790587283")
            )
            add(
                Card("Mail:\n2220401791@gjzsr.com")
            )
            add(
                Category(getString(R.string.about_developers))
            )
            add(
                Contributor(
                    R.drawable.avatar_developer,
                    "笨蛋ovo (@liuran001)",
                    "Developer",
                    "https://bdovo.cc"
                )
            )
            add(
                Contributor(
                    R.drawable.avatar_qqlittleice233,
                    "QQ little ice",
                    "Contributor",
                    "https://github.com/qqlittleice233"
                )
            )
            add(
                Contributor(
                    R.drawable.avatar_qwq233,
                    "James Clef",
                    "Contributor",
                    "https://qwq2333.top"
                )
            )
            add(
                Contributor(
                    R.drawable.avatar_original_developer,
                    "情非得已c",
                    "Original Developer",
                    "https://obdo.cc/ZxZ3T2"
                )
            )
            add(
                Contributor(
                    R.drawable.avatar_icon_designer,
                    "莫白の",
                    "Icon Designer",
                    "https://obdo.cc/RhtXwJ"
                )
            )

            add(Category(getString(R.string.license)))
            add(
                License(
                    "kotlin",
                    "JetBrains",
                    License.APACHE_2,
                    "https://github.com/JetBrains/kotlin"
                )
            )
            add(
                License(
                    "about-page",
                    "drakeet",
                    License.APACHE_2,
                    "https://github.com/drakeet/about-page"
                )
            )
            add(
                License(
                    "AndroidX",
                    "Google",
                    License.APACHE_2,
                    "https://source.google.com"
                )
            )
            add(
                License(
                    "Android Jetpack",
                    "Google",
                    License.APACHE_2,
                    "https://source.google.com"
                )
            )
            add(
                License(
                    "material-components-android",
                    "Google",
                    License.APACHE_2,
                    "https://github.com/material-components/material-components-android"
                )
            )
            add(
                License(
                    "HiddenApiRefinePlugin",
                    "RikkaApps",
                    License.MIT,
                    "https://github.com/RikkaApps/HiddenApiRefinePlugin"
                )
            )
            add(
                License(
                    "lottie-android",
                    "Airbnb",
                    License.APACHE_2,
                    "https://github.com/airbnb/lottie-android"
                )
            )
            add(
                License(
                    "MPAndroidChart",
                    "PhilJay",
                    License.APACHE_2,
                    "https://github.com/PhilJay/MPAndroidChart"
                )
            )
            add(
                License(
                    "OkHttp",
                    "Square",
                    License.APACHE_2,
                    "https://github.com/square/okhttp"
                )
            )
            add(
                License(
                    "AndroidFastScroll",
                    "zhanghai",
                    License.APACHE_2,
                    "https://github.com/zhanghai/AndroidFastScroll"
                )
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_transparent_theme, menu)

        val transparent = menu.findItem(R.id.transparent_ui)
        val themeConfig = ThemeConfig(this)
        transparent.isChecked = themeConfig.getAllowTransparentUI()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.transparent_ui -> {
                val themeConfig = ThemeConfig(this)
                if (menuItem.isChecked) {
                    themeConfig.setAllowTransparentUI(false)
                    startActivity(Intent(this, ActivityMenu::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                } else {
                    fun checkPermission(permission: String): Boolean =
                        PermissionChecker.checkSelfPermission(
                            this,
                            permission
                        ) == PermissionChecker.PERMISSION_GRANTED
                    if (menuItem.isChecked && !checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        themeConfig.setAllowTransparentUI(false)
                        toast(getString(R.string.kr_write_external_storage))
                    } else {
                        themeConfig.setAllowTransparentUI(true)
                        startActivity(Intent(this, ActivityMenu::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                        finish()
                    }
                }
                return true
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }
}