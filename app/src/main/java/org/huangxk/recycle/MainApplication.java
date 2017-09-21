package org.huangxk.recycle;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        CrashReport.initCrashReport(getApplicationContext(), "8f06ba34d2", false);
        Speeker.getInstance().initialize(this);

        DataBase.createInstance(this);
        DataBase.getInstance().countData(0);
        super.onCreate();
    }
}
