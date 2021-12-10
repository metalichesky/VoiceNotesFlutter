package com.metalichesky.voicenote.util.job

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor


class JobWorker private constructor(private val name: String) {
    companion object {
        private const val DEFAULT_NAME = "DefaultThread"
        private val workersCache = ConcurrentHashMap<String, WeakReference<JobWorker>>(4)
        private var defaultWorker: JobWorker? = null

        fun getWorker(name: String): JobWorker {
            if (workersCache.containsKey(name)) {
                val cached = workersCache[name]?.get()
                if (cached != null) {
                    if (cached.thread.isAlive && !cached.thread.isInterrupted) {
                        return cached
                    } else {
                        // Cleanup the old thread before creating a new one
                        cached.destroy()
                        workersCache.remove(name)
                    }
                } else {
                    workersCache.remove(name)
                }
            }
            val handler = JobWorker(name)
            workersCache[name] = WeakReference(handler)
            return handler
        }

        fun getWorker(): JobWorker {
            defaultWorker = getWorker(DEFAULT_NAME)
            return defaultWorker!!
        }

        fun execute(action: Runnable) {
            getWorker().post(action)
        }

        fun destroyAll() {
            for (key in workersCache.keys) {
                val ref = workersCache[key]
                val handler = ref?.get()
                handler?.destroy()
                ref?.clear()
            }
            workersCache.clear()
        }
    }

    val thread: HandlerThread
    val handler: Handler
    val executor: Executor
    val looper: Looper
        get() = thread.looper

    init {
        thread = object : HandlerThread(name) {
            override fun toString(): String {
                return super.toString() + "[" + threadId + "]"
            }
        }
        thread.isDaemon = true
        thread.start()
        handler = Handler(thread.looper)
        executor = Executor { command -> this@JobWorker.run(command) }

        // HandlerThreads/Handlers sometimes have a significant warmup time.
        // We want to spend this time here so when this object is built, it
        // is fully operational.
        val latch = CountDownLatch(1)
        post { latch.countDown() }
        try {
            latch.await()
        } catch (ignore: InterruptedException) { }
    }

    fun run(runnable: Runnable) {
        if (Thread.currentThread() == thread) {
            runnable.run()
        } else {
            post(runnable)
        }
    }

    fun post(runnable: Runnable) {
        handler.post(runnable)
    }

    fun post(delay: Long, runnable: Runnable) {
        handler.postDelayed(runnable, delay)
    }

    fun remove(runnable: Runnable) {
        handler.removeCallbacks(runnable)
    }

    /**
     * Destroys this handler and its thread. After this method returns, the handler
     * should be considered unusable.
     *
     * Internal note: this does not remove the thread from our cache, but it does
     * interrupt it, so the next [.get] call will remove it.
     * In any case, we only store weak references.
     */
    fun destroy() {
        val thread = thread
        if (thread.isAlive) {
            thread.interrupt()
            thread.quit()
            // after quit(), the thread will die at some point in the future. Might take some ms.
            // try { handler.getThread().join(); } catch (InterruptedException ignore) {}
        }
        // This should not be needed, but just to be sure, let's remove it from cache.
        // For example, interrupt() won't interrupt the thread if it's blocked - it will throw
        // an exception instead.
        workersCache.remove(name)
    }
}