package mapleleaf.materialdesign.engine.data

data class MenuItemInfo(
    val itemId: Int,
    val iconResId: Int,
    val textResId: Int,
    val activityClass: Class<*>,
)
