package com.metalichesky.voicenote.util.recognize.audio

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


private const val BUFFER_SIZE = 512 * 1024
private const val TRACK_UNKNOWN_IDX = -1

internal class AudioWriter(
    private val destPath: File
) {

    private var muxer: MediaMuxer? = null
    private var audioFormat: MediaFormat? = null
    private var byteBuffer: ByteBuffer? = null
    private val sampleDataQueue: MutableList<SampleData> = mutableListOf()
    private var audioTrackIdx: Int = TRACK_UNKNOWN_IDX
    private var outputFileStream: FileOutputStream? = null
    private var skipNewFormatSetup: Boolean = true

    fun setup() {
        muxer = try {
            if (Build.VERSION.SDK_INT >= 26) {
                outputFileStream = FileOutputStream(destPath)
                MediaMuxer(
                    outputFileStream!!.fd,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                )
            } else {
                MediaMuxer(
                    destPath.absolutePath,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                )
            }
        } catch (ex: Exception) {
            throw IllegalStateException(
                "can't create media muxer for provided output file path ${ex.message}"
            )
        }
    }

    fun setOutputFormat(format: MediaFormat?) {
        if (audioFormat == null || !skipNewFormatSetup) {
            audioFormat = format
            format ?: return
            val muxer = muxer ?: return
            audioTrackIdx = muxer.addTrack(format)
            muxer.start()
        }
        dequeueSampleData()
    }

    fun writeSampleData(
        byteBuf: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    ) {
        if (audioFormat != null && audioTrackIdx != TRACK_UNKNOWN_IDX) {
            dequeueSampleData()
            val muxer = muxer ?: return
            muxer.writeSampleData(audioTrackIdx, byteBuf, bufferInfo)
        } else {
            queueSampleData(byteBuf, bufferInfo)
        }
    }

    private fun queueSampleData(
        byteBuf: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    ) {
        byteBuf.limit(bufferInfo.offset + bufferInfo.size)
        byteBuf.position(bufferInfo.offset)
        if (byteBuffer == null) {
            byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE)
                .order(ByteOrder.nativeOrder())
        }
        byteBuffer?.put(byteBuf)
        sampleDataQueue.add(
            SampleData(
                bufferInfo.size,
                bufferInfo
            )
        )
    }

    private fun dequeueSampleData() {
        if (sampleDataQueue.isEmpty()) {
            return
        }
        if (byteBuffer == null) {
            byteBuffer = ByteBuffer.allocate(0)
        }
        val byteBuffer = this.byteBuffer ?: return
        byteBuffer.flip()
        val bufferInfo = MediaCodec.BufferInfo()
        var offset = 0
        for (sampleData in sampleDataQueue) {
            sampleData.writeToBufferInfo(bufferInfo, offset)
            muxer?.writeSampleData(
                audioTrackIdx,
                byteBuffer,
                bufferInfo
            )
            offset += sampleData.size
        }
        sampleDataQueue.clear()
        this.byteBuffer = null
    }

    fun stop() {
        try {
            muxer?.stop()
        } catch (ex: Exception) {
            ex.printStackTrace()
            throw IllegalStateException(
                "can't stop media muxer, saved file will be incorrect ${ex.message}"
            )
        }
    }

    fun release() {
        try {
            outputFileStream?.close()
            muxer?.release()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    class SampleData(
        val size: Int,
        bufferInfo: MediaCodec.BufferInfo
    ) {
        private val presentationTimeUs: Long = bufferInfo.presentationTimeUs
        private val flags: Int = bufferInfo.flags

        fun writeToBufferInfo(
            bufferInfo: MediaCodec.BufferInfo,
            offset: Int
        ) {
            bufferInfo[offset, size, presentationTimeUs] = flags
        }
    }
}