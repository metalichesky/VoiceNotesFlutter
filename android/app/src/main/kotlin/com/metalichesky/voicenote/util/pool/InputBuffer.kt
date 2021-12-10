package com.metalichesky.voicenote.util.pool

import java.nio.ByteBuffer

/**
 * Represents an input buffer, which means,
 * raw data that should be encoded by MediaCodec.
 */
internal class InputBuffer {
    var sourceBuffer: ByteBuffer? = null
    var encodeBuffer: ByteBuffer? = null
    var encodeBufferIndex = 0
    var sourceLength = 0
    var encodeLength = 0
    var firstTimestampUs: Long = 0
    var timestampUs: Long = 0
    var isEndOfStream = false

    fun fill(other: InputBuffer) {
        encodeBuffer = other.encodeBuffer
        sourceBuffer = other.sourceBuffer
        encodeBufferIndex = other.encodeBufferIndex
        sourceLength = other.sourceLength
        encodeLength = other.encodeLength
        timestampUs = other.timestampUs
        isEndOfStream = other.isEndOfStream
    }
}