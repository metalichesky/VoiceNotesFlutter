package com.metalichesky.voicenote.util.recognize.audio

import com.metalichesky.voicenote.util.math.ComplexNumber
import com.metalichesky.voicenote.util.math.MathUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Util to transform audio data
 */
internal class AudioDataTransformer(
    var audioParams: AudioParams = AudioParams.createDefault()
) {
    fun floatArrayToByteArray(srcArray: FloatArray, resultArray: ByteArray) {
        val channelsCount = audioParams.channelsCount
        val bytePerSample = audioParams.encoding.bytePerSample

        var resultIdx = 0
        var srcIdx = 0
        while (srcIdx < srcArray.size) {
            for (channel in 0 until channelsCount) {
                val value = srcArray[srcIdx]
                val resultValues = toByteArray(value, bytePerSample)
                for (resultValue in resultValues) {
                    if (resultIdx >= resultArray.size) return
                    resultArray[resultIdx] = resultValue
                    resultIdx++
                }
                srcIdx++
            }
        }
    }

    fun byteArrayToFloatArray(srcArray: ByteArray): FloatArray {
        val resultArraySize = srcArray.size / audioParams.encoding.bytePerSample
        val resultArray = FloatArray(resultArraySize)
        byteArrayToFloatArray(srcArray, resultArray)
        return resultArray
    }

    fun byteArrayToFloatArray(srcArray: ByteArray, resultArray: FloatArray) {
        val channelsCount = audioParams.channelsCount
        val bytePerSample = audioParams.encoding.bytePerSample
        var resultIdx = 0
        var srcIdx = 0
        val buffer = ByteArray(bytePerSample)

        while (srcIdx < srcArray.size) {
            for (channel in 0 until channelsCount) {
                if (resultIdx >= resultArray.size) return
                System.arraycopy(srcArray, srcIdx, buffer, 0, bytePerSample)
                srcIdx += bytePerSample
                resultArray[resultIdx] = toFloat(buffer, bytePerSample)
                resultIdx++
            }
        }
    }

    fun shortArrayToByteArray(srcArray: ShortArray): ByteArray {
        val resultArraySize = srcArray.size * audioParams.encoding.bytePerSample
        val resultArray = ByteArray(resultArraySize)
        shortArrayToByteArray(srcArray, resultArray)
        return resultArray
    }

    fun shortArrayToByteArray(srcArray: ShortArray, resultArray: ByteArray) {
        val channelsCount = audioParams.channelsCount
        val bytePerSample = audioParams.encoding.bytePerSample

        var resultIdx = 0
        var srcIdx = 0
        while (srcIdx < srcArray.size) {
            for (channel in 0 until channelsCount) {
                val value = srcArray[srcIdx]
                val resultValues = toByteArray(value, bytePerSample)
                for (resultValue in resultValues) {
                    if (resultIdx >= resultArray.size) return
                    resultArray[resultIdx] = resultValue
                    resultIdx++
                }
                srcIdx++
            }
        }
    }

    fun byteArrayToShortArray(srcArray: ByteArray): ShortArray {
        val resultArraySize = srcArray.size / audioParams.encoding.bytePerSample
        val resultArray = ShortArray(resultArraySize)
        byteArrayToShortArray(srcArray, resultArray)
        return resultArray
    }

    fun byteArrayToShortArray(srcArray: ByteArray, resultArray: ShortArray) {
        val channelsCount = audioParams.channelsCount
        val bytePerSample = audioParams.encoding.bytePerSample
        var resultIdx = 0
        var srcIdx = 0
        val buffer = ByteArray(bytePerSample)

        while (srcIdx < srcArray.size) {
            for (channel in 0 until channelsCount) {
                if (resultIdx >= resultArray.size) return
                System.arraycopy(srcArray, srcIdx, buffer, 0, bytePerSample)
                srcIdx += bytePerSample
                resultArray[resultIdx] = toShort(buffer, bytePerSample)
                resultIdx++
            }
        }
    }

    fun floatArrayToComplexArray(
        srcArray: FloatArray,
        sizeToPowerOfTwo: Boolean = false
    ): Array<ComplexNumber> {
        val resultArraySize = MathUtils.getNearestPowerOfTwo(srcArray.size)
        return Array(resultArraySize) { ComplexNumber(srcArray.getOrNull(it) ?: 0f, 0f) }
    }

    fun floatArrayToComplexArray(
        srcArray: FloatArray,
        resultArray: Array<ComplexNumber>,
        sizeToPowerOfTwo: Boolean = false
    ) {
        var idx = 0
        while (idx < srcArray.size && idx < resultArray.size) {
            resultArray[idx].r = srcArray[idx]
            resultArray[idx].i = 0f
            idx++
        }
    }

    fun complexArrayToFloatArray(srcArray: Array<ComplexNumber>): FloatArray {
        return FloatArray(srcArray.size) { srcArray[it].r }
    }

    fun complexArrayToFloatArray(srcArray: Array<ComplexNumber>, resultArray: FloatArray) {
        var idx = 0
        while (idx < srcArray.size && idx < resultArray.size) {
            resultArray[idx] = srcArray[idx].r
            idx++
        }
    }

    fun byteBufferToByteArray(srcBuffer: ByteBuffer): ByteArray {
        val resultArray = ByteArray(srcBuffer.capacity())
        byteBufferToByteArray(srcBuffer, resultArray)
        return resultArray
    }

    fun byteBufferToByteArray(srcBuffer: ByteBuffer, resultArray: ByteArray) {
        srcBuffer.rewind()
        srcBuffer.get(resultArray, 0, srcBuffer.remaining())
        srcBuffer.rewind()
    }

    fun shortArrayToByteBuffer(srcArray: ShortArray): ByteBuffer {
        val resultBufferSize = srcArray.size * audioParams.encoding.bytePerSample
        val resultBuffer = ByteBuffer.allocateDirect(resultBufferSize)
        shortArrayToByteBuffer(srcArray, resultBuffer)
        return resultBuffer
    }

    fun shortArrayToByteBuffer(srcArray: ShortArray, resultBuffer: ByteBuffer) {
        val channelsCount = audioParams.channelsCount
        val bytePerSample = audioParams.encoding.bytePerSample

        var resultIdx = 0
        var srcIdx = 0
        while (srcIdx < srcArray.size) {
            val value = srcArray[srcIdx]
            val resultValues = toByteArray(value, bytePerSample)
            resultBuffer.position(resultIdx)
            resultBuffer.put(resultValues)
            resultIdx += resultValues.size
            srcIdx++
        }
        resultBuffer.rewind()
    }

    fun byteBufferToFloatArray(srcBuffer: ByteBuffer): FloatArray {
        val resultArraySize = srcBuffer.remaining() / audioParams.encoding.bytePerSample
        val resultArray = FloatArray(resultArraySize)
        byteBufferToFloatArray(srcBuffer, resultArray)
        return resultArray
    }

    fun byteBufferToFloatArray(srcBuffer: ByteBuffer, resultArray: FloatArray) {
        val channelsCount = audioParams.channelsCount
        val bytePerSample = audioParams.encoding.bytePerSample
        var resultIdx = 0
        var srcIdx = 0
        val buffer = ByteArray(bytePerSample)
        srcBuffer.rewind()
        val srcBufferSize = srcBuffer.remaining()
        while (srcIdx < srcBufferSize) {
            if (resultIdx >= resultArray.size) return
            srcBuffer.position(srcIdx)
            srcBuffer.get(buffer)
            srcIdx += bytePerSample
            resultArray[resultIdx] = toFloat(buffer, bytePerSample)
            resultIdx++
        }
        srcBuffer.rewind()
    }

    fun floatArrayToByteBuffer(srcArray: FloatArray): ByteBuffer {
        val resultBufferSize = srcArray.size * audioParams.encoding.bytePerSample
        val resultBuffer = ByteBuffer.allocateDirect(resultBufferSize)
        floatArrayToByteBuffer(srcArray, resultBuffer)
        return resultBuffer
    }

    fun floatArrayToByteBuffer(srcArray: FloatArray, resultBuffer: ByteBuffer) {
        val channelsCount = audioParams.channelsCount
        val bytePerSample = audioParams.encoding.bytePerSample

        var resultIdx = 0
        var srcIdx = 0
        while (srcIdx < srcArray.size) {
            for (channel in 0 until channelsCount) {
                val value = srcArray[srcIdx]
                val resultValues = toByteArray(value, bytePerSample)
                resultBuffer.position(resultIdx)
                resultBuffer.put(resultValues)
                resultIdx += resultValues.size
                srcIdx++
            }
        }
        resultBuffer.rewind()
    }

    fun byteBufferToShortArray(srcBuffer: ByteBuffer): ShortArray {
        val resultArraySize = srcBuffer.remaining() / audioParams.encoding.bytePerSample
        val resultArray = ShortArray(resultArraySize)
        byteBufferToShortArray(srcBuffer, resultArray)
        return resultArray
    }

    fun byteBufferToShortArray(srcBuffer: ByteBuffer, resultArray: ShortArray) {
        val channelsCount = audioParams.channelsCount
        val bytePerSample = audioParams.encoding.bytePerSample
        var resultIdx = 0
        var srcIdx = 0
        val buffer = ByteArray(bytePerSample)
        srcBuffer.rewind()
        val srcBufferSize = srcBuffer.remaining()
        while (srcIdx < srcBufferSize) {
            if (resultIdx >= resultArray.size) return
            srcBuffer.position(srcIdx)
            srcBuffer.get(buffer)
            srcIdx += bytePerSample
            resultArray[resultIdx] = toShort(buffer, bytePerSample)
            resultIdx++
        }
        srcBuffer.rewind()
    }

    fun byteArrayToByteBuffer(srcArray: ByteArray): ByteBuffer {
        val resultBuffer = ByteBuffer.allocateDirect(srcArray.size)
        byteArrayToByteBuffer(srcArray, resultBuffer)
        return resultBuffer
    }

    fun byteArrayToByteBuffer(srcArray: ByteArray, resultBuffer: ByteBuffer) {
        resultBuffer.put(srcArray)
        resultBuffer.rewind()
    }

    fun copyFloatArray(srcArray: FloatArray): FloatArray {
        return FloatArray(srcArray.size) { srcArray[it] }
    }

    fun copyComplexArray(srcArray: Array<ComplexNumber>): Array<ComplexNumber> {
        return Array<ComplexNumber>(srcArray.size) { srcArray[it].copy() }
    }

    fun copyComplexArray(srcArray: Array<ComplexNumber>, resultArray: Array<ComplexNumber>) {
        var idx = 0
        while (idx < srcArray.size && idx < resultArray.size) {
            resultArray[idx] = srcArray[idx].copy()
            idx++
        }
    }

    fun clearFloatArray(dataArray: FloatArray) {
        dataArray.forEachIndexed { idx, _ ->
            dataArray[idx] = 0f
        }
    }

    fun clearComplexArray(dataArray: Array<ComplexNumber>) {
        dataArray.forEachIndexed { idx, value ->
            dataArray[idx].r = 0f
            dataArray[idx].i = 0f
        }
    }

    fun normalize(srcArray: FloatArray): FloatArray {
        val resultArray = FloatArray(srcArray.size)
        normalize(srcArray, resultArray)
        return resultArray
    }

    fun normalize(srcArray: FloatArray, resultArray: FloatArray) {
        val maxAmplitude = audioParams.samplesAmplitude
        var idx = 0
        while (idx < srcArray.size && idx < resultArray.size) {
            resultArray[idx] = srcArray[idx] / maxAmplitude
            idx++
        }
    }

    fun toByteArray(
        value: Float,
        bytePerSample: Int = audioParams.encoding.bytePerSample
    ): ByteArray {
        return when (bytePerSample) {
            4 -> {
                value.toByteArray()
            }
            3 -> {
                value.toInt().toByteArray(3)
            }
            2 -> {
                value.toInt().toShort().toByteArray()
            }
            else -> {
                //make value unsigned here
                (value + audioParams.samplesAmplitude).toInt().toByte().toByteArray()
            }
        }
    }

    fun toShort(value: ByteArray, bytePerSample: Int = audioParams.encoding.bytePerSample): Short {
        return when (bytePerSample) {
            4 -> {
                value.toShort()
            }
            3 -> {
                value.toInt().toShort()
            }
            2 -> {
                value.toShort()
            }
            else -> {
                //make value unsigned here
                (value.toByte().toUByte().toShort()
                        - audioParams.samplesAmplitude.toInt().toShort()).toShort()
            }
        }
    }

    fun toFloat(value: ByteArray, bytePerSample: Int = audioParams.encoding.bytePerSample): Float {
        return when (bytePerSample) {
            4 -> {
                value.toFloat()
            }
            3 -> {
                value.toInt().toFloat()
            }
            2 -> {
                value.toShort().toFloat()
            }
            else -> {
                //make value signed here
                value.toByte().toUByte().toFloat() - audioParams.samplesAmplitude
            }
        }
    }

    fun toByteArray(
        value: Short,
        bytePerSample: Int = audioParams.encoding.bytePerSample
    ): ByteArray {
        return when (bytePerSample) {
            4 -> {
                value.toFloat().toByteArray()
            }
            3 -> {
                value.toInt().toByteArray(3)
            }
            2 -> {
                value.toByteArray()
            }
            else -> {
                //make value unsigned here
                (value + audioParams.samplesAmplitude).toInt().toByte().toByteArray()
            }
        }
    }

}


fun ByteArray.toFloat(): Float {
    return ByteBuffer.wrap(this)
        .order(ByteOrder.LITTLE_ENDIAN)
        .float
}

fun ByteArray.toByte(): Byte {
    return this[0]
}

fun ByteArray.toShort(): Short {
    return ByteBuffer.wrap(this)
        .order(ByteOrder.LITTLE_ENDIAN)
        .short
}

fun ByteArray.toInt(): Int {
    return ByteBuffer.wrap(this)
        .order(ByteOrder.LITTLE_ENDIAN)
        .int
}

fun Float.toByteArray(bytesCount: Int = 4): ByteArray {
    return ByteBuffer.allocate(bytesCount)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putFloat(this)
        .array()
}

fun Int.toByteArray(size: Int = 4): ByteArray {
    return ByteBuffer.allocate(size)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putInt(this)
        .array()
}

fun Short.toByteArray(): ByteArray {
    return ByteBuffer.allocate(2)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putShort(this)
        .array()
}

fun Byte.toByteArray(): ByteArray {
    val array = ByteArray(1)
    array[0] = this
    return array
}