package com.metalichesky.voicenote.util.recognize.audio

import android.media.AudioFormat
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import java.lang.Exception
import kotlin.math.floor
import kotlin.math.pow

/**
 * Util class which contain params of audio part
 */
internal class AudioParams(
    val sampleRate: Int,
    val encoding: RawAudioEncoding,
    val channelsCount: Int
) {
    companion object {
        // The 44.1KHz frequency is the only setting guaranteed to be available on all devices.
        const val DEFAULT_SAMPLE_RATE = 44100
        // Prefer to use 16 bit samples encoding for better experience
        val DEFAULT_ENCODING = RawAudioEncoding.PCM_16BIT
        // Prefer to use stereo sound for better experience
        const val DEFAULT_CHANNELS_COUNT = 2

        fun createDefault(): AudioParams {
            return AudioParams(
                DEFAULT_SAMPLE_RATE,
                DEFAULT_ENCODING,
                DEFAULT_CHANNELS_COUNT
            )
        }

        fun createFrom(audioParams: AudioParams): AudioParams {
            return AudioParams(
                sampleRate = audioParams.sampleRate,
                channelsCount = audioParams.channelsCount,
                encoding = audioParams.encoding
            )
        }

        fun createFrom(format: MediaFormat?): AudioParams {
            val sampleRate =
                format?.getIntegerOrNull(MediaFormat.KEY_SAMPLE_RATE) ?: DEFAULT_SAMPLE_RATE
            val channelsCount =
                format?.getIntegerOrNull(MediaFormat.KEY_CHANNEL_COUNT) ?: DEFAULT_CHANNELS_COUNT

            val bitsPerSample = if (Build.VERSION.SDK_INT > 23) {
                when (format?.getIntegerOrNull(MediaFormat.KEY_PCM_ENCODING)) {
                    AudioFormat.ENCODING_PCM_8BIT -> 8
                    AudioFormat.ENCODING_PCM_16BIT -> 16
                    AudioFormat.ENCODING_PCM_FLOAT -> 32
                    else -> 16
                }
            } else {
                16
            }
            return AudioParams(
                channelsCount = channelsCount,
                sampleRate = sampleRate,
                encoding = when (bitsPerSample) {
                    8 -> RawAudioEncoding.PCM_8BIT
                    16 -> RawAudioEncoding.PCM_16BIT
                    else -> RawAudioEncoding.PCM_FLOAT
                }
            )
        }
    }

    enum class RawAudioEncoding(
        var bytePerSample: Int,
        var bitsPerSample: Int,
        val pcmEncoding: Int
    ) {
        PCM_8BIT(1, 8, AudioFormat.ENCODING_PCM_8BIT),
        PCM_16BIT(2, 16, AudioFormat.ENCODING_PCM_16BIT),
        PCM_FLOAT(4, 32, AudioFormat.ENCODING_PCM_FLOAT)
    }

    /**
     * bytes per one millisecond
     */
    val bytesPerMs: Int

    /**
     * bytes per one sample for all channels
     */
    val bytesPerFrame: Int

    /**
     * amplitude value for samples
     */
    val samplesAmplitude: Float

    /**
     * byte/sec for single channel
     */
    val byteRatePerChannel: Int

    /**
     * byte/sec for all channels
     */
    val byteRate: Int

    /**
     * bit/sec for all channels
     */
    val bitRate: Int

    val channelsFormatIn: Int
        get() = when (channelsCount) {
            1 -> AudioFormat.CHANNEL_IN_MONO
            2 -> AudioFormat.CHANNEL_IN_STEREO
            else -> AudioFormat.CHANNEL_INVALID
        }

    val channelsFormatOut: Int
        get() = when (channelsCount) {
            1 -> AudioFormat.CHANNEL_OUT_MONO
            2 -> AudioFormat.CHANNEL_OUT_STEREO
            4 -> AudioFormat.CHANNEL_OUT_QUAD
            6 -> AudioFormat.CHANNEL_OUT_5POINT1
            8 -> AudioFormat.CHANNEL_OUT_7POINT1_SURROUND
            else -> AudioFormat.CHANNEL_INVALID
        }

    init {
        this.bytesPerFrame = channelsCount * encoding.bytePerSample
        this.byteRatePerChannel = sampleRate * encoding.bytePerSample
        this.byteRate = this.byteRatePerChannel * channelsCount
        this.bitRate = this.byteRate * 8
        this.bytesPerMs = floor(byteRate / 1000f).toInt()
        val maxUnsignedNumber = (2.0.pow(encoding.bytePerSample.toDouble() * 8.0).toFloat())
        this.samplesAmplitude = maxUnsignedNumber / 2f - 1f
    }

    fun copy(): AudioParams {
        return createFrom(this)
    }
}


fun MediaFormat.getIntegerOrNull(key: String): Int? {
    return try {
        getInteger(key)
    } catch (ex: Exception) {
        null
    }
}

fun MediaExtractor.findFirstTrack(trackType: String): Int {
    var trackIdx = -1
    for (i in 0 until trackCount) {
        val mediaFormat = getTrackFormat(i)
        val mimeType = mediaFormat.getString(MediaFormat.KEY_MIME) ?: continue
        if (mimeType.startsWith(trackType)) {
            trackIdx = i
        }
    }
    return trackIdx
}