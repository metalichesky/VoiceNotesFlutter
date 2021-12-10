package com.metalichesky.voicenote.util.pool

/**
 * A simple [] implementation for input buffers.
 */
internal class InputBufferPool : Pool<InputBuffer>(
    Int.MAX_VALUE, object : Factory<InputBuffer> {
        override fun create(): InputBuffer {
            return InputBuffer()
        }

        override fun recycle(item: InputBuffer) {}

        override fun destroy(item: InputBuffer) {}
    })
