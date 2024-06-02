package mapleleaf.materialdesign.engine.model;

import android.graphics.drawable.Drawable;

import mapleleaf.materialdesign.engine.ui.adapter.AdapterAppChooser;

/**
 * 应用信息
 * Created by Hello on 2018/01/26.
 */

public class AppInfo extends AdapterAppChooser.AppInfo {
    public Drawable icon = null;
    public CharSequence stateTags = "";
    public CharSequence path = "";
    public CharSequence dir = "";
    public Boolean enabled = false;
    public Boolean suspended = false;
    public Boolean updated = false;
    public String versionName = "";
    public int versionCode = 0;
    public AppType appType = AppType.UNKNOWN;
    public CharSequence desc;
    public int targetSdkVersion;
    public int minSdkVersion;

    public static AppInfo getItem() {
        return new AppInfo();
    }

    public enum AppType {
        UNKNOWN,
        USER,
        SYSTEM,
        BACKUPFILE
    }
}
