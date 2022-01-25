package com.github.olga_yakovleva.rhvoice.player;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.github.olga_yakovleva.rhvoice.BuildConfig;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioTrackPlayer extends Player {
    private static final String LOG_TAG = AudioTrackPlayer.class.getSimpleName();
    private int sampleRate = 24000;
    private int channelCount = 1;
    private int androidEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private boolean isStarted = false;
    private boolean isPlaying = false;
    private AudioTrack audioTrack;
    private byte[] dataBytes;


    public AudioTrackPlayer() {
        audioTrack = createAudioTrack();
        if (audioTrack != null) {
            dataBytes = createDataBytes(getMinBufferSize() * 2);
        }
    }

    private int getMinBufferSize() {
        int channelMask = getChannelsMask(channelCount);
        return AudioTrack.getMinBufferSize(
                sampleRate, channelMask, androidEncoding
        );
    }


    private int getChannelsMask(int channelsCount) {
        int channelMask = 0;
        switch (channelsCount) {
            case 1:
                channelMask = AudioFormat.CHANNEL_OUT_MONO;
                break;
            case 2:
                channelMask = AudioFormat.CHANNEL_OUT_STEREO;
                break;
            default:
                channelMask = AudioFormat.CHANNEL_OUT_QUAD;

        }
        return channelMask;
    }

    private AudioTrack createAudioTrack() {
        if (sampleRate == 0) {
            return null;
        }
        int bufferSize = getMinBufferSize();
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();
        AudioFormat format = new AudioFormat.Builder()
                .setEncoding(androidEncoding)
                .setSampleRate(sampleRate)
                .setChannelMask(getChannelsMask(channelCount))
                .build();
        AudioTrack audioTrack = new AudioTrack(
                audioAttributes,
                format,
                bufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
        );
        if (audioTrack == null) {
            if (BuildConfig.DEBUG) {
                Log.e(LOG_TAG, "audio track is not initialized");
            }
            return null;
        }
        if (BuildConfig.DEBUG) {
            Log.d(
                    LOG_TAG,
                    "Bytes per ms = ${audioParams.bytesPerMs} buffer size = ${byteData?.limit()}"
            );
        }
        return audioTrack;
    }

    private byte[] createDataBytes(int size) {
        return new byte[size];
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public void start() {
        if (!isStarted) {
            this.isStarted = true;
            if (audioTrack == null) {
                audioTrack = createAudioTrack();
            }
            if (audioTrack != null) {
                if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack.play();
                }
            }
        }
    }

    @Override
    public void stop() {
        if (isStarted) {
            this.isStarted = false;
            if (audioTrack != null) {
                if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack.pause();
                    audioTrack.flush();
                }
            }
        }
    }

    @Override
    public void pause() {
        if (isStarted) {
            isPlaying = false;
        }
    }

    @Override
    public void play() {
        if (isStarted) {
            isPlaying = true;
        }
    }

    @Override
    public boolean setSampleRate(int sr) {
        if (sampleRate != 0 && sampleRate == sr) {
            return true;
        }
        sampleRate = sr;
        if (audioTrack == null) {
            audioTrack = createAudioTrack();
        }
        if (dataBytes == null) {
            dataBytes = createDataBytes(getMinBufferSize() * 2);
        }
        return true;
    }


    @Override
    public boolean playSpeech(short[] samples) {
        if (!isStarted) {
            return false;
        }
        if (BuildConfig.DEBUG && sampleRate == 0) {
            throw new IllegalStateException();
        }
        if (audioTrack == null) {
            audioTrack = createAudioTrack();
        }
        if (audioTrack == null) {
            return false;
        } else {
            if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.play();
            }
        }
        isPlaying = true;
        boolean completed = true;
        int totalBytesCount = samples.length * 2;
        final ByteBuffer buffer = ByteBuffer.allocate(totalBytesCount);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.asShortBuffer().put(samples);
        if (dataBytes == null || dataBytes.length < totalBytesCount) {
            dataBytes = createDataBytes(totalBytesCount);
        }
        int offset = 0;
        int length = 0;
        int count = 0;
        while (offset < totalBytesCount) {
            if (!isStarted) {
                completed = false;
                break;
            }
            if(!isPlaying) {
                // paused
                try {
                    Thread.sleep(30L);
                } catch (InterruptedException ex) {
                    completed = false;
                    break;
                }
                continue;
            }
            length = Math.min(totalBytesCount, dataBytes.length) - offset;
            buffer.get(dataBytes, offset, length);
            count = playSamples(dataBytes, offset, length);
            if (count < 0) {
                completed = false;
                break;
            }
            offset += count;
        }
        isPlaying = false;
        return completed;
    }

    private int playSamples(byte[] byteArray, int offset, int length) {
        if (audioTrack != null) {
            return audioTrack.write(byteArray, offset, length);
        } else {
            return -1;
        }
    }

    @Override
    public void release() {
        if (audioTrack != null) {
            if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.stop();
            }
            audioTrack.release();
            audioTrack = null;
        }
    }
}