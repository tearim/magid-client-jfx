module com.example.magidclientjfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires jdk.jsobject;
    requires java.json;
    requires sac;
    requires cssparser;
    requires jdk.xml.dom;
    requires java.desktop;
    requires javafx.media;
    requires javafx.graphics;
    requires jdk.jdi;
    requires java.prefs;
    requires javafx.web;
    requires javafx.base;
    requires java.logging;

    opens com.zarterstein.magidclientjfx to javafx.fxml;
    exports com.zarterstein.magidclientjfx;
}