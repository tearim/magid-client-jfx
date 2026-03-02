package com.zarterstein.maqshiv.utils;

import com.zarterstein.magidclientjfx.Launcher;

import java.io.InputStream;
import java.net.URL;

public class PathToInputStream {
    private PathToInputStream() {

    }
    private static boolean isURL(String path) {
        try {
            new URL(path);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public static InputStream get(String fileName) {
        if ( isURL(fileName) ) {
            try {
                URL url = new URL(fileName);
                return url.openStream();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
        ClassLoader classLoader = Launcher.class.getClassLoader();
        return classLoader.getResourceAsStream(fileName);
    }
}
