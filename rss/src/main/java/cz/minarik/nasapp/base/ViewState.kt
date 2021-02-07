package cz.minarik.nasapp.base

open class ViewModelState
object Loading : ViewModelState()
object Success : ViewModelState()
object Reload : ViewModelState()
object Reloaded : ViewModelState()
object Empty : ViewModelState()
object NoConnection : ViewModelState()

data class FailedWithError(val error: Exception? = null, val throwable: Throwable? = null) :
    ViewModelState()

data class SuccessItem<T>(val content: T) : ViewModelState()