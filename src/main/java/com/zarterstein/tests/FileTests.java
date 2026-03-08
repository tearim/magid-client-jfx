package com.zarterstein.tests;

import javafx.scene.image.Image;

public class FileTests {
    public static void main(String[] args) {
        try {
            Image img = new Image("http://127.0.0.1:8090/bundle/images/dark-sea-ai-gen.png", true);
            img.errorProperty().addListener((obs, oldV, newV) -> {
                if (newV) img.getException().printStackTrace();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
