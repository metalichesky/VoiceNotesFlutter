package com.metalichesky.voicenote.util.recognize.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.metalichesky.voicenote.util.pool.ByteBufferPool
import com.metalichesky.voicenote.util.pool.InputBuffer
import com.metalichesky.voicenote.util.pool.InputBufferPool
import com.metalichesky.voicenote.util.recognize.*
import com.metalichesky.voicenote.util.recognize.RecognizeListener
import kotlinx.coroutines.suspendCancellableCoroutine
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.StorageService
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue

internal class AudioRecognizeRecorder(
    val context: Context,
    val config: AudioRecorderConfig,
    val params: RecognizeParams,
    val outputFile: String
) : Recorder(AudioRecognizeRecorder.javaClass.simpleName) {
    companion object {
        private val LOG_TAG = AudioRecognizeRecorder::class.java.simpleName
        private val PERFORMANCE_DEBUG = false
        private val PERFORMANCE_FILL_GAPS = true
        private val PERFORMANCE_MAX_GAPS = 8
    }

    private var isPauseRequested = false
    private var isStopRequested = false

    private var mEncoder: AudioEncodingThread? = null
    private var mRecorder: AudioRecordingThread? = null
    private var mRecognizer: AudioRecognitionThread? = null
    private var mainHandler = Handler(Looper.getMainLooper())

    private val mTimestamp: AudioTimestamp = AudioTimestamp(config.byteRate)

    val encodedBitRate: Int get() = config.bitRate
    private var mRecordByteBufferPool: ByteBufferPool? = null

    private val mRecognizerBufferPool: InputBufferPool = InputBufferPool()
    private val mRecognizerBufferQueue: LinkedBlockingQueue<InputBuffer> =
        LinkedBlockingQueue<InputBuffer>()
    private val mEncoderBufferPool: InputBufferPool = InputBufferPool()
    private val mEncoderBufferQueue: LinkedBlockingQueue<InputBuffer> =
        LinkedBlockingQueue<InputBuffer>()

    private var audioNoiseGenerator: AudioNoiseGenerator = AudioNoiseGenerator(config.audioParams, config.frameSize)
    private var recognizeListener: RecognizeListener? = null

    fun setRecognizeListener(recognizeListener: RecognizeListener?) {
        this.recognizeListener = recognizeListener
    }

    override suspend fun onPrepare(callback: Callback, maxLengthUs: Long) {
        mRecordByteBufferPool = ByteBufferPool(config.frameSize, config.bufferPoolMaxSize)
        val model = suspendCancellableCoroutine<Model?> { continuation ->
            StorageService.unpack(context, "model-${params.locale}", PATH_MODEL_DIR,
                { model: Model ->
                    Log.d(LOG_TAG, "initModel: model initialized")
                    continuation.resumeWith(Result.success(model))
                },
                { exception: IOException ->
                    Log.e(LOG_TAG, "initModel: model error initialization", exception)
                    continuation.resumeWith(Result.success(null))
                })
        }
        mRecorder = AudioRecordingThread()
        mEncoder = AudioEncodingThread()
        if (model != null) {
            mRecognizer = AudioRecognitionThread(model)
        }
        onPrepared()
    }

    override fun onStart() {
        isStopRequested = false
        if (mRecorder?.isAlive != true) {
            mRecorder?.start()
        }
        if (mRecognizer?.isAlive != true) {
            mRecognizer?.start()
        }
        if (mEncoder?.isAlive != true) {
            mEncoder?.start()
        }
        onStarted()
    }

    override fun onPause() {
        isPauseRequested = true
        onPaused()
    }

    override fun onStop() {
        isStopRequested = true
        onStopped()
    }

    override fun onStopped() {
        isStopRequested = false
        mEncoder = null
        mRecognizer = null
        mRecorder = null
        mRecordByteBufferPool?.clear()
        mRecordByteBufferPool = null
    }

    /**
     * Sleeps for some frames duration, to skip them. This can be used to slow down
     * the recording operation to balance it with encoding.
     */
    private fun skipFrames(frames: Int) {
        try {
            Thread.sleep(
                AudioTimestamp.bytesToMillis(
                    config.frameSize * frames.toLong(),
                    config.byteRate
                )
            )
        } catch (ignore: InterruptedException) {
        }
    }

    /**
     * A thread recording from microphone using [AudioRecord] class.
     * Communicates with [AudioRecognitionThread] using [.mRecordedBufferQueue].
     */
    @SuppressLint("MissingPermission")
    private inner class AudioRecordingThread : Thread() {
        private var audioRecord: AudioRecord? = null

        private var currentByteBuffer: ByteBuffer? = null
        private var currentReadBytes: Int = 0
        private var lastTimeUs: Long = 0L
        private var firstTimeUs = Long.MIN_VALUE

        init {
            priority = MAX_PRIORITY
            createAudioRecorder()
        }

        private fun createAudioRecorder() {
            if (audioRecord != null) {
                releaseAudioRecorder()
            }
            val minBufferSize = AudioRecord.getMinBufferSize(
                config.sampleRate,
                config.channelsFormatIn,
                config.audioParams.encoding.pcmEncoding
            )
            // Make this bigger so we don't skip frames. 25: Stereo: 51200. Mono: 25600
            // 25 is quite big already. Tried to make it bigger to solve the read() delay
            // but it just makes things worse (ruins MONO as well).
            // Tried to make it smaller and things change as well.
            var bufferSize = config.frameSize * config.audioRecordBufferFrames
            while (bufferSize < minBufferSize) {
                bufferSize += config.frameSize // Unlikely.
            }
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.CAMCORDER,
                config.sampleRate,
                config.channelsFormatIn,
                config.pcmEncoding,
                bufferSize
            )
        }

        private fun releaseAudioRecorder() {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        }

        override fun run() {
            if (audioRecord == null) {
                createAudioRecorder()
            }

            audioRecord?.startRecording()
            while (!isStopRequested) {
                while (isPauseRequested && !isStopRequested) {
                    // stuck here on pause
                }
                if (!hasReachedMaxLength()) {
                    read(false)
                } else {
                    // We have reached the max length, so stop reading.
                    // However, do not get out of the loop - the controller
                    // will call stop() on us soon. It's not our responsibility
                    // to stop ourselves.
                    continue
                }
            }
            Log.w(LOG_TAG, "Stop was requested. We're out of the loop. Will post an endOfStream.")
            // Last input with 0 length. This will signal the endOfStream.
            // Can't use drain(true); it is only available when writing to the codec InputSurface.
            var didReadEos = false
            while (!didReadEos) {
                didReadEos = read(true)
            }

            releaseAudioRecorder()
        }

        /**
         * Returns true if we found a buffer and could proceed, false if we found no buffer
         * so the operation should be performed again by the caller.
         * @param endOfStream true if last read
         * @return true if proceeded
         */
        private fun read(endOfStream: Boolean): Boolean {
            currentByteBuffer = mRecordByteBufferPool?.get()
            val currentByteBuffer = currentByteBuffer
            if (currentByteBuffer == null) {
                // This can happen and it means that encoding is slow with respect to recording.
                // One might be tempted to fix precisely the next frame presentation time when
                // this happens, but this is not needed because the current increaseTime()
                // algorithm will consider delays when they get large.
                // Sleeping before returning is a good way of balancing the two operations.
                // However, if endOfStream, we CAN'T lose this frame!
                if (endOfStream) {
                    Log.v(LOG_TAG, "read thread - eos: true - No buffer, retrying.")
                } else {
                    Log.w(
                        LOG_TAG,
                        "read thread - eos: false - Skipping audio frame, encoding is too slow."
                    )
                    skipFrames(6) // sleep a bit
                }
                return false
            } else {
                currentByteBuffer.clear()
                // When stereo, we read twice the data here and AudioRecord will fill the buffer
                // with left and right bytes. https://stackoverflow.com/q/20594750/4288782
                if (PERFORMANCE_DEBUG) {
                    val before = System.nanoTime()
                    currentReadBytes = audioRecord?.read(currentByteBuffer, config.frameSize) ?: 0
                    val after = System.nanoTime()
                    val delayMillis = (after - before) / 1000000f
                    val durationMillis = AudioTimestamp.bytesToMillis(
                        currentReadBytes.toLong(),
                        config.byteRate
                    )
                    Log.v(
                        LOG_TAG,
                        "read thread - reading took: $delayMillis should be: $durationMillis delay: ${delayMillis - durationMillis}"
                    )
                } else {
                    currentReadBytes = audioRecord?.read(currentByteBuffer, config.frameSize) ?: 0
                }
                Log.v(
                    LOG_TAG,
                    "read thread - eos: $endOfStream - Read new audio frame. Bytes: $currentReadBytes"
                )

                if (currentReadBytes > 0) { // Good read: increase PTS.
                    increaseTime(currentReadBytes, endOfStream)
                    Log.v(LOG_TAG, "read thread - eos:$endOfStream - mLastTimeUs:$lastTimeUs")
                    currentByteBuffer.limit(currentReadBytes)
                    enqueue(currentByteBuffer, lastTimeUs, endOfStream)
                } else if (currentReadBytes == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(
                        LOG_TAG,
                        "read thread - eos:$endOfStream - Got AudioRecord.ERROR_INVALID_OPERATION"
                    )
                } else if (currentReadBytes == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(
                        LOG_TAG,
                        "read thread - eos:$endOfStream - Got AudioRecord.ERROR_BAD_VALUE"
                    )
                }
                return true
            }
        }

        /**
         * Increases presentation time and checks for max length constraint. This is much faster
         * then waiting for the encoder to check it during [.drainOutput]. We
         * want to catch this as soon as possible so we stop recording useless frames and bother
         * all the threads involved.
         * @param readBytes bytes read in last reading
         * @param endOfStream end of stream?
         */
        private fun increaseTime(readBytes: Int, endOfStream: Boolean) {
            // Get the latest frame timestamp.
            lastTimeUs = mTimestamp.increaseUs(readBytes)
            if (firstTimeUs == Long.MIN_VALUE) {
                firstTimeUs = lastTimeUs
                // Compute the first frame milliseconds as well.
                notifyFirstFrameMillis(
                    System.currentTimeMillis() -
                            AudioTimestamp.bytesToMillis(
                                readBytes.toLong(),
                                config.byteRate
                            )
                )
            }

            // See if we reached the max length value.
            if (!hasReachedMaxLength()) {
                val didReachMaxLength: Boolean = (lastTimeUs - firstTimeUs) > maxLengthUs
                if (didReachMaxLength && !endOfStream) {
                    Log.w(
                        LOG_TAG,
                        "read thread - this frame reached the maxLength! deltaUs: ${lastTimeUs - firstTimeUs}"
                    )
                    notifyMaxLengthReached()
                }
            }

            // Maybe add noise.
            maybeAddNoise()
        }

        private fun enqueue(
            byteBuffer: ByteBuffer,
            timestampUs: Long,
            isEndOfStream: Boolean
        ) {
            // byteBuffer.position() here is 0
            // byteBuffer.limit() here is currentReadBytes
            // so read bytes count, or data length is byteBuffer.remaining()
            val readBytes = byteBuffer.remaining()
            mRecognizerBufferPool.get()?.let { inputBuffer ->
                inputBuffer.sourceBuffer = byteBuffer
                inputBuffer.firstTimestampUs = firstTimeUs
                inputBuffer.timestampUs = timestampUs
                inputBuffer.sourceLength = readBytes
                inputBuffer.isEndOfStream = isEndOfStream
                mRecognizerBufferQueue.add(inputBuffer)
            }
            mEncoderBufferPool.get()?.let { inputBuffer ->
                inputBuffer.sourceBuffer = byteBuffer
                inputBuffer.firstTimestampUs = firstTimeUs
                inputBuffer.timestampUs = timestampUs
                inputBuffer.sourceLength = readBytes
                inputBuffer.isEndOfStream = isEndOfStream
                mEncoderBufferQueue.add(inputBuffer)
            }
        }

        /**
         * If our [AudioTimestamp] detected huge gap, and the performance flag is enabled,
         * we can add noise to fill them.
         *
         * Even if we always pass the correct timestamps, if there are big gaps between the frames,
         * the encoder implementation might shrink all timestamps to have a continuous audio.
         * This results in a video that is fast-forwarded.
         *
         * Adding noise does not solve the gaps issue, we'll still have distorted audio, but
         * at least we get a video that has the correct playback speed.
         *
         * NOTE: this MUST be fast!
         * If this operation is slow, we make the [AudioRecordingThread] busy, so we'll
         * read the next frame with a delay, so we'll have even more gaps at the next call
         * and spend even more time here. The result might be recording no audio at all - just
         * random noise.
         * This is the reason why we have a [.PERFORMANCE_MAX_GAPS] number.
         */
        private fun maybeAddNoise() {
            if (!PERFORMANCE_FILL_GAPS) return
            val gaps: Int = mTimestamp.getGapCount(config.frameSize)
            if (gaps <= 0) return
            var gapStart: Long = mTimestamp.getGapStartUs(lastTimeUs)
            val frameUs: Long = AudioTimestamp.bytesToUs(
                config.frameSize.toLong(),
                config.byteRate
            )
            Log.w(
                LOG_TAG,
                "read thread - GAPS: trying to add $gaps noise buffers. PERFORMANCE_MAX_GAPS:$PERFORMANCE_MAX_GAPS"
            )
            for (i in 0 until Math.min(gaps, PERFORMANCE_MAX_GAPS)) {
                val noiseBuffer = mRecordByteBufferPool?.get()
                if (noiseBuffer == null) {
                    Log.e(LOG_TAG, "read thread - GAPS: aborting because we have no free buffer.")
                    break
                }
                noiseBuffer.clear()
                audioNoiseGenerator?.fill(noiseBuffer)
                noiseBuffer.rewind()
                enqueue(noiseBuffer, gapStart, false)
                gapStart += frameUs
            }
        }
    }

    /**
     * A thread that performs recognize the microphone data using Vosk API.
     * Communicates with [AudioRecordingThread] using [.mRecordedBufferQueue].
     * We want to do this operation on a different thread than the recording and encoding one
     * (to avoid losing frames while we're working here)
     */
    internal inner class AudioRecognitionThread(
        val model: Model
    ) : Thread() {
        private var recognizer: Recognizer? = null
        private var inputDataArray: ByteArray = ByteArray(0)

        init {
            createRecognizer()
        }

        fun createRecognizer() {
            if (recognizer != null) {
                releaseRecognizer()
            }
            recognizer = Recognizer(model, config.sampleRate.toFloat())
        }

        fun releaseRecognizer() {
            recognizer?.close()
            recognizer = null
        }

        override fun run() {
            if (recognizer == null) {
                createRecognizer()
            }
            val recognizer =
                recognizer ?: throw IllegalStateException("recognizer must not be null here")

            interpolating@ while (!isInterrupted) {
                if (mRecognizerBufferQueue.isEmpty()) {
                    skipFrames(1)
                } else {
                    Log.v(
                        LOG_TAG,
                        "recognize thread - performing ${mRecognizerBufferQueue.size} pending operations."
                    )
                    var inputBuffer: InputBuffer? = mRecognizerBufferQueue.peek()
                    while (inputBuffer != null) {
                        var outputBuffer = mRecognizerBufferPool.get()
                        while (outputBuffer == null) {
                            skipFrames(1)
                            outputBuffer = mRecognizerBufferPool.get()
                        }

                        val inputDataBuffer = inputBuffer.sourceBuffer ?: break
                        if (inputDataBuffer.capacity() > inputDataArray.size) {
                            inputDataArray = ByteArray(inputDataBuffer.capacity())
                        }
                        //recognize here
                        var result: String
                        if (recognizer.acceptWaveForm(inputDataArray, inputDataBuffer.remaining())) {
                            result = recognizer.result
                        } else {
                            result = recognizer.partialResult
                        }
                        val recognizeResult = RecognizeResult.fromVoskApiResult(result)
                        mainHandler.post {
                            recognizeListener?.onRecognized(recognizeResult)
                        }
                        mRecognizerBufferQueue.remove(inputBuffer)
                        mRecognizerBufferPool.recycle(inputBuffer)
                        if (inputBuffer.isEndOfStream) {
                            break@interpolating
                        }
                        inputBuffer = mRecognizerBufferQueue.peek()
                    }
                }
            }
            // We got an end of stream.
            mRecognizerBufferPool.clear()
            mRecognizerBufferQueue.clear()
            releaseRecognizer()
        }
    }

    /**
     * A thread encoding the microphone data using the media encoder APIs.
     * Communicates with [AudioRecordingThread] using [.mInputBufferQueue].
     *
     * We want to do this operation on a different thread than the recording one (to avoid
     * losing frames while we're working here), and different than the [Recorder]
     * own thread (we want that to be reactive - stop() must become onStop() soon).
     */
    internal inner class AudioEncodingThread : Thread() {
        private var inputDataArray: ByteArray = ByteArray(0)
        private var audioEncoder: AudioEncoder? = null
        private var audioWriter: AudioWriter? = null

        init {
            createAudioEncoder()
        }

        private fun createAudioEncoder() {
            if (audioEncoder != null) {
                releaseAudioEncoder()
            }
            val audioFormat = MediaFormat.createAudioFormat(
                config.mimeType,
                config.sampleRate,
                config.channelsCount
            )
            audioFormat.setInteger(
                MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC
            )
            audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, config.channelsFormatIn)
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, config.bitRate)

            val audioWriter = AudioWriter(File(outputFile))
            audioWriter.setup()
            val audioEncoder = AudioEncoderImpl(audioWriter, audioFormat)
            audioEncoder.setup()
            this.audioWriter = audioWriter
            this.audioEncoder = audioEncoder
        }

        private fun releaseAudioEncoder() {
            audioEncoder?.release()
            audioEncoder = null
            audioWriter?.release()
            audioWriter = null
        }

        override fun run() {
            if (audioEncoder == null) {
                createAudioEncoder()
            }

            encoding@ while (!isInterrupted) {
                if (mEncoderBufferQueue.isEmpty()) {
                    skipFrames(3)
                } else {
                    var inputBuffer: InputBuffer? = null
                    while (inputBuffer == null) {
                        inputBuffer = mEncoderBufferQueue.peek()
                    }
                    Log.v(
                        LOG_TAG,
                        "encoding thread - performing ${mEncoderBufferQueue.size} pending operations."
                    )
                    val eos = inputBuffer.isEndOfStream
                    val timestampUs = inputBuffer.timestampUs
                    val inputDataBuffer = inputBuffer.sourceBuffer
                    if (inputDataBuffer != null) {
                        if (inputDataArray.size < inputDataBuffer.capacity()) {
                            inputDataArray = ByteArray(inputDataBuffer.capacity())
                        }
                        val dataSize = inputDataBuffer.remaining()
                        val dataOffset = inputDataBuffer.position()
                        inputDataBuffer.get(inputDataArray, dataOffset, dataSize)
                        audioEncoder?.encode(inputDataArray, dataOffset, dataSize, timestampUs, eos)
                    }

                    mEncoderBufferQueue.remove(inputBuffer)
                    mEncoderBufferPool.recycle(inputBuffer)
                    if (inputDataBuffer != null && inputDataBuffer.remaining() == 0) {
                        // all data from input buffer was written, free that buffer
                        mRecordByteBufferPool?.recycle(inputDataBuffer)
                    }
                    if (eos) {
                        audioWriter?.stop()
                        break@encoding
                    }
                }
            }
            // We got an end of stream.
            mEncoderBufferQueue.clear()
            mEncoderBufferPool.clear()
            releaseAudioEncoder()
        }
    }
}
