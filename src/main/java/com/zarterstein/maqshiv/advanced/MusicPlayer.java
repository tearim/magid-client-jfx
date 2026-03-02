package com.zarterstein.maqshiv.advanced;

import javafx.scene.media.AudioClip;
import java.io.File;

public class MusicPlayer {
    private float volume;
    private String fileName;
    private File file;
    private String status;


    public String getFileName() {
        return fileName;
    }

    public MusicPlayer setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public float getVolume() {
        return volume;
    }

    public MusicPlayer setVolume(float volume) {
        this.volume = volume;
        return this;
    }

    public File getFile() {
        return file;
    }

    public MusicPlayer setFile(File file) {
        this.file = file;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public MusicPlayer setStatus(String status) {
        this.status = status;
        return this;
    }


    public MusicPlayer(float volume, String fileName) {
        this.volume = volume;
        this.fileName = fileName;
    }

    public void play() {
        AudioClip audioClip = new AudioClip(fileName);
        audioClip.setVolume(volume);
        audioClip.play();
    }



}
