package com.metalichesky.voicenote.util.pool

import java.nio.ByteBuffer

/**
 * A simple [] implementation for byte buffers.
 */
internal class ByteBufferPool(
    bufferSize: Int,
    maxPoolSize: Int
) : Pool<ByteBuffer>(maxPoolSize, object : Factory<ByteBuffer> {
    override fun create(): ByteBuffer {
        return ByteBuffer.allocateDirect(bufferSize)
    }

    override fun recycle(item: ByteBuffer) {
        item.clear()
    }

    override fun destroy(item: ByteBuffer) {}
})
