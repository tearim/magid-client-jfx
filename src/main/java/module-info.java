module com.example.magidclientjfx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires jdk.jsobject;
    requires java.json;
    requires sac;
    requires cssparser;
    requires jdk.xml.dom;

    opens com.zarterstein.magidclientjfx to javafx.fxml;
    exports com.zarterstein.magidclientjfx;
}