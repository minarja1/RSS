package cz.minarik.nasapp.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class BaseFragment<Binding : ViewBinding> : Fragment() {

    protected lateinit var binding: Binding
    protected abstract fun getViewBinding(): Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getViewBinding()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    open fun setUpViews() {}

    protected fun <T> LiveData<T>.observe(function: (value: T) -> Unit) {
        this.observe(viewLifecycleOwner, Observer { function(it) })
    }

    /**
     * refer to https://medium.com/androiddevelopers/a-safer-way-to-collect-flows-from-android-uis-23080b1f8bda
     */
    protected fun <T> Flow<T>.collectWhenStarted(function: (value: T) -> Unit) {
        this.let { flow ->
            // Create a new coroutine since repeatOnLifecycle is a suspend function
            lifecycleScope.launch {
                // The block passed to repeatOnLifecycle is executed when the lifecycle
                // is at least STARTED and is cancelled when the lifecycle is STOPPED.
                // It automatically restarts the block when the lifecycle is STARTED again.
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    // Safely collect from locationFlow when the lifecycle is STARTED
                    // and stops collection when the lifecycle is STOPPED
                    flow.collect {
                        function.invoke(it)
                    }
                }
            }
        }
    }

}