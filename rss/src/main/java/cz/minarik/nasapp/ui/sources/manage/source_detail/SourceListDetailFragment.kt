package cz.minarik.nasapp.ui.sources.manage.source_detail

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.sources.selection.SourcesViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SourceListDetailFragment : BaseFragment(R.layout.fragment_recycler) {

    private val args: SourceListDetailFragmentArgs by navArgs()

    private val source by lazy {
        args.source
    }

    val viewModel by viewModel<SourcesViewModel> {
        parametersOf(source)
    }

    private val sourceSelectionViewModel: SourcesViewModel by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserve()
    }

    private fun initObserve() {
    }

    private fun initViews(view: View) {

    }
}