package com.metalichesky.voicenote.util.math

import kotlin.math.absoluteValue

internal object MathUtils {
    private const val INT_BITS_COUNT = 32

    fun reverseBits(num: Int, bitCount: Int): Int {
        var result = 0
        var powerOfTwo = 1
        var reversedPowerOfTwo = 1 shl (bitCount - 1)
        for (i in 0 until bitCount) {
            if (num and powerOfTwo != 0) {
                result = result or reversedPowerOfTwo
            }
            powerOfTwo = powerOfTwo shl 1
            reversedPowerOfTwo = reversedPowerOfTwo shr 1
        }
        return result
    }

    fun logTwo(num: Int): Int {
        var logN = 0
        while ((1 shl logN) < num) {
            logN++
        }
        return logN
    }

    fun isPowerOfTwo(x: Int): Boolean {
        return (x != 0) && (x and (x - 1)) == 0
    }

    fun getNearestPowerOfTwo(number: Int): Int {
        return if (isPowerOfTwo(number)) {
            number
        } else {
            getNextPowerOfTwo(number)
        }
    }

    fun getNextPowerOfTwo(number: Int): Int {
        var maxBit = 1
        var bit = 1
        for(i in 0 until INT_BITS_COUNT) {
            if ((bit and number) != 0) {
                maxBit = bit
            }
            bit = bit shl 1
        }
        maxBit = maxBit shl 1
        return maxBit
    }
}

internal fun Float.clamp(from: Float, to: Float): Float {
    return Math.min(Math.max(this, from), to)
}

internal fun Long.clamp(from: Long, to: Long): Long {
    return Math.min(Math.max(this, from), to)
}

internal fun Int.clamp(from: Int, to: Int): Int {
    return Math.min(Math.max(this, from), to)
}

const val DEFAULT_DOUBLE_EPS = 0.000001
const val DEFAULT_FLOAT_EPS = 0.000001f

internal fun Double.equalsAlmost(other: Double, eps: Double = DEFAULT_DOUBLE_EPS): Boolean {
    return (this-other).absoluteValue < eps
}

internal fun Float.equalsAlmost(other: Float, eps: Float = DEFAULT_FLOAT_EPS): Boolean {
    return (this-other).absoluteValue < eps
}