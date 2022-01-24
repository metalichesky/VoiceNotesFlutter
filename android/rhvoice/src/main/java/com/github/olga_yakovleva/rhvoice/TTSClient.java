package com.github.olga_yakovleva.rhvoice;

public interface TTSClient {
    boolean playSpeech(short[] samples);

    boolean setSampleRate(int sampleRate);
}
