package com.metalichesky.voicenote.util.recognize.audio


/**
 * Audio configuration to be passed as input to the constructor
 * of an [AudioMediaEncoder].
 * Prefer to use precalculated immutable values here to speed up audio recording process.
 */
internal class AudioRecorderConfig(
    // Configurable options
    val audioParams: AudioParams = AudioParams.createDefault(),
    val mimeType: String = MIME_TYPE,
    val codecName: String? = null,
    val targetBitRate: Int = 0 // ENCODED bit rate
) {
    companion object {
        const val SAMPLES_PER_FRAME = 1024
        const val RECORD_BUFFER_FRAMES_COUNT = 50
        const val RECORD_BUFFER_POOL_SIZE = 500
        const val SPEED = 1.0
        const val MIME_TYPE = "audio/mp4a-latm"
    }

    // Not configurable options (for now)
    val pcmEncoding: Int // Determines the sampleSizePerChannel
        get() = audioParams.encoding.pcmEncoding

    val sampleRate: Int
        get() = audioParams.sampleRate // samples/sec

    val channelsCount: Int
        get() = audioParams.channelsCount

    /**
     * byte/sample for single channel
     */
    val bytePerSample: Int
        get() = audioParams.encoding.bytePerSample

    /**
     * byte/sec for single channel
     */
    val byteRatePerChannel: Int
        get() = audioParams.byteRatePerChannel

    /**
     * byte/sec for all channels
     */
    val byteRate: Int
        get() = audioParams.byteRate

    /**
     * bit/sec for all channels
     */
    val bitRate: Int
        get() = audioParams.bitRate

    /**
     * channels format for input sound
     */
    val channelsFormatIn: Int
        get() = audioParams.channelsFormatIn

    /**
     * We call FRAME here the chunk of data that we want to read at each loop cycle.
     *
     * When this number is HIGH, the AudioRecord might be unable to keep a good pace and
     * we might end up skip some frames.
     *
     * When this number is LOW, we pull a bigger number of frames and this might end up
     * delaying our recorder/encoder balance (more frames means more encoding operations).
     * In the end, this means that the recorder will skip some frames to restore the balance.
     *
     * Only even values acceptable
     *
     * @return the frame size
     */
    val frameSize: Int = (SAMPLES_PER_FRAME * channelsCount) and (0x1).inv()

    /**
     * Number of frames contained in the [android.media.AudioRecord] buffer.
     * In theory, the higher this value is, the safer it is to delay reading as the
     * audioRecord will hold the recorded samples anyway and return to us next time we read.
     *
     * Should be coordinated with [.frameSize].
     *
     * @return the number of frames
     */
    val audioRecordBufferFrames: Int = RECORD_BUFFER_FRAMES_COUNT

    /**
     * We allocate buffers of [.frameSize] each, which is not much.
     *
     * This value indicates the maximum number of these buffers that we can allocate at a given
     * instant. This value is the number of runnables that the encoder thread is allowed to be
     * 'behind' the recorder thread. It's not safe to have it very large or we can end encoding
     * A LOT AFTER the actual recording. It's better to reduce this and skip recording at all.
     *
     * Should be coordinated with [.frameSize].
     *
     * @return the buffer pool max size
     */
    val bufferPoolMaxSize: Int = RECORD_BUFFER_POOL_SIZE

    fun copy(): AudioRecorderConfig {
        val config = AudioRecorderConfig(
            audioParams = audioParams.copy(),
            targetBitRate = this.targetBitRate,
            codecName = this.codecName,
            mimeType = this.mimeType
        )
        return config
    }
}
