package com.metalichesky.voicenote.util.job

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.runBlocking
import java.util.HashSet
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class JobManager(
    var maxQueueSize: Int = DEFAULT_MAX_QUEUE_SIZE,
    var jobWorkerProvider: JobWorkerProvider? = null
) {
    companion object {
        const val DEFAULT_MAX_QUEUE_SIZE: Int = 100
        val LOG_TAG = JobManager.javaClass.simpleName
    }

    fun interface JobWorkerProvider {
        fun provideJobWorker(jobName: String): JobWorker?
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val jobsQueue: ConcurrentLinkedQueue<Job<*>> = ConcurrentLinkedQueue()
    protected val jobsLock: ReentrantLock = ReentrantLock()
    var isJobRunning: Boolean = false
        private set

    private fun getJobWorker(name: String? = null): JobWorker {
        return name?.let {
            jobWorkerProvider?.provideJobWorker(it) ?: JobWorker.getWorker()
        } ?: JobWorker.getWorker()
    }

    fun hasJob(jobName: String): Boolean {
        return jobsLock.withLock {
            jobsQueue.find { it.name == jobName } != null
        }
    }

    fun post(job: Job<*>, minDelay: Long = 0L) {
        jobsLock.withLock {
            jobsQueue.add(job)
            sync(minDelay)
        }
    }

    private fun sync(after: Long) {
        // Jumping on the message handler even if after = 0L should avoid StackOverflow errors.
        getJobWorker("_sync").post(after) {
            var job: Job<*>? = null
            jobsLock.withLock {
                if (isJobRunning) {
                    // Do nothing, job will be picked in executed().
                } else {
                    job = jobsQueue.firstOrNull()
                    if (job != null) {
                        isJobRunning = true
                    }
                }
            }
            // This must be out of mJobsLock! See comments in execute().
            job?.let {
                execute(it)
            }
        }
    }

    // Since we use WorkerHandler.run(), the job can end up being executed on the current thread.
    // For this reason, it's important that this method is never guarded by mJobsLock! Because
    // all threads can be waiting on that, even the UI thread e.g. through scheduleInternal.
    private fun <T> execute(job: Job<T>) {
        val worker = getJobWorker(job.name)
        worker.run(Runnable {
            var result: T? = null
            var exception: Exception? = null
            try {
                Log.d(LOG_TAG,"${job.name} - Executing.")
                runBlocking {
                    result = job.work.invoke()
                }
            } catch (e: Exception) {
                exception = e
            }
            onComplete(job, result, exception)
            jobsLock.withLock {
                executed(job)
            }
        })
    }

    private fun <T> onComplete(
        job: Job<T>,
        result: T?,
        exception: Exception?
    ) {
        mainHandler.post {
            if (result != null) {
                Log.d(LOG_TAG, "${job.name} - Finished.")
                job.onComplete?.onResult(result)
            } else {
                Log.w(LOG_TAG, "${job.name} - Finished with ERROR.")
                Log.w(LOG_TAG, exception)
                job.onComplete?.onFailure(exception)
            }
        }
    }

    private fun <T> executed(job: Job<T>) {
        isJobRunning = false
        jobsQueue.remove(job)
        sync(0L)
    }

    fun remove(jobName: String) {
        jobsLock.withLock {
            val jobsQueueIterator = jobsQueue.iterator()
            while(jobsQueueIterator.hasNext()) {
                val job = jobsQueueIterator.next()
                if (job.name == jobName) {
                    jobsQueueIterator.remove()
                }
            }
        }
    }

    fun trim(jobName: String, allowedJobsCount: Int) {
        jobsLock.withLock {
            var scheduled = mutableListOf<Job<*>?>()
            for (job in jobsQueue) {
                if (job.name == jobName) {
                    scheduled.add(job)
                }
            }
            Log.v(LOG_TAG, "trim() name=$jobName scheduled=${scheduled.size} allowed=$allowedJobsCount")
            val countToRemove = Math.max(scheduled.size - allowedJobsCount, 0)
            if (countToRemove > 0) {
                // To remove the oldest ones first, we must reverse the list.
                // Note that we will potentially remove a job that is being executed: we don't
                // have a mechanism to cancel the ongoing execution, but it shouldn't be a problem.
                scheduled.reverse()
                scheduled = scheduled.subList(0, countToRemove)
                for (job in scheduled) {
                    jobsQueue.remove(job)
                }
            }
        }
    }

    fun reset() {
        jobsLock.withLock {
            val all: MutableSet<String> = HashSet()
            for (job in jobsQueue) {
                all.add(job.name)
            }
            for (job in all) {
                remove(job)
            }
        }
    }
}