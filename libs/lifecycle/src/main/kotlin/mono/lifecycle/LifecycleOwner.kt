/*
 * Copyright (c) 2023, tuanchauict
 */

package mono.lifecycle

/**
 * An abstract class that has lifecycle from start (with [onStart]) to stop (with [onStop]).
 * The other classes can use this to self-aware lifecycle via [addObserver] method.
 */
abstract class LifecycleOwner {
    private val lifecycleObservers: MutableList<LifecycleObserver> = mutableListOf()
    private var state: State = State.INITIAL

    val isStopped: Boolean
        get() = state == State.STOPPED

    fun addObserver(lifecycleObserver: LifecycleObserver) {
        if (state == State.STOPPED) {
            return
        }
        lifecycleObservers.add(lifecycleObserver)
        if (state == State.STARTED) {
            lifecycleObserver.onStart()
        }
    }

    fun onStart() {
        state = State.STARTED
        for (observer in lifecycleObservers) {
            observer.onStart()
        }
        onStartInternal()
    }

    protected open fun onStartInternal() = Unit

    fun onStop() {
        state = State.STOPPED
        onStopInternal()

        for (observer in lifecycleObservers) {
            observer.onStop()
        }
        lifecycleObservers.clear()
    }

    protected open fun onStopInternal() = Unit

    fun hasObserver(lifecycleObserver: LifecycleObserver): Boolean =
        lifecycleObserver in lifecycleObservers

    fun getObserverCount(): Int = lifecycleObservers.size

    private enum class State {
        INITIAL, STARTED, STOPPED
    }
}
