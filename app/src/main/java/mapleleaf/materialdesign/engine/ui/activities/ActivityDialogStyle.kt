package mapleleaf.materialdesign.engine.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import mapleleaf.materialdesign.engine.MaterialDesignEngine
import mapleleaf.materialdesign.engine.R
import mapleleaf.materialdesign.engine.base.UniversalActivityBase
import mapleleaf.materialdesign.engine.ui.activities.dialogs.ActivityCalcDialog
import mapleleaf.materialdesign.engine.ui.activities.dialogs.ActivityKongzueDialog
import me.zhanghai.android.fastscroll.FastScrollNestedScrollView
import me.zhanghai.android.fastscroll.FastScrollerBuilder

class ActivityDialogStyle : UniversalActivityBase() {

    override fun getLayoutResourceId() = R.layout.activity_dialog_style

    override fun initializeComponents(savedInstanceState: Bundle?) {

        val scrollingView = findViewById<FastScrollNestedScrollView>(R.id.nestedScrollView)
        FastScrollerBuilder(scrollingView).build()
        val menuItems = arrayOf(
            ActivityMenu.MenuItemInfo(
                R.id.KongzueDialog,
                R.drawable.ic_bilibili,
                R.string.menu_start_kongzue_dialog,
                ActivityKongzueDialog::class.java
            ),
            ActivityMenu.MenuItemInfo(
                R.id.CalcDialog,
                R.drawable.ic_bilibili,
                R.string.menu_start_calcDialog,
                ActivityCalcDialog::class.java
            )
        )
        for (menuItem in menuItems) {
            setMenuItem(menuItem)
        }

        setToolbarTitle(getString(R.string.toolbar_title_activity_dialog_show))
    }

    private fun setMenuItem(menuItemInfo: ActivityMenu.MenuItemInfo) {
        val primaryColor =
            ContextCompat.getColor(MaterialDesignEngine.context, R.color.colorPrimary)
        findViewById<View>(menuItemInfo.itemId).apply {
            findViewById<MaterialCardView>(menuItemInfo.itemId).apply {
                val baseColor = ContextCompat.getColor(context, R.color.background)
                setCardBackgroundColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.15f))
            }
            findViewById<AppCompatImageView>(R.id.menuIcon).setImageResource(menuItemInfo.iconResId)
            findViewById<MaterialTextView>(R.id.menuText).apply {
                setText(menuItemInfo.textResId)
                val baseColor = ContextCompat.getColor(context, R.color.text_color)
                setTextColor(ColorUtils.blendARGB(baseColor, primaryColor, 0.2f))
            }
            setOnClickListener {
                context.startActivity(Intent(context, menuItemInfo.activityClass))
            }
        }
    }

}
