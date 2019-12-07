package com.example.memoryslide;

import android.app.usage.UsageStats;
import android.graphics.drawable.Drawable;

public class AppInfo {
    public String appName;
    public Drawable appIcon;
    public String installDate;
    public UsageStats usageStats;
    public String executeTime;
    public boolean blockingState;
    public int spinnerPos;

    public String getAppName() {
        return this.appName;
    }

    public Drawable getAppIcon() {
        return this.appIcon;
    }

    public String getInstallDate() {
        return this.installDate;
    }

    public String getExecuteTime() {
        return this.executeTime;
    }
}
