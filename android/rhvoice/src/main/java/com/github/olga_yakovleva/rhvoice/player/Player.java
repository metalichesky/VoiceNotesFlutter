package com.github.olga_yakovleva.rhvoice.player;

import com.github.olga_yakovleva.rhvoice.TTSClient;

public abstract class Player implements TTSClient {
    public abstract boolean isStarted();

    public abstract void start();

    public abstract void stop();

    public abstract boolean isPlaying();

    public abstract void release();
}
