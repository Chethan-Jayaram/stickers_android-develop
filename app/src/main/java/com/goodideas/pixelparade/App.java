package com.goodideas.pixelparade;

import androidx.multidex.MultiDexApplication;

import com.goodideas.pixelparade.data.analityc.AnalyticHelper;
import com.goodideas.pixelparade.data.analityc.event.LaunchFirstTime;
import com.goodideas.pixelparade.data.analityc.event.SessionStart;

/**
 * Created by ogogorev on 13.03.18.
 */

public class App extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        AnalyticHelper.Companion.init(this);

        if (SharedPreferencesHelper.isFirstStart(this)) {
            SharedPreferencesHelper.setStartIsNotFirst(this);
            AnalyticHelper.Companion.sendEvent(new LaunchFirstTime());
        } else {
            AnalyticHelper.Companion.sendEvent(new SessionStart());
        }
    }
}
