package com.zarterstein.magidclientjfx;

import javafx.application.Application;

import java.util.logging.Level;

public class Launcher {
    public static void main(String[] args) {
       // com.sun.javafx.util.Logging.getCSSLogger().disableLogging();
        Application.launch(MagidClientApplication.class, args);
    }
}
