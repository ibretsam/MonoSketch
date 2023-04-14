/*
 * Copyright (c) 2023, tuanchauict
 */

package mono.lifecycle

interface LifecycleObserver {
    fun onStart() = Unit

    fun onStop() = Unit
}
