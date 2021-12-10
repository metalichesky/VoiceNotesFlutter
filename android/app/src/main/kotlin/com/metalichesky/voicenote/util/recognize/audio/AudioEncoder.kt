package com.metalichesky.voicenote.util.recognize.audio

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import java.nio.ByteBuffer


// Did some test to see which value would maximize our performance in the current setup
// (infinite audio pool). Measured the time it would take to write a 30 seconds video.
// Based on this, we'll go with TIMEOUT=0 for now.
// INPUT_TIMEOUT_US 10000: 46 seconds
// INPUT_TIMEOUT_US 1000: 37 seconds
// INPUT_TIMEOUT_US 100: 33 seconds
// INPUT_TIMEOUT_US 0: 32 seconds
private const val INPUT_TIMEOUT_US: Long = 0L

// 0 also seems to be the best, although it does not change so much.
// Can't go too high or this is a bottleneck for the audio encoder.
private const val OUTPUT_TIMEOUT_US: Long = 0L

private const val DRAIN_STATE_NONE = 0
private const val DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY = 1
private const val DRAIN_STATE_CONSUMED = 2
private const val AUDIO_FORMAT_PREFIX = "audio/"

/**
 * Encoder for audio
 */
internal interface AudioEncoder {

    fun setup()

    fun encode(inputArray: ByteArray, timestampUs: Long, writeEOS: Boolean = false)

    fun encode(
        inputArray: ByteArray,
        inputOffset: Int,
        inputSize: Int,
        timestampUs: Long,
        writeEOS: Boolean = false)

    fun release()
}

internal class AudioEncoderImpl(
    private val writer: AudioWriter,
    private val inputFormat: MediaFormat
) : AudioEncoder {
    companion object {
        const val LOG_TAG = "AudioEncoder"
    }
    private val outputFormat: MediaFormat = MediaFormat()
    private lateinit var encoder: MediaCodec
    private var encoderBufferInfo = MediaCodec.BufferInfo()
    private val params = AudioParams.createFrom(inputFormat)

    private var actualOutputFormat: MediaFormat? = null
    private var writeEOS: Boolean = false
    private var isEncoderEOS: Boolean = false
    private var dataToEncode: ByteArray? = null
    private var dataToEncodeTimestamp: Long = 0L
    private var dataToEncodeSize: Int = 0
    private var dataToEncodeOffset: Int = 0

    private var lastPresentationTimeUs: Long = 0L

    override fun setup() {
        outputFormat.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC)
        outputFormat.setInteger(
            MediaFormat.KEY_AAC_PROFILE,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC
        )
        outputFormat.setInteger(
            MediaFormat.KEY_SAMPLE_RATE,
            inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        )
        outputFormat.setInteger(
            MediaFormat.KEY_BIT_RATE,
            inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        )
        outputFormat.setInteger(
            MediaFormat.KEY_CHANNEL_COUNT,
            inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        )

        encoder = try {
            MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        } catch (ex: Exception) {
            throw IllegalStateException(
                "can't create codec for provided output format",
                ex
            )
        }
        encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        encoder.start()
    }


    override fun encode(
        inputArray: ByteArray,
        timestampUs: Long,
        writeEOS: Boolean
    ) {
        encode(inputArray, 0, inputArray.size, timestampUs, writeEOS)
    }

    override fun encode(
        inputArray: ByteArray,
        inputOffset: Int,
        inputSize: Int,
        timestampUs: Long,
        writeEOS: Boolean
    ) {
        dataToEncodeSize = inputSize
        dataToEncode = inputArray
        dataToEncodeOffset = inputOffset
        dataToEncodeTimestamp = timestampUs
        this.writeEOS = writeEOS

        while (dataToEncode != null && !isEncoderEOS) {
            stepEncoder()
        }
    }

    override fun release() {
        releaseData()
        try {
            encoder.flush()
            encoder.stop()
            encoder.release()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun releaseData() {
        dataToEncodeOffset = 0
        dataToEncodeSize = 0
        dataToEncode = null
        dataToEncodeTimestamp = 0L
    }

    private fun canEncode(): Boolean {
        return (!isEncoderEOS)
    }

    private fun stepEncoder() {
        feedEncoder()
        drainEncoder()
    }

    private fun feedEncoder(timeoutUs: Long = INPUT_TIMEOUT_US): Int {
        val audioParams = params ?: return DRAIN_STATE_NONE
        val dataToEncode = dataToEncode ?: return DRAIN_STATE_NONE
        val result = encoder.dequeueInputBuffer(timeoutUs)
        return if (result >= 0) {
            val inputBuffer = encoder.getInputBuffer(result) ?: return DRAIN_STATE_NONE
            val presentationTimeUs = calcTimestamp()
            val fillSize = fillInputBuffer(inputBuffer)
            val flags = if (writeEOS) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
            encoder.queueInputBuffer(result, 0, fillSize, presentationTimeUs, flags)


            DRAIN_STATE_CONSUMED
        } else {
            when (result) {
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY
                else -> DRAIN_STATE_NONE
            }
        }
    }

    private fun calcTimestamp(): Long {
        val timestamp =
            dataToEncodeTimestamp + (dataToEncodeOffset / params.bytesPerMs) * 1000L
        return timestamp
    }

    private fun fillInputBuffer(byteBuffer: ByteBuffer): Int {
        val dataToEncode = dataToEncode ?: return 0
        val availableCapacity = byteBuffer.capacity()
        val fillSize = Math.min(dataToEncodeSize, availableCapacity)
        byteBuffer.clear()
        byteBuffer.put(dataToEncode, dataToEncodeOffset, fillSize)
        this.dataToEncodeSize -= fillSize
        this.dataToEncodeOffset += fillSize

        if (dataToEncodeSize <= 0) {
            releaseData()
        }
        return fillSize
    }

    private fun drainEncoder(timeoutUs: Long = OUTPUT_TIMEOUT_US): Int {
        if (isEncoderEOS) return DRAIN_STATE_NONE

        val result = encoder.dequeueOutputBuffer(encoderBufferInfo, timeoutUs)

        return when {
            result == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                DRAIN_STATE_NONE
            }
            result == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                if (actualOutputFormat != null) {
                    throw RuntimeException("Audio output format changed twice.")
                }
                actualOutputFormat = encoder.outputFormat

                writer.setOutputFormat(actualOutputFormat)
                DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY
            }
            result == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY
            }
            result >= 0 -> {
                if (actualOutputFormat == null) {
                    throw RuntimeException("Could not determine actual output format.")
                }
                if (encoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    isEncoderEOS = true
                    encoderBufferInfo[0, 0, 0] = encoderBufferInfo.flags
                }
                if (encoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    encoder.releaseOutputBuffer(result, false)
                    return DRAIN_STATE_SHOULD_RETRY_IMMEDIATELY
                }
                val presentationTime = encoderBufferInfo.presentationTimeUs
                val encodedData = encoder.getOutputBuffer(result) ?: return DRAIN_STATE_NONE

                if (lastPresentationTimeUs < presentationTime || isEncoderEOS) {
                    lastPresentationTimeUs = presentationTime
                    writer.writeSampleData(encodedData, encoderBufferInfo)
                } else {
                    Log.d(LOG_TAG, "Out of order buffer")
                }
                encoder.releaseOutputBuffer(result, false)
                DRAIN_STATE_CONSUMED
            }
            else -> {
                DRAIN_STATE_NONE
            }
        }
    }
}