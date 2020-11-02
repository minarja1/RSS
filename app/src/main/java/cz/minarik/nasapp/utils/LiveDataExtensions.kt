package cz.minarik.nasapp.utils

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

@MainThread
fun <T> LiveData<T>.toFreshLiveData(): LiveData<T> {
    val freshLiveData = FreshLiveData<T>()
    val output = MediatorLiveData<T>()
    // push any onChange from the LiveData to the FreshLiveData
    output.addSource(this) { liveDataValue -> freshLiveData.value = liveDataValue }
    // then push any onChange from the FreshLiveData out
    output.addSource(freshLiveData) { freshLiveDataValue -> output.value = freshLiveDataValue }
    return output
}