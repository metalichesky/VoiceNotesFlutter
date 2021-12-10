package com.metalichesky.voicenote.util.recognize.audio

import java.nio.ByteBuffer
import kotlin.math.floor

internal class AudioInterpolator(
    initAudioParams: AudioParams = AudioParams.createDefault()
) {
    var audioParams: AudioParams = initAudioParams
        set(value) {
            field = value
            dataTransformer = AudioDataTransformer(value)
        }
    private var dataTransformer: AudioDataTransformer = AudioDataTransformer(audioParams)

    fun interpolate(inputBuffer: ByteBuffer, outputBuffer: ByteBuffer) {
        when {
            inputBuffer.remaining() == outputBuffer.remaining() -> {
                outputBuffer.put(inputBuffer)
                outputBuffer.rewind()
                inputBuffer.rewind()
            }
            audioParams.encoding == AudioParams.RawAudioEncoding.PCM_16BIT -> {
                interpolateShortArray(inputBuffer, outputBuffer)
            }
            audioParams.encoding == AudioParams.RawAudioEncoding.PCM_8BIT -> {
                interpolateByteArray(inputBuffer, outputBuffer)
            }
            else -> {
                TODO()
            }
        }
    }

    fun interpolate(inputArray: ByteArray, outputArray: ByteArray) {
        when {
            inputArray.size == outputArray.size -> {
                System.arraycopy(inputArray, 0, outputArray, 0, inputArray.size)
            }
            audioParams.encoding == AudioParams.RawAudioEncoding.PCM_16BIT -> {
                interpolateShortArray(inputArray, outputArray)
            }
            audioParams.encoding == AudioParams.RawAudioEncoding.PCM_8BIT -> {
                interpolateByteArray(inputArray, outputArray)
            }
            else -> {
                TODO()
            }
        }
    }

    private fun interpolateByteArray(inputBuffer: ByteBuffer, outputBuffer: ByteBuffer) {
        val preparedArray = dataTransformer.byteBufferToByteArray(inputBuffer)
        val resultArray = ByteArray(outputBuffer.remaining() / Short.SIZE_BYTES)
        interpolateInternal(preparedArray, resultArray)
        dataTransformer.byteArrayToByteBuffer(resultArray, outputBuffer)
    }

    private fun interpolateShortArray(inputBuffer: ByteBuffer, outputBuffer: ByteBuffer) {
        val preparedArray = dataTransformer.byteBufferToShortArray(inputBuffer)
        val resultArray = ShortArray(outputBuffer.remaining() / Short.SIZE_BYTES)
        interpolateInternal(preparedArray, resultArray)
        dataTransformer.shortArrayToByteBuffer(resultArray, outputBuffer)
    }

    private fun interpolateByteArray(inputArray: ByteArray, outputArray: ByteArray) {
        interpolateInternal(inputArray, outputArray)
    }

    private fun interpolateShortArray(inputArray: ByteArray, outputArray: ByteArray) {
        val preparedArray = dataTransformer.byteArrayToShortArray(inputArray)
        val resultArray = ShortArray(outputArray.size / Short.SIZE_BYTES)
        interpolateInternal(preparedArray, resultArray)
        dataTransformer.shortArrayToByteArray(resultArray, outputArray)
    }

    private fun interpolateInternal(inputArray: ByteArray, resultArray: ByteArray) {
        val inputSize = inputArray.size
        val resultSize = resultArray.size
        //Log.d("Interpolate","speed ${speed} input size ${inputArray.size} result size ${resultSize}")
        val sizeRatio = resultSize.toDouble() / inputSize
        val channelsCount = audioParams.channelsCount
        val inputSamplesCount = inputSize / channelsCount
        val resultSamplesCount = resultSize / channelsCount
        for (resultSampleIdx in 0 until resultSamplesCount) {
            for (channelIdx in 0 until channelsCount) {
                val resultIdx = resultSampleIdx * channelsCount + channelIdx
                val inputSampleIdx = resultSampleIdx.toDouble() / sizeRatio

                // x point for interpolation
                val inputIdx = inputSampleIdx * channelsCount + channelIdx
                // x left argument
                val inputLeftIdx = (floor(inputSampleIdx) * channelsCount + channelIdx).coerceIn(0.0, inputSize - 1.0)
                // x right argument
                val inputRightIdx = (floor(inputSampleIdx + 1.0)  * channelsCount + channelIdx).coerceIn(0.0, inputSize-1.0)
                // y left value
                val leftValue = inputArray[inputLeftIdx.toInt()]
                // y right value
                val rightValue = inputArray[inputRightIdx.toInt()]
                // y interpolated value
                val resultValue = if (inputLeftIdx != inputRightIdx) {
                    val ratio = (rightValue - leftValue) / (inputRightIdx - inputLeftIdx)
                    leftValue + (inputIdx - inputLeftIdx) * ratio
                } else {
                    leftValue
                }
                resultArray[resultIdx] = resultValue.toInt().toByte()
            }
        }
    }

    private fun interpolateInternal(inputArray: ShortArray, resultArray: ShortArray) {
        val inputSize = inputArray.size
        val resultSize = resultArray.size
        //Log.d("Interpolate","speed ${speed} input size ${inputArray.size} result size ${resultSize}")
        val sizeRatio = resultSize.toDouble() / inputSize
        val channelsCount = audioParams.channelsCount
        val inputSamplesCount = inputSize / channelsCount
        val resultSamplesCount = resultSize / channelsCount
        for (resultSampleIdx in 0 until resultSamplesCount) {
            for (channelIdx in 0 until channelsCount) {
                val resultIdx = resultSampleIdx * channelsCount + channelIdx
                val inputSampleIdx = resultSampleIdx.toDouble() / sizeRatio

                // x point for interpolation
                val inputIdx = inputSampleIdx * channelsCount + channelIdx
                // x left argument
                val inputLeftIdx = (floor(inputSampleIdx) * channelsCount + channelIdx).coerceIn(0.0, inputSize - 1.0)
                // x right argument
                val inputRightIdx = (floor(inputSampleIdx + 1.0)  * channelsCount + channelIdx).coerceIn(0.0, inputSize-1.0)
                // y left value
                val leftValue = inputArray[inputLeftIdx.toInt()]
                // y right value
                val rightValue = inputArray[inputRightIdx.toInt()]
                // y interpolated value
                val resultValue = if (inputLeftIdx != inputRightIdx) {
                    val ratio = (rightValue - leftValue) / (inputRightIdx - inputLeftIdx)
                    leftValue + (inputIdx - inputLeftIdx) * ratio
                } else {
                    leftValue
                }
                resultArray[resultIdx] = resultValue.toInt().toShort()
            }
        }
    }
}