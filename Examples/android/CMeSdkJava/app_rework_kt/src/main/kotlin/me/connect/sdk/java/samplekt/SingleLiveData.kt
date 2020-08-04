package me.connect.sdk.java.samplekt

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean


/**
 * [MutableLiveData] variant that sends data only once.
 * @param <T>
</T> */
class SingleLiveData<T> : MutableLiveData<T>() {
    private val pending = AtomicBoolean(false)

    @MainThread
    fun observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
        super.observe(lifecycleOwner, Observer { t: T ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    @MainThread
    override fun setValue(t: T) {
        pending.set(true)
        super.setValue(t)
    }
}
