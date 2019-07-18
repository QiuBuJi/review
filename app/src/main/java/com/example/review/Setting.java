package com.example.review;

import android.content.Context;
import android.content.SharedPreferences;

public class Setting {

    static public SharedPreferences        sp;
    static public SharedPreferences.Editor edit;

    public static void init(Context context) {
        sp = context.getSharedPreferences("ActivitysData", Context.MODE_PRIVATE);
        edit = sp.edit();
    }

    public static boolean getBoolean(String extra) {
        return sp.getBoolean(extra, false);
    }

    public static int getInt(String extra) {
        return sp.getInt(extra, 0);
    }

    public static void set(String extra, boolean value) {
        edit.putBoolean(extra, value).commit();
    }

    public static void set(String extra, int value) {
        edit.putInt(extra, value).commit();
    }
}
