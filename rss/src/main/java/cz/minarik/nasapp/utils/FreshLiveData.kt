package cz.minarik.nasapp.utils

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A lifecycle-aware observable that emits only new data after subscription. Any data that has
 * already been set, before the observable has subscribed, will be ignored.
 *
 * This avoids a common problem with events: on configuration change (like rotation, font change) an
 * update can be emitted if the observer is active. This LiveData only calls the observable if
 * there's an explicit call to setValue() or postValue().
 *
 * All observers will be notified of change(s).
 */

class FreshLiveData<T> : MutableLiveData<T>() {

    private val observers = mutableMapOf<LifecycleOwner, FreshLiveDataObserver>()

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        @Suppress("UNCHECKED_CAST")
        observer as Observer<T>
        observers[owner].apply {
            if (this == null) {
                observers[owner] = FreshLiveDataObserver(observer).apply {
                    super.observe(owner, this)
                }
            } else {
                add(observer)
            }
        }
    }

    override fun observeForever(observer: Observer<in T>) {
        @Suppress("UNCHECKED_CAST")
        observer as Observer<T>
        observers[ProcessLifecycleOwner.get()].apply {
            if (this == null) {
                observers[ProcessLifecycleOwner.get()] = FreshLiveDataObserver(observer).apply {
                    super.observeForever(this)
                }
            } else {
                add(observer)
            }
        }
    }

    override fun removeObservers(owner: LifecycleOwner) {
        observers.remove(owner)
        super.removeObservers(owner)
    }

    override fun removeObserver(observer: Observer<in T>) {
        @Suppress("UNCHECKED_CAST")
        observers.forEach { it.value.remove(observer as Observer<T>) }
        super.removeObserver(observer)
    }

    @MainThread
    override fun setValue(t: T?) {
        observers.forEach { it.value.setPending() }
        super.setValue(t)
    }

    override fun postValue(value: T) {
        observers.forEach { it.value.setPending() }
        super.postValue(value)
    }

    inner class FreshLiveDataObserver(observer: Observer<T>) : Observer<T> {
        private val observers = mutableSetOf<Observer<T>>()
        private val pending = AtomicBoolean(false)

        init {
            observers.add(observer)
        }

        fun add(observer: Observer<T>) = observers.add(observer)
        fun remove(observer: Observer<T>) = observers.remove(observer)
        fun setPending() = pending.set(true)

        override fun onChanged(t: T) {
            if (pending.compareAndSet(true, false)) {
                observers.forEach { observer ->
                    observer.onChanged(t)
                }
            }
        }

    }
}