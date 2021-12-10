package com.metalichesky.voicenote.util.recognize.audio

import android.util.Log
import com.metalichesky.voicenote.util.job.Job
import com.metalichesky.voicenote.util.job.JobManager
import com.metalichesky.voicenote.util.job.JobWorker
import java.util.concurrent.atomic.AtomicInteger

private const val JOB_NAME_PREPARE = "PREPARE"
private const val JOB_NAME_START = "START"
private const val JOB_NAME_PAUSE = "PAUSE"
private const val JOB_NAME_STOP = "STOP"
private const val JOB_NAME_NOTIFY = "NOTIFY"

internal abstract class Recorder protected constructor(
    private val mName: String
) {
    companion object {
        private val LOG_TAG = Recorder::class.java.simpleName
    }

    interface Callback {
        fun onPrepared()
        fun onStarted()
        fun onPaused()
        fun onStopped()
        fun onMaxLengthReached()
    }

    protected var jobManager: JobManager = JobManager() {
        JobWorker.getWorker(mName).apply {
            thread.priority = Thread.MAX_PRIORITY
        }
    }
    private var recorderCallback: Callback? = null
    private var recorderState = RecorderState.STATE_NONE
    private var recorderStateTimestamp = Long.MIN_VALUE

    protected var maxLengthUs: Long = 0
        private set
    private var isMaxLengthReached = false

    protected var startTimeMs: Long = 0 // In System.currentTimeMillis()
    protected var firstTimeUs = Long.MIN_VALUE // In unknown reference
    protected var lastTimeUs: Long = 0

    private val pendingEvents: MutableMap<String, AtomicInteger> = HashMap()

    private fun setState(newState: RecorderState) {
        if (recorderStateTimestamp == Long.MIN_VALUE) {
            recorderStateTimestamp = System.currentTimeMillis()
        }
        val millis = System.currentTimeMillis() - recorderStateTimestamp
        recorderStateTimestamp = System.currentTimeMillis()
        Log.w(LOG_TAG, "$mName setState() ${newState.stateName} millisSinceLastState:$millis")
        recorderState = newState
    }

    open fun prepare(
        callback: Callback,
        maxLengthUs: Long
    ) {
        Log.w(LOG_TAG, "$mName prepare()")
        when (recorderState) {
            RecorderState.STATE_NONE -> {
                // ready to prepare
            }
            else -> {
                Log.w(
                    LOG_TAG,
                    "$mName prepare() Wrong state while preparing. Aborting. state $recorderState"
                )
                return
            }
        }
        this.recorderCallback = callback
        this.maxLengthUs = maxLengthUs
        jobManager.post(Job(JOB_NAME_PREPARE) {
            Log.w(LOG_TAG, "$mName prepare() run")
            setState(RecorderState.STATE_PREPARING)
            onPrepare(callback, maxLengthUs)
        })
    }

    @RecorderThread
    protected abstract suspend fun onPrepare(
        callback: Callback,
        maxLengthUs: Long
    )

    @RecorderThread
    protected open fun onPrepared() {
        Log.d(LOG_TAG, "$mName onPrepared()")
        recorderCallback?.onPrepared()
        setState(RecorderState.STATE_PREPARED)
    }

    open fun start() {
        when (recorderState) {
            RecorderState.STATE_PREPARED, RecorderState.STATE_PAUSED -> {
                // ready to start
            }
            else -> {
                Log.w(
                    LOG_TAG,
                    "$mName start() Wrong state while starting. Aborting. state $recorderState"
                )
                return
            }
        }
        Log.d(LOG_TAG, "$mName start()")
        jobManager.post(Job(JOB_NAME_START) {
            Log.d(LOG_TAG, "$mName start() run")
            setState(RecorderState.STATE_STARTING)
            onStart()
        })
    }

    @RecorderThread
    protected abstract fun onStart()

    @RecorderThread
    protected open fun onStarted() {
        Log.d(LOG_TAG, "$mName onStarted()")
        recorderCallback?.onStarted()
        setState(RecorderState.STATE_STARTED)
    }

    fun pause() {
        when (recorderState) {
            RecorderState.STATE_STARTED -> {
                // ready to pause
            }
            else -> {
                Log.w(
                    LOG_TAG,
                    "$mName pause() Wrong state while starting. Aborting. state $recorderState"
                )
                return
            }
        }
        Log.d(LOG_TAG, "$mName pause()")
        jobManager.post(Job(JOB_NAME_PAUSE) {
            Log.d(LOG_TAG, "$mName pause() run")
            setState(RecorderState.STATE_PAUSING)
            onPause()
        })
    }

    @RecorderThread
    protected abstract fun onPause()

    @RecorderThread
    protected open fun onPaused() {
        Log.d(LOG_TAG, "$mName onPaused()")
        recorderCallback?.onPaused()
        setState(RecorderState.STATE_PAUSED)
    }

    fun stop() {
        Log.d(LOG_TAG, "$mName stop()")
        when (recorderState) {
            RecorderState.STATE_STARTED, RecorderState.STATE_PAUSED -> {
                // ready to stop
            }
            else -> {
                Log.w(
                    LOG_TAG,
                    "$mName stop() Wrong state while stopping. Aborting. state $recorderState"
                )
            }
        }
        jobManager.post(Job(JOB_NAME_STOP) {
            Log.d(LOG_TAG, "$mName stop() run")
            setState(RecorderState.STATE_STOPPING)
            onStop()
        })
    }

    @RecorderThread
    protected abstract fun onStop()

    @RecorderThread
    protected open fun onStopped() {
        Log.d(
            LOG_TAG,
            "$mName onStopped() is being released. Notifying controller and releasing codecs."
        )
        setState(RecorderState.STATE_STOPPED)
        recorderCallback?.onStopped()
        jobManager.reset()
    }

    fun notify(event: String, data: Any?) {
        if (!pendingEvents.containsKey(event)) {
            pendingEvents[event] = AtomicInteger(0)
        }
        val pendingEvents = pendingEvents[event]
        pendingEvents?.incrementAndGet()
        Log.d(LOG_TAG, "$mName notify() event ${event} pendingEvents:${pendingEvents?.toInt()}")
        jobManager.post(Job(JOB_NAME_NOTIFY) {
            Log.d(
                LOG_TAG,
                "$mName notify() run event ${event} pendingEvents:${pendingEvents?.toInt()}"
            )
            onEvent(event, data)
            pendingEvents?.decrementAndGet()
        })
    }

    @RecorderThread
    protected open fun onEvent(event: String, data: Any?) {
    }

    protected fun getPendingEvents(event: String): Int {
        return pendingEvents[event]?.toInt() ?: 0
    }

    protected fun notifyMaxLengthReached() {
        onMaxLengthReached()
    }

    protected fun hasReachedMaxLength(): Boolean {
        return isMaxLengthReached
    }

    private fun onMaxLengthReached() {
        if (isMaxLengthReached) {
            Log.d(LOG_TAG, "$mName onMaxLengthReached() Called twice.")
        } else {
            when (recorderState) {
                RecorderState.STATE_STARTED, RecorderState.STATE_PAUSED -> {
                    // ready to notify
                }
                else -> {
                    Log.d(
                        LOG_TAG,
                        "$mName onMaxLengthReached() Reached in wrong state. Aborting.$recorderState"
                    )
                    return
                }
            }
            Log.d(LOG_TAG, "$mName onMaxLengthReached() Requesting a stop.")
            isMaxLengthReached = true
            recorderCallback?.onMaxLengthReached()
        }
    }

    protected fun notifyFirstFrameMillis(firstFrameMillis: Long = System.currentTimeMillis()) {
        startTimeMs = firstFrameMillis
    }
}

/**
 * Indicates that some action is being executed on the recorder thread.
 */
annotation class RecorderThread