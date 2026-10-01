package com.dastbigharar.focus;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public final class Prefs {
    private static final String NAME = "focus_prefs";
    private Prefs() {}
    public static SharedPreferences p(Context c){ return c.getSharedPreferences(NAME, Context.MODE_PRIVATE); }
    public static Set<String> getSelected(Context c){ return new HashSet<>(p(c).getStringSet("selected", new HashSet<>())); }
    public static void setSelected(Context c, Set<String> s){ p(c).edit().putStringSet("selected", new HashSet<>(s)).apply(); }
    public static long end(Context c){ return p(c).getLong("end",0L); }
    public static boolean active(Context c){ return p(c).getBoolean("active",false) && end(c) > System.currentTimeMillis(); }
    public static void start(Context c,long end){ p(c).edit().putLong("end",end).putBoolean("active",true).apply(); }
    public static void stop(Context c){ p(c).edit().putBoolean("active",false).putLong("end",0L).apply(); }
}
