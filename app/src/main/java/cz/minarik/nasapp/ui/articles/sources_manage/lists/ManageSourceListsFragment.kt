package cz.minarik.nasapp.ui.articles.sources_manage.lists

import androidx.lifecycle.ViewModelProvider
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.articles.sources_manage.sources.ManageSourcesViewModel

class ManageSourceListsFragment : BaseFragment(R.layout.placeholder_fragment) {

    //todo odstranit z base
    override val viewModel by lazy {
        ViewModelProvider(requireActivity()).get(ManageSourcesViewModel::class.java)
    }

    override fun showError(error: String?) {
    }

    override fun showLoading(show: Boolean) {
    }


}