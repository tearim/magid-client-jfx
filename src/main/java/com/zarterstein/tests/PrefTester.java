package com.zarterstein.tests;

import com.zarterstein.prefs.PrefHelper;

public class PrefTester {
    public static void main(String[] args) {
        //PrefHelper.removePreference("setting.subsetting");
        System.out.println("I'll set a preference");
      //  PrefHelper.set("setting.subsetting", "value");
      //  PrefHelper.set("setting.subsetting1", "value1");
        System.out.println("I'll get a preference: " +  PrefHelper.get("setting.subsetting"));
        System.out.println(PrefHelper.getNames());
        //PrefHelper.clearAll();
    }
}
