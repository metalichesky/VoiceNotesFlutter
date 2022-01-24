package com.github.olga_yakovleva.rhvoice.player;

import android.media.AudioFormat;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.TextToSpeech;

import com.github.olga_yakovleva.rhvoice.BuildConfig;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AndroidTTSPlayer extends Player {
    private SynthesisCallback callback;
    private int sampleRate;
    private boolean isStarted = false;
    private boolean isPlaying = false;

    public AndroidTTSPlayer(SynthesisCallback callback) {
        this.callback = callback;
    }

    public boolean setSampleRate(int sr) {
        if (sampleRate != 0)
            return true;
        sampleRate = sr;
        callback.start(sampleRate, AudioFormat.ENCODING_PCM_16BIT, 1);
        return true;
    }

    @Override
    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void start() {
        this.isStarted = true;
    }

    @Override
    public void stop() {
        this.isStarted = false;
    }

    @Override
    public boolean playSpeech(short[] samples) {
        if (!isStarted) {
            return false;
        }
        if (BuildConfig.DEBUG && sampleRate == 0) {
            throw new IllegalStateException();
        }
        isPlaying = true;
        boolean completed = true;
        final ByteBuffer buffer = ByteBuffer.allocate(samples.length * 2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.asShortBuffer().put(samples);
        final byte[] bytes = buffer.array();
        final int size = callback.getMaxBufferSize();
        int offset = 0;
        int count;
        while (offset < bytes.length) {
            if (!isStarted) {
                completed = false;
                break;
            }
            count = Math.min(size, bytes.length - offset);
            if (callback.audioAvailable(bytes, offset, count) != TextToSpeech.SUCCESS) {
                completed = false;
                break;
            }
            offset += count;
        }
        isPlaying = false;
        return completed;
    }

    @Override
    public void release() {
        sampleRate = 0;
    }
}