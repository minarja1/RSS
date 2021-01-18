package cz.minarik.nasapp.ui.articles.sources_manage.sources

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.articles.source_selection.SourceSelectionViewModel
import cz.minarik.nasapp.ui.articles.sources_manage.ManageSourcesParentFragmentDirections
import cz.minarik.nasapp.utils.dividerFullWidth
import kotlinx.android.synthetic.main.fragment_recycler.*
import org.koin.android.ext.android.inject

class ManageSourcesFragment : BaseFragment(R.layout.fragment_recycler) {

    override val viewModel: SourceSelectionViewModel by inject()

    private lateinit var manageSourcesAdapter: ManageSourcesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserve()
    }

    private fun initObserve() {
        viewModel.sourcesManagementData.observe {
            manageSourcesAdapter.submitList(it)
        }
    }

    private fun initViews(view: View) {
        manageSourcesAdapter = ManageSourcesAdapter(
            onShow = { source ->
                source.URLs.firstOrNull()?.let {
                    val action =
                        ManageSourcesParentFragmentDirections.actionManageSourcesToSimpleArticles(it)
                    findNavController().navigate(action)
                }
            },
            onAdd = {

            },
            onBlock = { source, position ->
                val blocked = !source.isBlocked
                source.isBlocked = blocked
                manageSourcesAdapter.notifyItemChanged(position)
                viewModel.markAsBlocked(source, blocked)
            },
        )

        recyclerView.run {
            adapter = manageSourcesAdapter
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            dividerFullWidth()
        }
    }

    override fun showError(error: String?) {
    }

    override fun showLoading(show: Boolean) {
    }

}