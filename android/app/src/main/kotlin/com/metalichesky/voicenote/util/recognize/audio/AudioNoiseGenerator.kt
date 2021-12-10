package com.metalichesky.voicenote.util.recognize.audio

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import java.util.*
import kotlin.math.sin


/**
 * An AudioNoise instance offers buffers of noise that we can use when recording
 * some samples failed for some reason.
 *
 * Since we can't create noise anytime it's needed - that would be expensive and
 * slow down the recording thread - we create a big noise buffer at start time.
 *
 * We'd like to work with [ShortBuffer]s, but this requires converting the
 * input buffer to ShortBuffer each time, and this can be expensive.
 */
internal class AudioNoiseGenerator(
    val audioParams: AudioParams,
    val frameSize: Int = audioParams.channelsCount,
    val framesCount: Int = DEFAULT_FRAMES_COUNT,
    val noiseVolume: Float = DEFAULT_NOISE_VOLUME
) {
    companion object {
        const val DEFAULT_NOISE_VOLUME = 0.1f
        const val DEFAULT_FRAMES_COUNT = 1
        private val RANDOM = Random()
    }

    private val audioDataTransformer = AudioDataTransformer(audioParams)

    private val noiseBuffer: ByteBuffer = ByteBuffer
        .allocateDirect(frameSize * framesCount)
        .order(ByteOrder.nativeOrder())

    init {
        // each X samples, the signal repeats
        val frequency = frameSize / 2.0
        // the increase in radians
        val step = (Math.PI / frequency).toFloat()
        var stepIdx = 0f
        val max = noiseVolume * audioParams.samplesAmplitude
        channelsLoop@ while (noiseBuffer.hasRemaining()) {
            stepIdx++
            val noise = generateNoise(stepIdx, step, max)
            val noiseByteArray = audioDataTransformer.toByteArray(noise)
            for (channelIdx in 0 until audioParams.channelsCount) {
                for (noiseByte in noiseByteArray) {
                    if (noiseBuffer.hasRemaining()) {
                        noiseBuffer.put(noiseByte)
                    } else {
                        break@channelsLoop
                    }
                }
            }
        }
        noiseBuffer.rewind()
    }

    private fun generateNoise(stepIdx: Float, step: Float, max: Float): Float {
        return sin(stepIdx * step) * max
    }

    fun fill(outBuffer: ByteBuffer) {
        noiseBuffer.clear()
        if (noiseBuffer.capacity() == outBuffer.remaining()) {
            noiseBuffer.position(0) // Happens if FRAMES = 1.
        } else {
            noiseBuffer.position(
                RANDOM.nextInt(noiseBuffer.capacity() - outBuffer.remaining())
            )
        }
        noiseBuffer.limit(noiseBuffer.position() + outBuffer.remaining())
        outBuffer.put(noiseBuffer)
    }
}
