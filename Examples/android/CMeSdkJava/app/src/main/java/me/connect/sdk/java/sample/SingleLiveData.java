package me.connect.sdk.java.sample;

import androidx.annotation.MainThread;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link MutableLiveData} variant that sends data only once.
 * @param <T>
 */
public class SingleLiveData<T> extends MutableLiveData<T> {

    private final AtomicBoolean pending = new AtomicBoolean(false);

    @MainThread
    public void observeOnce(LifecycleOwner lifecycleOwner, final Observer<T> observer) {
        super.observe(lifecycleOwner, t -> {
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t);
            }
        });

    }

    @MainThread
    public void setValue(T t) {
        pending.set(true);
        super.setValue(t);
    }
}
