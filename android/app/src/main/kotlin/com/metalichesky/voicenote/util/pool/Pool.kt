package com.metalichesky.voicenote.util.pool

import android.util.Log
import androidx.annotation.CallSuper
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


/**
 * Base class for thread-safe pools of recycleable objects.
 * Creates a new pool with the given pool size and factory.
 * @param <T> the object type
 * @param maxPoolSize the max pool size
 * @param itemFactory the factory
 */
internal open class Pool<T>(
    private val maxPoolSize: Int,
    itemFactory: Factory<T>,
    initPool: Boolean = false
) {
    companion object {
        private val LOG_TAG = Pool::class.java.simpleName
    }

    /**
     * Used to create new instances of objects when needed.
     * @param <T> object type
    </T> */
    interface Factory<T> {
        /**
         * Create new item
         */
        fun create(): T

        /**
         * Recycle exists item before next use
         */
        fun recycle(item: T)

        /**
         * Destroy exists item forever
         */
        fun destroy(item: T)
    }

    private var activeCount = 0
    private val queue: LinkedBlockingQueue<T> = LinkedBlockingQueue(maxPoolSize)
    private val factory: Factory<T> = itemFactory
    private val lock: ReentrantLock = ReentrantLock()

    init {
        if (initPool) {
            lock.withLock {
                repeat(maxPoolSize) {
                    queue.offer(factory.create())
                }
            }
        }
    }

    /**
     * Whether the pool is empty. This means that [.get] will return
     * a null item, because all objects were reclaimed and not recycled yet.
     *
     * @return whether the pool is empty
     */
    val isEmpty: Boolean
        get() {
            lock.withLock {
                return count() >= maxPoolSize
            }
        }

    /**
     * Returns a new item, from the recycled pool if possible (if there are recycled items),
     * or instantiating one through the factory (if we can respect the pool size).
     * If these conditions are not met, this returns null.
     *
     * @return an item or null
     */
    fun get(): T? {
        lock.withLock {
            val item = queue.poll()
            if (item != null) {
                activeCount++ // poll decreases, this fixes
                Log.d(LOG_TAG, "get() GET Reusing recycled item. $this")
                return item
            }
            if (isEmpty) {
                Log.d(LOG_TAG, "get() GET Returning null. Too much items requested. $this")
                return null
            }
            activeCount++
            Log.d(LOG_TAG, "get() GET Creating a new item. $this")
            return factory.create()
        }
    }

    /**
     * Recycles an item after it has been used. The item should come from a previous
     * [.get] call.
     *
     * @param item used item
     */
    fun recycle(item: T) {
        lock.withLock {
            Log.d(LOG_TAG, "recycle() RECYCLE - Recycling item ${item.hashCode()}. $this")
            factory.recycle(item)
            check(--activeCount >= 0) {
                "Trying to recycle an item which makes " +
                        "activeCount < 0. This means that this or some previous items being " +
                        "recycled were not coming from this pool, or some item was recycled " +
                        "more than once. " + this
            }
            check(queue.offer(item)) {
                "Trying to recycle an item while the queue " +
                        "is full. This means that this or some previous items being recycled " +
                        "were not coming from this pool, or some item was recycled " +
                        "more than once. " + this
            }
        }
    }

    /**
     * Clears the pool of recycled items.
     */
    @CallSuper
    fun clear() {
        lock.withLock {
            queue.forEach {
                factory.destroy(it)
            }
            queue.clear()
        }
    }

    /**
     * Returns the count of all items managed by this pool. Includes
     * - active items: currently being used
     * - recycled items: used and recycled, available for second use
     *
     * @return count
     */
    fun count(): Int {
        lock.withLock {
            return activeCount() + recycledCount()
        }
    }

    /**
     * Returns the active items managed by this pools, which means, items
     * currently being used.
     *
     * @return active count
     */
    fun activeCount(): Int {
        lock.withLock {
            return activeCount
        }
    }

    /**
     * Returns the recycled items managed by this pool, which means, items
     * that were used and later recycled, and are currently available for
     * second use.
     *
     * @return recycled count
     */
    fun recycledCount(): Int {
        lock.withLock {
            return queue.size
        }
    }

    override fun toString(): String {
        return javaClass.simpleName + " - count:" + count() + ", active:" + activeCount() + ", recycled:" + recycledCount()
    }
}
