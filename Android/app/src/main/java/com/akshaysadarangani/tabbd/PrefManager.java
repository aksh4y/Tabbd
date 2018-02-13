package com.akshaysadarangani.tabbd;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Akshay on 8/8/2017.
 */

public class PrefManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;

    // shared pref mode
    private int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "tabbd-welcome";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String IS_DEVICE_PAIRED = "IsDevicePaired";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setDevicePaired(boolean isPaired) {
        editor.putBoolean(IS_DEVICE_PAIRED, isPaired);
        editor.commit();
    }

    public boolean devicePaired() {
        return pref.getBoolean(IS_DEVICE_PAIRED, false);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }
}
