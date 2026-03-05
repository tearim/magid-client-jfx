package com.zarterstein.prefs;

import com.zarterstein.magidclientjfx.MagidClientApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PrefHelper {
    private static Preferences prefs;
    private static boolean prefsInitialized = false;
    private static void getPrefsNode() {
        prefs = Preferences.userNodeForPackage(MagidClientApplication.class);
        prefsInitialized = true;
    }
    public static String get(String name) {
        if ( !prefsInitialized) {
            getPrefsNode();
        }
        return prefs.get(name, "");
    }
    public static int getInt(String name) {
        if ( !prefsInitialized) {
            getPrefsNode();
        }
        try {
            return prefs.getInt(name, 0);
        } catch (Exception e) {
            return 0;
        }
    }
    public static long getLong(String name) {
        if ( !prefsInitialized) {
            getPrefsNode();
        }
        try {
            return prefs.getLong(name, 0);
        }  catch (Exception e) {
            return 0;
        }
    }
    public static float getFloat(String name) {
        if ( !prefsInitialized) {
            getPrefsNode();
        }
        try {
            return prefs.getFloat(name, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean getBoolean(String name) {
        if ( !prefsInitialized) {
            getPrefsNode();
        }
        try {
            return prefs.getBoolean(name, false);
        }  catch (Exception e) {
            return false;
        }
    }

    public static double getDouble(String name) {
        if ( !prefsInitialized) {
            getPrefsNode();
        }
        try {
            return prefs.getDouble(name, 0);
        }  catch (Exception e) {
            return 0;
        }
    }

    public static void set(String name, String value) {
        if ( !prefsInitialized) {
            getPrefsNode();
        }
        prefs.put(name, value);
    }
    public static void setInt(String name, int value) {
        if ( !prefsInitialized) {
            getPrefsNode();
        }
        prefs.putInt(name, value);
    }
    public static void setLong(String name, long value) {
        if ( !prefsInitialized) {
            getPrefsNode();
        }
        prefs.putLong(name, value);
    }
    public static void setFloat(String name, float value) {
        if ( !prefsInitialized) {
            getPrefsNode();
        }
        prefs.putFloat(name, value);
    }
    public static void setDouble(String name, double value) {
        if ( !prefsInitialized) {
            getPrefsNode();
        }
        prefs.putDouble(name, value);
    }
    public static void setBoolean(String name, boolean value) {
        if ( !prefsInitialized) {
            getPrefsNode();
        }
        prefs.putBoolean(name, value);
    }


    public static void remove(String name) {
        if ( !prefsInitialized) {
            getPrefsNode();
        }
        prefs.remove(name);
    }

    public static List<String> getNames() {
        if ( !prefsInitialized) {
            getPrefsNode();
        }
        try {
            return List.of(prefs.keys());
        } catch (BackingStoreException e) {
            return new ArrayList<>();
        }
    }

    public static void clearAll() {
        List<String>  prefNames = getNames();
        for (String prefName : prefNames) {
            prefs.remove(prefName);
        }
    }
}
