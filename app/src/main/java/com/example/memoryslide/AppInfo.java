package com.example.memoryslide;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String appPackageName;
    private String appName;
    private Drawable appIcon;
    private String installDate;

    public AppInfo(String packageName, String appName, Drawable appIcon,String installDate) {
        this.appPackageName = packageName;
        this.appName = appName;
        this.appIcon = appIcon;
        this.installDate = installDate;
    }

    public String getAppPackageName() {
        return this.appPackageName;
    }

    public String getAppName() {
        return this.appName;
    }

    public Drawable getAppIcon() {
        return this.appIcon;
    }

    public String getInstallDate() {
        return this.installDate;
    }
}
